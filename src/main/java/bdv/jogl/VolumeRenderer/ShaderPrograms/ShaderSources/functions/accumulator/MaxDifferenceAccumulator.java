package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;

public class MaxDifferenceAccumulator extends AbstractVolumeAccumulator {

	public MaxDifferenceAccumulator() {
		super("difference");
	}
	
	@Override
	public String[] declaration() {
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
		return dec;
	}

}
