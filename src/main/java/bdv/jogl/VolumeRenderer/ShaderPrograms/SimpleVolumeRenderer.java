package bdv.jogl.VolumeRenderer.ShaderPrograms;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.SingleVolumeRendererShaderSources.*;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.getNewIdentityMatrix;

import java.nio.FloatBuffer;

import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.SingleVolumeRendererShaderSources;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.gui.TransferFunctionListener;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.glsl.ShaderCode;


/**
 * Volume renderer for single volume
 * @author michael
 *
 */
public class SimpleVolumeRenderer extends AbstractShaderSceneElement {

	private VolumeDataBlock data;

	private float[] coordinates = GeometryUtils.getUnitCubeVerticesQuads(); 

	private Texture volumeTexture;

	private TransferFunction1D tf;
	
	private Texture colorTexture;

	private boolean isEyeUpdateable = false;

	private boolean isColorUpdateable = true;

	private SingleVolumeRendererShaderSources source = new SingleVolumeRendererShaderSources();
	

	/**
	 * constructor 
	 */
	public SimpleVolumeRenderer(){
		for(ShaderCode code :source.getShaderCodes()){
			shaderCodes.add(code);
		}
	
	}

	/**
	 * Updates color data
	 * @param newData
	 */
	public void setTransferFunction(final TransferFunction1D tf){
		this.tf = tf;
		this.tf.addTransferFunctionListener(new TransferFunctionListener() {
			
			@Override
			public void colorChanged(TransferFunction1D transferFunction) {
				isColorUpdateable = true;
			}
		});
		
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
	}


	@Override
	protected void updateVertexBufferSubClass(GL2 gl2, VertexAttribute position) {
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);
	}

	private void updateTextureData(GL2 gl2){
		if(!data.needsUpdate()){
			return;
		}


		//get Buffer
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(data.data);
		buffer.rewind();

		//uploade data
		volumeTexture.update(gl2, 0, buffer, 
				new int[]{(int)data.dimensions[0], 
						(int)data.dimensions[1], 
						(int)data.dimensions[2]});

		//gl2.glBindTexture(GL2.GL_TEXTURE_3D, 0);

		//min max
		gl2.glUniform1f(getLocation(suvMinVolumeValue), data.minValue);
		gl2.glUniform1f(getLocation(suvMaxVolumeValue), data.maxValue);


		data.setNeedsUpdate(false);
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

	private void updateEye(GL2 gl2){
		if(!isEyeUpdateable){
			return;
		}

		float [] eyePosition = calculateEyePosition();

		//eye position
		gl2.glUniform3f(getLocation(suvEyePosition), eyePosition[0],eyePosition[1],eyePosition[2]);
		isEyeUpdateable = false;
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
	protected void updateShaderAttributesSubClass(GL2 gl2) {

		updateTextureData(gl2);

		updateColor(gl2);

		updateEye(gl2);

	}

	@Override
	protected void generateIdMappingSubClass(GL2 gl2) {

		//get location
		mapUniforms(gl2, new String[]{
				suvEyePosition,
				suvMinVolumeValue,
				suvMaxVolumeValue,
				suvVolumeTexture,
				suvColorTexture
		});

		int location = getLocation(suvVolumeTexture);
		volumeTexture = new Texture(GL2.GL_TEXTURE_3D,location,GL2.GL_R32F,GL2.GL_RED,GL2.GL_FLOAT);
		volumeTexture.genTexture(gl2);
		volumeTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
		volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
		volumeTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);

		location = getLocation(suvColorTexture);
		colorTexture = new Texture(GL2.GL_TEXTURE_1D,location,GL2.GL_RGBA,GL2.GL_RGBA,GL2.GL_FLOAT);
		colorTexture.genTexture(gl2);
		colorTexture.setTexParameteri(gl2,GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		colorTexture.setTexParameteri(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
	}


	@Override
	protected int getVertexBufferSize() {

		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}

	@Override
	protected void renderSubClass(GL2 gl2) {

		gl2.glDrawArrays(GL2.GL_QUADS, 0,coordinates.length/3);

	}
}
