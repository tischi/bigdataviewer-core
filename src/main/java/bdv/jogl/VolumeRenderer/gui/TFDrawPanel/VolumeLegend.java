package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bdv.jogl.VolumeRenderer.utils.IVolumeDataManagerListener;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getColorOfVolume;
public class VolumeLegend extends JPanel {

	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;

	private final VolumeDataManager dataManager;
	
	private final Map<Integer,JLabel> idLabelMap = new HashMap<Integer, JLabel>(); 
	
	public VolumeLegend(final VolumeDataManager m){
		this.dataManager = m;
		initLegend();
		initListener();
	}
	
	private void initListener() {
		dataManager.addVolumeDataManagerListener(new IVolumeDataManagerListener() {
			
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

	private void updateLegend(Integer id) {
			if(idLabelMap.containsKey(id)){
				return;
			}
			Color volumeColor = getColorOfVolume(id);
			JLabel tmp = new JLabel("Volume: "+id);
			tmp.setForeground(volumeColor);
			idLabelMap.put(id, tmp);
			add(tmp);
	
	}
}
