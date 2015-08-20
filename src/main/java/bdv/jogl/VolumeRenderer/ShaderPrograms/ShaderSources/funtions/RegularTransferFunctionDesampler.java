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
				"	return texture("+suvTransferFunctionTexture+",vbegin);",
				"}",
				""
		};
		return dec;
	}
}
