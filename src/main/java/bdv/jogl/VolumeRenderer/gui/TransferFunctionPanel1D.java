package bdv.jogl.VolumeRenderer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JPanel;

import com.jogamp.opengl.math.VectorUtil;

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

	private final int pointRadius = 10;
	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireEvent(final TransferFunctionListener listener){

		listener.colorChanged(getTexturColor());
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

	private void drawPointIcon(Graphics2D painter, final Point point){

		painter.setStroke(new BasicStroke(3));
		painter.drawOval(point.x-pointRadius, point.y-pointRadius, 
				pointRadius*2, pointRadius*2);
	}

	private void paintLines(Graphics g){
		if(points.size() < 2){
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		float [] dimFactors = {(float)(getWidth()/ (maxPoint.getX()-minPoint.getX())),
				(float)(getHeight()/ (maxPoint.getY()-minPoint.getY()))};

		//draw line and points
		Point latestRenderedPoint = null;
		for( Integer mapIndex: points.keySet()){
			Point currentPoint = new Point((int)Math.round(mapIndex.doubleValue()*dimFactors[0]),
					(int)Math.round(getHeight()- points.get(mapIndex).doubleValue()*dimFactors[1]));

			if(mapIndex != points.firstKey() ){

				//print line
				g2d.setStroke(new BasicStroke(5));
				g2d.drawLine(latestRenderedPoint.x, 
						latestRenderedPoint.y,
						currentPoint.x, 
						currentPoint.y);
			}
			latestRenderedPoint = currentPoint;

			//print points
			drawPointIcon(g2d, currentPoint);
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		paintSkala(g);

		paintLines(g);
	} 

	private Color getColorComponent(int index){
		float [] result = {0,0,0};
		
		//get RGB
		int nextIndex = colorMap.ceilingKey(index);
		if(nextIndex == index){
			return colorMap.get(index);
		}
		int previousIndex = colorMap.lowerKey(index);
		float colorDiff = nextIndex-previousIndex;
		float colorOffset = index - previousIndex;
	
		float[] colorPrev ={0,0,0};
		float[] colorNext ={0,0,0};
		
		colorMap.get(previousIndex).getColorComponents(colorPrev);
		colorMap.get(nextIndex).getColorComponents(colorNext);
	
		VectorUtil.addVec3(result, colorPrev, 
				VectorUtil.scaleVec3(null,
						VectorUtil.subVec3(null, colorNext, colorPrev),colorOffset/colorDiff));
		
		return new Color(result[0],result[1],result[2],0);
	}
	
	private float getAlpha (int index){
		//get RGB
		int nextIndex = points.ceilingKey(index);
		float normFactor = 1f/ (float)maxPoint.getY()-(float)minPoint.getY();
		if(nextIndex == index){
			return (points.get(index).floatValue()-(float)minPoint.getY()) * normFactor;
		}
		int previousIndex = points.lowerKey(index);
		float colorDiff = nextIndex-previousIndex;
		float colorOffset = index - previousIndex;
		
		float m = (points.get(nextIndex).floatValue()- points.get(previousIndex).floatValue()-(float)minPoint.getY())/colorDiff* normFactor;
		return m*colorOffset+(points.get(previousIndex).floatValue()-(float)minPoint.getY()) * normFactor;
		
	} 
	
	private Color getColorForXOrdinateInObjectTransferSpace(int index){
		
		float [] result = {0,0,0,0};
		
		getColorComponent(index).getColorComponents(result);
		
		result[3] = getAlpha(index);
		
		return new Color(result[0],result[1],result[2],result[3]);
	}


	/**
	 * @return the with alpha values
	 */
	public final TreeMap<Integer, Color> getTexturColor() {		
		TreeMap<Integer, Color> colors = new TreeMap<Integer, Color>();


		Color currentColor = null;
		//get colors from gradient
		for(Integer index : colorMap.keySet()){

			if(colors.containsKey(index)){
				continue;
			}
			
			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}
			
			colors.put(index, currentColor);
		}

		//get colors from line
		for(Integer index : points.keySet()){

			if(colors.containsKey(index)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}
			colors.put(index, currentColor);
		}

		return colors;
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
