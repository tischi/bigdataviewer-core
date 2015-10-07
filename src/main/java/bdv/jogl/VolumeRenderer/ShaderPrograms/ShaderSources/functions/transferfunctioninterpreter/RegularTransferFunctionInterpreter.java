package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.transferfunctioninterpreter;

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
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 11",
				"",
				"uniform sampler1D "+suvColorTexture+";",
				"const float maxDistance = 0.75;",
				"float texoffset = 1.0/(2.0*"+suvMaxVolumeValue+");",
				"float texnorm = ("+suvMaxVolumeValue+"-1.0)/"+suvMaxVolumeValue+";",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	vec4 color = texture("+suvColorTexture+",vend*texnorm+texoffset);",
				"	float tau = color.a;",
				"	//fixed assumtion that distance is small to jav accurate results",
				"	if(maxDistance < distance){",
				"		distance = maxDistance;",
				"	}",
				"	float alpha = 1.0 - exp(-tau*distance);",
				"	color.rgb*= tau*distance;",
				"	color.a = alpha;",
				"	return color;",
				"}",
				""
		};
		return dec;
	}
}
