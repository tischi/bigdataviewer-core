package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

public class PreIntegrationDesampler extends AbstractTransferFunctionDesampler {
	//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
	@Override
	public String[] declaration() {
		String dec[] ={
				"",
				"uniform sampler1D "+suvTransferFunctionTexture+";",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	if(vbegin < 0.f || vend < 0.f){",
				"		return vec4(0.f);",
				"	}",
				"	vec4 iFront = texture("+suvTransferFunctionTexture+",vbegin);",
				"	vec4 iBack = texture("+suvTransferFunctionTexture+",vend);",
				"	vec4 iDiff = iBack - iFront;",
				"	vec4 color = vec4(0.f);",
				"	float vDistFactor = distance/(vend - vbegin);",
				"	iDiff = vDistFactor * iDiff;",
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
