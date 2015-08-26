package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;

public class MaximumVolumeAccumulator extends AbstractVolumeAccumulator {

	public MaximumVolumeAccumulator(){
		super("maximum");
	}
	
	@Override
	public String[] declaration() {
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = "+Float.MIN_VALUE+";",		
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(density < densities[n]){",
				"			density = densities[n];",
				"		}",	
				"	}",
				"	return density;",	
				"}"
		};
		return dec;
	}

}
