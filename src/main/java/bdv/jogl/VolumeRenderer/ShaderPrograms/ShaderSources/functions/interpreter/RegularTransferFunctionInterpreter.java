package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.interpreter;

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
				"	vec4 color = texture("+suvColorTexture+",vend) ;",
				"	float tau = color.a;",
				"	float alpha = 1.0 - exp(-tau*distance);",
				"	color *= tau;",
				"	color.a = alpha;",
				"	return color;",
				"}",
				""
		};
		return dec;
	}
}
