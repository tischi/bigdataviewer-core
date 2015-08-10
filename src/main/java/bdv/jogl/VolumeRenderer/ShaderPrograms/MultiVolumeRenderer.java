package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.glsl.ShaderProgram;


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

	private final int maxNumberOfDataBlocks = 6;
	
	private final Map<Integer,Matrix4> transformations = new HashMap<Integer, Matrix4>(); 
	
	private final Map<Integer,VolumeDataBlock> dataValues = new HashMap<Integer, VolumeDataBlock>();
	


	@Override
	protected void updateShaderAttributesSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void generateIdMappingSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping,
			ShaderProgram shaderProgram) {

		int location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableGlobalScale);
		shaderVariableMapping.put(shaderVariableGlobalScale, location);
		
		gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableLocalTransformation);
		shaderVariableMapping.put(shaderVariableLocalTransformation, location);
		
		gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableActiveVolumes);
		shaderVariableMapping.put(shaderVariableActiveVolumes, location);
		

	/*	location = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableEyePosition);
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
*/	}
	
	/**
	 * Return the map of volume data with a user specific index.
	 * @return
	 */
	public final Map<Integer, VolumeDataBlock> getVolumeDataMap() {
		return dataValues;
	}
	
	/**
	 * Return the map of transformations for each volume data with a user specific index.
	 * @return
	 */
	public final Map<Integer,Matrix4> getLocalTransformations() {
		return transformations;
	}

	@Override
	protected void updateVertexBufferSubClass(GL2 gl2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getVertexBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void renderSubClass(GL2 gl2, Map<String, Integer> shaderVariableMapping) {
		// TODO Auto-generated method stub
		
	}
}
