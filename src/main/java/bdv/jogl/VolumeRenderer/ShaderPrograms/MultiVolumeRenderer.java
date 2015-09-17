package bdv.jogl.VolumeRenderer.ShaderPrograms;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.getNewIdentityMatrix;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.ISourceListener;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunctionAdapter;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import static bdv.jogl.VolumeRenderer.utils.GeometryUtils.*;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.math.geom.AABBox;

import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;

/**
 * Renderer for multiple volume data
 * @author michael
 *
 */
public class MultiVolumeRenderer extends AbstractShaderSceneElement{

	private float[] coordinates = GeometryUtils.getUnitCubeVerticesQuads(); 

	private VolumeDataManager dataManager;

	private float isoSurfaceValue = 0;
	
	private float[] lightPosition = new float[]{0,1000,-100};
	
	private boolean isLightPositionUpdateable = true;
	
	private boolean isIsoSurfaceValueUpdatable= true;
	
	private final Map<Integer,Texture> volumeTextureMap = new HashMap<Integer, Texture>();

	private TransferFunction1D tf;
	
	private Texture colorTexture;

	private boolean isColorUpdateable;

	private boolean isEyeUpdateable = true;
	
	private Color backgroundColor = Color.BLACK;
	
	private boolean isBackgroundColorUpdateable = true;

	private Matrix4 drawCubeTransformation = getNewIdentityMatrix();
	
	private MultiVolumeRendererShaderSource sources =new MultiVolumeRendererShaderSource ();

	private boolean isSliceUpdateable;

	private boolean showSlice = false;

	private boolean isShownUpdatable = true; 

	@Override
	protected void updateShaderAttributesSubClass(GL2 gl2) {

		updateBackgroundColor(gl2);
		
		updateActiveVolumes(gl2);

		updateLocalTransformationInverse(gl2);

		updateEyePositions(gl2);
		
		updateIsoValue(gl2);
		
		boolean update = updateTextureData(gl2);
		if(update){
			updateGlobalScale(gl2);

			updateMaxDiagonalLength(gl2);
		}

		updateColor(gl2);

		updateEyes(gl2);
		
		updateSliceShown(gl2);
		
		updateSlice(gl2);
		
		
	}
	
	private int boolToInt(boolean bool){
		if(bool){
			return 1;
		}else{
			return 0;
		}
	}
	
	private void updateSliceShown(GL2 gl2) {
		if(!isShownUpdatable){
			return;
		}
		
		gl2.glUniform1i(getLocation(suvShowSlice), boolToInt( this.showSlice));
	}

	private void updateSlice(GL2 gl2) {
		if(!isSliceUpdateable){
			return;
		}
		
	
		float[]	zNormalVector=new float[3];
		float zeroDist=calcSlicePlane( zNormalVector);
		
		gl2.glUniform3fv(getLocation(suvNormalSlice), 1, zNormalVector, 0);
		gl2.glUniform1f(getLocation(suvZeroDistSlice), zeroDist);
	}

	private float calcSlicePlane(float[] zNormalVector) {
		float centerPoint[] = {-500f,0.5f,0.f,1};
		float normVector[] = {0,0,1,0};
		float dist = 0;
		
		Matrix4 inversGlobal = copyMatrix(getModelTransformation());
	//	nullPoint[0]=inversGlobal.getMatrix()[14];
//		nullPoint[1]=inversGlobal.getMatrix()[13];
	//	nullPoint[2]=inversGlobal.getMatrix()[12];
		inversGlobal.invert();
		
		Matrix4 localInverse = copyMatrix(dataManager.getVolume(0).localTransformation);
		localInverse.scale(dataManager.getVolume(0).dimensions[0], dataManager.getVolume(0).dimensions[1], dataManager.getVolume(0).dimensions[2]);
		Matrix4 localViewInverse = copyMatrix(dataManager.getVolume(0).localTransformation);
		localViewInverse.invert();
		Matrix4 viewerscaleinverse = getNewIdentityMatrix();
		viewerscaleinverse.scale(dataManager.getVolume(0).dimensions[0],dataManager.getVolume(0).dimensions[1],dataManager.getVolume(0).dimensions[2]);
		viewerscaleinverse.invert();
		localInverse.invert();
	
		Matrix4 mat = getNewIdentityMatrix();
		mat.scale(1, 1, -1);
		
		//from cube to tex 1
		mat.multMatrix(localInverse);
		//canonic to cube
		mat.multMatrix(getDrawCubeTransformation());
		
		Matrix4 bdvTrans = getNewIdentityMatrix();
		
		//to screen
		bdvTrans.multMatrix(getModelTransformation());
		//to local
		bdvTrans.multMatrix(dataManager.getVolume(0).localTransformation);
		//0 1 to volume
		bdvTrans.scale(dataManager.getVolume(0).dimensions[0],dataManager.getVolume(0).dimensions[1],dataManager.getVolume(0).dimensions[2]);
		
	
		 
		//bdvTrans.multVec(new float[]{0.5f,0.5f,0.0f,1.0f }, centerPoint);
		bdvTrans.invert();
		//bdvTrans.transpose();
		mat.multMatrix(bdvTrans);
		
		float[]transformedZero ={0,0,0,0};
		mat.multVec(centerPoint, transformedZero);
		mat.invert();
		mat.transpose();
		
		float[]transformedNormal ={0,0,0,0};
		mat.multVec(normVector, transformedNormal);
		VectorUtil.normalizeVec3(zNormalVector,transformedNormal);
		
		
		//prepare return
		for(int i =0; i < 3; i++){
			//zNormalVector[i]= transformedNormal[i];
			dist+= (transformedZero[i]/*/transformedZero[3]*/)*zNormalVector[i] ;
		}
		return dist;
	}

	/**
	 * returns the source
	 * @return
	 */
	@Override
	public MultiVolumeRendererShaderSource getSource(){
		return sources;
	}
	
	private void setAllUpdate(boolean flag){

		isColorUpdateable = flag;
		isEyeUpdateable = flag;
		isIsoSurfaceValueUpdatable = flag;
		isBackgroundColorUpdateable = flag;
		isLightPositionUpdateable = flag;
		isSliceUpdateable = flag;
		for(VolumeDataBlock data: dataManager.getVolumes()){
			data.setNeedsUpdate(true);
		}
		sources.setTransferFunctionCode(tf.getTransferFunctionShaderCode());
	} 
	
	private void setVolumeDataManager(VolumeDataManager manager){
		dataManager = manager;
		this.dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
			
			@Override
			public void addedData(Integer i) {
				sources.setMaxNumberOfVolumes(dataManager.getVolumeKeys().size());
			}
			
			@Override
			public void dataRemoved(Integer i) {
				sources.setMaxNumberOfVolumes(dataManager.getVolumeKeys().size());
			}
		});
		
	}
	public MultiVolumeRenderer(TransferFunction1D tf, VolumeDataManager manager){
		setVolumeDataManager(manager);
		setTransferFunction(tf);

		sources.addSourceListener(new ISourceListener() {
			
			@Override
			public void sourceCodeChanged() {
				setNeedsRebuild(true);
				setAllUpdate(true);
				
			}
		});
	}

	private float[] calculateEyePositions(){
		final int maxNumVolumes = sources.getMaxNumberOfVolumes();
		float eyePositionsObjectSpace[] = new float[3*maxNumVolumes];

		Matrix4 globalTransformation = getNewIdentityMatrix();
		globalTransformation.multMatrix(getView());
	
		for(int i =0; i< maxNumVolumes;i++){
			int fieldOffset = 3*i;
			if(!dataManager.getVolumeKeys().contains(i)){
				break;
			}
			VolumeDataBlock data = dataManager.getVolume(i);
			Matrix4 modelViewMatrixInverse= copyMatrix(globalTransformation);

			modelViewMatrixInverse.multMatrix(copyMatrix(data.localTransformation));
			modelViewMatrixInverse.scale(data.dimensions[0], data.dimensions[1], data.dimensions[2]);
			modelViewMatrixInverse.invert();

			eyePositionsObjectSpace[fieldOffset] = modelViewMatrixInverse.getMatrix()[12];
			eyePositionsObjectSpace[fieldOffset+1] = modelViewMatrixInverse.getMatrix()[13];
			eyePositionsObjectSpace[fieldOffset+2] = modelViewMatrixInverse.getMatrix()[14];

		}
		return eyePositionsObjectSpace;
	}

	private void updateEyes(GL2 gl2){
		if(!isEyeUpdateable ){
			return;
		}

		float [] eyePositions = calculateEyePositions();

		//eye position
		gl2.glUniform3fv(getLocation(suvEyePosition), 
				sources.getMaxNumberOfVolumes(),eyePositions,0);

		isEyeUpdateable = false;
	}

	private void updateEyePositions(GL2 gl2) {
		if(!isLightPositionUpdateable){
			return;
		}
		float textureLightPositions[] = calculateTextureLightPositions();
		
		gl2.glUniform3fv(getLocation(suvLightPosition),sources.getMaxNumberOfVolumes(), textureLightPositions,0);
		
	}

	private float[] calculateTextureLightPositions() {
		final int maxNumVolumes = sources.getMaxNumberOfVolumes();
		float lightPositionsObjectSpace[] = new float[3*maxNumVolumes];

		Matrix4 globalTransformation = getNewIdentityMatrix();
		//globalTransformation.multMatrix(getView());
		globalTransformation.multMatrix(getModelTransformation());

		for(int i =0; i< maxNumVolumes;i++){
			int fieldOffset = 3*i;
			if(!dataManager.getVolumeKeys().contains(i)){
				break;
			}
			VolumeDataBlock data = dataManager.getVolume(i);
			Matrix4 modelViewMatrixInverse= copyMatrix(globalTransformation);

			modelViewMatrixInverse.multMatrix(copyMatrix(data.localTransformation));
			modelViewMatrixInverse.scale(data.dimensions[0], data.dimensions[1], data.dimensions[2]);
			modelViewMatrixInverse.invert();
			
			float transformer[] = new float[]{lightPosition[0],lightPosition[1],lightPosition[2],1};
			float transformed[] = new float[4];
			modelViewMatrixInverse.multVec(transformer, transformed);
			
			lightPositionsObjectSpace[fieldOffset] = transformed[0]/transformed[3];
			lightPositionsObjectSpace[fieldOffset+1] = transformed[1]/transformed[3];
			lightPositionsObjectSpace[fieldOffset+2] = transformed[2]/transformed[3];

		}
		return lightPositionsObjectSpace;
	}

	private void updateBackgroundColor(GL2 gl2) {
		if(!this.isBackgroundColorUpdateable){
			return;
		}
		
		float[] c=getNormalizedColor(backgroundColor);
		
		gl2.glUniform3fv(getLocation(suvBackgroundColor),1, c, 0);
		
		isBackgroundColorUpdateable = false;
	}

	private void updateMaxDiagonalLength(GL2 gl2) {
		float length = Float.MIN_VALUE;
		float[][] globalUnitCubeHighLow = new float[][]{{0,0,0,1},{1,1,1,1}};

		//get cube to global space
		for(int i =0; i < globalUnitCubeHighLow.length;i++){
			drawCubeTransformation.multVec(globalUnitCubeHighLow[i], globalUnitCubeHighLow[i]);
		}

		//cube transform in texture space to get the maximum extend
		for(VolumeDataBlock data: dataManager.getVolumes()){
			float[][] coordsInTextureSpace = new float[2][4];

			Matrix4 textureTransformation = copyMatrix(data.localTransformation);
			textureTransformation.scale(data.dimensions[0], data.dimensions[1], data.dimensions[2]);
			textureTransformation.invert();		
			for(int i =0; i < globalUnitCubeHighLow.length;i++){
				textureTransformation.multVec(globalUnitCubeHighLow[i], coordsInTextureSpace[i]);
				for(int j = 0; j < 4; j++){
					coordsInTextureSpace[i][j] /= coordsInTextureSpace[i][3];
				}
			}

			float currentLength = VectorUtil.distVec3(coordsInTextureSpace[0], coordsInTextureSpace[1]);

			length = Math.max(currentLength, length);
		}
		gl2.glUniform1f(getLocation(suvMaxDiagonalLength), length);
	}

	private void updateActiveVolumes(GL2 gl2) {
		IntBuffer activeBuffers = Buffers.newDirectIntBuffer(sources.getMaxNumberOfVolumes());
		activeBuffers.rewind();
		for(int i = 0; i<sources.getMaxNumberOfVolumes();i++){
			int active; 
			if(dataManager.isEnabled(i)){
				active=1;
			}else{
				active=0;
			}
			activeBuffers.put(i, active);

		}
		activeBuffers.rewind();
		gl2.glUniform1iv(getLocation(suvActiveVolumes),
				activeBuffers.capacity(),activeBuffers);
	}

	private void updateLocalTransformationInverse(GL2 gl2) {
		for(Integer index: dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(index);
			if(!data.needsUpdate()){
				continue;
			}

			Matrix4 localInverse = copyMatrix(data.localTransformation);
			localInverse.scale(data.dimensions[0], data.dimensions[1], data.dimensions[2]);
			localInverse.invert();
			gl2.glUniformMatrix4fv(getLocation(suvTextureTransformationInverse)+index,
					1,false,localInverse.getMatrix(),0);
		}
	}

	private void updateGlobalScale(GL2 gl2) {


		drawCubeTransformation = getNewIdentityMatrix();

		//iterate data for get bounding volume
		float lowhighPoint[][] = {
				{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
				{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}
				};


		for(int index: dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(index);
			
			AABBox box = getAABBOfTransformedBox(data.dimensions, data.localTransformation);
			
			for(int d =0; d < 3; d++){
				lowhighPoint[0][d] = Math.min(lowhighPoint[0][d], box.getLow()[d]);
				lowhighPoint[1][d] = Math.max(lowhighPoint[1][d], box.getHigh()[d]); 
			}
		}
		
		AABBox boundingVolume = new AABBox(lowhighPoint[0],lowhighPoint[1]);
		
		//correct origo
		drawCubeTransformation.translate(boundingVolume.getMinX(),boundingVolume.getMinY(),boundingVolume.getMinZ());
		drawCubeTransformation.scale(boundingVolume.getWidth(),boundingVolume.getHeight(),boundingVolume.getDepth());
		gl2.glUniformMatrix4fv(getLocation(suvDrawCubeTransformation),1,false,drawCubeTransformation.getMatrix(),0);
	}

	private void updateIsoValue(GL2 gl2){
		if(!isIsoSurfaceValueUpdatable){
			return;
		}
		gl2.glUniform1f(getLocation(suvIsoValue), isoSurfaceValue);
		GLErrorHandler.assertGL(gl2);
		isIsoSurfaceValueUpdatable = false;
	}
	
	private boolean updateTextureData(GL2 gl2){

		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		boolean somethingUpdated = false;

		for(Integer i : dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(i);

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
			gl2.glUniform1f(getLocation(suvMinVolumeValue), min);
			gl2.glUniform1f(getLocation(suvMaxVolumeValue), max);
		}
		return somethingUpdated;
	}

	@Override
	protected void generateIdMappingSubClass(GL2 gl2) {


		mapUniforms(gl2, new String[]{
				suvDrawCubeTransformation,
				suvTextureTransformationInverse,
				suvActiveVolumes,
				suvEyePosition,
				suvMinVolumeValue,
				suvMaxVolumeValue,
				suvVolumeTexture,
				suvColorTexture,
				suvMaxDiagonalLength,
				suvIsoValue, 
				suvBackgroundColor,
				suvLightPosition,
				suvNormalSlice,
				suvZeroDistSlice,
				suvShowSlice
				});

		int location = getLocation(suvVolumeTexture);
		for(int i =0; i< sources.getMaxNumberOfVolumes(); i++){
			Texture volumeTexture = new Texture(GL2.GL_TEXTURE_3D,location+i,GL2.GL_R32F,GL2.GL_RED,GL2.GL_FLOAT);
			volumeTexture.genTexture(gl2);
			volumeTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			volumeTexture.setTexParameterfv(gl2, GL2.GL_TEXTURE_BORDER_COLOR, new float[]{-1,-1,-1,-1});
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
			volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
			volumeTextureMap.put(i, volumeTexture);
		}
		location = getLocation(suvColorTexture);
		colorTexture = new Texture(GL2.GL_TEXTURE_1D,location,GL2.GL_RGBA,GL2.GL_RGBA,GL2.GL_FLOAT);
		colorTexture.genTexture(gl2);
		colorTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
	}

	@Override
	protected void updateVertexBufferSubClass(GL2 gl2, VertexAttribute position) {
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);	
	}

	@Override
	public void setModelTransformation(Matrix4 modelTransformations) {
		super.setModelTransformation(modelTransformations);

		isSliceUpdateable = true;
	}

	@Override
	public void setView(Matrix4 view) {
		super.setView(view);

		isEyeUpdateable = true;
	}

	private void updateColor(GL2 gl2){
		if(!isColorUpdateable){
			return;
		}

		//get Buffer last key is the highest number 
		FloatBuffer buffer = tf.getTexture(); 
				
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
	public void setTransferFunction(final TransferFunction1D tf){
		this.tf = tf;
		this.tf.addTransferFunctionListener(new TransferFunctionAdapter() {
			
			@Override
			public void colorChanged(TransferFunction1D transferFunction) {
				isColorUpdateable =true;
				
			}
			
			@Override
			public void samplerChanged(TransferFunction1D transferFunction1D) {
				setNeedsRebuild(true);
				setAllUpdate(true);
			}
		});
		sources.setTransferFunctionCode(this.tf.getTransferFunctionShaderCode());
	}

	/**
	 * @return the drawCubeTransformation
	 */
	public Matrix4 getDrawCubeTransformation() {
		return copyMatrix( drawCubeTransformation);
	}	
	
	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		if(this.backgroundColor.equals(backgroundColor)){
			return;
		}
		this.backgroundColor = backgroundColor;
		isBackgroundColorUpdateable=true;
	}


	@Override
	protected void disposeSubClass(GL2 gl2) {
		colorTexture.delete(gl2);
		
		for(Texture texture: volumeTextureMap.values() ){
			texture.delete(gl2);
		}
		setAllUpdate(true);
	}

	public void setIsoSurface(float floatValue) {
		this.isoSurfaceValue = floatValue;
		
		isIsoSurfaceValueUpdatable = true;
	}

	public void setSliceShown(boolean selected) {
		this.showSlice = selected;
		
		isShownUpdatable  = true; 
		
	}
}
