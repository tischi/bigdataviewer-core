package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;

public abstract class AbstractVolumeInterpreter extends AbstractShaderFunction{

	protected AbstractVolumeAccumulator accumulator;
	protected AbstractVolumeInterpreter(String functionName) {
		super(functionName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param accumulator the accumulator to set
	 */
	public void setAccumulator(AbstractVolumeAccumulator accumulator) {
		this.accumulator = accumulator;
	}
	
	

}
