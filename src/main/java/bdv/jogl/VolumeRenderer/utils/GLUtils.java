package bdv.jogl.VolumeRenderer.utils;

import com.jogamp.opengl.GL;

/**
 * utils for handling gl contextes
 * @author michael
 *
 */
public class GLUtils {

	/**
	 * Checks if a specific extension is supported in the current gl context
	 * @param context The current gl context.
	 * @param extensionName The extension name. 
	 * @return True if the extension is supported, else false.
	 */
	public static boolean contextSupportsExtension(final GL context, final String extensionName){
		String extensions = context.glGetString(GL.GL_EXTENSIONS);
		return extensions == null ? false : extensions.indexOf(extensionName) != -1;
	}
	
}
