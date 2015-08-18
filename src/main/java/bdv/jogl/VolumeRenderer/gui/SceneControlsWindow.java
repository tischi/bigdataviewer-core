package bdv.jogl.VolumeRenderer.gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.ShaderPrograms.SimpleVolumeRenderer;;

/**
 * Class for providing tf scene controls
 * @author michael
 *
 */
public class SceneControlsWindow extends JFrame {
	private TransferFunctionPanel1D tfpanel = null;

	public SceneControlsWindow(final TransferFunction1D tf){
		createTFWindow(tf);
	}
	
	private void createTFWindow(final TransferFunction1D tf){
		tfpanel = new TransferFunctionPanel1D(tf);
	


		setTitle("Transfer function configurations");
		setSize(640, 100);
		JPanel mainPanel  = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(tfpanel);
		mainPanel.add(new TransferFunctionDataPanel());
		getContentPane().add(mainPanel);
		pack();
		setVisible(true);
	}

	public void destroyTFWindow() {
		dispose();
		tfpanel = null;
	}
}
