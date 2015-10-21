package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.volumeninterpreter;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
/**
 * Supports Maximum Intensity Projection 
 * @author michael
 *
 */
public class MaximumIntensityProjectionInterpreter extends AbstractVolumeInterpreter {
	public MaximumIntensityProjectionInterpreter(){
		super("maximumIntensityProjection");
	}
	@Override
	public String[] declaration() {

		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 4",
				"float maxIntensity = 0;",
				"vec4 maxIntensityColor = vec4(0.0);",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v ){",
				"	if(vm1 > v){",
				"		if(vm1 > maxIntensity){",
				"			maxIntensity = vm1;",
				"			maxIntensityColor = c_in;",
				"			maxIntensityColor.rbg /= "+suvRenderRectStepSize+";",
				"		}",
				"	}else{",
				"		if(v > maxIntensity){",
				"			maxIntensity = v;",
				"			maxIntensityColor = c;",
				"			maxIntensityColor.rbg /= "+suvRenderRectStepSize+";",
				"		}",
				"	}",
				"	return maxIntensityColor;",
				"}"
		};
	};

}
