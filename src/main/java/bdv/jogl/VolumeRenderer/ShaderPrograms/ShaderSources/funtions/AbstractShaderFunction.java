package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

/**
 * defines function name for function and std call syntax
 * @author michael
 *
 */
public abstract class AbstractShaderFunction implements IFunction {

	private final String functionName;

	
	protected AbstractShaderFunction(String functionName){
		this.functionName = functionName; 
	}

	/**
	 * Returns the name of the shader function
	 * @return
	 */
	public String getFunctionName() {
		return functionName;
	}
	
	@Override
	public String call(String[] parameters) {
		String call = functionName + "(";
		int i =0;
		for(String parameter:parameters){
			call += parameter;
			if(i < parameters.length-1){
				call+=",";
			}
			i++;
		}
		call+=")";
		return call;
	}
}
