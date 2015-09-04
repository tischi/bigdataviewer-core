package bdv.jogl.VolumeRenderer.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.RegularSampler;
import bdv.jogl.VolumeRenderer.gui.TFDataPanel.TransferFunctionDataPanel;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.TransferFunctionDrawPanel;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.AggregatorManager;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.VolumeDataAggregationPanel;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;

/**
 * Class for providing tf scene controls
 * @author michael
 *
 */
public class SceneControlsWindow extends JFrame {
	
	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private TransferFunctionDrawPanel tfpanel = null;
	
	private final JPanel mainPanel  = new JPanel();
	
	private TransferFunctionDataPanel tfDataPanel = null;
	
	private TransferFunction1D transferFunction;
	
	private VolumeDataAggregationPanel aggregationPanel;
	
	private JCheckBox usePreIntegration = new JCheckBox("Use pre-integration",true);
	
	private JCheckBox advancedCheck = new JCheckBox("Advanced configurations",false);
	
	
	public SceneControlsWindow(final TransferFunction1D tf,final AggregatorManager agm, final VolumeDataManager dataManager){
		transferFunction = tf;
		createTFWindow(tf,agm,dataManager);
	}
	
	private void createTFWindow(final TransferFunction1D tf,final AggregatorManager agm,final VolumeDataManager dataManager){
		tfpanel = new TransferFunctionDrawPanel(tf,dataManager);
		tfDataPanel = new TransferFunctionDataPanel(tf);
		aggregationPanel = new VolumeDataAggregationPanel(agm);

		

		setTitle("Transfer function configurations");
		setSize(640, 100);
		initAdvancedBox();
		initUsePreIntegration();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(tfpanel);
		mainPanel.add(advancedCheck);
		mainPanel.add(tfDataPanel);
		mainPanel.add(usePreIntegration);
		
		mainPanel.add(aggregationPanel);
		tfDataPanel.setVisible(advancedCheck.isSelected());
		
		getContentPane().add(mainPanel);
		pack();
		setVisible(true);
	}

	private void initUsePreIntegration() {
		usePreIntegration.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(usePreIntegration.isSelected()){
					transferFunction.setSampler(new PreIntegrationSampler());
				}else{
					transferFunction.setSampler(new RegularSampler());
				}
				
			}
		});
		
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
