package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;







import static bdv.jogl.VolumeRenderer.utils.WindowUtils.*;

/**
 * Represents the context menu of the transfer function panel and delivers standard interactions
 * @author michael
 *
 */
public class TransferFunctionContexMenu extends JPopupMenu{

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

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Point functionPoint = transformWindowNormalSpace(colorPickPoint,parent.getSize());
				parent.getTransferFunction().addFunctionPoint(functionPoint);

			}
		});
		add(new AbstractAction("Insert color") {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(new JFrame(), "color dialog", Color.black);

				//nothing choosen
				if(color == null){
					return;
				}

				parent.getTransferFunction().setColor(new Point(colorPickPoint.x,0), color);
			}
		});

		add(new AbstractAction("Reset Points") {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getTransferFunction().resetLine();
			}
		});

		add(new AbstractAction("Reset colors") {

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
