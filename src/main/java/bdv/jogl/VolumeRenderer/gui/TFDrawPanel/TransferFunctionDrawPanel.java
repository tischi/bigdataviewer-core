package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.Axis.AxisType;
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

	private final GridBagLayout layout = new GridBagLayout(); 
	
	private final Axis yTauAxis = new Axis("y",AxisType.YAXIS);
	
	private final Axis yDistributionAxis = new Axis("y2", AxisType.YAXIS);
	
	private final Axis xAxis = new Axis("x",AxisType.XAXIS);
	
	private final VolumeDataManager dataManager;
	
	private JCheckBox logarithmicOccuranceCheck = new JCheckBox("Logarithmic distribution");
	
	/**
	 * Constructor to create the render image
	 * @param tf
	 * @param dataManager
	 */
	public TransferFunctionDrawPanel(final TransferFunction1D tf, final VolumeDataManager dataManager){
		this.dataManager = dataManager;
		logarithmicOccuranceCheck.setSelected(true);
		
		renderPanel = new TransferFunctionRenderPanel1D(tf, dataManager);
		renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());
		initUI();
		initListener();
	}
	
	private void initUI(){
		setLayout(layout);
		//render area + axis
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx =0;
		c.gridy = 0;
		add(yTauAxis,c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx =2;
		c.gridy = 0;
		add(yDistributionAxis,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx =1;
		c.gridy = 1;
		add(xAxis,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx =1;
		c.gridy = 0;
		add(renderPanel,c);
		
		//controls
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx =0;
		c.gridy = 2;
		c.gridwidth = 3;
		add(logarithmicOccuranceCheck,c);
	}

	@Override
	public void paint(Graphics g) {
		//update xAxis TODO
		xAxis.setMax(dataManager.getGlobalMaxVolumeValue());
		yDistributionAxis.setMax(dataManager.getGlobalMaxOccurance());
		super.paint(g);
	};
	private void initListener(){
		logarithmicOccuranceCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());			
				
			}
		});
	}
}
