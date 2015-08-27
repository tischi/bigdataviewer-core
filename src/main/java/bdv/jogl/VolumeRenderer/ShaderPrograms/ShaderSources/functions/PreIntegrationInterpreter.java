package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class PreIntegrationInterpreter extends AbstractTransferFunctionInterpreter {
	//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
	@Override
	public String[] declaration() {
		String dec[] ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"uniform sampler1D "+suvColorTexture+";",
				"const float minValue = 0.00001f;",
				"const float minValueHalf = minValue/2.f;",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	if(vbegin - vend < minValue){",
				"		vend+=minValueHalf;",
				"		vbegin-=minValueHalf;",
				"	}",
				"	vec4 iFront = texture("+suvColorTexture+",vbegin);",
				"	vec4 iBack = texture("+suvColorTexture+",vend);",
				"	vec4 color = vec4(0.f);",
				"	vec4 iDiff = distance/(vend - vbegin) * (iBack - iFront);",
				"",
				"	//alpha desample",
				"	color.a = 1.f - exp(-iDiff.a);",
				"",
				"	//rgb desample",
				"	color.rgb = iDiff.rgb;",
				"	return color;",
				"}",
				""
		};
		return dec;
	}

}
