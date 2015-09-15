package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getColorOfVolume;
public class VolumeLegend extends JPanel {

	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;

	private final VolumeDataManager dataManager;
	
	private final Map<Integer,JCheckBox> idCheckboxMap = new HashMap<Integer, JCheckBox>(); 
	
	public VolumeLegend(final VolumeDataManager m){
		this.dataManager = m;
		initLegend();
		initListener();
	}
	
	private void initListener() {
		dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
			
			@Override
			public void addedData(Integer id) {
				updateLegend(id);
				repaint();
			}
		});
	}

	private void initLegend() {
		setBorder(BorderFactory.createTitledBorder("Volume data Legend"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	private void updateLegend(final Integer id) {
			if(idCheckboxMap.containsKey(id)){
				return;
			}

			Color volumeColor = getColorOfVolume(id);
			final JCheckBox tmp = new JCheckBox("Volume: "+id);
			tmp.setForeground(volumeColor);
			tmp.setSelected(true);
			tmp.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
				 	dataManager.enableVolume(id, tmp.isSelected());
					
				}
			});
			idCheckboxMap.put(id, tmp);
			add(tmp);
	
	}
}
