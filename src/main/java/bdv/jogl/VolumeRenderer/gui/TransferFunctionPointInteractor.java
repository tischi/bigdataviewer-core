package bdv.jogl.VolumeRenderer.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
		return selectedPoint;
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
			
			parent.addFunctionPoint(e.getPoint());
			e.consume();
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(selectedPoint == null && e.getButton() != MouseEvent.BUTTON1){
			return;
		}
		
		parent.updateFunctionPoint(selectedPoint,e.getPoint());
		selectedPoint = e.getPoint();
		e.consume();
		

	}
}
