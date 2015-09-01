package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.jogamp.opengl.math.VectorUtil;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.ITransferFunctionSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;

/**
 * store data for 1D Transfer functions
 * @author michael
 *
 */
public class TransferFunction1D {

	private List<TransferFunctionListener> transferFunctionListeners = new ArrayList<TransferFunctionListener>();

	private ITransferFunctionSampler sampler; 

	//order points first by x then by y
	private final Comparator<Point2D.Float> pointOrderXOperator = new Comparator<Point2D.Float>() {

		@Override
		public int compare(Point2D.Float a, Point2D.Float b) {
			//same x
			if(a.x == b.x){
				return (int)Math.signum(a.y-b.y);
			}
			return (int)Math.signum(a.x-b.x);
		}
	};

	private Point2D.Float maxOrdinates;

	private final Point2D.Float minOrdinates = new Point2D.Float(0,0);

	private final TreeMap<Point2D.Float,Color> colors = new TreeMap<Point2D.Float, Color>(pointOrderXOperator);

	private final TreeSet<Point2D.Float> functionPoints = new TreeSet<Point2D.Float>(pointOrderXOperator);;

	private void fireSamplerChangedEventAll(){
		for(TransferFunctionListener listener: transferFunctionListeners){
			fireSamplerChangedEvent(listener);
		}
	}

	private void fireSamplerChangedEvent(final TransferFunctionListener l){
		l.samplerChanged(this);
	}

	/**
	 * Calls all event methods on listener using the current data state
	 * @param listener
	 */
	private void fireColorChangedEvent(final TransferFunctionListener listener){

		listener.colorChanged(this);
	}

	private void fireColorChangedEventAll(){

		for(TransferFunctionListener listener:transferFunctionListeners){
			fireColorChangedEvent(listener);
		}
	}

	public void resetLine(){
		functionPoints.clear();
		functionPoints.add(new Point2D.Float(minOrdinates.x,minOrdinates.y));
		functionPoints.add(new Point2D.Float(maxOrdinates.x, maxOrdinates.y));	

		fireColorChangedEventAll();
	}

	public void resetColors(){
		colors.clear();
		colors.put(new Point2D.Float(minOrdinates.x, minOrdinates.y), Color.BLUE);
		colors.put(new Point2D.Float(maxOrdinates.x/2,minOrdinates.y), Color.WHITE);
		colors.put(new Point2D.Float(maxOrdinates.x,minOrdinates.y),Color.RED);

		fireColorChangedEventAll();
	}


	private void init( Point2D.Float maxOrdinates){
		this.maxOrdinates = maxOrdinates;


		resetColors();
		resetLine();
		setSampler( new PreIntegrationSampler());
	}

	public TransferFunction1D(float maxVolume,  float maxTau) {
		Double maxV = Math.ceil(maxVolume);
		Double maxT = Math.ceil(maxTau);
		init(new Point2D.Float(maxV.intValue(),maxT.intValue()));
	}

	public TransferFunction1D(){
		init(new Point2D.Float(256,1.f));
	}


	/**
	 * @return the colors
	 */
	public TreeMap<Point2D.Float, Color> getColors() {
		return new TreeMap<Point2D.Float, Color>(colors);
	}

	/**
	 * Sets the color of a certain point in the panel.
	 * @param point Position on the panel area.
	 * @param color Color to be set.
	 */
	public void setColor(final Point2D.Float point, final Color color){
		colors.put(point, color);

		fireColorChangedEventAll();
	}

	/**
	 * Updates points in the transfer function panel. 
	 * @param oldPoint Old point in the panel
	 * @param newPoint New position of the old point instance
	 */
	public void updateFunctionPoint(Point2D.Float oldPoint, Point2D.Float newPoint) {

		Point2D.Float ceil= functionPoints.ceiling(oldPoint);
		Point2D.Float dragPoint = ceil;
		if(ceil.x!= oldPoint.x){
			Point2D.Float low= functionPoints.lower(oldPoint);			
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

		fireColorChangedEventAll();
	}

	/**
	 * @return the functionPoints
	 */
	public TreeSet<Point2D.Float> getFunctionPoints() {
		return new TreeSet<Point2D.Float>(functionPoints);
	}

	/**
	 * Function to add points to the transfer Function
	 * @param point Point to be added on the panel area
	 */
	public void addFunctionPoint(final Point2D.Float point) {
		if(functionPoints.contains(point)){
			return;
		}
		functionPoints.add(point);

		fireColorChangedEventAll();
	}

	/**
	 * Adds a listener to the transfer function panel
	 * @param listener
	 */
	public void addTransferFunctionListener(final TransferFunctionListener listener){
		transferFunctionListeners.add(listener);

		fireColorChangedEvent(listener);
	}


	/**
	 * @return the maxOrdinates
	 */
	public Point2D.Float getMaxOrdinates() {
		return maxOrdinates;
	}

	/**
	 * @param maxOrdinates the maxOrdinates to set
	 */
	public void setMaxOrdinates(Point2D.Float maxOrdinates) {

		Point2D.Float oldMax = new Point2D.Float(this.maxOrdinates.x,this.maxOrdinates.y);

		this.maxOrdinates = maxOrdinates;

		rescale(oldMax);

		fireColorChangedEventAll();
	}

	/**
	 * rescales the transfer function from 0 to max ordinate in each dim 
	 */
	private void rescale(Point2D.Float oldMax){
		float [] scaleFactors = {(float)maxOrdinates.x/(float)oldMax.x, 
				(float)maxOrdinates.y/(float)oldMax.y };

		//scale color points
		TreeMap<Point2D.Float, Color> newColorMap = new TreeMap<Point2D.Float, Color>(pointOrderXOperator);
		for(Point2D.Float position:colors.keySet()){
			int newX = (int)Math.round(scaleFactors[0]*position.getX());
			int newY = (int)Math.round(scaleFactors[1]*position.getY());
			Color color = colors.get(position);
			newColorMap.put(new Point2D.Float(newX,newY),color);
		}
		colors.clear();
		colors.putAll(newColorMap);

		//scale function Points
		TreeSet<Point2D.Float> newFunctionPoints = new TreeSet<Point2D.Float>(pointOrderXOperator);
		for(Point2D.Float functionPoint:functionPoints){
			int newX = (int)Math.round(scaleFactors[0]*functionPoint.getX());
			int newY = (int)Math.round(scaleFactors[1]*functionPoint.getY());
			newFunctionPoints.add(new Point2D.Float(newX, newY));
		}
		functionPoints.clear();
		functionPoints.addAll(newFunctionPoints);
	}

	private Color getColorComponent(Point2D.Float index){
		float [] result = {0,0,0};


		//get RGB
		Point2D.Float nextIndex = colors.ceilingKey(index);
		if(nextIndex == null){
			return colors.lastEntry().getValue();
		}

		if(nextIndex == index){
			return colors.get(index);
		}
		Point2D.Float previousIndex = colors.lowerKey(index);
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


	private float getNormalizedAlphaValue(float unNormalizedValue){
		float normFactor = 1f/ (float)getMaxOrdinates().y;

		float calculatedValue =  unNormalizedValue*normFactor;

		return Math.min(1, Math.max(calculatedValue, 0));

	}

	private float getAlpha (Point2D.Float index){
		//get alpha
		Point2D.Float nextIndex = functionPoints.ceiling(index);
		float nextAlpha = getNormalizedAlphaValue(nextIndex.y);

		if(nextIndex.x == index.x){
			return nextAlpha;
		}
		Point2D.Float previousIndex = functionPoints.lower(index);
		float prevAlpha = getNormalizedAlphaValue(previousIndex.y);

		float colorDiff = nextIndex.x-previousIndex.x;
		float colorOffset = index.x - previousIndex.x;

		float m = (nextAlpha - prevAlpha)/colorDiff;
		return m* colorOffset + prevAlpha;

	} 

	private Color getColorForXOrdinateInObjectTransferSpace(Point2D.Float index){

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
		for(Point2D.Float index : colors.keySet()){

			if(returnColors.containsKey((int)index.x)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}

			//TODO
			returnColors.put((int)index.x, currentColor);
		}

		//get colors from line
		for(Point2D.Float index : functionPoints){

			if(returnColors.containsKey((int)index.x)){
				continue;
			}

			currentColor = getColorForXOrdinateInObjectTransferSpace(index);
			if(currentColor == null){
				continue;
			}

			//TODO
			returnColors.put((int)index.x, currentColor);
		}

		return returnColors;
	}

	/**
	 * updates a color point
	 * @param oldPoint
	 * @param newPoint
	 */
	public void moveColor(Point2D.Float oldPoint, Point2D.Float newPoint) {
		Color color = colors.get(oldPoint);
		if(color == null){
			return;
		}

		colors.remove(oldPoint);
		colors.put(newPoint, color);
		fireColorChangedEventAll();
	}

	public FloatBuffer getTexture(){
		return sampler.sample(this, 1);
	}

	public IFunction getTransferFunctionShaderCode(){
		return sampler.getShaderCode();
	}

	/**
	 * @return the sampler
	 */
	public ITransferFunctionSampler getSampler() {
		return sampler;
	}

	/**
	 * @param sampler the sampler to set
	 */
	public void setSampler(ITransferFunctionSampler sampler) {
		this.sampler = sampler;
		fireSamplerChangedEventAll();
	}

	/**
	 * Calculates the draw point of a transfer function  coordinate in window space
	 * @param transferFunctionPoint
	 * @param transferFunction
	 * @param drawAreaSize
	 * @return
	 */
	public static Point calculateDrawPoint(Point2D.Float transferFunctionPoint, 
			TransferFunction1D transferFunction,
			Dimension drawAreaSize){
		float xyScale[] = {(float)drawAreaSize.getWidth()/ transferFunction.getMaxOrdinates().x,
				(float)drawAreaSize.getHeight()/transferFunction.getMaxOrdinates().y};
		Point drawPoint = new Point((int)Math.round(transferFunctionPoint.getX() * xyScale[0]),
				(int)Math.round(transferFunctionPoint.getY() * xyScale[1]));
		return drawPoint;
	}

	/**
	 * Calculates the transfer function point from a given window space point 
	 * @param windowSpacePoint
	 * @param transferFunction
	 * @param drawAreaSize
	 * @return
	 */
	public static Point2D.Float calculateTransferFunctionPoint(Point windowSpacePoint, 
			TransferFunction1D transferFunction,
			Dimension drawAreaSize){
		float xyScale[] = {transferFunction.getMaxOrdinates().x/(float)drawAreaSize.getWidth(),
				transferFunction.getMaxOrdinates().y/(float)drawAreaSize.getHeight()};
		Point2D.Float functionPoint = new Point2D.Float((float)windowSpacePoint.getX() * xyScale[0],
				(float)windowSpacePoint.getY() * xyScale[1]);
		return functionPoint;
	}
}
