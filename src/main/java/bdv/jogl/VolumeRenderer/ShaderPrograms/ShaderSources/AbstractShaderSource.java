package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.jogamp.opengl.util.glsl.ShaderCode;

/**
 * Defines basic methods for Shader sources of a single program.
 * All shaders (Vertex, fragment, etc) should be put in here.
 * @author michael
 *
 */
public abstract class AbstractShaderSource {
	
	private List<ISourceListener> sourceListeners = new LinkedList<ISourceListener>();

	//default vertex shader attributes
	public static String shaderAttributePosition = "inPosition";
	
	//default vertex shader uniforms
	public static final String shaderUniformVariableProjectionMatrix = "inProjection";

	public static final String shaderUniformVariableViewMatrix = "inView";

	public static final String shaderUniformVariableModelMatrix = "inModel";
	
	/**
	 * Retruns all shader codes build from the specific source.
	 * @return
	 */
	public abstract Set<ShaderCode> getShaderCodes();
	
	/**
	 * adds a source listener
	 * @param listener
	 */
	public void addSourceListener(ISourceListener listener){
		sourceListeners.add(listener);
	}
	
	/**
	 * clears all source listeners
	 */
	public void clearSourceListeners(){
		sourceListeners.clear();
	}
}
