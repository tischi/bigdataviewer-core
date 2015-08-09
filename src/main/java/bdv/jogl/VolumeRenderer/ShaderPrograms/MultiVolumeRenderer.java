package bdv.jogl.VolumeRenderer.ShaderPrograms;

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
public class MultiVolumeRenderer extends AbstractShaderSceneElement {

	private final Map<Integer,Matrix4> transformations = new HashMap<Integer, Matrix4>(); 
	
	private final Map<Integer,VolumeDataBlock> dataValues = new HashMap<Integer, VolumeDataBlock>();
	
	@Override
	protected void updateVertexBufferSubClass(GL2 gl2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateShaderAttributesSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void generateIdMappingSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping,
			ShaderProgram shaderProgram) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getVertexBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void renderSubClass(GL2 gl2,
			Map<String, Integer> shaderVariableMapping) {
		// TODO Auto-generated method stub
		
	}
	
	
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
	public final Map<Integer,Matrix4> getModelTransformations() {
		return transformations;
	}
}
