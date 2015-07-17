package bdv.jogl.VolumeRenderer.gui;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.convertToJoglTransform;

import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;

/**
 * Listener for global transformation changes
 * @author michael
 *
 */
public class SceneGlobalTransformationListener implements TransformListener<AffineTransform3D> {

	private final VolumeDataScene scene;
	
	/**
	 * add scene to transfer data
	 * @param scene
	 */
	public SceneGlobalTransformationListener(final VolumeDataScene scene){
		this.scene = scene;
	}
	
	@Override
	public void transformChanged(AffineTransform3D transform) {
		scene.setGlobalModelTransformation(convertToJoglTransform(transform));
	}
}
