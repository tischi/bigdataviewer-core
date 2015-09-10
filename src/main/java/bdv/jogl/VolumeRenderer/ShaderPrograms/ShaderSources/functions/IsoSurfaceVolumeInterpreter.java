package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;

public class IsoSurfaceVolumeInterpreter extends AbstractVolumeInterpreter {

	public IsoSurfaceVolumeInterpreter() {
		super("isoSurfaceInterpreter");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] declaration() {
		// TODO Auto-generated method stub
		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 1",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v){",
				"	vec4 c_out = c_in + (1.0 - c_in.a)*c;",
				//"	if(vm1 >= "+suvMinVolumeValue+"){",
				"	if(vm1 -"+scvMinDelta+" <= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" <= v +"+scvMinDelta+"  ||",
				"		    vm1 + "+scvMinDelta+" >= "+sgvNormIsoValue+"&&"+sgvNormIsoValue+" >= v -"+scvMinDelta+"){",
				"		c_out.rgba = vec4(1.0);",
				"	}//}",	
				"	return c_out;",
				"}"
		};
	}

}
