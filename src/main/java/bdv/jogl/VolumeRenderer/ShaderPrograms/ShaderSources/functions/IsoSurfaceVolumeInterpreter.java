package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class IsoSurfaceVolumeInterpreter extends AbstractVolumeInterpreter {

	private VolumeGradientEvaluationFunction gradEval = new VolumeGradientEvaluationFunction();
	
	private final int refinementSteps = 4;
	
	public IsoSurfaceVolumeInterpreter() {
		super("isoSurfaceInterpreter");

	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		addCodeArrayToList(gradEval.declaration(),code);
		addCodeArrayToList( new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"//bisection form http://onlinelibrary.wiley.com/doi/10.1111/j.1467-8659.2005.00855.x/abstract",
				"vec3 bisection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue){",
				"	vec3 xNew = (xFar - xNear) * (isoValue - fNear)/(fFar - fNear) + xNear;",
				"	return xNew;",	
				"}",
				"",
				"vec3[2] refineIntersection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue, sampler3D volume){",
				"	vec3[2] refined;",
				"	for(int i =0; i < "+refinementSteps+"; i++){",
				"		vec3 xNew = bisection(fNear,fFar,xNear,xFar,isoValue);  ",
				"		float fNew = texture(volume, xNew).r * "+sgvVolumeNormalizeFactor+";",
				"		if(fNew > isoValue ){",
				"			xNear = xNew;",
				"		}else{",
				"			xFar = xNew;",	
				"		}",	
				"	}",
				"	refined[0] = xNear;",
				"	refined[1] = xFar;",
				"	return refined;",
				"}",
				"",		
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v){",
				"	int n = 0;",
				"	if(vm1 -"+scvMinDelta+" <= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" <= v +"+scvMinDelta+"  ||",
				"		    vm1 + "+scvMinDelta+" >= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" >= v -"+scvMinDelta+"){",
				"		float factorDiffuse = 0.0;",
				"		for(int volume = 0; volume < "+scvMaxNumberOfVolumes+"; volume++){",
				"			if(any(lessThan("+sgvRayPositions+"[volume],vec3(0.0)))||any(greaterThan("+sgvRayPositions+"[volume],vec3(1.0)))){",
				"				continue;",				
				"			}",	
				"			vec3 xNear = "+sgvRayPositions+"[volume] - "+sgvRayDirections+"[volume] * "+sgvSampleSize+";",
				"			vec3 xFar = "+sgvRayPositions+"[volume];", 
				"			vec3 refined[2] = refineIntersection(vm1,v,xNear,xFar,"+sgvNormIsoValue+","+suvVolumeTexture+"[volume]);",
			//	"			vec3 gradient = "+gradEval.call(new String[]{sgvRayPositions+"[volume]", suvVolumeTexture+"[volume]"})+";",
				"			vec3 gradient = "+gradEval.call(new String[]{"refined[1]", suvVolumeTexture+"[volume]"})+";",
				"			factorDiffuse += max(dot(normalize(gradient) , "+sgvRayDirections+"[volume]),0.0);",
				"			n++;",	
				"		}",
				"		factorDiffuse /= float(n);",
				"		c = vec4(0.8);",
				"		c.a = 1.0;",
				"		c.rgb *= factorDiffuse;",	
				"	}",	
				"	vec4 c_out = c_in + (1.0 - c_in.a)*c;",
				"	return c_out;",
				"}"
		},code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
