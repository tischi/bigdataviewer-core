package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.TreeMap;

import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunctionAdapter;

public class ColorSliderPanel extends JPanel {

	/**
	 * default version id 
	 */
	private static final long serialVersionUID = 1L;
	
	private TransferFunction1D transferFunction;
	
	private final TransferFunctionColorPositionInteractor interactor;
	
	public ColorSliderPanel(TransferFunction1D tf){
		setTransferFuntion(tf);
		interactor = new TransferFunctionColorPositionInteractor(this);
		
		addMouseListener(interactor.getMouseListener());
		addMouseMotionListener(interactor.getMouseMotionListener());
		setPreferredSize(new Dimension(getPreferredSize().width, 20));
	}
	
	private void drawColorRepresentants(Graphics2D g){
		TreeMap<Point2D.Float, Color> colors = transferFunction.getColors();
		
		float colorPosNormalizeColor =  (float)getWidth() / colors.lastKey().x;
		
		//draw color representant (except for first and last color since those positions should not be altered)
		for(Point2D.Float pos : colors.keySet()){
			if(pos.equals(colors.firstKey()) || pos.equals(colors.lastKey())){
				continue;
			}
			
			//single color position
			int xpos = Math.round(pos.x * colorPosNormalizeColor);
			g.drawLine(xpos, getHeight(), xpos, 0);
			
		}
		
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		drawColorRepresentants(g2d);
		
	}

	/**
	 * @return the transferFuntion
	 */
	public TransferFunction1D getTransferFunction() {
		return transferFunction;
	}

	/**
	 * @param transferFuntion the transferFuntion to set
	 */
	public void setTransferFuntion(TransferFunction1D transferFuntion) {
		this.transferFunction = transferFuntion;
		transferFuntion.addTransferFunctionListener(new TransferFunctionAdapter() {
			@Override
			public void colorChanged(TransferFunction1D transferFunction) {
				repaint();
			}
		});
	}
}
