package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.awt.Color;
import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.jogamp.opengl.math.VectorUtil;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.funtions.IFunction;
import bdv.jogl.VolumeRenderer.gui.TransferFunctionListener;

/**
 * store data for 1D Transfer functions
 * @author michael
 *
 */
public class TransferFunction1D {

	private List<TransferFunctionListener> transferFunctionListeners = new ArrayList<TransferFunctionListener>();

	private ITransferFunctionSampler sampler = new PreIntegrationSampler();
	
	//order points first by x then by y
	private final Comparator<Point> pointOrderXOperator = new Comparator<Point>() {

		@Override
		public int compare(Point a, Point b) {
			//same x
			if(a.x == b.x){
				return a.y-b.y;
			}
			return a.x-b.x;
		}
	};

	private Point maxOrdinates;

	private final TreeMap<Point,Color> colors;

	private final TreeSet<Point > functionPoints;

	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireEvent(final TransferFunctionListener listener){

		listener.colorChanged(this);
	}

	private void fireEventAll(){

		for(TransferFunctionListener listener:transferFunctionListeners){
			fireEvent(listener);
		}
	}

	public void resetLine(){
		functionPoints.clear();
		functionPoints.add(new Point(0,0));
		functionPoints.add(new Point(maxOrdinates));	

		fireEventAll();
	}

	public void resetColors(){
		colors.clear();
		colors.put(new Point(0,0), Color.BLUE);
		colors.put(new Point(maxOrdinates.x/2,0), Color.WHITE);
		colors.put(new Point(maxOrdinates.x,0),Color.RED);

		fireEventAll();
	}


	public TransferFunction1D(int maxX, int maxY) {
		colors = new TreeMap<Point, Color>(pointOrderXOperator);
		functionPoints = new  TreeSet<Point>(pointOrderXOperator);

		maxOrdinates = new Point(maxX, maxY);

		resetColors();
		resetLine();
	}

	/**
	 * @return the colors
	 */
	public TreeMap<Point, Color> getColors() {
		return new TreeMap<Point, Color>(colors);
	}

	/**
	 * Sets the color of a certain point in the panel.
	 * @param point Position on the panel area.
	 * @param color Color to be set.
	 */
	public void setColor(final Point point, final Color color){
		colors.put(point, color);

		fireEventAll();
	}

	/**
	 * Updates points in the transfer function panel. 
	 * @param oldPoint Old point in the panel
	 * @param newPoint New position of the old point instance
	 */
	public void updateFunctionPoint(Point oldPoint, Point newPoint) {

		Point ceil= functionPoints.ceiling(oldPoint);
		Point dragPoint = ceil;
		if(ceil.x!= oldPoint.x){
			Point low= functionPoints.lower(oldPoint);			
			dragPoint = (oldPoint.x -low.x < ceil.x - oldPoint.x)?low:ceil;
		}
		//begin and end x must not be altered
		if(dragPoint.equals(functionPoints.first())){
			newPoint.x = functionPoints.first().x;
		}
		if(dragPoint.equals(functionPoints.last())){
			newPoint.x = functionPoints.last().x;
		}
		functionPoints.remove(dragPoint);
		functionPoints.add(newPoint);

		fireEventAll();
	}

	/**
	 * @return the functionPoints
	 */
	public TreeSet<Point> getFunctionPoints() {
		return new TreeSet<Point>(functionPoints);
	}

	/**
	 * Function to add points to the transfer Function
	 * @param point Point to be added on the panel area
	 */
	public void addFunctionPoint(final Point point) {
		if(functionPoints.contains(point)){
			return;
		}
		functionPoints.add(point);

		fireEventAll();
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
	 * @return the maxOrdinates
	 */
	public Point getMaxOrdinates() {
		return maxOrdinates;
	}

	/**
	 * @param maxOrdinates the maxOrdinates to set
	 */
	public void setMaxOrdinates(Point maxOrdinates) {

		Point oldMax = new Point(this.maxOrdinates);

		this.maxOrdinates = maxOrdinates;

		rescale(oldMax);

		fireEventAll();
	}

	/**
	 * rescales the transfer function from 0 to max ordinate in each dim 
	 */
	private void rescale(Point oldMax){
		float [] scaleFactors = {(float)maxOrdinates.x/(float)oldMax.x, 
				(float)maxOrdinates.y/(float)oldMax.y };

		//scale color points
		TreeMap<Point, Color> newColorMap = new TreeMap<Point, Color>(pointOrderXOperator);
		for(Point position:colors.keySet()){
			int newX = (int)Math.round(scaleFactors[0]*position.getX());
			int newY = (int)Math.round(scaleFactors[1]*position.getY());
			Color color = colors.get(position);
			newColorMap.put(new Point(newX,newY),color);
		}
		colors.clear();
		colors.putAll(newColorMap);

		//scale function Points
		TreeSet<Point> newFunctionPoints = new TreeSet<Point>(pointOrderXOperator);
		for(Point functionPoint:functionPoints){
			int newX = (int)Math.round(scaleFactors[0]*functionPoint.getX());
			int newY = (int)Math.round(scaleFactors[1]*functionPoint.getY());
			newFunctionPoints.add(new Point(newX, newY));
		}
		functionPoints.clear();
		functionPoints.addAll(newFunctionPoints);
	}

	private Color getColorComponent(Point index){
		float [] result = {0,0,0};


		//get RGB
		Point nextIndex = colors.ceilingKey(index);
		if(nextIndex == null){
			return colors.lastEntry().getValue();
		}

		if(nextIndex == index){
			return colors.get(index);
		}
		Point previousIndex = colors.lowerKey(index);
		float colorDiff = nextIndex.x-previousIndex.x;
		float colorOffset = index.x - previousIndex.x;

		float[] colorPrev ={0,0,0};
		float[] colorNext ={0,0,0};

		colors.get(previousIndex).getColorComponents(colorPrev);
		colors.get(nextIndex).getColorComponents(colorNext);

		float []tmpColor= {0,0,0};
		VectorUtil.subVec3(tmpColor, colorNext, colorPrev);
		VectorUtil.scaleVec3(tmpColor,tmpColor,colorOffset/colorDiff);
		VectorUtil.addVec3(result, colorPrev,tmpColor );

		return new Color(result[0],result[1],result[2],0);
	}


	private float getNormalizedAlphaValue(int unNormalizedValue){
		float normFactor = 1f/ (float)getMaxOrdinates().y;

		float calculatedValue =  unNormalizedValue*normFactor;

		return Math.min(1, Math.max(calculatedValue, 0));

	}

	private float getAlpha (Point index){
		//get alpha
		Point nextIndex = functionPoints.ceiling(index);
		float nextAlpha = getNormalizedAlphaValue(nextIndex.y);

		if(nextIndex.x == index.x){
			return nextAlpha;
		}
		Point previousIndex = functionPoints.lower(index);
		float prevAlpha = getNormalizedAlphaValue(previousIndex.y);

		float colorDiff = nextIndex.x-previousIndex.x;
		float colorOffset = index.x - previousIndex.x;

		float m = (nextAlpha - prevAlpha)/colorDiff;
		return m* colorOffset + prevAlpha;

	} 

	private Color getColorForXOrdinateInObjectTransferSpace(Point index){

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
		TreeMap<Integer, Color> returnColors = new TreeMap<Integer, Color>();


		Color currentColor = null;
		//get colors from gradient
		for(Point index : colors.keySet()){

			if(returnColors.containsKey(index.x)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}

			returnColors.put(index.x, currentColor);
		}

		//get colors from line
		for(Point index : functionPoints){

			if(returnColors.containsKey(index.x)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}
			returnColors.put(index.x, currentColor);
		}

		return returnColors;
	}

	/**
	 * updates a color point
	 * @param oldPoint
	 * @param newPoint
	 */
	public void moveColor(Point oldPoint, Point newPoint) {
		Color color = colors.get(oldPoint);
		if(color == null){
			return;
		}

		colors.remove(oldPoint);
		colors.put(newPoint, color);
		fireEventAll();
	}

	public FloatBuffer getTexture(){
		return sampler.sample(this, 1);
	}
	
	public IFunction getTransferFunctionShaderCode(){
		return sampler.getShaderCode();
	}
}
