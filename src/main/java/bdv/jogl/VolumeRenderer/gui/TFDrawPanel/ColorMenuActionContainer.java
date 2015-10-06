package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import static bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D.calculateTransferFunctionPoint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

public class ColorMenuActionContainer {

	private final JComponent parent;
	
	private final TransferFunction1D tf; 
	
	private Point interactionPoint;
	
	private final Action insertAction = new AbstractAction("Insert color") {

		/**
		 * default version
		 */
		private static final long serialVersionUID = 1L;
		 

		@Override
		public void actionPerformed(ActionEvent e) {
			Color color = JColorChooser.showDialog(new JFrame(), "color dialog", Color.black);

			//nothing choosen
			if(color == null){
				return;
			}
			
			Dimension winSize = parent.getSize();
			Point2D.Float colorPoint = calculateTransferFunctionPoint(new Point(interactionPoint.x,0), tf, winSize);
			tf.setColor(colorPoint, color);
		}
	};
	
	private final Action resetAction = new AbstractAction("Reset colors") {

		/**
		 * default version
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			tf.resetColors();
		}
	};

	/**
	 * @return the interactionPoint
	 */
	public Point getInteractionPoint() {
		return interactionPoint;
	}

	/**
	 * @param interactionPoint the interactionPoint to set
	 */
	public void setInteractionPoint(Point interactionPoint) {
		this.interactionPoint = interactionPoint;
	}

	public ColorMenuActionContainer(JComponent parent, TransferFunction1D tf) {
		super();
		this.parent = parent;
		this.tf = tf;
	}

	/**
	 * @return the insertAction
	 */
	public Action getInsertAction() {
		return insertAction;
	}

	/**
	 * @return the resetAction
	 */
	public Action getResetAction() {
		return resetAction;
	}
	
}
