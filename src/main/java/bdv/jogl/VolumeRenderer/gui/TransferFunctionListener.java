package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.util.TreeMap;

/**
 * listen if a certain transfer function was altered.
 * @author michael
 *
 */
public interface TransferFunctionListener {

	/**
	 * Triggered by color changes. 
	 * @param xToColorMap Colors per ordinate.
	 */
	public void colorChanged(final TreeMap<Integer, Color> xToColorMap);
}
