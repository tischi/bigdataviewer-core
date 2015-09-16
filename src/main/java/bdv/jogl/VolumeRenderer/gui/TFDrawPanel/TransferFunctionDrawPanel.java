package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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

	private final GridBagLayout raaalayout = new GridBagLayout();
	
	private final BoxLayout mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	
	private final Axis yTauAxis = new Axis("Tau",AxisType.YAXIS);
	
	private final Axis yDistributionAxis = new Axis("Count", AxisType.YAXIS);
	
	private final Axis xAxis = new Axis("Volume values",AxisType.XAXIS);
	
	private final VolumeDataManager dataManager;
	
	private final JPanel renderAndAxisArea= new JPanel();
	
	private final JScrollPane scrollArea = new JScrollPane(renderAndAxisArea);
	
	private final VolumeLegend legend;
	
	private JCheckBox logarithmicOccuranceCheck = new JCheckBox("Logarithmic distribution");
	
	/**
	 * Constructor to create the render image
	 * @param tf
	 * @param dataManager
	 */
	public TransferFunctionDrawPanel(final TransferFunction1D tf, final VolumeDataManager dataManager){
		this.dataManager = dataManager;
		this.legend = new VolumeLegend(dataManager);
		logarithmicOccuranceCheck.setSelected(true);
		
		renderPanel = new TransferFunctionRenderPanel1D(tf, dataManager);
		renderPanel.setLogscaleDistribution(logarithmicOccuranceCheck.isSelected());
		initUI();
		initListener();
	}
	
	private void addComponent(JComponent c){
		c.setAlignmentX(LEFT_ALIGNMENT);
		add(c);
	}
	
	private void initUI(){
		setLayout(mainLayout);
		
		renderAndAxisArea.setLayout(raaalayout);
		
		yTauAxis.setLeftAxis(true);
		//render area + axis
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx =0;
		c.gridy = 0;
		renderAndAxisArea.add(yTauAxis,c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx =2;
		c.gridy = 0;
		renderAndAxisArea.add(yDistributionAxis,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx =1;
		c.gridy = 1;
		renderAndAxisArea.add(xAxis,c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx =1;
		c.gridy = 0;
		renderAndAxisArea.add(renderPanel,c);
		//renderAndAxisArea.setPreferredSize(new Dimension(2000,180));
		
		
		scrollArea.setPreferredSize(new Dimension(700,200));
		
		
		addComponent(scrollArea);
		addComponent(legend);
		
		
		//controls
		addComponent(logarithmicOccuranceCheck);
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
