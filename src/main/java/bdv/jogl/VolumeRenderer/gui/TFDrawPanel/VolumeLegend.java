package bdv.jogl.VolumeRenderer.gui.TFDrawPanel;

import java.awt.Color;
import java.awt.Graphics;

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
	
	public VolumeLegend(final VolumeDataManager m){
		this.dataManager = m;
		initLegend();
		initListener();
		updateLegend();
	}
	
	private void initListener() {
		dataManager.addVolumeDataManagerListener(new IVolumeDataManagerListener() {
			
			@Override
			public void updatedData() {
				updateLegend();
				repaint();
			}
		});
	}

	private void initLegend() {
		setBorder(BorderFactory.createTitledBorder("Volume data Legend"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	private void updateLegend() {
		removeAll();
		for(Integer index : dataManager.getVolumeKeys()){
			Color volumeColor = getColorOfVolume(index);
			JLabel tmp = new JLabel("Volume: "+index);
			tmp.setForeground(volumeColor);
			add(tmp);
		}
	}
}
