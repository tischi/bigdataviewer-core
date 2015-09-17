package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvActiveVolumes;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class MaximumVolumeAccumulator extends AbstractVolumeAccumulator {

	public MaximumVolumeAccumulator(){
		super("maximum");
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
			"		if(value < 0.0){",
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
			"}"
		};
		return dec;
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 8",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0;",		
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		density = max(density,densities[n]);",
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
