package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.GetMaxStepsFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AverageVolumeAccumulator;

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
	
	private AbstractVolumeAccumulator accumulator = new AverageVolumeAccumulator();
	
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

	public static final String scvMaxNumberOfVolumes = "maxNumberOfVolumes";
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
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
				"#version "+getShaderLanguageVersion(),
				"",
				"const int "+scvMaxNumberOfVolumes+" = "+maxNumberOfVolumes+";",
				"",
				"uniform mat4x4 "+suvViewMatrix+";",
				"uniform mat4x4 "+suvProjectionMatrix+";",
				"uniform mat4x4 "+suvModelMatrix+";",
				"uniform mat4x4 "+suvDrawCubeTransformation+";",
				"uniform mat4x4 "+suvTextureTransformationInverse+"["+scvMaxNumberOfVolumes+"];",
				"",
				"in vec3 "+satPosition+";",
				"out vec3 "+svTextureCoordinate+"["+scvMaxNumberOfVolumes+"];",
				"",
				"void main(){",
				"",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",
				"	vec4 positionInGlobalSpace = "+suvDrawCubeTransformation+" * position4d;",
				"",
				"	//calculate transformed texture coordinates",
				"	for(int i =0; i<"+scvMaxNumberOfVolumes+"; i++ ){",
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
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
				"#version "+getShaderLanguageVersion(),
				"const int "+scvMaxNumberOfVolumes+" = "+maxNumberOfVolumes+";",
				"const int maxInt = "+Integer.MAX_VALUE+";",
				"const float val_threshold =1;",
				"",
				"uniform int "+suvActiveVolumes+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvMaxVolumeValue+";",
				"uniform float "+suvMinVolumeValue+";",
				"uniform vec3 "+suvEyePosition+"["+scvMaxNumberOfVolumes+"];",
				"uniform sampler3D "+suvVolumeTexture+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvMaxDiagonalLength+" ;",
				"",
				"in vec3 "+svTextureCoordinate+"["+scvMaxNumberOfVolumes+"];",
				"out vec4 fragmentColor;",
				"",
				
		
				};

		String[] body ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
				"float["+scvMaxNumberOfVolumes+"] getVolumeValues(vec3 positions["+scvMaxNumberOfVolumes+"] ){",
				"	float volumeValues["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+"; i++){",
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
				"	vec3 ray_dirs["+scvMaxNumberOfVolumes+"];",	
				"	vec3 ray_poss["+scvMaxNumberOfVolumes+"];",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
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
				"		float densities["+scvMaxNumberOfVolumes+"] = getVolumeValues(ray_poss);",
				"		nextDensity = "+accumulator.call(new String[]{"densities"})+" - "+suvMinVolumeValue+";",		
				"      	nextDensity *= volumeNormalizeFactor;",
				"",
				"      	color = "+transferFunctionCode.call(new String[]{"density","nextDensity","sample_step"})+";",
				"      	fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*color;",
				"		density = nextDensity;",
				"		for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",	
				"			ray_poss[n] += ray_dirs[n] * sample_step;",
				"		}",	
				"   }",
				"	fragmentColor = vec4 (fragmentColor.rgb,0.1);", 
		"}"};
		addCodeArrayToList(head, code);
		addCodeArrayToList(stepsFunction.declaration(), code);
		addCodeArrayToList(accumulator.declaration(), code);
		addCodeArrayToList(transferFunctionCode.declaration(), code);
		addCodeArrayToList(body, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}


	public void setAccumulator(AbstractVolumeAccumulator a1) {
		if(a1.equals(this.accumulator)){
			return;
		}
		this.accumulator = a1;
		notifySourceCodeChanged();
	}

}
