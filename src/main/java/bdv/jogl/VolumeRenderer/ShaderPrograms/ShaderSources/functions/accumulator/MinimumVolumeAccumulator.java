package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvNormIsoValue;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvActiveVolumes;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class MinimumVolumeAccumulator extends AbstractVolumeAccumulator {

	public MinimumVolumeAccumulator(){
		super("minimum");
	}
	@Override
	public String[] colorAccDecl() {
		String[] dec = {
			"vec3 "+getColorFunctionName()+"(vec4 colors["+scvMaxNumberOfVolumes+"],vec4 refinedValues["+scvMaxNumberOfVolumes+"]){",
			"	vec4 color = vec4(0.0,0.0,0.0,1.0);",
			"	float maxValue =0;",
			"	for(int v =0; v < "+scvMaxNumberOfVolumes+"; v++){",
			"		if("+suvActiveVolumes+"[v]==0){",
			"			continue;",
			"		}",
			"		float value = refinedValues[v].a;",
			"		if(value < 0.0|| colors[v].a < 0.0){",
			"			continue;",	
			"		}",
			"		if(colors[v].a > color.a ){",
			"			continue;",	
			"		}",	
			"		if(maxValue < value){",
			"			maxValue = value;",
			"			color= colors[v];",
			"		}",
			"	}",
			"	return color.rgb;",
			
			/*	"	vec3 color = vec3(0.0);",
			"	float minValue = "+Float.MAX_VALUE+";",	
			"	for(int v =0; v < "+scvMaxNumberOfVolumes+"; v++){",
			"		float value = refinedValues[v].a;",
			"		if("+suvActiveVolumes+"[v]==0){",
			"			continue;",
			"		}",
			"		if(value < 0.0){",
			"			continue;",	
			"		}",
			"		float threshold = 0.2;",
			"		if(value < "+sgvNormIsoValue+"-threshold||value > "+sgvNormIsoValue+" + threshold){",
			"			continue;",
			"		}",
			"		if(minValue > value){",
			"			minValue = value;",
			"			color= colors[v].rgb;",
			"		}",
			"	}",
			"	return color;",*/
			"}"
		};
		return dec;
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 9",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = "+Float.MAX_VALUE+";",		
				"	int numberOfChanges=  0;",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(densities[n] < 0){",
				"			continue;",	
				"		}",	
				"		density = min(density, densities[n]);",
				"		numberOfChanges++;",
				"	}",
				"	if(numberOfChanges ==0){",
				"		return 0.0;",
				"	}",
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
