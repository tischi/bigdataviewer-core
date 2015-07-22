package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 * Represents the context menu of the transfer function panel and delivers standard interactions
 * @author michael
 *
 */
public class TransferFunctionContexMenu extends JPopupMenu implements MouseListener {

	private final TransferFunctionPanel1D parent;

	private Point colorPickPoint = null;
	
	private void initActions(){
		add(new AbstractAction("Insert color") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(new JFrame(), "color dialog", Color.black);
				
				//nothing choosen
				if(color == null){
					return;
				}
		
				parent.setColor(colorPickPoint, color);
			}
		});
		
		add(new AbstractAction("Reset Points") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.resetLine();
			}
		});
		
		add(new AbstractAction("Reset colors") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.resetColors();
			}
		});
	}
	
	public TransferFunctionContexMenu(final TransferFunctionPanel1D parent){
		this.parent = parent;
		
		initActions();
	}

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

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
