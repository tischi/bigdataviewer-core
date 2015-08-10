package bdv.jogl.VolumeRenderer.ShaderPrograms;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.getNewIdentityMatrix;

import java.awt.Color;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import bdv.jogl.VolumeRenderer.utils.GeometryUtils;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.glsl.ShaderProgram;
/**
 * Volume renderer for single volume
 * @author michael
 *
 */
public class SimpleVolumeRenderer extends AbstractShaderSceneElement {

	static {
		Map<Integer, String> aMap = new HashMap<Integer, String>();
		aMap.put(GL2.GL_VERTEX_SHADER, "glsl"+File.separator+"SimpleVolumeRendererVertexShader.glsl");
		aMap.put(GL2.GL_FRAGMENT_SHADER, "glsl"+File.separator+"SimpleVolumeRendererFragmentShader.glsl");
		shaderFiles = Collections.unmodifiableMap(aMap);
	}	

	public static final String shaderVariableVolumeTexture = "inVolumeTexture";

	public static final String shaderVariableColorTexture = "inColorTexture";

	public static final String shaderVariableEyePosition = "inEyePosition";

	public static final String shaderVariableMinVolumeValue = "inMinVolumeValue";

	public static final String shaderVariableMaxVolumeValue = "inMaxVolumeValue";

	private final TreeMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();

	
	private VolumeDataBlock data;

	private int volumeTextureObject =-1;

	private float[] coordinates = GeometryUtils.getUnitCubeVerticesQuads(); 

	private boolean isDataUpdateable = false;

	private boolean isEyeUpdateable = false;

	private int colorTextureObject;

	private boolean isColorUpdateable = true;
	
	private void initColorDefaults(){
		colorMap.put(100,Color.green);
		colorMap.put(0,Color.red);
	}
	
	/**
	 * constructor 
	 */
	public SimpleVolumeRenderer(){
		initColorDefaults();
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
	
	@Override
	public void setModelTransformation(Matrix4 modelTransformations) {
		super.setModelTransformation(modelTransformations);

		isEyeUpdateable = true;
	}

	@Override
	public void setView(Matrix4 view) {
		super.setView(view);

		isEyeUpdateable = true;
	}

	/**
	 * @return the data
	 */
	public VolumeDataBlock getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(VolumeDataBlock data) {
		this.data = data;
		isDataUpdateable = true;
	}


	@Override
	protected void updateVertexBufferSubClass(GL2 gl2) {
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		gl2.glBufferSubData(
				GL2.GL_ARRAY_BUFFER,
				0, 
				bufferData.capacity() * Buffers.SIZEOF_FLOAT, 
				bufferData);	
	}

	private void updateTextureData(GL2 gl2,
			final Map<String, Integer> shaderVariableMapping){
		if(!isDataUpdateable){
			return;
		}

		//activate context
		gl2.glActiveTexture(GL2.GL_TEXTURE0);
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, volumeTextureObject);
		gl2.glUniform1i(shaderVariableMapping.get(shaderVariableVolumeTexture),0);

		//get Buffer
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(data.data);
		buffer.rewind();

		//uploade data
		gl2.glTexImage3D(GL2.GL_TEXTURE_3D, 
				0, 
				GL2.GL_R32F, 
				(int)data.dimensions[0], 
				(int)data.dimensions[1], 
				(int)data.dimensions[2], 
				0,
				GL2.GL_RED, 
				GL2.GL_FLOAT, 
				buffer);

		//gl2.glBindTexture(GL2.GL_TEXTURE_3D, 0);

		//min max
		gl2.glUniform1f(shaderVariableMapping.get(shaderVariableMinVolumeValue), data.minValue);
		gl2.glUniform1f(shaderVariableMapping.get(shaderVariableMaxVolumeValue), data.maxValue);


		isDataUpdateable = false;
	}
	
	
	/**
	 * transform eye to object space
	 * https://www.opengl.org/archives/resources/faq/technical/viewing.htm
	 * @return transformed eye
	 */
	private float[] calculateEyePosition(){
		Matrix4 modelViewMatrixInverse=getNewIdentityMatrix();

		modelViewMatrixInverse.multMatrix(getView());
		modelViewMatrixInverse.multMatrix(getModelTransformation());
		modelViewMatrixInverse.invert();

		float [] eyeTrans = {
				modelViewMatrixInverse.getMatrix()[12],
				modelViewMatrixInverse.getMatrix()[13],
				modelViewMatrixInverse.getMatrix()[14]
		};
		
		return eyeTrans;
	}

	private void updateEye(GL2 gl2,
			Map<String, Integer> shaderVariableMapping){
		if(!isEyeUpdateable){
			return;
		}

		float [] eyePosition = calculateEyePosition();

		//eye position
		gl2.glUniform3f(shaderVariableMapping.get(shaderVariableEyePosition), eyePosition[0],eyePosition[1],eyePosition[2]);
		isEyeUpdateable = false;
	}

	
	private void updateColor(GL2 gl2,
			Map<String, Integer> shaderVariableMapping){
		if(!isColorUpdateable){
			return;
		}
		//activate context
		gl2.glActiveTexture(GL2.GL_TEXTURE1);
		gl2.glBindTexture(GL2.GL_TEXTURE_1D, colorTextureObject);
		gl2.glUniform1i(shaderVariableMapping.get(shaderVariableColorTexture),1);

		
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
		gl2.glTexImage1D(GL2.GL_TEXTURE_1D, 
				0, 
				GL2.GL_RGBA, 
				buffer.capacity()/4, 
				0,
				GL2.GL_RGBA, 
				GL2.GL_FLOAT, 
				buffer);
		//gl2.glBindTexture(GL2.GL_TEXTURE_1D, 0);
		isColorUpdateable = false;
	}

	@Override
	protected void updateShaderAttributesSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping) {

		updateTextureData(gl2, shaderVariableMapping);

		updateColor(gl2, shaderVariableMapping);

		updateEye(gl2, shaderVariableMapping);

	}

	private static int genTexture(GL2 gl2, int type, int id,int uniformID, int location  ){
		//activate texture
		gl2.glActiveTexture(id);

		//generate texture object
		int[] textures = new int[1];
		gl2.glGenTextures(textures.length, textures,0);


		gl2.glBindTexture(type, textures[0]);


		//activate texture unit
		gl2.glUniform1i(location,uniformID);
		//gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
		//gl2.glBindTexture(type, 0);
		return textures[0];

	}

	@Override
	protected void generateIdMappingSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping,
			ShaderProgram shaderProgram) {

		//get location


		int location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableEyePosition);
		shaderVariableMapping.put(shaderVariableEyePosition, location);

		location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableMinVolumeValue);
		shaderVariableMapping.put(shaderVariableMinVolumeValue, location);

		location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableMaxVolumeValue);
		shaderVariableMapping.put(shaderVariableMaxVolumeValue, location);

		location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableVolumeTexture);
		shaderVariableMapping.put(shaderVariableVolumeTexture, location);
		volumeTextureObject = genTexture(gl2,GL2.GL_TEXTURE_3D ,GL2.GL_TEXTURE0,0 , location);

		location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableColorTexture);
		shaderVariableMapping.put(shaderVariableColorTexture, location);
		colorTextureObject = genTexture(gl2,GL2.GL_TEXTURE_1D ,GL2.GL_TEXTURE1,1, location);

	}


	@Override
	protected int getVertexBufferSize() {

		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}

	@Override
	protected void renderSubClass(GL2 gl2,Map<String, Integer> shaderVariableMapping) {
		/*	gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK); 
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthRangef(0.1f, 1000000);
		gl2.glDepthFunc(GL2.GL_LEQUAL);*/
		//gl2.glActiveTexture(GL2.GL_TEXTURE0);

		/*	gl2.glActiveTexture(GL2.GL_TEXTURE0);
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, volumeTextureObject);
		gl2.glUniform1i(shaderVariableMapping.get(shaderVariableVolumeTexture),0);

		gl2.glActiveTexture(GL2.GL_TEXTURE0);
		gl2.glBindTexture(GL2.GL_TEXTURE_1D, colorTextureObject);
		gl2.glUniform1i(shaderVariableMapping.get(shaderVariableColorTexture),1);
		 */
		gl2.glDrawArrays(GL2.GL_QUADS, 0,coordinates.length/3);
		//gl2.glBindTexture(GL2.GL_TEXTURE_1D, 0);
		//	gl2.glBindTexture(GL2.GL_TEXTURE_3D, 0); 
		/*gl2.glDisable(GL2.GL_CULL_FACE);
	    gl2.glDisable(GL2.GL_DEPTH_TEST);*/
	}
}
