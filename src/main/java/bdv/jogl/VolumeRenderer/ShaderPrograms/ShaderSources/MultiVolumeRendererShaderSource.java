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
	
	private static final String svRayStartCoordinate = "textureCoordinate";

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

	public static final String scvMaxNumberOfVolumes = "maxNumberOfVolumes";
	
	public static final String sgvNormIsoValue = "normIsoValue";
	
	public static final String scvMinDelta = "minDelta";
	
	public static final String sgvRayPositions = "ray_poss";
	
	public static final String sgvRayDirections = "ray_dirs";
	
	public static final String sgvVolumeNormalizeFactor = "volumeNormalizeFactor";
	
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
	
	public static final String suvRenderRectClippingPlanes = "inRectClippingPlanes";
	 
	public static final String suvRenderRectStepSize = "inRectStepSize";
	
	public static final String suvTransferFuntionSize = "inTransferFunctionSize";
	
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
				"out vec3 "+svRayStartCoordinate+";",
				"",
				"void main(){",
				"",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",
				"	vec4 positionInGlobalSpace = "+suvDrawCubeTransformation+" * position4d;",
				"",
				"	gl_Position ="+suvProjectionMatrix+" * "+suvViewMatrix+"  * positionInGlobalSpace;",
				"	"+svRayStartCoordinate+"= positionInGlobalSpace.xyz /positionInGlobalSpace.w;",
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
				"uniform vec3 "+suvEyePosition+";",
				"uniform sampler3D "+suvVolumeTexture+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvIsoValue+";",
				"uniform vec3 "+suvBackgroundColor+";",
				"uniform vec3 "+suvLightPosition+"["+scvMaxNumberOfVolumes+"];",
				"uniform vec3 "+suvLightIntensiy+" = vec3(0.0,1.0,0.0);",
				"uniform vec4 "+suvNormalSlice+";",
				"uniform int "+suvShowSlice+";",
				"uniform int "+suvSamples+";",
				"uniform int "+suvUseGradient+"=1;",
				"uniform vec4 "+suvRenderRectClippingPlanes+"[6];",
				"uniform float "+suvRenderRectStepSize+";",
				"uniform mat4x4 "+suvTextureTransformationInverse+"["+scvMaxNumberOfVolumes+"];",
				"float "+sgvNormIsoValue+";",
				"vec3 "+sgvRayDirections+";",	
				"vec3 "+sgvRayPositions+";",
				"float "+sgvVolumeNormalizeFactor+";",
				"float "+suvTransferFuntionSize+";",
				"",
				"in vec3 "+svRayStartCoordinate+";",
				"out vec4 fragmentColor;",
				"vec3 vtextOffsets["+scvMaxNumberOfVolumes+"]; ",
				"vec3 vtextScales["+scvMaxNumberOfVolumes+"]; ",
				"",
				"vec3 correctTexturePositions(vec3 positionOnRay, vec3 offset, vec3 scale){",
				"	return positionOnRay * scale + offset;",
				"}",
				"",
				"vec3 getCoordinateInVolumeSpace(vec3 positionOnRay, int volumeNumber){",
				"	vec4 transformedPosition ="+suvTextureTransformationInverse+"[volumeNumber]*vec4(positionOnRay.xyz,1.0); ",
				"	transformedPosition /= transformedPosition.w;",	
				"	return transformedPosition.xyz;",
				"}",
				"",
				"vec3 getCorrectedTexturePositions(vec3 positionOnRay, int volumeNumber){",
				"	vec3 transformedPosition =getCoordinateInVolumeSpace(positionOnRay,volumeNumber); ",
				"	return correctTexturePositions(transformedPosition,vtextOffsets[volumeNumber],vtextScales[volumeNumber]);",
				"}",
				"",
				"",
				"float getPlaneDistance(vec4 plane, vec3 position){",
				"	float distance = -plane.a;",
				"	distance += dot(plane.xyz,position);",
				"	return distance;",
				"}",
				"",
				"int getStepsTillClipp(){",
				"	int steps = "+suvSamples+";",
				"	//render cube clipping plane",
				"	for(int p = 0; p < 3; p++){",
				"		float dist = 0;",
				"		for(int i = 0; i < 2;i++){",
				"		vec4 plane = "+suvRenderRectClippingPlanes+"[p*2 + i];",
				"			if(sign("+sgvRayDirections+"[p]) == sign(plane[p])){",
				"				dist = max(dist, abs(getPlaneDistance(plane, "+sgvRayPositions+")));",
				"			}",
				"		}",
				"		if(!(dist > 0.0)){",
				"			continue;",
				"		}",
				"",
				"		int stepsInRect = int(floor(abs(dist)/ (abs("+sgvRayDirections+"[p]) * "+suvRenderRectStepSize+")));",
				"		steps = min(steps, stepsInRect);",
				"	}",
				"	return steps;",
				"}",
				"",

		};

		String[] dependingFunctions ={
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() + " 1",
				"float["+scvMaxNumberOfVolumes+"] getVolumeValues(vec3 positions ){",
				"	float volumeValues["+scvMaxNumberOfVolumes+"];",
				"	for(int i = 0; i < "+scvMaxNumberOfVolumes+"; i++){",
				"		vec3 normtexturePos = getCorrectedTexturePositions(positions,i);",
				"",
				"		//check for activity and containment of the volume",
				"		if("+suvActiveVolumes+"[i]==1",
				"			&& all(greaterThanEqual(normtexturePos,vtextOffsets[i]))",
				"			&& all(lessThanEqual(normtexturePos, vec3(1.0)-vtextOffsets[i] ))){",
				"			float value = texture("+suvVolumeTexture+"[i], normtexturePos ).r;",	
				"			volumeValues[i] = value;",
				"		}else{",
				"			volumeValues[i]=-1.0;",	
				"		}",
				"	}",
				"	return volumeValues;",
				"}",
				"",
				"float getNormalizedAndAggregatedVolumeValue(vec3 positionOnRay){",
				"	float densities["+scvMaxNumberOfVolumes+"] = getVolumeValues(positionOnRay);",
				"	return "+accumulator.call(new String[]{"densities"})+" * "+sgvVolumeNormalizeFactor+";",		
				"}",
				"",
				"float getValue(vec3 positionOnRay);",
				
				};
				
			String body[] = {	
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() + " 1111",
				"float getValue(vec3 positionOnRay){",
				"	if("+suvUseGradient+"== 0){",
				"		return getNormalizedAndAggregatedVolumeValue(positionOnRay );",
				"	}else{",
				"		return 7.0*length("+gradient.call(new String[]{"positionOnRay"})+".xyz);",
				"	}",
				"}",
				"",
				"void main(void)",
				"{",	
				"",	
				"	fragmentColor = vec4("+suvBackgroundColor+".xyz,0.0);",
				"	"+sgvVolumeNormalizeFactor+" = 1.0/ ("+suvMaxVolumeValue+");",
				"	"+sgvNormIsoValue+"="+suvIsoValue+"*"+sgvVolumeNormalizeFactor+";",
				"	//get rays of volumes",
				"	int steps = "+Short.MAX_VALUE+";",
				"	int startStep = "+Short.MAX_VALUE+";",	
				"",
				"	//init positions, directions ,etc",
				"	"+sgvRayDirections+" = normalize("+svRayStartCoordinate+" - "+suvEyePosition+" );",
				"	"+sgvRayPositions+" = "+svRayStartCoordinate+";",	
				/*TODO REMOVE DEBUG*/ "mat4x4 toGlobal["+scvMaxNumberOfVolumes+"];",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",//TODO REMOVE DEBUG
				"toGlobal[n] = inverse("+suvTextureTransformationInverse+"[n]);",
				"		vec3 tmp = "+suvVoxelCount+"[n];",
				"	    vtextOffsets[n] = vec3(1.0/tmp);",
				"	    vtextScales[n] = (vec3("+suvVoxelCount+"[n]-ivec3(1)))/(vec3("+suvVoxelCount+"[n])*vec3("+suvVoxelCount+"[n]-ivec3(1)));",
				"",   
				//"    	//steps = max(steps,"+stepsFunction.call(new String[]{""+sgvSampleSize+"[n]","ray_pos","ray_dir"})+");",
				//"		//startStep = min(startStep,"+stepsToVolume.call(new String[]{""+sgvSampleSize+"[n]","ray_pos","ray_dir"})+");",
				"	}",
				"	steps = min(steps,getStepsTillClipp());",
				"   steps = min(steps, "+suvSamples+");",
				"	startStep = max(startStep,steps-1);",	
				"	//end init",
				"",
				"	//ray casting",
				"  	float density =getValue("+sgvRayPositions+"); ",
				"	int renderedSlice =0;",
				"	float latestdDistanceToSlice = getPlaneDistance("+suvNormalSlice+","+sgvRayPositions+" );",	//TODO find gpu killing bug of start steps
				"  	for(int i = 0; i < steps; i++){",
				"",
				"      	// note:", 
				"      	// - ray_dir * "+suvRenderRectStepSize+" can be precomputed",
				"      	// - we assume the volume has a cube-like shape",
				"",
				"		"+sgvRayPositions+" += "+sgvRayDirections+" * "+suvRenderRectStepSize+";",
				"      	// break out if ray reached the end of the cube.",
				"		float nextDensity = getValue("+sgvRayPositions+");",
				"",
				"      	vec4 color = "+transferFunctionCode.call(new String[]{"density","nextDensity",suvRenderRectStepSize})+";",
				"      	vec4 c_out =  "+interpreter.call(new String[]{"fragmentColor","color","density","nextDensity"})+";",
				"",		
				"		fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*c_out;",
				"		if(c_out.a +"+scvMinDelta+" >= 1.0){",
				"			break;",
				"		}",	
				"		//render slice",
				"		vec4 sliceColor= vec4(0.0);  ",
				"		if("+suvShowSlice+" == 1 && renderedSlice != 1){",
				"			float currentSliceDistance = getPlaneDistance("+suvNormalSlice+","+sgvRayPositions+" );",
				"			if(sign(currentSliceDistance) != sign(latestdDistanceToSlice) || sign(currentSliceDistance) == 0 ){",			
				"				sliceColor =vec4(nextDensity,nextDensity,nextDensity,1.0);",
				"				sliceColor.a  = 1.0;",
				"				sliceColor.rgb *= gamma;",
				"				",
				"				fragmentColor = fragmentColor + (1.0 - fragmentColor.a)*sliceColor;",
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
				//"vec4 globalEye0 =toGlobal[0]*vec4("+suvEyePosition+"[0].xyz,1.0); ",
				//"vec4 globalEye1 =toGlobal[1]*vec4("+suvEyePosition+"[1].xyz,1.0); ",
			
			//	"vec4 globalEye0 =toGlobal[0]*vec4("+svTextureCoordinate+"[0].xyz+"+sgvRayDirections+"[0] *( "+sgvSampleSize+"[0]),1.0); ",
			//	"vec4 globalEye1 =toGlobal[1]*vec4("+svTextureCoordinate+"[1].xyz+"+sgvRayDirections+"[1] *( "+sgvSampleSize+"[1]),1.0); ",
			
			//	"globalEye0.xyz/=globalEye0.w;",
			//	"globalEye1.xyz/=globalEye1.w;",
			//	"if(length(globalEye1.xyz-globalEye0.xyz) > 1.0 ){",
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

		addCodeArrayToList(stepsToVolume.declaration(), code);
		addCodeArrayToList(stepsFunction.declaration(), code);
		addCodeArrayToList(accumulator.declaration(), code);
		addCodeArrayToList(transferFunctionCode.declaration(), code);
		addCodeArrayToList(dependingFunctions, code);
		addCodeArrayToList(gradient.declaration(), code);
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
