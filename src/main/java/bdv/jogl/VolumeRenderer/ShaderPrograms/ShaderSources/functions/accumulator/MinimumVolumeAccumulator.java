package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;

public class MinimumVolumeAccumulator extends AbstractVolumeAccumulator {

	public MinimumVolumeAccumulator(){
		super("minimum");
	}
	
	@Override
	public String[] declaration() {
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
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
		return dec;
	}

}
