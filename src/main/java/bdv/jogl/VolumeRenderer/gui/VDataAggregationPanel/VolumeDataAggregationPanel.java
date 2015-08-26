package bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * configuration for volume renderer data aggregation
 * @author michael
 *
 */
public class VolumeDataAggregationPanel extends JPanel {
	
	private ButtonGroup aggregationGroup = new ButtonGroup();
	
	private List<JRadioButton> aggregationButtons = new LinkedList<JRadioButton>();
	
	private final AggregatorManager dataManager;
	
	private ActionListener radioListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dataManager.setActiveAcumulator(e.getActionCommand());
		}
	}; 
	
	private void initPanel(){
		setBorder(BorderFactory.createTitledBorder("Volume data Aggregation types"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//create buttons
		String selected = dataManager.getActiveAccumulator();
		for(String name: dataManager.getAccumulatorNames()){
			
			JRadioButton avg= new JRadioButton(name);
			avg.setActionCommand(name);
			if(name.equals(selected)){
				avg.setSelected(true);
			}
			avg.addActionListener(radioListener);
			aggregationButtons.add(avg);
		}
		
		//add buttons
		for(JRadioButton button: aggregationButtons){
			aggregationGroup.add(button);
			this.add(button);
		}
	}
	
	public VolumeDataAggregationPanel(final AggregatorManager manager){
		this.dataManager = manager;
		initPanel();
	}
}
