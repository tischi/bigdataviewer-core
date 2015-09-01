package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import static bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D.calculateTransferFunctionPoint;






import static bdv.jogl.VolumeRenderer.utils.WindowUtils.*;

/**
 * Represents the context menu of the transfer function panel and delivers standard interactions
 * @author michael
 *
 */
public class TransferFunctionContexMenu extends JPopupMenu{

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private final TransferFunctionPanel1D parent;

	private Point colorPickPoint = null;
	
	private final MouseListener mouseListenrer = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			//context menu
			if(e.getButton() != MouseEvent.BUTTON3){
				return;
			}
			colorPickPoint = new Point(e.getPoint());
			show(parent, e.getX(), e.getY());
			e.consume();

		}
	};
	
	
	private void initActions(){
		add(new AbstractAction("Insert function point") {

			/**
			 * default version
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				TransferFunction1D tf = parent.getTransferFunction();
				Dimension winSize = parent.getSize(); 
				Point windowPoint = transformWindowNormalSpace(colorPickPoint,parent.getSize());
				Point2D.Float functionPoint = calculateTransferFunctionPoint(windowPoint, tf, winSize);
				tf.addFunctionPoint(functionPoint);

			}
		});
		add(new AbstractAction("Insert color") {

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
				TransferFunction1D tf = parent.getTransferFunction();
				Dimension winSize = parent.getSize();
				Point2D.Float colorPoint = calculateTransferFunctionPoint(new Point(colorPickPoint.x,0), tf, winSize);
				tf.setColor(colorPoint, color);
			}
		});

		add(new AbstractAction("Reset Points") {

			/**
			 * default version
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getTransferFunction().resetLine();
			}
		});

		add(new AbstractAction("Reset colors") {

			/**
			 * default version
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getTransferFunction().resetColors();
			}
		});
	}

	public TransferFunctionContexMenu(final TransferFunctionPanel1D parent){
		this.parent = parent;

		initActions();
	}
	
	public MouseListener getMouseListener(){
		return mouseListenrer;
	}
}
