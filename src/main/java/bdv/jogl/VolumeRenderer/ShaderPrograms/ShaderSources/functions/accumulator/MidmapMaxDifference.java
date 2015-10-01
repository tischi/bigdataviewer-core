package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates the max difference of the midmaps
 * @author michael
 *
 */
public class MidmapMaxDifference extends AbstractVolumeAccumulator {

	public MidmapMaxDifference(){
		super("difference_of_midmap_area");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 57",
				"float["+scvMaxNumberOfVolumes+"] getMidmapValues(vec3 position){",
				"	float ret["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+"){",
				"		vec3 textureCoord = getCorrectedTexturePositions(position,i);",
				"		float midmapValue = textureLod("+suvVolumeTexture+"[i],textureCoord,1.0).r;",
				"		ret[i] = midmapValue;",
				"	}",
				"	return ret;",
				"}",
				"",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = "+Float.MAX_VALUE+";",
				"	float midMap["+scvMaxNumberOfVolumes+"] = getMidmapValues("+sgvRayPositions+");",	
				"	int numberOfChanges=  0;",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(midMap[n] < 0){",
				"			continue;",	
				"		}",	
				"		for(int m = n+1; m < "+scvMaxNumberOfVolumes+"; m++)",
				"		density = min(density, densities[n]);",
				"		numberOfChanges++;",
				"	}",
				"	if(numberOfChanges ==0){",
				"		return 0.0;",
				"	}",
				"	return density;",	
				"}"
		};
		
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
