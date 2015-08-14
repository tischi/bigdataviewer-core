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

	//Vertex shader uniforms
	public static final String shaderUniformVariableDrawCubeTransformation ="inDrawCubeTransformation";

	public static final String shaderUniformVariableLocalTransformation ="inTextureTransformationInverse";

	//Fragment shader uniforms 
	public static final String shaderUniformVariableActiveVolumes = "inActiveVolume";

	public static final String shaderUniformVariableVolumeTexture = "inVolumeTexture";

	public static final String shaderUniformVariableColorTexture = "inColorTexture";

	public static final String shaderUniformVariableEyePosition = "inEyePosition";

	public static final String shaderUniformVariableMinVolumeValue = "inMinVolumeValue";

	public static final String shaderUniformVariableMaxVolumeValue = "inMaxVolumeValue";

	public static final String shaderUniformVariableMaxDiagonalLength = "inMaxDiagonalLength";

	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}


	private String[] vertexShaderCode() {
		String[] shaderCode ={
				"#version 130",
				"",
				"const int maxNumberOfData = 2;",
				"",
				"uniform mat4x4 inView;",
				"uniform mat4x4 inProjection;",
				"uniform mat4x4 inModel;",
				"uniform mat4x4 inDrawCubeTransformation;",
				"uniform mat4x4 inTextureTransformationInverse[maxNumberOfData];",
				"",
				"in vec3 inPosition;",
				"out vec3 textureCoord[maxNumberOfData];",
				"",
				"void main(){",
				"",
				"	vec4 position4d = vec4(inPosition.xyz,1.f);",
				"",
				"	vec4 positionInGlobalSpace = inDrawCubeTransformation * position4d;",
				"",
				"	//calculate transformed texture coordinates",
				"	for(int i =0; i<maxNumberOfData; i++ ){",
				"		vec4 transformed = inTextureTransformationInverse[i] * positionInGlobalSpace;",
				"		textureCoord[i] = transformed.xyz/transformed.w;",
				"	}",
				"",
				"	gl_Position =inProjection * inView * inModel * positionInGlobalSpace;",
				"}",
			
		};
		appendNewLines(shaderCode);
		return shaderCode;
	}

	private String[] fragmentShaderCode(){
		String[] shaderCode = {
				"#version 130",
				"const int maxNumberOfData = 2;",
				"const int maxInt = 9999;",
				"const float val_threshold =1;",
				"",
				"uniform int inActiveVolume[maxNumberOfData];",
				"uniform float inMaxVolumeValue;",
				"uniform float inMinVolumeValue;",
				"uniform vec3 inEyePosition[maxNumberOfData];",
				"uniform sampler3D inVolumeTexture[maxNumberOfData];",
				"uniform sampler1D inColorTexture;",
				"uniform float inMaxDiagonalLength ;",
				"",
				"in vec3 textureCoord[maxNumberOfData];",
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
				"	float sample_step =inMaxDiagonalLength/float(samples);",
				"	const float brightness = 150.0f;",
				"",	
				"	fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);",
				"	float volumeNormalizeFactor = 1.f/ (inMaxVolumeValue-inMinVolumeValue+0.01);",
				"",
				"	for(int n = 0; n < maxNumberOfData; n++){",
				"    	vec3 ray_dir = normalize(textureCoord[n] - inEyePosition[n] );",
				"    	vec3 ray_pos = textureCoord[n]; // the current ray position",
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
				"        	density = (texture(inVolumeTexture[n], ray_pos).r-inMinVolumeValue) *volumeNormalizeFactor;",
				"",
				"",
				"        	color.rgb = texture(inColorTexture, density).rgb;",
				"        	color.a   = texture(inColorTexture, density).a /*density*/ * sample_step * val_threshold * brightness;",
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
