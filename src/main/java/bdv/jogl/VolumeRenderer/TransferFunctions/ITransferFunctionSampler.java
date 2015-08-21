package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.nio.FloatBuffer;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions.IFunction;

/**
 * Samples the Transfer function and creates the texture and defines the desampling
 * @author michael
 *
 */
public interface ITransferFunctionSampler {
	/**
	 * Retruns the desampling shader code
	 * @return
	 */
	public IFunction getShaderCode();
	
	/**
	 * samples the texture
	 * @param transferFunction
	 * @param sampleStep
	 * @return
	 */
	public FloatBuffer sample(TransferFunction1D transferFunction, float sampleStep);
}
