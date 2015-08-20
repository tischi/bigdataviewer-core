package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

/**
 * Function to get the maximal amount of steps in a volume 
 * @author michael
 *
 */
public class GetMaxStepsFunction extends AbstractShaderFunction implements IFunction {
	
	public GetMaxStepsFunction(){
		super("getMaxSteps");
	}
	
	@Override
	public String[] declaration() {
		String dec[] = {
		"int "+getFunctionName()+"(float stepsSize, vec3 position, vec3 direction){",
		"	//infinite steps ;)",
		"	int steps = maxInt;",
		"",	
		"	vec3 targetPoint = max(sign(direction),vec3(0,0,0));",
		"	vec3 differenceVector = targetPoint - position;",
		"	vec3 stepsInDirections = differenceVector / (direction * stepsSize);",
		"	for(int i =0; i< 3; i++){",
		"		if(stepsInDirections[i] < steps){",
		"			steps = int(stepsInDirections[i])+1;",
		"		}",
		"	}",
		"	return steps;",
		"}"};
		return dec;
	}
}
