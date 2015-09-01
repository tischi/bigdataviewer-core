package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunctionAdapter;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.*;
import static bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D.calculateDrawPoint;

/**
 * Transfer function interaction similar to paraview
 * @author michael
 *
 */
public class TransferFunctionPanel1D extends JPanel {

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private final TransferFunctionContexMenu contextMenue = new TransferFunctionContexMenu(this);

	private final TransferFunctionPointInteractor pointInteractor = new TransferFunctionPointInteractor(this);

	private TransferFunction1D transferFunction;
	
	private final int pointRadius = 10;

	private void addControls(){
		addMouseListener(contextMenue.getMouseListener());

		addMouseMotionListener(pointInteractor.getMouseMotionListener());

		addMouseListener(pointInteractor.getMouseListener());
	}
	
	/**
	 * constructor
	 */
	public TransferFunctionPanel1D(final TransferFunction1D tf){

		initWindow();
		setTransferFunction(tf);
		
		//resizeHandler = new TransferFunctionWindowResizeHandler(getSize(),transferFunction);

		addControls();
	}

	private void initWindow() {
		setSize(640, 100);
		setPreferredSize(getSize());
		setMinimumSize(getSize());
		setMaximumSize(getSize());
	}

	
	/**
	 * @return the transferFunction
	 */
	public TransferFunction1D getTransferFunction() {
		return transferFunction;
	}

	/**
	 * @param transferFunction the transferFunction to set
	 */
	public void setTransferFunction(TransferFunction1D transferFunction) {
		this.transferFunction = transferFunction;
		this.transferFunction.addTransferFunctionListener(new TransferFunctionAdapter() {
			
			@Override
			public void colorChanged(TransferFunction1D transferFunction) {
				repaint();
			}
		});
	}

	

	private void paintSkala(Graphics g){
		//paint gradient image
		//error check
		TreeMap<Point2D.Float, Color> colors = transferFunction.getColors();
		if(colors.size() < 2){
			return;
		}

		//get painter
		Graphics2D painter = (Graphics2D) g;


		Point2D.Float latestPoint = colors.firstKey();
		for(Point2D.Float currentPoint: colors.keySet()){
			//skip first iteration
			if(currentPoint.equals( latestPoint)){
				continue;
			}
			Point beginGradient = transformWindowNormalSpace(calculateDrawPoint(latestPoint,transferFunction,getSize()), getSize());
			Point endGradient = transformWindowNormalSpace(calculateDrawPoint(currentPoint,transferFunction,getSize()), getSize());
			
			//gradient
			GradientPaint gradient = new GradientPaint(
					beginGradient, colors.get(latestPoint),
					endGradient, colors.get(currentPoint));


			//draw gradient
			painter.setPaint(gradient);
			painter.fillRect(beginGradient.x, 0, 
					endGradient.x, getHeight());
			latestPoint = currentPoint;
		}
	}

	private void drawPointIcon(Graphics2D painter, final Point point){

		painter.setStroke(new BasicStroke(3));
		painter.drawOval(point.x-pointRadius, point.y-pointRadius, 
				pointRadius*2, pointRadius*2);
	}


	
	private void paintLines(Graphics g){
		TreeSet<Point2D.Float> functionPoints = transferFunction.getFunctionPoints();
		if(functionPoints.size() < 2){
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		//draw line and points
		Point2D.Float latestRenderedPoint = functionPoints.first();
		for( Point2D.Float currentPoint: functionPoints){
		

			if(!currentPoint.equals( latestRenderedPoint) ){
				Point a = transformWindowNormalSpace(calculateDrawPoint(latestRenderedPoint,transferFunction,getSize()), getSize());
				Point b = transformWindowNormalSpace(calculateDrawPoint(currentPoint,transferFunction,getSize()), getSize());
				
				//print line
				g2d.setStroke(new BasicStroke(5));
				g2d.drawLine(a.x, 
						a.y,
						b.x, 
						b.y);
			}
			latestRenderedPoint = currentPoint;
		}
	}

	private void paintPoints(Graphics g){
		//print points	
		Graphics2D g2d = (Graphics2D) g;	
		
		for( Point2D.Float currentPoint: transferFunction.getFunctionPoints()){
			
			//TODO
			if(currentPoint.equals(pointInteractor.getSelectedPoint())){
				g2d.setColor(Color.gray);
			}
			
			Point drawPoint = transformWindowNormalSpace(calculateDrawPoint(currentPoint,transferFunction,getSize()), getSize());
			drawPointIcon(g2d, drawPoint);
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
}
