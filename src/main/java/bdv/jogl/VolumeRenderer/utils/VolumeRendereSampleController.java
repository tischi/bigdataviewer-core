package bdv.jogl.VolumeRenderer.utils;

import javax.swing.JSpinner;

import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;

/**
 * 
 * @author michael
 *
 */
public class VolumeRendereSampleController {
	
	private int lowSamples = 64;
	
	private final JSpinner sampleSpinner;
	
	private final MultiVolumeRenderer volumeRenderer;
	
	private final GLWindow window;

	private int sampleRequests = 0;

	public VolumeRendereSampleController(
			final GLWindow window,
			final JSpinner sampleSpinner,
			final MultiVolumeRenderer volumeRenderer,
			int lowSampleSize) {
		super();
		this.sampleSpinner = sampleSpinner;
		this.volumeRenderer = volumeRenderer;
		this.window = window;
		this.lowSamples = lowSampleSize;
	}
	
	public synchronized void upSample(){
		sampleRequests--;
		
		if(sampleRequests == 0){
	
			int chosenSamples = ((Number)sampleSpinner.getValue()).intValue();
			volumeRenderer.setSamples(chosenSamples);
			window.getGlCanvas().repaint();
		}
	}
	
	public synchronized void downSample(){
		volumeRenderer.setSamples(lowSamples);
		sampleRequests++;
	}

	/**
	 * @return the lowSamples
	 */
	public int getLowSamples() {
		return lowSamples;
	}

	/**
	 * @param lowSamples the lowSamples to set
	 */
	public void setLowSamples(int lowSamples) {
		this.lowSamples = lowSamples;
	}
	
}
