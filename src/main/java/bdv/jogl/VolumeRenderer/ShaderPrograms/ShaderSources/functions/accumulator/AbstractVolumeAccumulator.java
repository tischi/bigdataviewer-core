package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.AbstractShaderFunction;

public abstract class AbstractVolumeAccumulator extends AbstractShaderFunction  {
	
	private String colorFunctionName = "accumulateColor"; 
	
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

	/**
	 * @return the colorFunctionName
	 */
	public String getColorFunctionName() {
		return colorFunctionName;
	}

	protected AbstractVolumeAccumulator(String name){
		super(name);
	}
	
	public String callColor(String[]par){
		String call = getColorFunctionName() + "(";
		for(int i = 0; i<par.length; i++){
			call+=par[i];
			if(i < par.length-1){
				call+=",";
			}
		}
		call+=")";
		return call;
 	}
	
	protected abstract String[] colorAccDecl();
	
	public void disposeGL(GL4 gl2) {}
	
	public void init(GL4 gl) {}
	
	public void updateData(GL4 gl){}
	
	
	
}
