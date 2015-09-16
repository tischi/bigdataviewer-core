package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;

public class IsoSurfaceVolumeInterpreter extends AbstractVolumeInterpreter {

	private VolumeGradientEvaluationFunction gradEval = new VolumeGradientEvaluationFunction();
	
	private final int refinementSteps = 4;
	
	public IsoSurfaceVolumeInterpreter() {
		super("isoSurfaceInterpreter");

	}

	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		addCodeArrayToList(gradEval.declaration(),code);
		addCodeArrayToList( new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 101",
				"//bisection form http://onlinelibrary.wiley.com/doi/10.1111/j.1467-8659.2005.00855.x/abstract",
				"vec3 bisection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue){",
				"	vec3 xNew = (xFar - xNear) * (isoValue - fNear)/(fFar - fNear) + xNear;",
				"	return xNew;",	
				"}",
				"",
				"vec4[2] refineIntersection(float fNear, float fFar, vec3 xNear, vec3 xFar, float isoValue, sampler3D volume){",
				"	vec4[2] refined;",
				"	for(int i =0; i < "+refinementSteps+"; i++){",
				"		vec3 xNew = bisection(fNear,fFar,xNear,xFar,isoValue);  ",
				"		float fNew = texture(volume, xNew).r * "+sgvVolumeNormalizeFactor+";",
				"		if(fNew > isoValue ){",
				"			xNear = xNew;",
				//"			fNear = fNew;",	
				"		}else{",
				"			xFar = xNew;",
			//	"			fFar = fNew;",
				"		}",	
				"	}",
				"	refined[0].xyz = xNear;",
				"	refined[0].w = fNear;",
				"	refined[1].xyz = xFar;",
				"	refined[1].w = fFar;",
				"	return refined;",
				"}",
				"",
				"const int numberOfLights = 0;",
				"const vec3 inconstants = vec3(0.8,0.4,0.2);",
				"const vec3 ambientColor = vec3(0.5);",
				"vec3 blinnPhongShading(vec3 constants, ",
				"						vec3 iAmbient,",
				"						vec3 normal, ",
				"						vec3 lookVector, ",
				"						vec3 lightDirs,",
				"						vec3 iIn){",
				"	vec3 halfVec = (-lookVector-lightDirs)/length(-lookVector-lightDirs);",
				"	vec3 iOut = constants.x * iAmbient;//ambient",
				"	iOut += iIn*(constants.y*(dot(-lightDirs, normal)));//diffuse",
				"	iOut += iIn*(constants.y*pow( dot(halfVec,normal),1.0));//specular",
				"	",
				"	return iOut;",
				"}",
				"",		
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v){",
				"	int n = 0;",
				"	if(vm1 -"+scvMinDelta+" <= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" <= v +"+scvMinDelta+"  ||",
				"		    vm1 + "+scvMinDelta+" >= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" >= v -"+scvMinDelta+"){",
				"		vec3 color = vec3(0.0,0.0,0.0);",
				"		for(int volume = 0; volume < "+scvMaxNumberOfVolumes+"; volume++){",
				"			",
				"			if(any(lessThan("+sgvRayPositions+"[volume],vec3(0.0)))||",
				"				any(greaterThan("+sgvRayPositions+"[volume],vec3(1.0)))||",
				"				"+suvActiveVolumes+"[volume] == 0){",
				"				continue;",				
				"			}",	
				"			vec3 xNear = "+sgvRayPositions+"[volume] - "+sgvRayDirections+"[volume] * "+sgvSampleSize+";",
				"			vec3 xFar = "+sgvRayPositions+"[volume];", 
				"			vec4 refined[2] = refineIntersection(vm1,v,xNear,xFar,"+sgvNormIsoValue+","+suvVolumeTexture+"[volume]);",
			//	"			vec3 gradient = "+gradEval.call(new String[]{sgvRayPositions+"[volume]", suvVolumeTexture+"[volume]"})+";",
				"			vec3 gradient = "+gradEval.call(new String[]{"refined[1].xyz", suvVolumeTexture+"[volume]"})+";",
				"			color += blinnPhongShading(inconstants,ambientColor,normalize(gradient),"+sgvRayDirections+"[volume], normalize("+sgvRayPositions+"[volume]-"+suvLightPosition+"[volume]),"+suvLightIntensiy+");",
				"			n++;",	
				"		}",
				"		if(n==0){",
				"			color = ambientColor;",
				"			n=1;",
				"		}",
				"		color /= float(n);",
				"		c.rgb = color.rgb;",
				"		c.a = 1.0;",
				//"		c.rgb *= rDiffuse*factorDiffuse;",	
				"	}",	
				"	vec4 c_out = c_in + (1.0 - c_in.a)*c;",
				"	return c_out;",
				"}"
		},code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

}
