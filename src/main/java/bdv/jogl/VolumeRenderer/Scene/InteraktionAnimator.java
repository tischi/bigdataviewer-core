package bdv.jogl.VolumeRenderer.Scene;

import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;

public class InteraktionAnimator {
	
	private final MultiVolumeRenderer renderer;
	
	private final GLWindow renderWindow;
	
	private int currentAnimationPercentage =0;
	
	private final int percentageIncrement = 5;
	
	private Thread initAnimationThread = null;
	
	private Thread motionToTargetThread = null;
	
	/**
	 * Animation constructor
	 * @param renderer
	 * @param renderWindow
	 */
	public InteraktionAnimator(final MultiVolumeRenderer renderer, final GLWindow renderWindow){
		this.renderer = renderer;
		this.renderWindow = renderWindow;
	}
	
	/**
	 * stop init animation thread
	 */
	public void interruptInitAnimation(){
		if(initAnimationThread == null){
			return;
		}
		initAnimationThread.interrupt();
	};
	


	/**
	 * blending of 3D on 3D
	 */
	private void doInitAnimationStep(){
		
		renderer.setOpacity3D((float)(currentAnimationPercentage)/ 100f);
		renderWindow.getGlCanvas().repaint();

	}
	
	/**
	 * Starts the big volume in fading 
	 */
	public void startInitAnimation(){
		if(initAnimationThread!= null){
			if(initAnimationThread.isAlive()){
				return;
			}
			initAnimationThread = null;
		}
		initAnimationThread = new Thread(){
				public void run(){
					for(currentAnimationPercentage =0; currentAnimationPercentage< 100; currentAnimationPercentage+=percentageIncrement){
						doInitAnimationStep();
						try {
							sleep(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					
					//make 100% animation mode
						currentAnimationPercentage = 100;
						doInitAnimationStep();
						
				}
		};
		
		initAnimationThread.start();
	}
	
	
	/**
	 * stops all running animation threads 
	 */
	public void stopAllAnimations() {
		interruptInitAnimation();
		
	}
}
