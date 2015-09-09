package bdv.jogl.VolumeRenderer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.gui.VolumeRendereActions.OpenVolumeRendererAction;

/**
 * The context class of the bdv 3D volume extension
 * @author michael
 *
 */
public class VolumeRendererExtension {

	private final BigDataViewer bdv;
	
	private final GLWindow glWindow;
	
	private final VolumeDataScene dataScene;
	
	private final static String preferedMenuName = "Tools";

	private final static String actionName = "3D Volume";

	
	public VolumeRendererExtension(final BigDataViewer bdv){
		if(bdv == null){
			throw new NullPointerException("The extension needs a valid big data viewer instance");
		}
		
		this.bdv = bdv;
		
		dataScene = new VolumeDataScene(bdv);
		this.bdv.getViewer().addTransformListener(new SceneGlobalTransformationListener(dataScene));
		glWindow = new GLWindow(dataScene,bdv);
		createAndConnect3DView(this.bdv);
	}
	
	/**
	 * creates a GLWidget and connects it to the viewer 
	 * @param parent The BigDataViewer to connect to
	 * @return The new created GLWidget
	 */
	private void createAndConnect3DView(BigDataViewer parent){
		JMenuBar menuBar = bdv.getViewerFrame().getJMenuBar();


		JMenu preferedMenu = null;

		//find Tools menu
		for(int i = 0; i < menuBar.getMenuCount(); i++){
			JMenu currentMenu = menuBar.getMenu(i);
			if(currentMenu.getText().equals(preferedMenuName)){
				preferedMenu = currentMenu;
			}
		}

		//create if not exists
		if(preferedMenu == null){
			preferedMenu = new JMenu(preferedMenuName);
		}

		//add open action for 3D view
		this.bdv.getViewer().addRenderTransformListener(new TransformListener<AffineTransform3D>() {

			@Override
			public void transformChanged(AffineTransform3D transform) {

				glWindow.repaint();

			}
		});

		//close listener
		this.bdv.getViewerFrame().addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				glWindow.dispose();
			}
		});
		Action open3DViewAction = new OpenVolumeRendererAction(actionName, glWindow);
		preferedMenu.add(open3DViewAction);
		preferedMenu.updateUI();

	}
	
	/**
	 * clears the context
	 */
	public void delete(){
		glWindow.dispose();
	}
}
