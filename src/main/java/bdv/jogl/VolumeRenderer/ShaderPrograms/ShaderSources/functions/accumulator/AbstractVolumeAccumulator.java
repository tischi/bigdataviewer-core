package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.AbstractShaderFunction;

public abstract class AbstractVolumeAccumulator extends AbstractShaderFunction {
	
	private String colorFunctionName = "accumulateColor"; 
	
	/**
	 * @return the colorFunctionName
	 */
	public String getColorFunctionName() {
		return colorFunctionName;
	}

	protected AbstractVolumeAccumulator(String name){
		super(name);
	}
	
	public String callColor(String[]par){
		String call = getColorFunctionName() + "(";
		for(int i = 0; i<par.length; i++){
			call+=par[i];
			if(i < par.length-1){
				call+=",";
			}
		}
		call+=")";
		return call;
 	}
	
	protected abstract String[] colorAccDecl();
	
}
