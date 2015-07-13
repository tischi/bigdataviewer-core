package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.utils.GeometryUtils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
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
	
	private float[] data;
	
	private int[] dimensionOfData;
	
	private int textureObject =-1;
	
	private float[] coordinates = GeometryUtils.getUnitCubeVertices(); 
	
	private boolean isDataUpdateable = false;
	
	/**
	 * @return the data
	 */
	public float[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(float[] data) {
		this.data = data;
		isDataUpdateable = true;
	}

	/**
	 * @return the dimensionOfData
	 */
	public int[] getDimension() {
		return dimensionOfData;
	}

	/**
	 * @param dimensionOfData the dimensionOfData to set
	 */
	public void setDimension(int[] dimensionOfData) {
		this.dimensionOfData = dimensionOfData;
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

	
	@Override
	protected void updateShaderAttributesSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping) {
			
		if(!isDataUpdateable){
			return;
		}
		
		//activate context
		gl2.glActiveTexture(GL2.GL_TEXTURE0);
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, textureObject);
		gl2.glUniform1i(shaderVariableMapping.get(shaderVariableVolumeTexture),0);
		
		//get Buffer
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(data);
		buffer.rewind();
	
		//uploade data
		gl2.glTexImage3D(GL2.GL_TEXTURE_3D, 
						0, 
						GL2.GL_R32F, 
						dimensionOfData[0], 
						dimensionOfData[1], 
						dimensionOfData[2], 
						0,
						GL2.GL_RED, 
						GL2.GL_FLOAT, 
						buffer);
		
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, 0);
		isDataUpdateable = false;
	}

	
	@Override
	protected void generateIdMappingSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping,
			ShaderProgram shaderProgram) {
		
		//get location
		int location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableVolumeTexture);
		shaderVariableMapping.put(shaderVariableVolumeTexture, location);
		
		//activate texture
		gl2.glActiveTexture(GL2.GL_TEXTURE0);
		
		//generate texture object
		int[] textures = new int[1];
		gl2.glGenTextures(textures.length, textures,0);
		textureObject = textures[0];
		
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, textureObject);

		
		//activate texture unit
		gl2.glUniform1i(shaderVariableMapping.get(shaderVariableVolumeTexture),0);
		//gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
		gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
		gl2.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
	}

	
	@Override
	protected int getVertexBufferSize() {
		
		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}

	
	@Override
	protected void renderSubClass(GL2 gl2) {
	/*	gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK); 
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthRangef(0.1f, 1000000);
		gl2.glDepthFunc(GL2.GL_LEQUAL);*/
		//gl2.glActiveTexture(GL2.GL_TEXTURE0);
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, textureObject);
		gl2.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0,coordinates.length/3);
		gl2.glBindTexture(GL2.GL_TEXTURE_3D, 0); 
		/*gl2.glDisable(GL2.GL_CULL_FACE);
	    gl2.glDisable(GL2.GL_DEPTH_TEST);*/
	}
}
