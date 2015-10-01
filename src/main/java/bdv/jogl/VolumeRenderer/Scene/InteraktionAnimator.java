package bdv.jogl.VolumeRenderer.Scene;

import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;

public class InteraktionAnimator {
	private final MultiVolumeRenderer renderer;
	private final GLWindow renderWindow;
	private int currentAnimationPercentage =0;
	private final int percentageIncrement = 10;
	
	public InteraktionAnimator(final MultiVolumeRenderer renderer, final GLWindow renderWindow){
		this.renderer = renderer;
		this.renderWindow = renderWindow;
	}
	
	public void interruptInitAnimation(){
		if(initAnimationThread == null){
			return;
		}
		initAnimationThread.interrupt();
	};
	
	private Thread initAnimationThread = null;

	private void doInitAnimationStep(){
		
		renderer.setOpacity3D((float)(currentAnimationPercentage)/ 100f);
		renderWindow.getGlCanvas().repaint();

	}
	
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
						System.out.println("test "+ currentAnimationPercentage);
						doInitAnimationStep();
						try {
							sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println("interrupt"); 
							break;
						}
					}
					
					//make 100% animation mode
						currentAnimationPercentage = 100;
						System.out.println("fix");
						doInitAnimationStep();
						
				}
		};
		
		initAnimationThread.start();
	}

	public void stopAllAnimations() {
		interruptInitAnimation();
		
	}
}
