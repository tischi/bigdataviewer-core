package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
/**
 * Desamples a regular sampled transfer function
 * @author michael
 *
 */
public class RegularTransferFunctionInterpreter extends AbstractTransferFunctionInterpreter {
	@Override
	public String[] declaration() {
		String dec[] ={
				"",
				"uniform sampler1D "+suvColorTexture+";",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	vec4 color = texture("+suvColorTexture+",vbegin);",
				"	color.a = 1-exp(-color.a * distance);",	
				"	return color;",
				"}",
				""
		};
		return dec;
	}
}
