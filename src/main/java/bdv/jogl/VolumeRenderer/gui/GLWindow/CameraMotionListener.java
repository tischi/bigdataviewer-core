package bdv.jogl.VolumeRenderer.gui.GLWindow;

import java.util.EventListener;

/**
 * listens for begin and end of a camera motion
 * @author michael
 *
 */
public interface CameraMotionListener extends EventListener {

	public void motionStart();
	
	public void motionStop();
}
