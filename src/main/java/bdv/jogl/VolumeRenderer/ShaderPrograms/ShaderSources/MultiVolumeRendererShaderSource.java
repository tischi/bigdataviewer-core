package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvTexTOffsets;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvTexTScales;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvRenderRectClippingPlanes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvVolumeTexture;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.AbstractVolumeInterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.GetMaxStepsFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.GetStepsToVolumeFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.TransparentVolumeinterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.VolumeGradientEvaluationFunction;
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

	private VolumeGradientEvaluationFunction gradient = new VolumeGradientEvaluationFunction();
	
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
	
	public static final String suvNormalSlice = "inZeroSliceNormal";
	
	public static final String suvShowSlice = "inShowSlice";
	
	public static final String suvVoxelCount = "inVoxelCount";
	
	public static final String suvSamples = "inSamples";
	
	public static final String sgvTexTOffsets = "vtextOffsets";
	
	public static final String sgvTexTScales = "vtextScales";
	
	public static final String suvUseGradient = "inUseGradient";
	
	public static final String suvMinCubeSpace = "inMinCubeSpace";
	
	public static final String suvMaxCubeSpace = "inMaxCubeSpace";
	
	public static final String suvRenderRectClippingPlanes = "inRectClippingPlanes";
	 
	public static final String suvRenderRectStepSize = "inRectStepSize";
	
	public MultiVolumeRendererShaderSource(){
		setVolumeInterpreter(  new TransparentVolumeinterpreter());
		setShaderLanguageVersion(330);
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
				"		vec4 transformed= "+suvTextureTransformationInverse+"[i] * positionInGlobalSpace;",
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
				"uniform float "+suvMaxDiagonalLength+"["+scvMaxNumberOfVolumes+"] ;",
				"uniform float "+suvIsoValue+";",
				"uniform vec3 "+suvBackgroundColor+";",
				"uniform vec3 "+suvLightPosition+"["+scvMaxNumberOfVolumes+"];",
				"uniform vec3 "+suvLightIntensiy+" = vec3(0.0,1.0,0.0);",
				"uniform vec4 "+suvNormalSlice+";",
				"uniform int "+suvShowSlice+";",
				"uniform int "+suvSamples+";",
				"uniform int "+suvUseGradient+"=1;",
				"uniform vec3 "+suvMinCubeSpace+"["+scvMaxNumberOfVolumes+"];",
				"uniform vec3 "+suvMaxCubeSpace+"["+scvMaxNumberOfVolumes+"];",
				"uniform vec4 "+suvRenderRectClippingPlanes+"[6];",
				"uniform float "+suvRenderRectStepSize+";",
		//TODO remove debug
				"uniform mat4x4 "+suvTextureTransformationInverse+"["+scvMaxNumberOfVolumes+"];",
				//TODO remove debug
				
				"float "+sgvNormIsoValue+";",
				"vec3 "+sgvRayDirections+"["+scvMaxNumberOfVolumes+"];",	
				"vec3 "+sgvRayPositions+"["+scvMaxNumberOfVolumes+"];",
				"float "+sgvVolumeNormalizeFactor+";",
				"float "+sgvSampleSize+"["+scvMaxNumberOfVolumes+"];",
				"",
				"in vec3 "+svTextureCoordinate+"["+scvMaxNumberOfVolumes+"];",
				"out vec4 fragmentColor;",
				"vec3 vtextOffsets["+scvMaxNumberOfVolumes+"]; ",
				"vec3 vtextScales["+scvMaxNumberOfVolumes+"]; ",
				"",
				"vec3 correctTexturePositions(vec3 positionOnRay, vec3 offset, vec3 scale){",
				"	return positionOnRay * scale + offset;",
				"}",
				"",
				"vec3 getCorrectedTexturePositions(vec3 positionOnRay, int volumeNumber){",
				"	return correctTexturePositions(positionOnRay,vtextOffsets[volumeNumber],vtextScales[volumeNumber]);",
				"}",
				"",
			
				"",
				"float getPlaneDistance(vec4 plane, vec3 position){",
				"	float distance = plane.a;",
				"	distance+=dot(plane.xyz,position);",
				"	return distance;",
				"}",
				"int getStepsTillClipp(){",
				"	int steps = "+Integer.MAX_VALUE+";",
				"	//render cube clipping plane",
				"	vec3 pointAfterTinyStep = "+sgvRayPositions+"[0]+ "+sgvRayDirections+"[0] * "+sgvSampleSize+"[0];",
				"	for(int p = 0; p < 6; p++){",
				"		//move away from plane",
				"		vec4 plane = "+suvRenderRectClippingPlanes+"[p];",
				"		float distPosition0 = abs(getPlaneDistance(plane,"+sgvRayPositions+"[0]));",
				"		float distPosition1 = abs(getPlaneDistance(plane,pointAfterTinyStep));",
				"		if(distPosition0 <= distPosition1){",
				"			continue;",
				"		}",
				"		int stepsInRect = int(ceil(distPosition0/ "+sgvSampleSize+"[0]));",
				"		steps = min(steps, stepsInRect)+1;",
				"	}",
				"	return steps;",
				"}",
				"",

		};

		String[] body ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() + " 1",
				"float["+scvMaxNumberOfVolumes+"] getVolumeValues(vec3 positions["+scvMaxNumberOfVolumes+"] ){",
				"	float volumeValues["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+"; i++){",
				"		vec3 normtexturePos = getCorrectedTexturePositions(positions[i],i);",
				"",
				"		//check for activity and containment of the volume",
				"		if("+suvActiveVolumes+"[i]==1/*",
				"			&& all(greaterThanEqual(normtexturePos,vtextOffsets[i]))",
				"			&& all(lessThanEqual(normtexturePos, vtextOffsets[i] +vtextScales[i]))*/){",
				"			float value;",
				"			if("+suvUseGradient+"==0){",	
				"				value = texture("+suvVolumeTexture+"[i], normtexturePos ).r;",	
				"			}else{",
				"				value = 7.0*length("+gradient.call(new String[]{"positions[i].xyz", suvVolumeTexture+"[i]",sgvTexTOffsets+"[i]", sgvTexTScales+"[i]"})+".xyz);",
				"			}",
				"			volumeValues[i] = value;",
				"		}else{",
				"			volumeValues[i]=-1.0;",	
				"		}",
				"	}",
				"	return volumeValues;",
				"}",
				"",
				"float getNormalizedAndAggregatedVolumeValue(vec3 positionsOnRay["+scvMaxNumberOfVolumes+"]){",
				"	float densities["+scvMaxNumberOfVolumes+"] = getVolumeValues(positionsOnRay);",
				"	return "+accumulator.call(new String[]{"densities"})+" * "+sgvVolumeNormalizeFactor+";",		
				"}",
				"",
				"void main(void)",
				"{",	
				"",	
				"	fragmentColor = vec4("+suvBackgroundColor+".xyz,0.0);",
				"	"+sgvVolumeNormalizeFactor+" = 1.0/ ("+suvMaxVolumeValue+");",
				"	"+sgvNormIsoValue+"="+suvIsoValue+"*"+sgvVolumeNormalizeFactor+";",
				"	//get rays of volumes",
				"	int steps = 0;",
				"	int startStep = "+Short.MAX_VALUE+";",	
				"",
				"	//init positions, directions ,etc",
				//TODO REMOVE DEBUG
				"mat4x4 toGlobal["+scvMaxNumberOfVolumes+"];",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				//TODO REMOVE DEBUG
				"toGlobal[n] = inverse("+suvTextureTransformationInverse+"[n]);",
				"    	vec3 ray_dir =("+svTextureCoordinate+"[n] - "+suvEyePosition+"[n] );",
				"		ray_dir = normalize(ray_dir);",
				"    	vec3 ray_pos = "+svTextureCoordinate+"[n]; // the current ray position",
				"		"+sgvRayDirections+"[n] = ray_dir;",
				"		"+sgvRayPositions+"[n] = ray_pos;",
				"		vec3 tmp = "+suvVoxelCount+"[n];",
				"	    vtextOffsets[n] = vec3(1.0/tmp);",
				"	    vtextScales[n] = (vec3("+suvVoxelCount+"[n]-ivec3(1)))/(vec3("+suvVoxelCount+"[n]));",
				"	 	"+sgvSampleSize+"[n] ="+suvMaxDiagonalLength+"[n]/float("+suvSamples+");",
			
				"",   
				"    	steps = max(steps,"+stepsFunction.call(new String[]{""+sgvSampleSize+"[n]","ray_pos","ray_dir"})+");",
				"		startStep = min(startStep,"+stepsToVolume.call(new String[]{""+sgvSampleSize+"[n]","ray_pos","ray_dir"})+");",
				"	}",
				//"	steps = min(steps,getStepsTillClipp());",
				"   steps =min(steps, "+suvSamples+");",
				"	startStep = max(startStep,steps-1);",	
				"	//end init",
				"",
				"	//ray casting",
				"  	float density =0; ",
				"	int renderedSlice =0;",
				"	float latestdDistanceToSlice = getPlaneDistance("+suvNormalSlice+","+sgvRayPositions+"[0] );",
				//TODO find gpu killing bug of start steps
				"  	for(int i = 0; i < /*steps*/"+suvSamples+"; i++){",
				"",
				"      	// note:", 
				"      	// - ray_dir * "+sgvSampleSize+" can be precomputed",
				"      	// - we assume the volume has a cube-like shape",
				"",
				"      	// break out if ray reached the end of the cube.",
				"		float nextDensity = getNormalizedAndAggregatedVolumeValue("+sgvRayPositions+");",
				"",
				"      	vec4 color = "+transferFunctionCode.call(new String[]{"density","nextDensity",suvRenderRectStepSize})+";",
				"      	vec4 c_out =  "+interpreter.call(new String[]{"fragmentColor","color","density","nextDensity"})+";",
				"",		
				"		fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*c_out;",
				"		if(c_out.a +"+scvMinDelta+" >= 1.0){",
				"			break;",
				"		}",	
				"		for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"			"+sgvRayPositions+"[n] += "+sgvRayDirections+"[n] * "+sgvSampleSize+"[n];",
				"		}",
				"		//render slice",
				"		vec4 sliceColor= vec4(0.0);  ",
				"		if("+suvShowSlice+" == 1 && renderedSlice != 1){",
				"			float currentSliceDistance = getPlaneDistance("+suvNormalSlice+","+sgvRayPositions+"[0] );",
				"			if(sign(currentSliceDistance)!=sign(latestdDistanceToSlice)||sign(currentSliceDistance)==0 ){",
				"				int residents = 0;",				
				"				float densities["+scvMaxNumberOfVolumes+"] = getVolumeValues("+sgvRayPositions+");",
				"				densities[0] *= "+sgvVolumeNormalizeFactor+";",
				"				sliceColor =vec4(densities[0],densities[0],densities[0],1.0);",
				"				sliceColor.a  = 1.0;",
				"				for(int n = 0; n < "+scvMaxNumberOfVolumes+";n++){",
				"					if(densities[n]>0){",
				"						residents++;",		
				"					}",
				"				}",
				"				sliceColor.rgb *= gamma;",
				"				",
				"				//for(int n = 0; n < "+scvMaxNumberOfVolumes+";n++){",
				"					//"+sgvRayPositions+"[n] += "+sgvRayDirections+"[n]*float(abs(latestdDistanceToSlice))*"+sgvSampleSize+"[n]/"+sgvSampleSize+"[0];",
				"				//}",
				"					fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*sliceColor;",
				"				renderedSlice = 1;",
				"			}",
				"			latestdDistanceToSlice = currentSliceDistance;",
				"		}",
				"",
				"",
				"",
				"		density = nextDensity;",
				"   }",
				//TODO DEBUG
				//"vec4 globalVec0 =/*transpose(inverse(*/toGlobal[0]/*))*/*vec4("+sgvRayDirections+"[0].xyz,0.0);",
				//"vec4 globalVec1 =/*transpose(inverse(*/toGlobal[1]/*))*/*vec4("+sgvRayDirections+"[1].xyz,0.0);",
				
				//"if(dot(globalVec1.xyz,globalVec0.xyz) < 1.0){",
				//"	fragmentColor = vec4(1.0,0.0,0.0,1.0);",
				//"}",
			//	"vec4 globalEye0 =toGlobal[0]*vec4("+suvEyePosition+"[0].xyz,1.0); ",
			//	"vec4 globalEye1 =toGlobal[1]*vec4("+suvEyePosition+"[1].xyz,1.0); ",
			
				//"vec4 globalEye0 =toGlobal[0]*vec4("+svTextureCoordinate+"[0].xyz+0.0*"+sgvRayDirections+"[0] * "+suvRenderRectStepSize+",1.0); ",
				//"vec4 globalEye1 =toGlobal[1]*vec4("+svTextureCoordinate+"[1].xyz+0.0*"+sgvRayDirections+"[1] * "+suvRenderRectStepSize+",1.0); ",
			
			//	"globalEye0.xyz/=globalEye0.w;",
		//		"globalEye1.xyz/=globalEye1.w;",
			//	"if(length(globalEye1.xyz-globalEye0.xyz) > 0.1 ){",
			//	"	fragmentColor = vec4(1.0,0.0,0.0,1.0);",
			//	"}",
				/*"}else{",
				"	fragmentColor = vec4(0.0,1.0,0.0,1.0);",
				"}",*/
				//"fragmentColor = vec4();",
				//"	fragmentColor.rgb *= gamma;",
				//"	fragmentColor = max(vec4(0.0),min(fragmentColor, vec4(1.0)));",
			//	"	fragmentColor = vec4("+suvBackgroundColor+".rgb,0.0);",
				"}"
		};
		addCodeArrayToList(head, code);
		addCodeArrayToList(gradient.declaration(), code);
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
