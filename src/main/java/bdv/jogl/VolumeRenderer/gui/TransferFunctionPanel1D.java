package bdv.jogl.VolumeRenderer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

	private final TransferFunctionContexMenu contextMenue = new TransferFunctionContexMenu(this);
	
	private final TransferFunctionWindowResizeHandler resizeHandler;

	private final TransferFunctionPointInteractor pointInteractor = new TransferFunctionPointInteractor(this);

	private List<TransferFunctionListener> transferFunctionListeners = new ArrayList<TransferFunctionListener>();

	private TreeMap<Integer,Color> colorMap = new TreeMap<Integer, Color>();

	private TreeMap<Integer, Integer> functionPoints = new TreeMap<Integer, Integer>();

	private final int pointRadius = 10;

	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireEvent(final TransferFunctionListener listener){

		listener.colorChanged(getTexturColor());
	}


	public void resetLine(){
		functionPoints.clear();
		functionPoints.put(getWidth()-1,0);
		functionPoints.put(0,getHeight());

		repaint();
		fireEventAll();
	}

	public void resetColors(){
		colorMap.clear();
		colorMap.put(0, Color.BLUE);
		colorMap.put(getWidth()/2, Color.WHITE);
		colorMap.put(getWidth()-1,Color.RED);

		repaint();
		fireEventAll();
	}


	private void addControls(){
		addMouseListener(contextMenue);

		addMouseMotionListener(pointInteractor);

		addMouseListener(pointInteractor);
		
		addComponentListener(resizeHandler);
	}

	private void fireEventAll(){

		for(TransferFunctionListener listener:transferFunctionListeners){
			fireEvent(listener);
		}
	}

	/**
	 * constructor
	 */
	public TransferFunctionPanel1D(){

		initWindow();
		
		resizeHandler = new TransferFunctionWindowResizeHandler(getSize(),colorMap,functionPoints);
		
		resetColors();

		resetLine();

		addControls();
	}

	private void initWindow() {
		setSize(200, 100);
		setPreferredSize(getSize());
		setMinimumSize(getSize());
		setMaximumSize(getSize());
		
	}


	/**
	 * Sets the color of a certain point in the panel.
	 * @param point Position on the panel area.
	 * @param color Color to be set.
	 */
	public void setColor(final Point point, final Color color){
		colorMap.put(point.x, color);

		repaint();
		fireEventAll();
	}

	private void paintSkala(Graphics g){
		//paint gradient image
		//error check
		if(colorMap.size() < 2){
			return;
		}

		//get painter
		Graphics2D painter = (Graphics2D) g;


		Integer latestX = 0;
		for(Integer x: colorMap.keySet()){
			//skip first iteration
			if(x.equals( latestX)){
				continue;
			}

			//gradient
			GradientPaint gradient = new GradientPaint(latestX, 0, colorMap.get(latestX),
					x,0/* getHeight()*/, colorMap.get(x));


			//draw gradient
			painter.setPaint(gradient);
			painter.fillRect(latestX.intValue(), 0, 
					x.intValue(), getHeight());
			latestX = x;
		}
	}

	private void drawPointIcon(Graphics2D painter, final Point point){

		painter.setStroke(new BasicStroke(3));
		painter.drawOval(point.x-pointRadius, point.y-pointRadius, 
				pointRadius*2, pointRadius*2);
	}

	private void paintLines(Graphics g){
		if(functionPoints.size() < 2){
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		//draw line and points
		Point latestRenderedPoint = null;
		for( Integer mapIndex: functionPoints.keySet()){
			Point currentPoint = new Point(mapIndex,functionPoints.get(mapIndex));

			if(mapIndex != functionPoints.firstKey() ){

				//print line
				g2d.setStroke(new BasicStroke(5));
				g2d.drawLine(latestRenderedPoint.x, 
						latestRenderedPoint.y,
						currentPoint.x, 
						currentPoint.y);
			}
			latestRenderedPoint = currentPoint;
		}
	}

	private void paintPoints(Graphics g){
		//print points	
		Graphics2D g2d = (Graphics2D) g;	
		
		for( Integer mapIndex: functionPoints.keySet()){
			Point currentPoint = new Point(mapIndex,functionPoints.get(mapIndex));
			
			//TODO
			if(currentPoint.equals(pointInteractor.getSelectedPoint())){
				g2d.setColor(Color.gray);
			}
			drawPointIcon(g2d, currentPoint);
			g2d.setColor(Color.black);
		}	
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		paintSkala(g);

		paintLines(g);
		
		paintPoints(g);
	} 

	private Color getColorComponent(int index){
		float [] result = {0,0,0};

		
		//get RGB
		Integer nextIndex = colorMap.ceilingKey(index);
		if(nextIndex == null){
			return colorMap.lastEntry().getValue();
		}
		
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

		float []tmpColor= {0,0,0};
		VectorUtil.subVec3(tmpColor, colorNext, colorPrev);
		VectorUtil.scaleVec3(tmpColor,tmpColor,colorOffset/colorDiff);
		VectorUtil.addVec3(result, colorPrev,tmpColor );

		return new Color(result[0],result[1],result[2],0);
	}


	private float getNormalizedAlphaValue(int unNormalizedValue){
		float normFactor = 1f/ (float)getHeight();
		
		float calculatedValue = (float)(getHeight()- unNormalizedValue)*normFactor;
		
		return Math.min(1, Math.max(calculatedValue, 0));
		
	}
	
	private float getAlpha (int index){
		//get alpha
		int nextIndex = functionPoints.ceilingKey(index);
		float nextAlpha = getNormalizedAlphaValue(functionPoints.get(nextIndex));
		
		if(nextIndex == index){
			return nextAlpha;
		}
		int previousIndex = functionPoints.lowerKey(index);
		float prevAlpha = getNormalizedAlphaValue(functionPoints.get(previousIndex));
		
		float colorDiff = nextIndex-previousIndex;
		float colorOffset = index - previousIndex;
		
		float m = (nextAlpha - prevAlpha)/colorDiff;
		return m* colorOffset + prevAlpha;

	} 

	private Color getColorForXOrdinateInObjectTransferSpace(int index){

		float [] result = {0,0,0,0};

		getColorComponent(index).getColorComponents(result);

		result[3] = getAlpha(index); 
		Color resultColor = null ;
		try {
			resultColor = new Color(result[0],result[1],result[2],result[3]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultColor;
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
		for(Integer index : functionPoints.keySet()){

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

	/**
	 * Function to add points to the transfer Function
	 * @param point Point to be added on the panel area
	 */
	public void addFunctionPoint(final Point point) {
		if(functionPoints.containsKey(point.x)){
			return;
		}
		functionPoints.put(point.x, point.y);

		fireEventAll();
		repaint();
	}

	/**
	 * Updates points in the transfer function panel. 
	 * @param oldPoint Old point in the panel
	 * @param newPoint New position of the old point instance
	 */
	public void updateFunctionPoint(Point oldPoint, Point newPoint) {


		int dragIndex = oldPoint.x;

		int ceil= functionPoints.ceilingKey(oldPoint.x);
		if(ceil != oldPoint.x){
			int low= functionPoints.lowerKey(oldPoint.x);			
			dragIndex = (oldPoint.x -low < ceil - oldPoint.x)?low:ceil;
		}

		functionPoints.put(dragIndex, Math.min(Math.max(newPoint.y,0),getHeight()));

		fireEventAll();
		repaint();
	}
}
