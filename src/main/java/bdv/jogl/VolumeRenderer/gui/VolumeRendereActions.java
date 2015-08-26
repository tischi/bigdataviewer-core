package bdv.jogl.VolumeRenderer.gui;

import java.awt.event.ActionEvent;

import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.util.AbstractNamedAction;

/**
 * Defines all menu actions
 * @author michael
 *
 */
public class VolumeRendereActions {

	public static class OpenVolumeRendererAction extends AbstractNamedAction{

		private GLWindow window3D = null;
		
		private final BigDataViewer bdv;
		
		public OpenVolumeRendererAction(final String name, final BigDataViewer bdv ) {
			super(name);
			this.bdv = bdv;
			
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(window3D != null){
				window3D.dispose();
				window3D = null;
			}
			window3D = new GLWindow();
			window3D.setBigDataViewer(bdv);
			window3D.setVisible(true);
			
		}
	}
	
}
