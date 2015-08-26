package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.GetMaxStepsFunction;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;


public class SingleVolumeRendererShaderSources extends AbstractShaderSource {

	public static final String suvVolumeTexture = "inVolumeTexture";

	public static final String suvColorTexture = "inColorTexture";

	public static final String suvEyePosition = "inEyePosition";

	public static final String suvMinVolumeValue = "inMinVolumeValue";

	public static final String suvMaxVolumeValue = "inMaxVolumeValue";

	private static final String svTextureCoordinate = "textureCoordinate";

	private final GetMaxStepsFunction stepsFunction = new GetMaxStepsFunction();
	
	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}

	private String[] vertexShaderCode(){
		String[] code={
				"#version "+ getShaderLanguageVersion(),
				"",
				"uniform mat4x4 "+suvProjectionMatrix+";",
				"uniform mat4x4 "+suvViewMatrix+";",
				"uniform mat4x4 "+suvModelMatrix+";",
				"",
				"in vec3 "+satPosition+";",
				"out vec3 "+svTextureCoordinate+";",
				"",
				"void main(){",
				"	"+svTextureCoordinate+" = "+satPosition+";",
				"	vec4 position4d = vec4("+satPosition+".xyz,1.f);",
				"",	
				"	gl_Position = "+suvProjectionMatrix+" *  "+suvViewMatrix+" * "+suvModelMatrix+" * position4d;",
				"	//unit cube vertex is also the texture coordinate", 
				"}"
		};
		appendNewLines(code);
		return code;
	}

	private String[] fragmentShaderCode(){
		List<String> code= new ArrayList<String>(); 
		String[] head={
				"#version "+getShaderLanguageVersion(),
				"//http://www.visualizationlibrary.org/documentation/pag_guide_raycast_volume.html",
				"uniform sampler3D "+suvVolumeTexture+";",
				"uniform sampler1D "+suvColorTexture+";",
				"out vec4 fragmentColor;",
				"in vec3 "+svTextureCoordinate+";",
				"uniform float "+suvMaxVolumeValue+";",
				"uniform float "+suvMinVolumeValue+";",
				"uniform vec3 "+suvEyePosition+";",
				"float val_threshold =1;",
				"const int maxInt = "+Integer.MAX_VALUE+";",
				""};
				addCodeArrayToList(head, code);
				addCodeArrayToList(stepsFunction.declaration(), code);
				String[] body={ 
				"",
				"void main(void)",
				"{",	
				"	const int samples = 512;//256;",
				"	float sample_step =sqrt(3f)/float(samples);",
				"	const float brightness = 150.0f;",
				"",	 
				"    vec3 ray_dir = normalize("+svTextureCoordinate+" - "+suvEyePosition+" );",
				"    vec3 ray_pos = "+svTextureCoordinate+"; // the current ray position",
				"",
				"    fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);",
				"    vec4 color;",
				"    float volumeNormalizeFactor = 1.f/ ("+suvMaxVolumeValue+"-"+suvMinVolumeValue+"+0.01);",
				"",    
				"    int steps =  "+stepsFunction.call(new String[]{"sample_step","ray_pos","ray_dir"})+";",
				"    if(steps > samples){",
				"    	steps = samples;",
				"    }",
				"   float density;",
				"  	for(int i = 0; i< steps; i++){",
				"",
				"       // note:", 
				"       // - ray_dir * sample_step can be precomputed",
				"       // - we assume the volume has a cube-like shape",
				"",        
				"       // break out if ray reached the end of the cube.",
				"       density = (texture("+suvVolumeTexture+", ray_pos).r-"+suvMinVolumeValue+") *volumeNormalizeFactor;",
				"",
				"       color.rgb = texture("+suvColorTexture+", density).rgb;",
				"       color.a   = texture("+suvColorTexture+", density).a /*density*/ * sample_step * val_threshold * brightness;",
				"       fragmentColor.rgb = fragmentColor.rgb * (1.0 - color.a) + color.rgb * color.a;",
				"		ray_pos += ray_dir * sample_step;",  		
				"   }",
				"	fragmentColor = vec4 (fragmentColor.rgb,0.1);", 
				"}"		
		};
		addCodeArrayToList(body, code);
		String [] codeArray=new String[code.size()]; 
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}
}
