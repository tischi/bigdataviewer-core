package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JPanel;

/**
 * Transfer function interaction similar to paraview
 * @author michael
 *
 */
public class TransferFunctionPanel1D extends JPanel {
	
	private List<TransferFunctionListener> transferFunctionListeners = new ArrayList<TransferFunctionListener>();
	
	private TreeMap<Integer,Color> colorMap = new TreeMap<Integer, Color>();
	private TreeMap<Integer, Integer> points = new TreeMap<Integer, Integer>();
	
	private final Point minPoint = new Point(0, 0);
	
	private final Point maxPoint = new Point(100,100);
	
	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireEvent(final TransferFunctionListener listener){
		
		listener.colorChanged(getColorMap());
	}
	
	private void initStdHistValues(){
		colorMap.put(minPoint.x, Color.BLUE);
		colorMap.put((maxPoint.x-minPoint.x)/2+minPoint.x, Color.WHITE);
		colorMap.put(maxPoint.x,Color.RED);
		
		//line
		points.put(minPoint.x, minPoint.y);
		points.put(maxPoint.x, maxPoint.y);
	}
	
	/**
	 * constructor
	 */
	public TransferFunctionPanel1D(){
		
		initStdHistValues();
	}
	
	void paintSkala(Graphics g){
		//paint gradient image
				//error check
				if(colorMap.size() < 2){
					return;
				}
				
				//get painter
				Graphics2D painter = (Graphics2D) g;

				
				Integer latestX = 0;
				float xFactor = 1f/100f;
				for(Integer x: colorMap.keySet()){
					//skip first iteration
					if(x.equals( latestX)){
						continue;
					}
					
					Float flx = latestX.floatValue()*xFactor*(float)getWidth();
					Float fx = x.floatValue()*xFactor*(float)getWidth();
					//gradient
					GradientPaint gradient = new GradientPaint(flx, 0, colorMap.get(latestX),
							fx,0/* getHeight()*/, colorMap.get(x));
					
					
					//draw gradient
					painter.setPaint(gradient);
					painter.fillRect(flx.intValue(), 0, 
							fx.intValue(), getHeight());
					latestX = x;
				}
	}
	
	private void paintPoints(Graphics g){
		if(points.size() < 2){
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		
		Point dimFactors = new Point();
		dimFactors.setLocation(getWidth()* (maxPoint.getX()-minPoint.getX()),
				getHeight()* (maxPoint.getX()-minPoint.getX()));
		
		Integer latestIndex = points.firstKey();
		for( Integer mapIndex: points.keySet()){
			if(mapIndex == points.firstKey() ){
				continue;
			}
			
			//print line
			g2d.drawLine((int)Math.round(latestIndex.doubleValue()*dimFactors.getX()), 
					(int)Math.round(getHeight()- points.get(latestIndex).doubleValue()*dimFactors.getY()),
					(int)Math.round(mapIndex.doubleValue()*dimFactors.getX()), 
					(int)Math.round(getHeight()-points.get(mapIndex).doubleValue()*dimFactors.getY()));
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		paintSkala(g);
		
		paintPoints(g);
	}
	
	/**
	 * @return the colorMap
	 */
	public final TreeMap<Integer, Color> getColorMap() {
		return colorMap;
	}

	/**
	 * Adds a listener to the transfer function panel
	 * @param listener
	 */
	public void addTransferFunctionListener(final TransferFunctionListener listener){
		transferFunctionListeners.add(listener);
		
		fireEvent(listener);
	}
}
