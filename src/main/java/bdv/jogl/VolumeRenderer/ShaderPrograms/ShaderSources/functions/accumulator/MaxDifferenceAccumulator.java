package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvActiveVolumes;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class MaxDifferenceAccumulator extends AbstractVolumeAccumulator {

	public MaxDifferenceAccumulator() {
		super("difference");
	}
	
	@Override
	public String[] colorAccDecl() {
		String[] dec = {
			"vec3 "+getColorFunctionName()+"(vec4 colors["+scvMaxNumberOfVolumes+"],vec4 refinedValues["+scvMaxNumberOfVolumes+"]){",
			"	vec3 color = vec3(0.0);",
			"	float maxValue =0;",
			"	for(int v =0; v < "+scvMaxNumberOfVolumes+"; v++){",
			"		if("+suvActiveVolumes+"[v]==0){",
			"			continue;",
			"		}",
			"		float value = refinedValues[v].a;",
			"		if(value < 0.0){",
			"			continue;",	
			"		}",
			"		if(maxValue < value){",
			"			maxValue = value;",
			"			color= colors[v].rgb;",
			"		}",
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
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 7",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float difference = 0;",		
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		for(int m = 0; m < "+scvMaxNumberOfVolumes+";m++){",
				"			if(densities[n]<0 || densities[m]<0){",
				"				continue;",
				"			}",	
				"			float currentDifference = densities[n]-densities[m];",
				"			difference = max(difference,currentDifference);",	
				"		}",
				"	}",
				"	return difference;",	
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
