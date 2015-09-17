package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.AbstractVolumeInterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.GetMaxStepsFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.GetStepsToVolumeFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.TransparentVolumeinterpreter;
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

	private final GetStepsToVolumeFunction stepsToVolume = new GetStepsToVolumeFunction();
	
	private IFunction transferFunctionCode;

	private AbstractVolumeAccumulator accumulator = new AverageVolumeAccumulator();
	
	private AbstractVolumeInterpreter interpreter; 

	private static final String svTextureCoordinate = "textureCoordinate";

	//Vertex shader uniforms
	public static final String suvDrawCubeTransformation ="inDrawCubeTransformation";

	public static final String suvTextureTransformationInverse ="inTextureTransformationInverse";

	//Fragment shader uniforms 
	public static final String suvIsoValue = "inIsoValue";
	
	public static final String suvActiveVolumes = "inActiveVolumes";

	public static final String suvVolumeTexture = "inVolumeTexture";

	public static final String suvColorTexture = "inColorTexture";

	public static final String suvEyePosition = "inEyePosition";

	public static final String suvMinVolumeValue = "inMinVolumeValue";

	public static final String suvMaxVolumeValue = "inMaxVolumeValue";

	public static final String suvMaxDiagonalLength = "inMaxDiagonalLength";

	public static final String scvMaxNumberOfVolumes = "maxNumberOfVolumes";
	
	public static final String sgvNormIsoValue = "normIsoValue";
	
	public static final String scvMinDelta = "minDelta";
	
	public static final String sgvRayPositions = "ray_poss";
	
	public static final String sgvRayDirections = "ray_dirs";
	
	public static final String sgvVolumeNormalizeFactor = "volumeNormalizeFactor";
	
	public static final String sgvSampleSize = "sample_step";
	
	public static final String suvBackgroundColor = "inBackgroundColorFragmentShader";
	
	public static final String suvLightPosition = "inlightPos";
	
	public static final String suvLightIntensiy = "iniIn";
	
	public static final String suvZeroDistSlice = "inZeroSlicePoint";
	
	public static final String suvNormalSlice = "inZeroSliceNormal";
	
	public static final String suvShowSlice = "inShowSlice";
	
	public static final String suvVoxelCount = "inVoxelCount";
	
	public static final String suvSamples = "inSamples";
	
	public MultiVolumeRendererShaderSource(){
		setVolumeInterpreter(  new TransparentVolumeinterpreter());
		setShaderLanguageVersion(400);
	}
	
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
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber(),
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
				"	gl_Position ="+suvProjectionMatrix+" * "+suvViewMatrix+"  * positionInGlobalSpace;",
				"}",

		};
		appendNewLines(shaderCode);
		return shaderCode;
	}

	private String[] fragmentShaderCode(){
		List<String> code = new ArrayList<String>();
		String[] head = {
				"#version "+getShaderLanguageVersion(),
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 0",
				"const int "+scvMaxNumberOfVolumes+" = "+maxNumberOfVolumes+";",
				"const int maxInt = "+Integer.MAX_VALUE+";",
				"const float gamma = 20.0;",
				"const float "+scvMinDelta+" = 0.00001;",
				"",
				"uniform ivec3 "+suvVoxelCount+"["+scvMaxNumberOfVolumes+"];",
				"uniform int "+suvActiveVolumes+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvMaxVolumeValue+";",
				"uniform float "+suvMinVolumeValue+";",
				"uniform vec3 "+suvEyePosition+"["+scvMaxNumberOfVolumes+"];",
				"uniform sampler3D "+suvVolumeTexture+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvMaxDiagonalLength+" ;",
				"uniform float "+suvIsoValue+";",
				"uniform vec3 "+suvBackgroundColor+";",
				"uniform vec3 "+suvLightPosition+"["+scvMaxNumberOfVolumes+"];",
				"uniform vec3 "+suvLightIntensiy+" = vec3(0.0,1.0,0.0);",
				"uniform vec3 "+suvNormalSlice+";",
				"uniform float "+suvZeroDistSlice+";",
				"uniform int "+suvShowSlice+";",
				"uniform int "+suvSamples+";",
		
				"float "+sgvNormIsoValue+";",
				"vec3 "+sgvRayDirections+"["+scvMaxNumberOfVolumes+"];",	
				"vec3 "+sgvRayPositions+"["+scvMaxNumberOfVolumes+"];",
				"float "+sgvVolumeNormalizeFactor+";",
				"float "+sgvSampleSize+";",
				"",
				"in vec3 "+svTextureCoordinate+"["+scvMaxNumberOfVolumes+"];",
				"out vec4 fragmentColor;",
				"",


		};

		String[] body ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() + " 1",
				"float["+scvMaxNumberOfVolumes+"] getVolumeValues(vec3 positions["+scvMaxNumberOfVolumes+"] ){",
				"	float volumeValues["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+"; i++){",
				"		if("+suvActiveVolumes+"[i]==1){",
				"			float value = texture("+suvVolumeTexture+"[i], positions[i]).r;",	
				"			volumeValues[i] = value;",
				"		}else{",
				"			volumeValues[i]=-1.0;",	
				"		}",
				"	}",
				"	return volumeValues;",
				"}",
				"",
				"float getSliceDistance(){",
				"	float distance = "+suvZeroDistSlice+";",
				"	distance+=dot("+suvNormalSlice+","+sgvRayPositions+"[0]);",
				"	return distance;",
				"}",
				"",
				"void main(void)",
				"{",	
				"	float "+sgvSampleSize+" ="+suvMaxDiagonalLength+"/float("+suvSamples+");",
				"",	
				"	fragmentColor = vec4("+suvBackgroundColor+".xyz,0.0);",
				"	"+sgvVolumeNormalizeFactor+" = 1.0/ ("+suvMaxVolumeValue+");",
				"	"+sgvNormIsoValue+"="+suvIsoValue+"*"+sgvVolumeNormalizeFactor+";",
				"	//get rays of volumes",
				"	int steps = 0;",
				"	int startStep = "+Short.MAX_VALUE+";",	
				
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"    	vec3 ray_dir = normalize("+svTextureCoordinate+"[n] - "+suvEyePosition+"[n] );",
				"    	vec3 ray_pos = "+svTextureCoordinate+"[n]; // the current ray position",
				"		"+sgvRayDirections+"[n] = ray_dir;",
				"		"+sgvRayPositions+"[n] = ray_pos;",
				"",   
				"    	int csteps = "+stepsFunction.call(new String[]{""+sgvSampleSize+"","ray_pos","ray_dir"})+";",
				"    	if(steps < csteps){",
				"    		steps = csteps;",
				"  		}",    
				"		csteps = "+stepsToVolume.call(new String[]{""+sgvSampleSize+"","ray_pos","ray_dir"})+";",
				"    	if(startStep > csteps){",
				"    		startStep = csteps;",
				"  		}",  
				"	}",
				"   if(steps > "+suvSamples+"){",
				"   	steps = "+suvSamples+";",
				"  	}",    
				"	if(startStep >= steps){",
				"		startStep = steps-1;",	
				"	}",
				"",
				"	//multi ray casting",
				"  	float density = 0.0;",
				"	float latestdDistanceToSlice = getSliceDistance();",
				//TODO find gpu killing bug of start steps
				"  	for(int i = 0; i< steps; i++){",
				"",
				"      	// note:", 
				"      	// - ray_dir * "+sgvSampleSize+" can be precomputed",
				"      	// - we assume the volume has a cube-like shape",
				"",
				"      	// break out if ray reached the end of the cube.",
				"		float nextDensity = 0.0;",
				"		float densities["+scvMaxNumberOfVolumes+"] = getVolumeValues("+sgvRayPositions+");",
				"		nextDensity = "+accumulator.call(new String[]{"densities"})+";",		
				"      	nextDensity *= "+sgvVolumeNormalizeFactor+";",
				"",
				"      	vec4 color = "+transferFunctionCode.call(new String[]{"density","nextDensity",sgvSampleSize})+";",
				"      	vec4 c_out =  "+interpreter.call(new String[]{"fragmentColor","color","density","nextDensity"})+";",
				"",
				"		for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",	
				"			"+sgvRayPositions+"[n] += "+sgvRayDirections+"[n] * "+sgvSampleSize+";",
				"		}",
				"",
				"		float currentSliceDistance = getSliceDistance();",
				"		if("+suvShowSlice+" == 1&&sign(currentSliceDistance)!=sign(latestdDistanceToSlice)||sign(currentSliceDistance)==0 ){",
				"			c_out = gamma*vec4(nextDensity,nextDensity,nextDensity,0.5);",
				"		}",
				"		latestdDistanceToSlice = currentSliceDistance;",
				"",		
				"		fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*c_out;",
				"		if(c_out.a +"+scvMinDelta+" >= 1.0){",
				"			break;",
				"		}",	
				"		density = nextDensity;",
				"   }",
				//"	fragmentColor.rgb *= gamma;",
				"	fragmentColor = max(vec4(0.0),min(fragmentColor, vec4(1.0)));",
			//	"	fragmentColor = vec4("+suvBackgroundColor+".rgb,0.0);",
				"}"
		};
		addCodeArrayToList(head, code);
		addCodeArrayToList(stepsToVolume.declaration(), code);
		addCodeArrayToList(stepsFunction.declaration(), code);
		addCodeArrayToList(accumulator.declaration(), code);
		addCodeArrayToList(transferFunctionCode.declaration(), code);
		addCodeArrayToList(interpreter.declaration(), code);
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
		this.interpreter.setAccumulator(accumulator);
		notifySourceCodeChanged();
	}


	public void setVolumeInterpreter(AbstractVolumeInterpreter volumeInterpreter) {
		this.interpreter = volumeInterpreter;
		this.interpreter.setAccumulator(accumulator);
		notifySourceCodeChanged();
		
	}

}
