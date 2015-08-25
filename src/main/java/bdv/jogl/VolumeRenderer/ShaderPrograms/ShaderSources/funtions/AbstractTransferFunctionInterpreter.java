package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions;

/**
 * defines syntax for GPU tf value desampler
 * @author michael
 *
 */
public abstract class AbstractTransferFunctionInterpreter extends AbstractShaderFunction {
	
	public static final String suvTransferFunctionTexture = "inColorTexture";
	
	protected AbstractTransferFunctionInterpreter(){
		super("desampler");
	}
	
	
}
