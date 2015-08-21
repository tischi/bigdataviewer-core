package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

/**
 * Desamples a regular sampled transfer function
 * @author michael
 *
 */
public class RegularTransferFunctionDesampler extends AbstractTransferFunctionDesampler {
	@Override
	public String[] declaration() {
		String dec[] ={
				"",
				"uniform sampler1D "+suvTransferFunctionTexture+";",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	vec4 color = texture("+suvTransferFunctionTexture+",vbegin);",
				"	color.a = 1-exp(-color.a * distance);",	
				"	return color;",
				"}",
				""
		};
		return dec;
	}
}
