package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.interpreter;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class PreIntegrationInterpreter extends AbstractTransferFunctionInterpreter {
	//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
	@Override
	public String[] declaration() {
		String dec[] ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"uniform sampler1D "+suvColorTexture+";",
				"const float minValue = 0.000001;",
				"const float minValueHalf = minValue/2.0;",
				"",
				"vec4 "+getFunctionName()+"(float vbegin, float vend, float distance){",
				"	if(vbegin - vend < minValue){",
				"		if(vbegin - minValueHalf < 0.0){",
				"			vend+= minValue;",	
				"		}else{",	
				"			if(vend +minValueHalf > 1.0){",
				"				vbegin-=minValue;",
				"			}else{",			
				"				vend+=minValueHalf;",
				"				vbegin-=minValueHalf;",
				"			}",
				"		}",	
				"	}",
				"	vec4 iFront = texture("+suvColorTexture+",vbegin);",
				"	vec4 iBack = texture("+suvColorTexture+",vend);",
				"	vec4 color = vec4(0.0);",
				"	vec4 iDiff = distance/(vend - vbegin) * (iBack - iFront);",
				"",
				"	//alpha desample",
				"	color.a = 1.0 - exp(-iDiff.a);",
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
