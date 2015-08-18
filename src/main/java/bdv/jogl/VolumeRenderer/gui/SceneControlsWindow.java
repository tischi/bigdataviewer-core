package bdv.jogl.VolumeRenderer.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
	
	private final JPanel mainPanel  = new JPanel();
	
	private TransferFunctionDataPanel tfDataPanel = null;
	
	private JCheckBox advancedCheck = new JCheckBox("Advanced configurations",true);
	
	public SceneControlsWindow(final TransferFunction1D tf){
		createTFWindow(tf);
	}
	
	private void createTFWindow(final TransferFunction1D tf){
		tfpanel = new TransferFunctionPanel1D(tf);
	
		tfDataPanel = new TransferFunctionDataPanel(tf);


		setTitle("Transfer function configurations");
		setSize(640, 100);
		initAdvancedBox();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(tfpanel);
		mainPanel.add(advancedCheck);
		mainPanel.add(tfDataPanel);
		tfDataPanel.setVisible(advancedCheck.isSelected());

	
		
		getContentPane().add(mainPanel);
		pack();
		setVisible(true);
	}

	private void initAdvancedBox() {
		advancedCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				tfDataPanel.setVisible(advancedCheck.isSelected());
				pack();
				
			}
		});
		
	}

	public void destroyTFWindow() {
		dispose();
		tfpanel = null;
	}
}
