package bdv.jogl.VolumeRenderer.ShaderPrograms;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.getNewIdentityMatrix;

import java.awt.Color;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;


/**
 * Renderer for multiple volume data
 * @author michael
 *
 */
public class MultiVolumeRenderer extends AbstractShaderSceneElement{

	static {
		Map<Integer, String> aMap = new HashMap<Integer, String>();
		aMap.put(GL2.GL_VERTEX_SHADER, "glsl"+File.separator+"MultiVolumeRendererVertexShader.glsl");
		aMap.put(GL2.GL_FRAGMENT_SHADER, "glsl"+File.separator+"MultiVolumeRendererFragmentShader.glsl");
		shaderFiles = Collections.unmodifiableMap(aMap);
	}	

	//Vertex shader uniforms
	public static final String shaderVariableGlobalScale ="inScaleGlobal";

	public static final String shaderVariableLocalTransformation ="inLocalTransformation";

	//Fragment shader uniforms 
	public static final String shaderVariableActiveVolumes = "inActiveVolume";

	public static final String shaderVariableVolumeTexture = "inVolumeTexture";

	public static final String shaderVariableColorTexture = "inColorTexture";

	public static final String shaderVariableEyePosition = "inEyePosition";

	public static final String shaderVariableMinVolumeValue = "inMinVolumeValue";

	public static final String shaderVariableMaxVolumeValue = "inMaxVolumeValue";

	private float[] coordinates = GeometryUtils.getUnitCubeVerticesQuads(); 

	private final int maxNumberOfDataBlocks = 6;

	private final Map<Integer,VolumeDataBlock> dataValues = new HashMap<Integer, VolumeDataBlock>();

	private final Map<Integer,Texture> volumeTextureMap = new HashMap<Integer, Texture>();

	private Texture colorTexture;

	private boolean isColorUpdateable;

	private final TreeMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();

	private boolean isEyeUpdateable = true;

	private Matrix4 globalScale = getNewIdentityMatrix();


	private float[] calculateEyePositions(){
		float eyePositionsObjectSpace[] = new float[3*maxNumberOfDataBlocks];

		Matrix4 globalTransformation = getNewIdentityMatrix();
		globalTransformation.multMatrix(getView());
		globalTransformation.multMatrix(getModelTransformation());

		for(int i =0; i< maxNumberOfDataBlocks;i++){
			int fieldOffset = 3*i;
			if(!getVolumeDataMap().containsKey(i)){
				break;
			}

			Matrix4 modelViewMatrixInverse= copyMatrix(globalTransformation);

			modelViewMatrixInverse.multMatrix(getVolumeDataMap().get(i).localTransformation);
			modelViewMatrixInverse.multMatrix(globalScale);
			modelViewMatrixInverse.invert();

			eyePositionsObjectSpace[fieldOffset] = modelViewMatrixInverse.getMatrix()[12];
			eyePositionsObjectSpace[fieldOffset] = modelViewMatrixInverse.getMatrix()[13];
			eyePositionsObjectSpace[fieldOffset] = modelViewMatrixInverse.getMatrix()[14];

		}
		return eyePositionsObjectSpace;
	}

	private void updateEyes(GL2 gl2){
		if(!isEyeUpdateable ){
			return;
		}

		float [] eyePositions = calculateEyePositions();

		//eye position
		gl2.glUniform3fv(getLocation(shaderVariableEyePosition),maxNumberOfDataBlocks, eyePositions,0);
		isEyeUpdateable = false;
	}

	@Override
	protected void updateShaderAttributesSubClass(GL2 gl2) {
		
		updateActiveVolumes(gl2);

		updateLocalTransformation(gl2);

		boolean update = updateTextureData(gl2);

		updateGlobalScale(gl2, update);

		updateColor(gl2);

		updateEyes(gl2);
	}


	private void updateActiveVolumes(GL2 gl2) {
		IntBuffer activeBuffers = Buffers.newDirectIntBuffer(maxNumberOfDataBlocks);
		activeBuffers.rewind();
		for(int i = 0; i<maxNumberOfDataBlocks;i++){
			int active; 
			if(getVolumeDataMap().containsKey(i)){
				active=1;
			}else{
				active=0;
			}
			activeBuffers.put(i, active);
			
		}
		activeBuffers.rewind();
		gl2.glUniform1iv(getLocation(shaderVariableActiveVolumes),
				activeBuffers.capacity(),activeBuffers);
	}

	private void updateLocalTransformation(GL2 gl2) {
		for(Integer index: getVolumeDataMap().keySet()){
			VolumeDataBlock data = getVolumeDataMap().get(index);
			if(!data.needsUpdate()){
				continue;
			}
			
			gl2.glUniformMatrix4fv(getLocation(shaderVariableLocalTransformation)+index,
					1,false,data.localTransformation.getMatrix(),0);
		}
	}

	private void updateGlobalScale(GL2 gl2, boolean globalScaleNeedsUpdate) {
		if(! globalScaleNeedsUpdate ){
			return;
		}
		
		globalScale = getNewIdentityMatrix();
		
		//iterate data for get bounding volume
		float highPoint[] = {Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE};
		float lowPoint[] = {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE};
		for(int index: getVolumeDataMap().keySet()){
			VolumeDataBlock data = getVolumeDataMap().get(index);
			float maxPoint[] = {1,1,1,1};//data.dimensions[0],data.dimensions[1],data.dimensions[2],1};
			float minPoint[] = {0,0,0,1};
			
			//transform
			data.localTransformation.multVec(maxPoint, maxPoint);
			data.localTransformation.multVec(minPoint, minPoint);

			//build box
			for(int i = 0; i < 3 ; i++){
				highPoint[i] = Math.max(highPoint[i], maxPoint[i]/ maxPoint[3]);
				lowPoint[i] = Math.min(lowPoint[i], minPoint[i]/ minPoint[3]);
			} 
		}
		globalScale.scale(highPoint[0]-lowPoint[0],highPoint[1]-lowPoint[1],highPoint[2]-lowPoint[2]);
		gl2.glUniformMatrix4fv(getLocation(shaderVariableGlobalScale),1,false,globalScale.getMatrix(),0);
		isEyeUpdateable = true;
	}

	private boolean updateTextureData(GL2 gl2){

		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		boolean somethingUpdated = false;
		
		for(Integer i : getVolumeDataMap().keySet()){
			VolumeDataBlock data = getVolumeDataMap().get(i);

			min = Math.min(min, data.minValue);
			max = Math.max(max, data.maxValue);

			if(!data.needsUpdate()){
				continue;
			}

			somethingUpdated= true;

			//get Buffer
			FloatBuffer buffer = Buffers.newDirectFloatBuffer(data.data);
			buffer.rewind();

			//uploade data
			volumeTextureMap.get(i).update(gl2, 0, buffer, 
					new int[]{(int)data.dimensions[0], 
							(int)data.dimensions[1], 
							(int)data.dimensions[2]});

			data.setNeedsUpdate(false);
		}

		//update values
		if(somethingUpdated){
			//min max
			gl2.glUniform1f(getLocation(shaderVariableMinVolumeValue), min);
			gl2.glUniform1f(getLocation(shaderVariableMaxVolumeValue), max);
		}
		return somethingUpdated;
	}

	@Override
	protected void generateIdMappingSubClass(GL2 gl2) {

		mapUniforms(gl2, new String[]{
				shaderVariableGlobalScale,
				shaderVariableLocalTransformation,
				shaderVariableActiveVolumes,
				shaderVariableEyePosition,
				shaderVariableMinVolumeValue,
				shaderVariableMaxVolumeValue,
				shaderVariableVolumeTexture,
				shaderVariableColorTexture});

		int location = getLocation(shaderVariableVolumeTexture);
		for(int i =0; i< maxNumberOfDataBlocks; i++){
			Texture volumeTexture = new Texture(GL2.GL_TEXTURE_3D,location+i,GL2.GL_R32F,GL2.GL_RED,GL2.GL_FLOAT);
			volumeTexture.genTexture(gl2);
			volumeTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
			volumeTextureMap.put(i, volumeTexture);
		}
		location = getLocation(shaderVariableColorTexture);
		colorTexture = new Texture(GL2.GL_TEXTURE_1D,location,GL2.GL_RGBA,GL2.GL_RGBA,GL2.GL_FLOAT);
		colorTexture.genTexture(gl2);
		colorTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
		
	}
	
	/**
	 * Return the map of volume data with a user specific index.
	 * @return
	 */
	public final Map<Integer, VolumeDataBlock> getVolumeDataMap() {
		return dataValues;
	}

	@Override
	protected void updateVertexBufferSubClass(GL2 gl2, VertexAttribute position) {
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);	
	}

	private void updateColor(GL2 gl2){
		if(!isColorUpdateable){
			return;
		}

		//get Buffer last key is the highest number 
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(((colorMap.lastKey()-colorMap.firstKey())+1)*4);


		//make samples
		Integer latestMapIndex = colorMap.firstKey();
		//iterate candidates
		for(Integer currentMapIndex: colorMap.keySet()){
			if(currentMapIndex == colorMap.firstKey()){
				continue;
			}

			float[] currentColor = {0,0,0,(float)(colorMap.get(latestMapIndex).getAlpha())/255.f};
			float[] finalColor = {0,0,0,(float)(colorMap.get(currentMapIndex).getAlpha())/255.f};
			float[] colorGradient = {0,0,0,0};
			colorMap.get(latestMapIndex).getColorComponents(currentColor);
			colorMap.get(currentMapIndex).getColorComponents(finalColor);

			//forward difference
			for(int dim = 0; dim < colorGradient.length; dim++){
				colorGradient[dim] = (finalColor[dim]-currentColor[dim])/(currentMapIndex-latestMapIndex);
			}

			//sample linear
			for(Integer step = latestMapIndex; step < currentMapIndex; step++ ){

				//add to buffer and increment
				for(int dim = 0; dim < colorGradient.length; dim++){
					buffer.put(Math.min( finalColor[dim],  currentColor[dim]));
					currentColor[dim] += colorGradient[dim];
				}
			}		
			//add latest color
			if(currentMapIndex == colorMap.lastKey()){
				for(int dim = 0; dim < finalColor.length; dim++){
					buffer.put(finalColor[dim]);
				}
			}
			latestMapIndex = currentMapIndex;
		}

		buffer.rewind();
		//upload data
		colorTexture.update(gl2, 0, buffer, new int[]{buffer.capacity()/4});
		//gl2.glBindTexture(GL2.GL_TEXTURE_1D, 0);
		isColorUpdateable = false;
	}


	@Override
	protected int getVertexBufferSize() {

		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}

	@Override
	protected void renderSubClass(GL2 gl2) {
		gl2.glDrawArrays(GL2.GL_QUADS, 0,coordinates.length/3);
	}

	/**
	 * Updates color data
	 * @param newData
	 */
	public void setColorMapData(final TreeMap<Integer, Color> newData){
		colorMap.clear();
		colorMap.putAll(newData);
		isColorUpdateable = true;
	}
}
