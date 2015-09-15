package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;

public class TransparentVolumeinterpreter extends AbstractVolumeInterpreter {
	public TransparentVolumeinterpreter(){
		super("transparentVolume");
	}
	@Override
	public String[] declaration() {

		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber() +" 4",
				"vec4 "+getFunctionName()+"(vec4 c_in, vec4 c, float vm1, float v ){",
				"	return c_in + (1.0 - c_in.a)*c;",
				"}"
		};
	};


}
