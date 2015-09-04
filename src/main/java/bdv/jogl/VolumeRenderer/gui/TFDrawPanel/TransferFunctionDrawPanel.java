package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;

/**
 * Frames the render panel and adds scales and options
 * @author michael
 *
 */
public class TransferFunctionDrawPanel extends JPanel {

	/**
	 *	default serial id 
	 */
	private static final long serialVersionUID = 1L;
	
	private final TransferFunctionRenderPanel1D renderPanel;
	
	private JCheckBox logarithmicOccuranceCheck = new JCheckBox("Logarithmic distribution");
	
	/**
	 * Constructor to create the render image
	 * @param tf
	 * @param dataManager
	 */
	public TransferFunctionDrawPanel(final TransferFunction1D tf, final VolumeDataManager dataManager){
		logarithmicOccuranceCheck.setSelected(true);
		
		renderPanel = new TransferFunctionRenderPanel1D(tf, dataManager);
		renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());
		initUI();
		initListener();
	}
	
	private void initUI(){
		add(renderPanel);
		add(logarithmicOccuranceCheck);
	}

	private void initListener(){
		logarithmicOccuranceCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());			
				
			}
		});
	}
}
