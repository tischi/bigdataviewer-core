package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.transferfunctioninterpreter;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.AbstractShaderFunction;

/**
 * defines syntax for GPU tf value desampler
 * @author michael
 *
 */
public abstract class AbstractTransferFunctionInterpreter extends AbstractShaderFunction {
	
	protected AbstractTransferFunctionInterpreter(){
		super("desampler");
	}
	
	
}
