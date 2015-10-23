package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.transferfunctioninterpreter;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class PreIntegrationInterpreter extends AbstractTransferFunctionInterpreter {
	//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
	@Override
	public String[] declaration() {
		String dec[] ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 10",
				"uniform sampler2D "+suvColorTexture+";",
				"float texoffset = 1.0/(2.0*"+suvMaxVolumeValue+");",
				"float texnorm = ("+suvMaxVolumeValue+"-1.0)/"+suvMaxVolumeValue+";",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	return texture("+suvColorTexture+",vec2(vbegin*texnorm + texoffset,vend*texnorm + texoffset)).rgba;",
				"}"
		};
		return dec;
	}

}
