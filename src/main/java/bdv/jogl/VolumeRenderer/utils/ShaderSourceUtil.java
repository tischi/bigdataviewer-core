package bdv.jogl.VolumeRenderer.utils;


/**
 * 
 * @author michael
 *
 */
public class ShaderSourceUtil {

	/**
	 * appends system depending new lines to each line of the source code
	 * @param shaderCode
	 */
	public static void appendNewLines(String[] shaderCode){
		for(int i = 0; i < shaderCode.length; i++){
			   
			shaderCode[i] = new String(shaderCode[i] + System.lineSeparator());
		}
	}
	
}
