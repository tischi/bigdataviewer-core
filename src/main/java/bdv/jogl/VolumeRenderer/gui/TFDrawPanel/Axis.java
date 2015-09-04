package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class Axis extends JPanel {

	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;
	
	public static enum AxisType {
		XAXIS,
		YAXIS
	};
	
	private final AxisType type;
	
	private final String axisName;
	
	private float min = 0;
	
	private float max = 1;
	
	private String minString = ""+min;
	
	private String maxString = ""+max;
	
	public Axis(String name, AxisType type){
		this.axisName = name;
		this.type = type;
		updateSize();
	}
	
	private void updateSize(){
		FontMetrics metrics = getFontMetrics(getFont());
		Dimension minSize = new Dimension();
		if(type == AxisType.XAXIS){
			minSize = getMinimumSize();
		}else{
			int widthMax = metrics.stringWidth(maxString);
			int widthMin = metrics.stringWidth(minString);
			minSize = new Dimension(Math.max(widthMax, widthMin), getMinimumSize().height);
		}
		setMinimumSize(minSize);
	
		Dimension currentSize = new Dimension(Math.max(getWidth(), minSize.width), Math.max(getHeight(), minSize.height));
		setSize(currentSize);
		setPreferredSize(currentSize);
		repaint();
	}
	
	private void drawMinMax(Graphics2D g){
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int maxx=0,maxy=0;
		int minx=0,miny=0;

		minx = 0;
		miny = getHeight()-1;
		
		if(type == AxisType.XAXIS){
			maxx = getWidth()-1 - metrics.stringWidth(maxString);
			maxy = getHeight()-1;
		}else{
			maxx = 0;
			maxy = 0 + metrics.getHeight();
		}
		
		//draw min in lower area
		g.drawChars(minString.toCharArray(), 0,  minString.length(), minx, miny);
		g.drawChars(maxString.toCharArray(), 0,  maxString.length(), maxx, maxy);
	}
	
	public void setMax(float max) {
		this.max = max;
		maxString = ""+max;
		updateSize();
	}
	
	public void setMin(float min) {
		this.min = min;
		minString = ""+min;
		updateSize();
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		drawMinMax(g2);
	}
}
