package bdv.jogl.VolumeRenderer.TransferFunctions.sampler;

import java.nio.FloatBuffer;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

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
