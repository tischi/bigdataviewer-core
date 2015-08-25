package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions.GetMaxStepsFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions.IFunction;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

/**
 * Class to handles the multi-volume shader codes
 * @author michael
 *
 */
public class MultiVolumeRendererShaderSource extends AbstractShaderSource{

	private int maxNumberOfVolumes = 2;
	
	private final GetMaxStepsFunction stepsFunction = new GetMaxStepsFunction(); 
	
	private IFunction transferFunctionCode;
	
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
		if(this.maxNumberOfVolumes == maxNumberOfVolumes){
			return;
		}
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

	/**
	 * @param transferFunctionCode the transferFunctionCode to set
	 */
	public void setTransferFunctionCode(IFunction transferFunctionCode) {
		if(transferFunctionCode.equals(this.transferFunctionCode)){
			return;
		}
		this.transferFunctionCode = transferFunctionCode;
		notifySourceCodeChanged();
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
		List<String> code = new ArrayList<String>();
		String[] head = {
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
				"uniform float "+suvMaxDiagonalLength+" ;",
				"",
				"in vec3 "+svTextureCoordinate+"[maxNumberOfData];",
				"out vec4 fragmentColor;",
				"",
				
		
				};

		String[] body ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
				"float[maxNumberOfData] getVolumeValues(vec3 positions[maxNumberOfData] ){",
				"	float volumeValues[maxNumberOfData];",
				"	for(int i = 0; i < maxNumberOfData; i++){",
				"		float value = texture("+suvVolumeTexture+"[i], positions[i]).r;",	
				"		volumeValues[i] = value;",
				"	}",
				"	return volumeValues;",
				"}",
				"",
				"void main(void)",
				"{",	
				"	const int samples = 256;",
				"	float sample_step ="+suvMaxDiagonalLength+"/float(samples);",
				"	const float brightness = 250.0f;",
				"",	
				"	fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);",
				"	float volumeNormalizeFactor = 1.f/ ("+suvMaxVolumeValue+"-"+suvMinVolumeValue+"+0.01);",
				"",
				"	//get rays of volumes",
				"	int steps = "+9999+";",
				"	vec3 ray_dirs[maxNumberOfData];",	
				"	vec3 ray_poss[maxNumberOfData];",
				"	for(int n = 0; n < maxNumberOfData; n++){",
				"    	vec3 ray_dir = normalize("+svTextureCoordinate+"[n] - "+suvEyePosition+"[n] );",
				"    	vec3 ray_pos = "+svTextureCoordinate+"[n]; // the current ray position",
				"		ray_dirs[n] = ray_dir;",
				"		ray_poss[n] = ray_pos;",
				"",   
				"    	int csteps = "+stepsFunction.call(new String[]{"sample_step","ray_pos","ray_dir"})+";",
				"    	if(steps > csteps){",
				"    		steps = csteps;",
				"  		}",    
				"	}",
				"   if(steps > samples){",
				"   	steps = samples;",
				"  	}",    
				"",
				"	vec3 zeros = vec3(0,0,0);",
				"	vec3 ones = vec3(1,1,1);",
				"	//multi ray casting",
				" 	vec4 color;",
				"  	float density = 0;//(texture("+suvVolumeTexture+"[n], ray_pos).r-"+suvMinVolumeValue+") *volumeNormalizeFactor;",
				"  	for(int i = 0; i< steps; i++){",
				"",
				"      	// note:", 
				"      	// - ray_dir * sample_step can be precomputed",
				"      	// - we assume the volume has a cube-like shape",
				"",
				"      	// break out if ray reached the end of the cube.",
				"		float nextDensity = 0;",
				"		float densities[maxNumberOfData] = getVolumeValues(ray_poss);",
				"		int intersectingVolumes = 0;",	
				"		for(int n = 0; n < maxNumberOfData; n++){",
			/*	"			if(any(lessThan(ray_poss[n], zeros))){",
				"				continue;",
				"			}",
				"			if(any(greaterThan(ray_poss[n], ones))){",
				"				continue;",
				"			}",			*/
				"			nextDensity += densities[n]-"+suvMinVolumeValue+";",
				"			intersectingVolumes++;",
				"		}",
				"		if(intersectingVolumes==0){",
				"			intersectingVolumes=1;",
				"		}",
				"      	nextDensity = nextDensity/intersectingVolumes *volumeNormalizeFactor;",
				"",
				"      	color = "+transferFunctionCode.call(new String[]{"density","nextDensity","sample_step"})+";",
				"      	color.a *= val_threshold * brightness;",
				"      	fragmentColor.rgb = fragmentColor.rgb * (1.0 - color.a) + color.rgb * color.a;",
				"		density = nextDensity;",
				"		for(int n = 0; n < maxNumberOfData; n++){",	
				"			ray_poss[n] += ray_dirs[n] * sample_step;",
				"		}",	
				"   }",
				"	fragmentColor = vec4 (fragmentColor.rgb,0.1);", 
		"}"};
		addCodeArrayToList(head, code);
		addCodeArrayToList(stepsFunction.declaration(), code);
		addCodeArrayToList(transferFunctionCode.declaration(), code);
		addCodeArrayToList(body, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
