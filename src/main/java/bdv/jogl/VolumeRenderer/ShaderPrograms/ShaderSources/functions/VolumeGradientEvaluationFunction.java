package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
//import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
public class VolumeGradientEvaluationFunction extends AbstractShaderFunction {

	public VolumeGradientEvaluationFunction() {
		super("gradient");
	}

	@Override
	public String[] declaration() {
		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 5",
				"vec3 "+getFunctionName()+"(vec3 texCoord, sampler3D volume){",
				/*"	vec3 left = texCoord+vec3(-"+scvMinDelta+",0.0,0.0);",
				"	vec3 right = texCoord+vec3("+scvMinDelta+",0.0,0.0);",
				"	vec3 up = texCoord+vec3(0.0,"+scvMinDelta+",0.0);",
				"	vec3 down = texCoord+vec3(0.0,-"+scvMinDelta+",0.0);",
				"	vec3 front = texCoord+vec3(0.0,0.0,-"+scvMinDelta+");",
				"	vec3 back = texCoord+vec3(0.0,0.0,"+scvMinDelta+");",*/
				"	const float offset = 0.1;",
				"	float center = texture(volume,texCoord);",
				"	vec3 plus = vec3(	texture(volume,texCoord+vec3(offset,0.0,0.0)).r,",
				"						texture(volume,texCoord+vec3(0.0,offset,0.0)).r,",
				"						texture(volume,texCoord+vec3(0.0,0.0,offset)).r);",
				"	vec3 minus = vec3(	texture(volume,texCoord+vec3(-offset,0.0,0.0)).r,",
				"						texture(volume,texCoord+vec3(0.0,-offset,0.0)).r,",
				"						texture(volume,texCoord+vec3(0.0,0.0,-offset)).r);",
				"	vec3 factor = vec3(0.5);",
			/*	"	for(int d =0; d < 3; d++){",
				"		if(plus[d] < 0.0){",
				"			plus[d] = center;",
				"			factor[d] = 1.0;",
				"		}",
				"		if(minus[d] < 0.0){",
				"			minus[d] = center;",
				"			factor[d] = 1.0;",
				"		}",
				"	}",*/
				"",
				"   vec3 gradient = factor * (plus- minus);",
				"	return gradient;",
				"}",
		};
	}

}
