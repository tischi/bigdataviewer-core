package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;

import java.util.ArrayList;
import java.util.List;

public class AverageVolumeAccumulator extends AbstractVolumeAccumulator {

	public AverageVolumeAccumulator(){
		super("average");
	}
	
	@Override
	protected String[] colorAccDecl() {
		String[] dec = {
			"vec3 "+getColorFunctionName()+"(vec4 colors["+scvMaxNumberOfVolumes+"], vec4 refinedValues["+scvMaxNumberOfVolumes+"]){",
			"	vec3 color = vec3(0.0);",
			"	int n=0;",	
			"	for(int v =0; v < "+scvMaxNumberOfVolumes+"; v++){",
			"		if("+suvActiveVolumes+"[v]==0 ){",
			"			continue;",
			"		}",
			"		float value = refinedValues[v].a;",
			"		if(value < 0.0){",
			"			continue;",	
			"		}",
			"		color+=colors[v].rgb;",
			"		n++;",
			"	}",
			"	if(n!=0){",
			"		color /= float(n);",
			"	}",	
			"	return color;",
			"}"
		};
		return dec;
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 6",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0.0;",
				"	int count =0;",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(densities[n]< 0.0){",
				"			continue;",
				"		}",
				"		density += densities[n];",
				"		count++;",
				"	}",
				"	if(count == 0 ){",
				"		return 0.0;",
				"	}",	
				"	density/=float(count);",
				"	return density;",	
				"}"
		};
		addCodeArrayToList(colorAccDecl(), code);
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
