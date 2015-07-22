package bdv.jogl.VolumeRenderer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.jogamp.opengl.math.VectorUtil;

/**
 * Transfer function interaction similar to paraview
 * @author michael
 *
 */
public class TransferFunctionPanel1D extends JPanel {
	
	private JPopupMenu contextMenue = new JPopupMenu();
	
	private List<TransferFunctionListener> transferFunctionListeners = new ArrayList<TransferFunctionListener>();

	private TreeMap<Integer,Color> colorMap = new TreeMap<Integer, Color>();

	private TreeMap<Integer, Integer> points = new TreeMap<Integer, Integer>();

	private final Point minPoint = new Point(0, 0);

	private final Point maxPoint = new Point(100,100);

	private final int pointRadius = 10;
	
	private int dragIndex = -1;
	
	private Point colorPickPoint = new Point();
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

	private void createMenu(){
		final TransferFunctionPanel1D me=this;
		contextMenue.add(new AbstractAction("Insert color") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(new JFrame(), "color dialog", Color.black);
				
				//nothing choosen
				if(color == null){
					return;
				}
		
				Point position = getPositionInTransferFunctionSpace(colorPickPoint);
				colorMap.put(position.x, color);
				me.repaint();
				me.fireEventAll();
			}
		});
	}
	
	private void addControls(){
		final TransferFunctionPanel1D me  =  this;
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(dragIndex<0 && e.getButton() != MouseEvent.BUTTON1){
					return;
				}
				Point position = getPositionInTransferFunctionSpace(e.getPoint());
				e.consume();
				
				points.put(dragIndex, Math.min(Math.max(position.y,minPoint.y),maxPoint.y));
				
				fireEventAll();
				repaint();
				
			}
		});
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() != MouseEvent.BUTTON1){
					return;
				}
				dragIndex = -1;
				e.consume();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				//drag point
				if(e.getClickCount() == 1&&e.getButton() == MouseEvent.BUTTON1){
					Point position = getPositionInTransferFunctionSpace(e.getPoint());
					e.consume();
					
					dragIndex = position.x;
					
					int ceil= points.ceilingKey(position.x);
					if(ceil == position.x){
						return;
					}
					int low= points.lowerKey(position.x);
					
				
					
					dragIndex = (position.x -low < ceil - position.x)?low:ceil;
				}
				

			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
				//insert new point
				if(e.getClickCount() ==2 && e.getButton() == MouseEvent.BUTTON1){
					Point position = getPositionInTransferFunctionSpace(e.getPoint());
					e.consume();
					if(points.containsKey(position.x)){
						return;
					}
					points.put(position.x, position.y);
					
					
					fireEventAll();
					repaint();
				}
				
				//context menu
				if(e.getButton() == MouseEvent.BUTTON3){
					colorPickPoint = new Point(e.getPoint());
					contextMenue.show(me, e.getX(), e.getY());
				}
				
				
			}
		});
	}
	
	private void fireEventAll(){
		
		for(TransferFunctionListener listener:transferFunctionListeners){
			fireEvent(listener);
		}
	}
	
	private Point getPositionInTransferFunctionSpace(final Point pointInImageSpace){
		float[] convertFactors = {(float)(maxPoint.x-minPoint.x)/(float)(getWidth()), 
				(float)(maxPoint.y-minPoint.y) /(float)(getHeight())};
		
		return new Point(Math.round(convertFactors[0]* (float)pointInImageSpace.x+(float)minPoint.x),
				Math.round(convertFactors[1]* (float)(getHeight()-pointInImageSpace.y)+(float)minPoint.y));
		
	}
	
	/**
	 * constructor
	 */
	public TransferFunctionPanel1D(){

		initStdHistValues();
		
		createMenu();
		
		addControls();
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
	
		float []tmpColor= {0,0,0};
		VectorUtil.subVec3(tmpColor, colorNext, colorPrev);
		VectorUtil.scaleVec3(tmpColor,tmpColor,colorOffset/colorDiff);
		VectorUtil.addVec3(result, colorPrev,tmpColor );
		
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
