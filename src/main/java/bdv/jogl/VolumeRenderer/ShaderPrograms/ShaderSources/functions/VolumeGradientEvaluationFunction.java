package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
//import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
public class VolumeGradientEvaluationFunction extends AbstractShaderFunction {

	public VolumeGradientEvaluationFunction() {
		super("gradient");
	}

	@Override
	public String[] declaration() {
		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"vec3 "+getFunctionName()+"(vec3 texCoord, sampler3D volume){",
				/*"	vec3 left = texCoord+vec3(-"+scvMinDelta+",0.0,0.0);",
				"	vec3 right = texCoord+vec3("+scvMinDelta+",0.0,0.0);",
				"	vec3 up = texCoord+vec3(0.0,"+scvMinDelta+",0.0);",
				"	vec3 down = texCoord+vec3(0.0,-"+scvMinDelta+",0.0);",
				"	vec3 front = texCoord+vec3(0.0,0.0,-"+scvMinDelta+");",
				"	vec3 back = texCoord+vec3(0.0,0.0,"+scvMinDelta+");",*/
				"	const float offset = 0.1;",
				"	vec3 left = texCoord+vec3(-offset,0.0,0.0);",
				"	vec3 right = texCoord+vec3(offset,0.0,0.0);",
				"	vec3 up = texCoord+vec3(0.0,offset,0.0);",
				"	vec3 down = texCoord+vec3(0.0,-offset,0.0);",
				"	vec3 front = texCoord+vec3(0.0,0.0,-offset);",
				"	vec3 back = texCoord+vec3(0.0,0.0,offset);",
				"",
				"   vec3 gradient = vec3(	0.5*texture(volume,right).r-texture(volume,left).r,",
				"							0.5*texture(volume,up).r-texture(volume,down).r,",
				"							0.5*texture(volume,back).r-texture(volume,front).r);",
				"	return gradient;",
				"}",
		};
	}

}
