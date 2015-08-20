package bdv.jogl.VolumeRenderer.gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.transformWindowNormalSpace;
import bdv.jogl.VolumeRenderer.Camera;

/**
 * Class for providing mouse camere interactions in gl scenes
 * @author michael
 *
 */
public class CameraUpdater {
	
	
	private final Camera camera;

	private Point previousOrbitPoint = null;
	
	private Point previousTracPoint = null;
	
	private final static float angleScale = 0.1f;
	
	private final static int orbitButton = MouseEvent.BUTTON1;
	
	private final static int tracButton = MouseEvent.BUTTON3;
	
	private final MouseListener mouseListener = new MouseAdapter() {
	
		@Override
		public void mousePressed(MouseEvent e) {
			Point point = transformWindowNormalSpace(e.getPoint(),e.getComponent().getSize());
			if(e.getButton() == orbitButton){
				previousOrbitPoint = point;	
			}
			
			if(e.getButton() == tracButton){
				previousTracPoint = point;	
			}
		};
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() == orbitButton){
				previousOrbitPoint = null;
			}
			
			if(e.getButton() == tracButton){
				previousTracPoint = null;
			}			
		};
	};
	
	private final MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
		@Override
		public void mouseDragged(MouseEvent e) {

			Point currentPoint = transformWindowNormalSpace(e.getPoint(),e.getComponent().getSize());
			if(previousOrbitPoint != null){
				float alpha = -( currentPoint.y - previousOrbitPoint.y )*angleScale;
				float beta = ( currentPoint.x - previousOrbitPoint.x )*angleScale;
				camera.orbit(alpha, beta);
				previousOrbitPoint = currentPoint;
				return;
			}

			if(previousTracPoint != null){
				float diffx = currentPoint.x - previousTracPoint.x;
				float diffy = currentPoint.y - previousTracPoint.y;
				camera.trac(diffx, diffy);
				previousTracPoint = currentPoint;
				return;
			}
		};
	};
	
	private final MouseWheelListener mouseWheelListener = new MouseWheelListener() {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			float alpha = camera.getAlpha(); 
			alpha += angleScale * (float)e.getWheelRotation();
			alpha = Math.min(Camera.maxAlpha,Math.max(Camera.minAlpha,alpha));
			camera.setAlpha(alpha);
			camera.updatePerspectiveMatrix();
		}
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

	/**
	 * @return the mouseWheelListener
	 */
	public MouseWheelListener getMouseWheelListener() {
		return mouseWheelListener;
	} 

}
