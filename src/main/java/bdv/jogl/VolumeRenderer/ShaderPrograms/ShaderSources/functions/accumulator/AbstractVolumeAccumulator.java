package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.AbstractShaderFunction;

public abstract class AbstractVolumeAccumulator extends AbstractShaderFunction  {
	
	private MultiVolumeRenderer parent;
	
	/**
	 * @return the parent
	 */
	public MultiVolumeRenderer getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(MultiVolumeRenderer parent) {
		this.parent = parent;
	}

	protected AbstractVolumeAccumulator(String name){
		super(name);
	}
	
	public void disposeGL(GL4 gl2) {}
	
	public void init(GL4 gl) {}
	
	public void updateData(GL4 gl){}
	
	
	
}
