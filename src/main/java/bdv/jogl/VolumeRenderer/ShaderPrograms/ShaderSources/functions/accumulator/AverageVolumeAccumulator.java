package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class AverageVolumeAccumulator extends AbstractVolumeAccumulator {

	public AverageVolumeAccumulator(){
		super("average");
	}
	
	@Override
	public String[] declaration() {
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
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
		return dec;
	}

}
