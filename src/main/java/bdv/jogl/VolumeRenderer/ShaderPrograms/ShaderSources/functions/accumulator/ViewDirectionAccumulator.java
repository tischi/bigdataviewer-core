package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvRayDirections;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

public class ViewDirectionAccumulator extends AbstractVolumeAccumulator {

	
	
	public ViewDirectionAccumulator() {
		super("view_direction_weight");
	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 33",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0.0;",		
				"	float sum = 0.0;",
				"	const int N = "+scvMaxNumberOfVolumes+";",
				"	float weights[N] = calcWeights("+sgvRayDirections+");",
				"	for(int n = 0; n< N; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		sum+=weights[n];",
				"	}",	
				"	for(int n = 0; n < N; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		float weight = (weights[n]/sum);",
				"		density += weight * densities[n];",
				"	}",
				"	density = density*0.5 +0.5;",
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

	@Override
	protected String[] colorAccDecl() {
		// TODO Auto-generated method stub
		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 505",
				"float["+scvMaxNumberOfVolumes+"] calcWeights(vec3 rayDirections["+scvMaxNumberOfVolumes+"]){",
				"	float weights["+scvMaxNumberOfVolumes+"];",
				"	for(int v=0; v < "+scvMaxNumberOfVolumes+"; v++){",
				"		weights[v] = abs(dot(rayDirections[v],vec3(0,0,1)));",
				"	}",
				"	return weights;",
				"}",
				"",
				"vec3 "+getColorFunctionName()+"(vec4 colors["+scvMaxNumberOfVolumes+"],vec4 refinedValues["+scvMaxNumberOfVolumes+"]){",
				"	vec3  color = vec3(0.0);",		
				"	float sum = 0.0;",
				"	const int N = "+scvMaxNumberOfVolumes+";",
				"	float weights[N] = calcWeights("+sgvRayDirections+");",
				"	for(int n = 0; n< N; n++){",
				"		if(refinedValues[n].a < 0.0){",
				"			continue;",	
				"		}",	
				"		sum+=weights[n];",
				"	}",	
				"	for(int n = 0; n < N; n++){",
				"		if(refinedValues[n].a < 0.0){",
				"			continue;",	
				"		}",	
				"		float weight = (weights[n]/sum);",
				"		color += weight * colors[n].rgb;",
				"	}",
				"	color = color*0.5 +vec3(0.5);",
				"	return color;",	
				"}"
		};
	}

}
