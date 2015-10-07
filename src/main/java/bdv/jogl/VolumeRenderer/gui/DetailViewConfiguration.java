package bdv.jogl.VolumeRenderer.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import static bdv.jogl.VolumeRenderer.utils.WindowUtils.aligneLeft;

public class DetailViewConfiguration extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 1));  

	private final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 1));  

	private final JSpinner depthSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 1));  

	private final JButton resetButton = new JButton("Reset to full volume view"); 
	
	public DetailViewConfiguration(){
		
		initUI();
		
	}

	private void createSliderPanel(final String name, final JSpinner value,final GridBagConstraints c){
		
		value.setPreferredSize(value.getMinimumSize());
		value.setMaximumSize(value.getMinimumSize());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		add(aligneLeft( new JLabel(name)),c);
		c.gridx = 1;
	    add(aligneLeft(value),c);
	    c.gridy++;
	} 
	
	private void initUI() {
		this.setBorder(BorderFactory.createTitledBorder("Detail View configuration"));
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridy =0;
		createSliderPanel("Width: ", widthSpinner, c);
		createSliderPanel("Heigth: ", heightSpinner, c);
		createSliderPanel("depth: ", depthSpinner, c);
		c.gridx = 0;
		c.gridwidth = 2;
		add(aligneLeft(resetButton),c);
		this.setMaximumSize(getPreferredSize());
	}

	/**
	 * @return the widthSpinner
	 */
	public JSpinner getWidthSpinner() {
		return widthSpinner;
	}

	/**
	 * @return the heightSpinner
	 */
	public JSpinner getHeightSpinner() {
		return heightSpinner;
	}

	/**
	 * @return the depthSpinner
	 */
	public JSpinner getDepthSpinner() {
		return depthSpinner;
	}

	/**
	 * @return the reset button instance for defining listeners on it
	 */
	public JButton getResetButton(){
		return resetButton;
	}
}
