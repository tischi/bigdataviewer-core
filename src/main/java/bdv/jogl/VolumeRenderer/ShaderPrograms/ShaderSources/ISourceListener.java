package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.EventListener;

/**
 * Listener for Shader Source events
 * @author michael
 *
 */
public interface ISourceListener extends EventListener {

	/**
	 * triggered if a certain operation changed the shader source code.
	 */
	public void sourceCodeChanged();
}
