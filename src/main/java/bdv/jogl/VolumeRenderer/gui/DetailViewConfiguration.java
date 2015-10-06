package bdv.jogl.VolumeRenderer.gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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

	
	public DetailViewConfiguration(){
		
		initUI();
		
	}

	private JPanel createSliderPanel(final String name, final JSpinner value){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.add(new JLabel(name));
		panel.add(value);
		
		return (JPanel)aligneLeft(panel);
	} 
	
	private void initUI() {
		this.setBorder(BorderFactory.createTitledBorder("Detail View configuration"));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(createSliderPanel("Width: ", widthSpinner));
		add(createSliderPanel("Heigth: ", heightSpinner));
		add(createSliderPanel("depth: ", depthSpinner));
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

}
