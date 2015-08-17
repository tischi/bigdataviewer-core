package bdv.jogl.VolumeRenderer.utils;

import java.awt.Dimension;
import java.awt.Point;



/**
 * Functions for handling java drawings
 * @author michael
 *
 */
public class WindowUtils {

	/**
	 * Transfers a point between normal space to java inverted y space 
	 * @param pointInNormalSpace
	 * @param windowSize
	 * @return
	 */
	public static Point transformWindowNormalSpace(final Point pointInNormalSpace, 
			final Dimension windowSize){
		return new Point(pointInNormalSpace.x,
				windowSize.height- pointInNormalSpace.y);
		
	}
	
}
