package bdv.jogl.VolumeRenderer.gui;

import java.awt.event.ActionEvent;

import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.util.AbstractNamedAction;

/**
 * Defines all menu actions
 * @author michael
 *
 */
public class VolumeRendereActions {

	public static class OpenVolumeRendererAction extends AbstractNamedAction{

		/**
		 * default version
		 */
		private static final long serialVersionUID = 1L;

		private final GLWindow window3D;
		
		private final SceneControlsWindow controls;
		
		public OpenVolumeRendererAction(final String name, final GLWindow win3d, SceneControlsWindow controls ) {
			super(name);
			window3D = win3d;
			this.controls = controls;
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			window3D.setVisible(true);
			controls.setVisible(true);
		}
	}
	
}
