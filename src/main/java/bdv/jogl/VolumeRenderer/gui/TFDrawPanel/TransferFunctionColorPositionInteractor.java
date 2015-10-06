package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import static bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D.calculateTransferFunctionPoint;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.transformWindowNormalSpace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseAdapter;
import java.awt.geom.Point2D;
import java.util.TreeMap;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

public class TransferFunctionColorPositionInteractor {
	
	private Point2D.Float selectedPoint = null;
	
	private final ColorSliderPanel parent;
	
	
	public TransferFunctionColorPositionInteractor(final ColorSliderPanel parent) {
		this.parent = parent;
	}

	private Point2D.Float getNearestValidPoint(final Point2D.Float p){
		Point2D.Float query = null;
		TreeMap<Point2D.Float, Color> colors = parent.getTransferFunction().getColors();
		Point2D.Float upperPoint = colors.higherKey(p);
		Point2D.Float lowerPoint = colors.lowerKey(p);
		
		float dists[] = {Float.MAX_VALUE,Float.MAX_VALUE};
		//valid points ?
		if(lowerPoint != null && lowerPoint != colors.firstKey() &&lowerPoint != colors.lastKey() ){
			dists[0]= Math.min(dists[0],(float)p.distance(lowerPoint));
		}
		if(upperPoint != null && upperPoint != colors.firstKey() &&upperPoint != colors.lastKey() ){
			dists[1]= Math.min(dists[1],(float)p.distance(upperPoint));
		}
		
		//min point ? 
		if(dists[0] < dists[1]){
			if(dists[0] <Float.MAX_VALUE ){
				query = lowerPoint;
			}
		}else{
			if(dists[1] <Float.MAX_VALUE ){
				query = upperPoint;
			}
		}
		
		return query;
	}
	
	private final MouseAdapter mouseListener = new MouseAdapter() {
		
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			
			TransferFunction1D tf = parent.getTransferFunction();
		
			Dimension size = parent.getSize();
			Point windowPoint = transformWindowNormalSpace( e.getPoint(), size);
			selectedPoint = getNearestValidPoint(calculateTransferFunctionPoint(windowPoint, tf, size));
		};
		
		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			
			selectedPoint = null;
		}
	};
	
	private final MouseMotionAdapter mouseMotionListener = new MouseMotionAdapter() {
		@Override
		public void mouseDragged(MouseEvent e) {
			super.mouseDragged(e);
			
			if(selectedPoint == null /*|| e.getButton() != MouseEvent.BUTTON1*/){
				return;
			}
			
			//convert Point
			Point p = new Point(e.getX(), 0);
			
			//not valid space
			Rectangle drawArea = parent.getVisibleRect();
			if(p.getX() <= drawArea.x || drawArea.x + drawArea.width <= p.getX()){
				return;
			} 
		
			Dimension size = parent.getSize();
			TransferFunction1D tf = parent.getTransferFunction();
			Point2D.Float oldPoint = selectedPoint;
			Point windowPoint = transformWindowNormalSpace(p, parent.getSize());
			Point2D.Float newPoint = calculateTransferFunctionPoint(windowPoint, tf, size);
			newPoint.y = 0;
			
			//already present
			if(parent.getTransferFunction().getColors().containsKey(newPoint)){
				return;
			}
			
			
			parent.getTransferFunction().moveColor(oldPoint, newPoint);
			selectedPoint = newPoint;
			e.consume();
		}
	};

	/**
	 * @return the mouseListener
	 */
	public MouseAdapter getMouseListener() {
		return mouseListener;
	}

	/**
	 * @return the mouseMotionListener
	 */
	public MouseMotionAdapter getMouseMotionListener() {
		return mouseMotionListener;
	}
	
	
}
