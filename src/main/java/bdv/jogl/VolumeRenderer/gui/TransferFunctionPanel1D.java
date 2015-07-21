package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	
	private final int minX = 0;
	
	private final int maxX = 100;
	
	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireEvent(final TransferFunctionListener listener){
		
		listener.colorChanged(getColorMap());
	}
	
	private void initStdHistValues(){
		colorMap.put(minX, Color.BLUE);
		colorMap.put((maxX-minX)/2+minX, Color.WHITE);
		colorMap.put(maxX,Color.RED);
	}
	
	/**
	 * constructor
	 */
	public TransferFunctionPanel1D(){
		
		initStdHistValues();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
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
