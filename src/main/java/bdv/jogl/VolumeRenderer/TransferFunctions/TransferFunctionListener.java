package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.awt.Color;
import java.util.EventListener;
import java.util.TreeMap;

/**
 * listen if a certain transfer function was altered.
 * @author michael
 *
 */
public interface TransferFunctionListener extends EventListener {

	/**
	 * Triggered by color changes. 
	 * @param xToColorMap Colors per ordinate.
	 */
	public void colorChanged(final TransferFunction1D transferFunction );
	

	/**
	 * called if the sampler (pre integration or normal sampler) was altered. 
	 * The shader then needs to recompile.
	 */
	public void samplerChanged(final TransferFunction1D transferFunction1D);
}
