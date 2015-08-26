package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class AverageVolumeAccumulator extends AbstractVolumeAccumulator {

	@Override
	public String[] declaration() {
		String[] dec= new String[]{
		"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
		"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
		"	float density = 0;",	
		"	int intersectingVolumes = 0;",	
		"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
	/*	"		if(any(lessThan(ray_poss[n], zeros))){",
		"			continue;",
		"		}",
		"		if(any(greaterThan(ray_poss[n], ones))){",
		"			continue;",
		"		}",			*/
		"		density += densities[n]-"+suvMinVolumeValue+";",
		"		intersectingVolumes++;",
		"	}",
		"	if(intersectingVolumes==0){",
		"		intersectingVolumes=1;",
		"	}",
		"	density/=intersectingVolumes;",
		"	return density;",	
		"}"
			};
		return dec;
	}

}
