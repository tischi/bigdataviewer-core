package bdv.jogl.VolumeRenderer.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static bdv.jogl.VolumeRenderer.utils.WindowUtils.*;

/**
 * Delivers all direct point interactions on the Transfer function panel
 * @author michael
 *
 */
public class TransferFunctionPointInteractor implements MouseListener, MouseMotionListener{

	private final TransferFunctionPanel1D parent;
	
	private Point selectedPoint = null;
	
	/**
	 * @return the selectedPoint
	 */
	public Point getSelectedPoint() {
		if(null == selectedPoint){
			return null;
		}
		return new Point(selectedPoint);
	}


	public TransferFunctionPointInteractor(final TransferFunctionPanel1D parent){
		this.parent = parent;
	}
	
	
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
		
		selectedPoint = e.getPoint();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		//insert new point
		if(e.getClickCount() ==2 && e.getButton() == MouseEvent.BUTTON1){
			Point functionPoint = transformWindowNormalSpace(e.getPoint(), parent.getSize());
			parent.getTransferFunction().addFunctionPoint(functionPoint);
			e.consume();
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}
	
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
		
		
		Point oldPoint = transformWindowNormalSpace(selectedPoint, parent.getSize());
		Point newPoint = transformWindowNormalSpace(query, parent.getSize());
		
		parent.getTransferFunction().updateFunctionPoint(oldPoint,newPoint);
		selectedPoint = e.getPoint();
		e.consume();
		

	}
}
