package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

public class PreIntegrationInterpreter extends AbstractTransferFunctionInterpreter {
	//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
	@Override
	public String[] declaration() {
		String dec[] ={
				"",
				"uniform sampler1D "+suvTransferFunctionTexture+";",
				"const float minValue = 0.00001f;",
				"const float minValueHalf = minValue/2.f;",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	if(vbegin - vend < minValue){",
				"		vend+=minValueHalf;",
				"		vbegin-=minValueHalf;",
				"	}",
				"	vec4 iFront = texture("+suvTransferFunctionTexture+",vbegin);",
				"	vec4 iBack = texture("+suvTransferFunctionTexture+",vend);",
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
