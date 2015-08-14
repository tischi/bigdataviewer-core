package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.HashSet;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

/**
 * Class to handles the multi-volume shader codes
 * @author michael
 *
 */
public class MultiVolumeRendererShaderSource extends AbstractShaderSource{
	
	private int maxNumberOfVolumes = 2;

	private static final String svTextureCoordinate = "textureCoordinate";
	
	//Vertex shader uniforms
	public static final String suvDrawCubeTransformation ="inDrawCubeTransformation";

	public static final String suvTextureTransformationInverse ="inTextureTransformationInverse";

	//Fragment shader uniforms 
	public static final String suvActiveVolumes = "inActiveVolumes";

	public static final String suvVolumeTexture = "inVolumeTexture";

	public static final String suvColorTexture = "inColorTexture";

	public static final String suvEyePosition = "inEyePosition";

	public static final String suvMinVolumeValue = "inMinVolumeValue";

	public static final String suvMaxVolumeValue = "inMaxVolumeValue";

	public static final String suvMaxDiagonalLength = "inMaxDiagonalLength";

	/**
	 * @return the maxNumberOfVolumes
	 */
	public int getMaxNumberOfVolumes() {
		return maxNumberOfVolumes;
	}


	/**
	 * @param maxNumberOfVolumes the maxNumberOfVolumes to set
	 */
	public void setMaxNumberOfVolumes(int maxNumberOfVolumes) {
		this.maxNumberOfVolumes = maxNumberOfVolumes;
		notifySourceCodeChanged();
	}


	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}

	private String[] vertexShaderCode() {
		String[] shaderCode ={
				"#version "+getShaderLanguageVersion(),
				"",
				"const int maxNumberOfData = "+maxNumberOfVolumes+";",
				"",
				"uniform mat4x4 "+suvViewMatrix+";",
				"uniform mat4x4 "+suvProjectionMatrix+";",
				"uniform mat4x4 "+suvModelMatrix+";",
				"uniform mat4x4 "+suvDrawCubeTransformation+";",
				"uniform mat4x4 "+suvTextureTransformationInverse+"[maxNumberOfData];",
				"",
				"in vec3 "+satPosition+";",
				"out vec3 "+svTextureCoordinate+"[maxNumberOfData];",
				"",
				"void main(){",
				"",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",
				"	vec4 positionInGlobalSpace = "+suvDrawCubeTransformation+" * position4d;",
				"",
				"	//calculate transformed texture coordinates",
				"	for(int i =0; i<maxNumberOfData; i++ ){",
				"		vec4 transformed = "+suvTextureTransformationInverse+"[i] * positionInGlobalSpace;",
				"		"+svTextureCoordinate+"[i] = transformed.xyz/transformed.w;",
				"	}",
				"",
				"	gl_Position ="+suvProjectionMatrix+" * "+suvViewMatrix+" * "+suvModelMatrix+" * positionInGlobalSpace;",
				"}",

		};
		appendNewLines(shaderCode);
		return shaderCode;
	}

	private String[] fragmentShaderCode(){
		String[] shaderCode = {
				"#version "+getShaderLanguageVersion(),
				"const int maxNumberOfData = "+maxNumberOfVolumes+";",
				"const int maxInt = "+Integer.MAX_VALUE+";",
				"const float val_threshold =1;",
				"",
				"uniform int "+suvActiveVolumes+"[maxNumberOfData];",
				"uniform float "+suvMaxVolumeValue+";",
				"uniform float "+suvMinVolumeValue+";",
				"uniform vec3 "+suvEyePosition+"[maxNumberOfData];",
				"uniform sampler3D "+suvVolumeTexture+"[maxNumberOfData];",
				"uniform sampler1D "+suvColorTexture+";",
				"uniform float "+suvMaxDiagonalLength+" ;",
				"",
				"in vec3 "+svTextureCoordinate+"[maxNumberOfData];",
				"out vec4 fragmentColor;",
				"",
				"int getStepsInVolume(float stepsSize, vec3 position, vec3 direction){",
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
				"",	
				"	}",
				"	return steps;",
				"}",
				"",
				"",
				"void main(void)",
				"{",	
				"	const int samples = 256;",
				"	float sample_step ="+suvMaxDiagonalLength+"/float(samples);",
				"	const float brightness = 150.0f;",
				"",	
				"	fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);",
				"	float volumeNormalizeFactor = 1.f/ ("+suvMaxVolumeValue+"-"+suvMinVolumeValue+"+0.01);",
				"",
				"	for(int n = 0; n < maxNumberOfData; n++){",
				"    	vec3 ray_dir = normalize("+svTextureCoordinate+"[n] - "+suvEyePosition+"[n] );",
				"    	vec3 ray_pos = "+svTextureCoordinate+"[n]; // the current ray position",
				"",
				" 	 	vec4 color;",
				"",
				"",   
				"    	int steps =  getStepsInVolume(sample_step,ray_pos,ray_dir);",
				"    	if(steps > samples){",
				"    		steps = samples;",
				"  		}",    
				"",
				"  		float density;",
				"  		for(int i = 0; i< steps; i++){",
				"",        				
				"",
				"",
				"        	// note:", 
				"        	// - ray_dir * sample_step can be precomputed",
				"        	// - we assume the volume has a cube-like shape",
				"",
				"        	// break out if ray reached the end of the cube.",
				"        	density = (texture("+suvVolumeTexture+"[n], ray_pos).r-"+suvMinVolumeValue+") *volumeNormalizeFactor;",
				"",
				"",
				"        	color.rgb = texture("+suvColorTexture+", density).rgb;",
				"        	color.a   = texture("+suvColorTexture+", density).a /*density*/ * sample_step * val_threshold * brightness;",
				"        	fragmentColor.rgb = fragmentColor.rgb * (1.0 - color.a) + color.rgb * color.a;",
				"			ray_pos += ray_dir * sample_step;",  	
				"    	}",
				"   }",
				"	fragmentColor = vec4 (fragmentColor.rgb,0.1);", 
		"}"};
		appendNewLines(shaderCode);
		return shaderCode;
	}

}
