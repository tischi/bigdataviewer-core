package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.*;
import static bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D.calculateTransferFunctionPoint;

/**
 * Delivers all direct point interactions on the Transfer function panel
 * @author michael
 *
 */
public class TransferFunctionPointInteractor {

	private final TransferFunctionPanel1D parent;
	
	private Point selectedPoint = null;
	
	private final MouseListener mouseListener = new MouseAdapter() {
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() != MouseEvent.BUTTON1){
				return;
			}
			selectedPoint = null;
			e.consume();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			//drag point
			if(e.getClickCount() != 1||e.getButton() != MouseEvent.BUTTON1){
				return;
			}
			
			TransferFunction1D tf = parent.getTransferFunction();
			Dimension size = parent.getSize();
			selectedPoint = transformWindowNormalSpace( e.getPoint(), size);
			selectedPoint = calculateTransferFunctionPoint(selectedPoint, tf, size);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			
			//insert new point
			if(e.getClickCount() ==2 && e.getButton() == MouseEvent.BUTTON1){
				Dimension size = parent.getSize();
				TransferFunction1D tf = parent.getTransferFunction();
				Point functionPoint = transformWindowNormalSpace(e.getPoint(), size);
				functionPoint = calculateTransferFunctionPoint(functionPoint, tf, size);
				tf.addFunctionPoint(functionPoint);
				e.consume();
			}
		}
	};
	
	private final MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Point query = e.getPoint();
			Rectangle drawArea = parent.getVisibleRect();
			if(!drawArea.contains(e.getPoint())){
				query.setLocation(Math.min(drawArea.getMaxX(),Math.max(drawArea.getMinX(), query.getX())),
						Math.min(drawArea.getMaxY(),Math.max(drawArea.getMinY(), query.getY())));
			}
			if(selectedPoint == null && e.getButton() != MouseEvent.BUTTON1){
				return;
			}			
			
			Dimension size = parent.getSize();
			TransferFunction1D tf = parent.getTransferFunction();
			Point oldPoint = selectedPoint;
			Point newPoint = transformWindowNormalSpace(query, parent.getSize());
			newPoint = calculateTransferFunctionPoint(newPoint, tf, size);
			
			parent.getTransferFunction().updateFunctionPoint(oldPoint,newPoint);
			selectedPoint = newPoint;
			e.consume();
		}
	}; 
	
	/**
	 * @return the selectedPoint in transfer function space
	 */
	public Point getSelectedPoint() {
		if(null == selectedPoint){
			return null;
		}
		return new Point(selectedPoint);
	}


	/**
	 * @return the mouseListener
	 */
	public MouseListener getMouseListener() {
		return mouseListener;
	}


	/**
	 * @return the mouseMotionListener
	 */
	public MouseMotionListener getMouseMotionListener() {
		return mouseMotionListener;
	}


	public TransferFunctionPointInteractor(final TransferFunctionPanel1D parent){
		this.parent = parent;
	}
}
