package bdv.jogl.VolumeRenderer.ShaderPrograms;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.getNewIdentityMatrix;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.ISourceListener;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunctionAdapter;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import static bdv.jogl.VolumeRenderer.utils.GLUtils.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
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

	private float[] coordinates = GeometryUtils.getUnitCubeVerticesTriangles(); 

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

	private Matrix4 drawCubeTransformation = null;

	private MultiVolumeRendererShaderSource sources =new MultiVolumeRendererShaderSource ();

	private boolean isSliceUpdateable;

	private boolean showSlice = false;

	private boolean isShownUpdatable = true;

	private int samples;

	private boolean isSamplesUpdatable;

	private float length; 

	private boolean isUseGradientUpdateable =true;

	private boolean useGradient= false;

	private AABBox drawRect;
	
	private final List<IMultiVolumeRendererListener> listeners = new ArrayList<IMultiVolumeRendererListener>();

	private boolean useSparseVolume = false;
	
	private AbstractVolumeAccumulator accumulator;

	private boolean isClippingUpdatable = true;
	
	private boolean islengthUpdatable = true;
	
	private boolean isOpacity3DUpdateable = true;
	
	private float opacity3D = 1.f;

	public void setUseSparseVolumes(boolean flag){
		useSparseVolume = flag;
		drawCubeTransformation = null;
	}
	
	public void setDrawRect(AABBox rect){
		drawCubeTransformation = getTransformationRepresentAABBox(rect);
		drawRect  = rect;
		isClippingUpdatable = true;
		islengthUpdatable = true;
		fireAllRect(rect);
	}

	/**
	 * @param useGradient the useGradient to set
	 */
	public void setUseGradient(boolean useGradient) {
		this.useGradient = useGradient;
		isUseGradientUpdateable = true;
	}

	private void fireAllRect(AABBox rect){
		for(IMultiVolumeRendererListener l : listeners){
			fireRect(l, rect);
		}
	}

	private void fireRect(IMultiVolumeRendererListener l, AABBox rect){
		l.drawRectChanged(rect);
	}

	public void addMultiVolumeListener(IMultiVolumeRendererListener l){
		listeners.add(l);
	}

	@Override
	protected void updateShaderAttributesSubClass(GL4 gl2) {
		accumulator.updateData(gl2);
		GLErrorHandler.assertGL(gl2);
		updateBackgroundColor(gl2);
		GLErrorHandler.assertGL(gl2);
		updateActiveVolumes(gl2);
		GLErrorHandler.assertGL(gl2);

		updateIsoValue(gl2);
		GLErrorHandler.assertGL(gl2);
		updateOpacity3D(gl2);
		
		boolean update = updateTextureData(gl2);
		
		GLErrorHandler.assertGL(gl2);
		if(update){

			updateGlobalScale(gl2);
			GLErrorHandler.assertGL(gl2);
			updateLocalTransformationInverse(gl2);
			GLErrorHandler.assertGL(gl2);

			GLErrorHandler.assertGL(gl2);
			updateLightPositions(gl2);
		}
		GLErrorHandler.assertGL(gl2);
		updateClippingPlanes(gl2);
		updateMaxDiagonalLength(gl2);
		updateColor(gl2);
		GLErrorHandler.assertGL(gl2);
		updateEye(gl2);
		GLErrorHandler.assertGL(gl2);
		updateSliceShown(gl2);
		GLErrorHandler.assertGL(gl2);
		updateSlice(gl2);
		GLErrorHandler.assertGL(gl2);
		updateSamples(gl2);
		GLErrorHandler.assertGL(gl2);
		updateUseGradient(gl2);
		GLErrorHandler.assertGL(gl2);
		
	
	}

	/**
	 * Updates the opacity of the 3D volume for animations 
	 * @param gl2
	 */
	private void updateOpacity3D(GL4 gl2) {
		gl2.glUniform1f(getLocation(suvOpacity3D), opacity3D);
	}

	private void updateUseGradient(GL4 gl2) {
		if(!isUseGradientUpdateable){
			return;
		}
		gl2.glUniform1i(getLocation(suvUseGradient), boolToInt(useGradient));
	}

	private void updateSamples(GL4 gl2) {
		if(!isSamplesUpdatable){
			return;
		}

		gl2.glUniform1i(getLocation(suvSamples), samples);
		isSamplesUpdatable = false;
	}

	private int boolToInt(boolean bool){
		if(bool){
			return 1;
		}else{
			return 0;
		}
	}

	private void updateSliceShown(GL4 gl2) {
		if(!isShownUpdatable){
			return;
		}

		gl2.glUniform1i(getLocation(suvShowSlice), boolToInt( this.showSlice));
	}

	private void updateSlice(GL4 gl2) {
		if(!isSliceUpdateable){
			return;
		}


		float[] plane=calcSlicePlane();

		gl2.glUniform4fv(getLocation(suvNormalSlice), 1, plane, 0);
		isSliceUpdateable=false;
	}

	/**
	 * calculates the bdv plane in texture space 1 since all ray positions are equal
	 * @return
	 */
	private float[] calcSlicePlane() {
		float normVector[] = {0,0,1,0};
		
		//viewer to global
		Matrix4 bdvTransSafe = getNewIdentityMatrix();
		bdvTransSafe.multMatrix(getModelTransformation());
		bdvTransSafe.invert();
		
		
		Matrix4 mat = getNewIdentityMatrix();

	//	mat.multMatrix(scaleInv);
	//	mat.multMatrix(calcScaledVolumeTransformation(data));
		
		mat.multMatrix(bdvTransSafe);
		mat.multMatrix(getProjection());
		mat.multMatrix(getView());
		
				


		mat.invert();
		mat.transpose();

		float[]transformedNormal ={0,0,0,0};
		mat.multVec(normVector, transformedNormal);

		float n =VectorUtil.normVec3(transformedNormal);

		float plane[] = new float[4];
		//prepare return
		for(int i =0; i < 4; i++){
			plane[i]= transformedNormal[i]/n;
		}
		//no idea but works
		plane[3] *= -1.f;
		return plane;
	}

	/**
	 * returns the source
	 * @return
	 */
	@Override
	public MultiVolumeRendererShaderSource getSource(){
		return sources;
	}

	/**
	 * reset all update flags. Insert new updates here 
	 * @param flag
	 */
	private void setAllUpdate(boolean flag){

		isColorUpdateable = flag;
		isEyeUpdateable = flag;
		isIsoSurfaceValueUpdatable = flag;
		isBackgroundColorUpdateable = flag;
		isLightPositionUpdateable = flag;
		isSliceUpdateable = flag;
		isSamplesUpdatable = flag;
		isClippingUpdatable = flag;
		islengthUpdatable = flag;
		isOpacity3DUpdateable = flag;
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
		setAccumulator(new MaximumVolumeAccumulator());
		sources.addSourceListener(new ISourceListener() {

			@Override
			public void sourceCodeChanged() {
				setNeedsRebuild(true);
				setAllUpdate(true);

			}
		});
	}

	private float[] calculateEyePosition(){
		float eyePositionsObjectSpace[] = new float[3];

		Matrix4 globalTransformation = getNewIdentityMatrix();
		globalTransformation.multMatrix(getView());
		globalTransformation.multMatrix(drawCubeTransformation);
		float eye[] = getEyeInCurrentSpace(globalTransformation);
		float eyeTransformer[] ={eye[0] ,eye[1],eye[2],1}; 
		float eyeTransformed[] = new float[4];
		
		drawCubeTransformation.multVec(eyeTransformer, eyeTransformed);
		for(int j = 0; j < eye.length; j++){
			eyePositionsObjectSpace[j] = eyeTransformed[j]/eyeTransformed[3];
		}

		return eyePositionsObjectSpace;
	}

	private void updateEye(GL4 gl2){
		if(!isEyeUpdateable ){
			return;
		}

		float [] eyePositions = calculateEyePosition();

		//eye position
		gl2.glUniform3fv(getLocation(suvEyePosition), 
				1,eyePositions,0);
		GLErrorHandler.assertGL(gl2);

		isEyeUpdateable = false;
	}

	private void updateLightPositions(GL4 gl2) {
		if(!isLightPositionUpdateable){
			return;
		}
		float textureLightPositions[] = calculateTextureLightPositions();

		gl2.glUniform3fv(getLocation(suvLightPosition),sources.getMaxNumberOfVolumes(), textureLightPositions,0);
		isLightPositionUpdateable = false;
	}

	private float[] calculateTextureLightPositions() {
		final int maxNumVolumes = sources.getMaxNumberOfVolumes();
		float lightPositionsObjectSpace[] = new float[3*maxNumVolumes];

		Matrix4 globalTransformation = getNewIdentityMatrix();
		globalTransformation.multMatrix(getView());
		globalTransformation.multMatrix(drawCubeTransformation);

		for(int i =0; i< maxNumVolumes;i++){
			int fieldOffset = 3*i;
			if(!dataManager.getVolumeKeys().contains(i)){
				break;
			}
			VolumeDataBlock data = dataManager.getVolume(i);
			Matrix4 modelViewMatrixInverse= copyMatrix(globalTransformation);
			
			modelViewMatrixInverse.multMatrix(fromCubeToVolumeSpace(data));
	
			modelViewMatrixInverse.invert();

			float transformer[] = new float[]{lightPosition[0],lightPosition[1],lightPosition[2],1};
			float transformed[] = new float[4];
			modelViewMatrixInverse.multVec(transformer, transformed);

			for(int d = 0; d < 3; d++){
				lightPositionsObjectSpace[fieldOffset+d] = transformed[d]/transformed[3];
			}
		}
		return lightPositionsObjectSpace;
	}

	private void updateBackgroundColor(GL4 gl2) {
		if(!this.isBackgroundColorUpdateable){
			return;
		}

		float[] c=getNormalizedColor(backgroundColor);

		gl2.glUniform3fv(getLocation(suvBackgroundColor),1, c, 0);

		isBackgroundColorUpdateable = false;
	}

	/**
	 * calculates the individual length of the bounding volume in each texture space since it may scale by transformation 
	 * @param gl2
	 */
	private void updateMaxDiagonalLength(GL4 gl2) {
		if(!islengthUpdatable){
			return;
		}
		
		length = Float.MIN_VALUE;
		//length = (float)Math.sqrt(3d);
		//cube transform in texture space to get the maximum extend
		final float diagVec[]={drawRect.getWidth(),drawRect.getHeight(),drawRect.getDepth(),0};
		float runLength = VectorUtil.normVec3(diagVec);
		
		gl2.glUniform1f(getLocation(suvRenderRectStepSize), runLength/(float)samples);
		GLErrorHandler.assertGL(gl2);
		//create a logical stepsize for the the classifications
		float sizeDim = 0;
		for(int i = 0; i < 3; i ++){
			sizeDim = Math.max(sizeDim, diagVec[i]);
		} 
		for(int i = 0; i< 3; i++){
			diagVec[i]/=sizeDim;
		}
		
		length = VectorUtil.normVec3(diagVec);
		
		gl2.glUniform1f(getLocation(suvTransferFuntionSize), length);
		GLErrorHandler.assertGL(gl2);

		

		islengthUpdatable = false;
		isColorUpdateable = true;
	}

	private void updateActiveVolumes(GL4 gl2) {
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

	private void updateLocalTransformationInverse(GL4 gl2) {
		Map<Integer, Matrix4> localInverses = new HashMap<Integer, Matrix4>();
		for(Integer index: dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(index);


			Matrix4 localInverse = fromCubeToVolumeSpace( data);
			gl2.glUniformMatrix4fv(getLocation(suvTextureTransformationInverse)+index,
					1,false,localInverse.getMatrix(),0);
			localInverses.put(index,localInverse);
		}

		GLErrorHandler.assertGL(gl2);
		updateNormalAxises(gl2,localInverses);GLErrorHandler.assertGL(gl2);
	}


	/**
	 * transforms the xyz Axis of the bounding volume in the local texture coordinate systems
	 * @param gl2 context to upload the data
	 * @param localInverses the transformation matrices from draw rectangle space to texture space
	 */
	private void updateNormalAxises(GL4 gl2, Map<Integer, Matrix4> localInverses) {
		float drawRectAxises[][] = {{1,0,0,0},{0,1,0,0},{0,0,1,0}};
		
		//transform and upload TODO
		for(Integer volume: localInverses.keySet()){
			float axisesInTextSpace[][] = new float[3][4]; 
			Matrix4  transformation = localInverses.get(volume);
			transformation.invert();
			transformation.transpose();
			
			for(int axis=0; axis < drawRectAxises.length; axis++){
				transformation.multVec(drawRectAxises[axis],axisesInTextSpace[axis]);
			}
		}
	}

	/**
	 * Calculates the clipping planes for the ray based of drawing rects
	 * @param gl
	 * @param localInverses
	 */
	private void updateClippingPlanes(GL4 gl) {
		if(!isClippingUpdatable){
			return;
		}
		
		float planesInDrawRectSpace[]= new float[]{
				1,0,0,drawRect.getMaxX(),
				-1,0,0,-drawRect.getMinX(),
				0,1,0,drawRect.getMaxY(),
				0,-1,0,-drawRect.getMinY(),
				0,0,1,drawRect.getMaxZ(),
				0,0,-1,-drawRect.getMinZ()
		};
		
		if(getLocation(suvRenderRectClippingPlanes) != -1){
			gl.glUniform4fv(getLocation(suvRenderRectClippingPlanes), planesInDrawRectSpace.length, planesInDrawRectSpace, 0);
		}
		
		isClippingUpdatable = false;
	}

	private void updateGlobalScale(GL4 gl2) {
		
		//full volume assumption
		if(!useSparseVolume && drawCubeTransformation == null){

			List<Matrix4> transformations = new ArrayList<Matrix4>();
			
			for(VolumeDataBlock data: dataManager.getVolumes()){
			
				transformations.add(calcScaledVolumeTransformation(data));
			}

			setDrawRect(calculateCloseFittingBox(transformations));
		}
		gl2.glUniformMatrix4fv(getLocation(suvDrawCubeTransformation),1,false,drawCubeTransformation.getMatrix(),0);
	}

	private void updateIsoValue(GL4 gl2){
		if(!isIsoSurfaceValueUpdatable){
			return;
		}
		gl2.glUniform1f(getLocation(suvIsoValue), isoSurfaceValue);

		isIsoSurfaceValueUpdatable = false;
	}

	private boolean updateTextureData(GL4 gl2){

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
			int dim[];
			if(!useSparseVolume ){
				dim = new int[]{(int)data.dimensions[0], 
						(int)data.dimensions[1], 
						(int)data.dimensions[2]};
				volumeTextureMap.get(i).update(gl2, 0, buffer, dim);
			}else{
				
				long tmp[] = calculateSparseVirtualTextures(gl2,volumeTextureMap.get(i),data.dimensions);
				data.dimensions = tmp.clone();
		
				dim = new int[]{(int)data.dimensions[0],(int)data.dimensions[1],(int)data.dimensions[2]};
				int[] offsets = new int[]{(int)data.memOffset[0],(int)data.memOffset[1],(int)data.memOffset[2]};
				int[] sizes = new int[]{(int)data.memSize[0],(int)data.memSize[1],(int)data.memSize[2]};
				volumeTextureMap.get(i).updateSparse(gl2, 0,buffer, dim, offsets, sizes);
			}
			if(getLocation(suvVoxelCount)!=-1){
				gl2.glUniform3iv(getLocation(suvVoxelCount)+i, 1,dim,0 );
			}
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
	protected void generateIdMappingSubClass(GL4 gl2) {


		mapUniforms(gl2, new String[]{		
				suvVoxelCount,
				suvDrawCubeTransformation,
				suvTextureTransformationInverse,
				suvActiveVolumes,
				suvEyePosition,
				suvMinVolumeValue,
				suvMaxVolumeValue,
				suvVolumeTexture,
				suvColorTexture,
				suvIsoValue, 
				suvBackgroundColor,
				suvLightPosition,
				suvNormalSlice,
				suvShowSlice,
				suvSamples,
				suvUseGradient,
				suvRenderRectClippingPlanes,
				suvRenderRectStepSize,
				suvTransferFuntionSize,
				suvOpacity3D
		});
		
		accumulator.init(gl2);

		int location = getLocation(suvVolumeTexture);
		for(int i =0; i< sources.getMaxNumberOfVolumes(); i++){
			Texture t = createVolumeTexture(gl2, location + i);
		//	t.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			t.setShouldGenerateMidmaps(true);
			volumeTextureMap.put(i, t);
		}
		location = getLocation(suvColorTexture);
		colorTexture = new Texture(GL2.GL_TEXTURE_1D,location,GL2.GL_RGBA,GL2.GL_RGBA,GL2.GL_FLOAT);
		colorTexture.genTexture(gl2);
		colorTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
	}

	@Override
	protected void updateVertexBufferSubClass(GL4 gl2, VertexAttribute position) {
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);	
	}

	@Override
	public void setModelTransformation(Matrix4 modelTransformations) {
		if(modelTransformations.equals(getModelTransformation())){
			return;
		}
		super.setModelTransformation(modelTransformations);

		isSliceUpdateable = true;
	}

	@Override
	public void setView(Matrix4 view) {
		if(view.equals(getView())){
			return;
		}
		super.setView(view);

		isEyeUpdateable = true;
	}

	private void updateColor(GL4 gl2){
		if(!isColorUpdateable){
			return;
		}

		//get Buffer last key is the highest number 
		FloatBuffer buffer = tf.getTexture(length/(float)samples); 

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
	protected void renderSubClass(GL4 gl2) {
		gl2.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0,coordinates.length/3);
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
	protected void disposeSubClass(GL4 gl2) {
		colorTexture.delete(gl2);

		for(Texture texture: volumeTextureMap.values() ){
			texture.delete(gl2);
		}
		
		accumulator.disposeGL(gl2);
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

	public void setSamples(int intValue) {
		this.samples = intValue;

		isSamplesUpdatable = true;
		isColorUpdateable = true;

	}

	/**
	 * @param accumulatur the accumulator to set
	 */
	public void setAccumulator(AbstractVolumeAccumulator accumulator) {
		this.accumulator = accumulator;
		accumulator.setParent(this);
		sources.setAccumulator(accumulator);
	}

	/**
	 * return The associated data manager 
	 */
	public VolumeDataManager getDataManager() {
		return dataManager;
	}

	/**
	 * 
	 * @return the current bounding volume to draw
	 */
	public AABBox getDrawRect() {
		
		return drawRect;
	}

	/**
	 * @return the opacity3D
	 */
	public float getOpacity3D() {
		return opacity3D;
	}

	/**
	 * @param opacity3d the opacity3D to set
	 */
	public void setOpacity3D(float opacity3d) {
		opacity3D = opacity3d;
		isOpacity3DUpdateable = true;
	}
}
