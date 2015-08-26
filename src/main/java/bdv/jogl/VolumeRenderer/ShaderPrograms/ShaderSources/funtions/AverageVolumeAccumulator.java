package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class AverageVolumeAccumulator extends AbstractVolumeAccumulator {

	@Override
	public String[] declaration() {
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0;",		
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		density += densities[n]-"+suvMinVolumeValue+";",
				"	}",
				"	density/="+scvMaxNumberOfVolumes+";",
				"	return density;",	
				"}"
		};
		return dec;
	}

}
