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
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(density > densities[n]){",
				"			density = densities[n];",
				"		}",	
				"	}",
				"	return density;",	
				"}"
		};
		return dec;
	}

}
