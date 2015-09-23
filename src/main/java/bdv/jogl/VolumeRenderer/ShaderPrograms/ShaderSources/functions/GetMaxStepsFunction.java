package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
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
		"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 2",
		"int "+getFunctionName()+"(float stepsSize, vec3 position, vec3 direction){",
		"	//infinite steps ;)",
		"	int steps = maxInt;",
		"",	
		"	//steps in volume",
		"	vec3 targetPoint = max(sign(direction),vec3(0,0,0));",
		"	vec3 differenceVector = targetPoint - position;",
		"	vec3 stepsInDirections = differenceVector / (direction * stepsSize);",
		"	for(int i =0; i< 3; i++){",
		"		if(stepsInDirections[i] < steps){",
		"			steps = int(stepsInDirections[i])+1;",
		"		}",
		"	}",
		"	//render cube clipping plane",
		"	vec3 pointAfterTinyStep = position+ direction * stepsSize;",
		"	for(int p = 0; p < 6; p++){",
		"		//move away from plane",
		"		vec4 plane = "+suvRenderRectClippingPlanes+"[p];",
		"		float distPosition0 = abs(plane.x*position.x+plane.y*position.y+plane.z*position.z+plane.a);",
		"		float distPosition1 = abs(plane.x*pointAfterTinyStep.x+plane.y*pointAfterTinyStep.y+plane.z*pointAfterTinyStep.z+plane.a);",
		"		if(distPosition0 <= distPosition1){",
		"			continue;",
		"		}",
		"		int stepsInRect = int(ceil(distPosition0/stepsSize));",
		"		steps = min(steps, stepsInRect);",
		"	}",
		"	return steps;",
		"}"};
		return dec;
	}
}
