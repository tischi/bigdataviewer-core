package bdv.jogl.VolumeRenderer.gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import bdv.jogl.VolumeRenderer.Camera;

/**
 * Class for providing mouse camere interactions in gl scenes
 * @author michael
 *
 */
public class CameraUpdater {
	
	
	private final Camera camera;

	private Point previousPoint = null;
	
	private final static float angleScale = 0.1f;
	
	private final static int leftButton = MouseEvent.BUTTON1;
	
	private final MouseListener mouseListener = new MouseAdapter() {
	
		@Override
		public void mousePressed(MouseEvent e) {
			
			if(e.getButton() != leftButton){
				return;
			}
			previousPoint = e.getPoint();
		};
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() != leftButton){
				return;
			}
			previousPoint = null;
		};
	};
	
	private final MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
		@Override
		public void mouseDragged(MouseEvent e) {


			if(previousPoint == null){
				return;
			}

			Point currentPoint = e.getPoint();
			float alpha = (previousPoint.y - currentPoint.y)*angleScale;
			float beta = (previousPoint.x - currentPoint.x)*angleScale;
			camera.orbit(alpha, beta);
			previousPoint = currentPoint;
		};
	};
	
	/**
	 * Constructor for setting the camera
	 * @param camera
	 */
	public CameraUpdater(Camera camera) {
		this.camera = camera;
	}

	/**
	 * return the motion listener to add to the scene widget
	 * @return
	 */
	public MouseMotionListener getMouseMotionListener() {
		return mouseMotionListener;
	}

	/**
	 * return the mouse listener to add to the scene widget
	 * @return
	 */
	public MouseListener getMouseListener() {
		return mouseListener;
	} 

}
