package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class IsoSurfaceVolumeInterpreter extends AbstractVolumeInterpreter {

	private VolumeGradientEvaluationFunction gradEval = new VolumeGradientEvaluationFunction();
	
	public IsoSurfaceVolumeInterpreter() {
		super("isoSurfaceInterpreter");

	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		addCodeArrayToList(gradEval.declaration(),code);
		addCodeArrayToList( new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v){",
				"	int n = 0;",
				"	if(vm1 -"+scvMinDelta+" <= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" <= v +"+scvMinDelta+"  ||",
				"		    vm1 + "+scvMinDelta+" >= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" >= v -"+scvMinDelta+"){",
				"		float factorDiffuse = 0.0;",
				"		for(int volume = 0; volume < "+scvMaxNumberOfVolumes+"; volume++){",
				"			if(any(lessThan("+sgvRayPositions+"[volume],vec3(0.0)))||any(greaterThan("+sgvRayPositions+"[volume],vec3(1.0)))){",
				"				continue;",				
				"			}",	
				"			vec3 gradient = "+gradEval.call(new String[]{sgvRayPositions+"[volume]", suvVolumeTexture+"[volume]"})+";",
				"			factorDiffuse += max(dot(normalize(gradient) , "+sgvRayDirections+"[volume]),0.0);",
				"			n++;",	
				"		}",
				"		factorDiffuse /= float(n);",
				"		c = vec4(0.5);",
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
