package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static bdv.jogl.VolumeRenderer.utils.WindowUtils.transformWindowNormalSpace;

import javax.swing.JPopupMenu;

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

	private final TransferFunctionRenderPanel1D parent;

	private Point colorPickPoint = null;
	
	private ColorMenuActionContainer colorActions;
	
	private final MouseListener mouseListenrer = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			//context menu
			if(e.getButton() != MouseEvent.BUTTON3){
				return;
			}
			colorPickPoint = new Point(transformWindowNormalSpace( e.getPoint(),parent.getSize()));
			colorActions.setInteractionPoint(colorPickPoint);
			show(parent, e.getX(), e.getY());
			e.consume();

		}
	};
	
	
	private void initActions(){
		
		add(colorActions.getInsertAction());
		
		add(colorActions.getSetColorAction());
		
		add(colorActions.getDeleteAction());
		
		add(colorActions.getResetAction());
	}

	public TransferFunctionContexMenu(final TransferFunctionRenderPanel1D parent){
		this.parent = parent;
		this.colorActions = new ColorMenuActionContainer(parent, parent.getTransferFunction()); 
		initActions();
	}
	
	public MouseListener getMouseListener(){
		return mouseListenrer;
	}
}
