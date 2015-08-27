package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;

/**
 * calculates the amount of steps of a certain size the ray
 * needs till it reaches a volume.
 * @author michael
 *
 */
public class GetStepsToVolumeFunction extends AbstractShaderFunction {

	public GetStepsToVolumeFunction(){
		super("getStepsToVolume");
	}

	@Override
	public String[] declaration() {
		String dec[] = {
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"int "+getFunctionName()+"(float stepsSize, vec3 position, vec3 direction){",
				"	//infinite steps ;)",
				"	int steps = 0;",
				"",	
				"	vec3 targetPoint =vec3(1) - max(sign(direction),vec3(0,0,0));",
				"	vec3 differenceVector = targetPoint - position;",
				"	vec3 stepsInDirections = differenceVector / (direction * stepsSize);",
				"	for(int i =0; i< 3; i++){",
				"		if(stepsInDirections[i] < steps){",
				"			steps = int(floor(stepsInDirections[i]));",
				"		}",
				"	}",
				"	return steps;",
				"}"
		};
		return dec;
	}



}
