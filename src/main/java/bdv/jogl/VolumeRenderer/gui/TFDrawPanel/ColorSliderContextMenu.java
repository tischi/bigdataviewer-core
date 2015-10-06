package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;


import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;


public class ColorSliderContextMenu extends JPopupMenu {

	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;
	
	private final ColorSliderPanel parent;

	private Point colorPickPoint = null;
	
	private ColorMenuActionContainer colorActions;
	
	private final MouseListener mouseListenrer = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			//context menu
			if(e.getButton() != MouseEvent.BUTTON3){
				return;
			}
			colorPickPoint = new Point(e.getPoint());
			colorActions.setInteractionPoint(colorPickPoint);
			show(parent, e.getX(), e.getY());
			e.consume();

		}
	};
	
	
	private void initActions(){
		
		add(colorActions.getInsertAction());

		add(colorActions.getResetAction());
	}

	public ColorSliderContextMenu(final ColorSliderPanel parent){
		this.parent = parent;
		this.colorActions = new ColorMenuActionContainer(parent, parent.getTransferFunction()); 
		initActions();
	}
	
	public MouseListener getMouseListener(){
		return mouseListenrer;
	}

}
