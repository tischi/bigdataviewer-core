package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvRayPositions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.suvVolumeTexture;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the maximum difference of the curvature values
 * @author michael
 *
 */
public class MaxCurvatureDifference extends AbstractVolumeAccumulator {
	public MaxCurvatureDifference() {
		super("curvature_difference");
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 6666",
				"const float curvoffset = 0.2;",
				"mat3x3 getIdentity(){",
				"	mat3x3 identity = mat3x3(0.0);",
				"	for(int i =0; i < 3; i++){",
				"		identity[i][i] = 1.0;",
				"	}",
				"	return identity;",
				"}",
				"",
				"float frobeniusNorm(mat3x3 m){",
				"	float f = 0.0;",
				"	for(int i = 0; i< 3; i++){",
				"		for(int j = 0; j < 3; j++){",
				"			f+= m[i][j]*m[i][j];",
				"		}",
				"	}",
				"	f= sqrt(f);",
				"	return f;",
				"}",
				"",
				"float trace(mat3x3 m){",
				"	float t = 0.0;",
				"	for(int i =0; i< 3; i++){",
				"		t+= m[i][i];",
				"	}",
				"	return t;",
				"}",
				"",
				"vec3 gradCentral(mat3x3 v[3]){",
				"	return vec3(v[2][1][1]-v[0][1][1],v[1][2][1]-v[1][0][1],v[1][1][2]-v[1][1][0])/(2.0*curvoffset);",
				"}",
				"",
				"mat3x3[3] getValuesForGradient(vec3 pos, int volume){",
				"	mat3x3 values[3];",
				"	vec3 offset = vec3(curvoffset);",
				"	for(int z = 0; z < 3; z++){",
				"		for(int y = 0; y < 3; y++){",
				"			for(int x = 0; x < 3; x++){",
				"				vec3 offsetFactor= vec3(x-1,y-1,z-1);",
				"				vec3 texC = getCorrectedTexturePositions(pos + offsetFactor*offset, volume);",
				"				values[x][y][z] = texture("+suvVolumeTexture+"[volume],texC).r;",	
				"			}",
				"		}",
				"	}",
				"	return values;",
				"}",
				"",
				"float dfdn(mat3x3 v[3],int n, int off, int offn){",
				"	if(n==0){",
				"		if(offn == 1){",
				"			return (v[2][off][1]-v[0][off][1])/(2.0*curvoffset);",
				"		}else{",
				"			return (v[2][1][off]-v[0][1][off])/(2.0*curvoffset);",
				"		}",
				"	}",
				"	if(n==1){",
				"		if(offn == 0){",
				"			return (v[off][2][1]-v[off][0][1])/(2.0*curvoffset);",
				"		}else{",
				"			return (v[1][2][off]-v[1][0][off])/(2.0*curvoffset);",
				"		}",
				"	}",
				"	if(n==2){",
				"		if(offn == 0){",
				"			return (v[off][1][2]-v[off][1][0])/(2.0*curvoffset);",
				"		}else{",
				"			return (v[1][off][2]-v[1][off][0])/(2.0*curvoffset);",
				"		}",
				"	}",
				"	return 0.0;",
				"}",
				"",
				"mat3x3 getHessianMatrix(mat3x3 v[3]){",
				"	mat3x3 hessian;",
				"	vec3 offset = vec3(curvoffset);",
				"	for(int i = 0; i< 3; i++){",
				"		for(int j = 0; j < 3; j++){",
				"			hessian[i][j]=(dfdn(v,i,2,j)-dfdn(v,i,0,j))/(2.0*curvoffset);",
				"		}",
				"	}",
				"	return hessian;",
				"}",
				"",
				"float[2] getCurvatureFactors(vec3 position,int volume){",
				"	//neighborhood for gradient",
				"	mat3x3 values[3]=getValuesForGradient("+sgvRayPositions+",volume);",
				"	mat3x3 h = getHessianMatrix(values);",
				"	vec3 gradient = gradCentral(values);",	
				"	mat3x3 n = mat3x3(-1.0* gradient/length(gradient),vec3(0.0),vec3(0.0));",
				"	mat3x3 p = getIdentity() - n * transpose(n);",
				"	mat3x3 g = -p*h*p/length(gradient);",
				"	float t = trace(g);",
				"	float f = frobeniusNorm(g);",
				"	float s = sqrt(2.0* f*f - t*t);",
				"	float k[2];",
				"	k[0] = (t+s)/2.0;",
				"	k[1] = (t-s)/2.0;",
				"	return k;",
				"}",
				"",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float difference = 0.0;",		
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		for(int m = 0; m < "+scvMaxNumberOfVolumes+";m++){",
				"			if(densities[n]<0 || densities[m]<0){",
				"				continue;",
				"			}",	
				"			float km[2] = getCurvatureFactors("+sgvRayPositions+",m);",
				"			float kn[2] = getCurvatureFactors("+sgvRayPositions+",n);",
				"",
				"			//manhatten distance",
				"			float currentDifference = abs(km[0]-kn[0]) + abs(km[1]-kn[1]) ;",
				"			difference = max(difference,currentDifference);",	
				"		}",
				"	}",
				"	return difference;",	
				"}"
		};
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}
}
