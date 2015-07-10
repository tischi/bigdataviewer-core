package bdv.jogl.VolumeRenderer.Scene;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;

/**
 * delivers standard methods for gl scene elements
 * @author michael
 *
 */
public interface ISceneElements {

	/**
	 * releases all gl resources
	 * @param gl2
	 */
	public void disposeGL(GL2 gl2);
	
	/**
	 * Updates the mandatory data and renders the scene element
	 * @param gl2
	 */
	public void render(GL2 gl2);
	
	/**
	 * initializes the scene element context
	 * @param gl
	 */
	public void init(GL2 gl);

	/**
	 * set the model transformation matrix of the current element
	 * @param modelTransformations the modelTransformations to set
	 */
	public void setModelTransformations(Matrix4 modelTransformations);
	
	/**
	 * set the projection transformation matrix of the current element
	 * @param projection the projection to set
	 */
	public void setProjection(Matrix4 projection);

	/**
	 * set the view transformation matrix of the current element
	 * @param view the view to set
	 */
	public void setView(Matrix4 view);
	
}
