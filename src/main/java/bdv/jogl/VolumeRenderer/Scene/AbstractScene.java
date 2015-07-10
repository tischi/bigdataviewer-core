package bdv.jogl.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;

/**
 * Class defining a standard scene.
 * @author michael
 *
 */
public abstract class AbstractScene {

	protected Camera camera = new Camera();

	protected Color backgroundColor = new Color(0.f,0.f,0.f,1.f);

	private List<ISceneElements> sceneElements = new ArrayList<ISceneElements>();

	/**
	 * adds an abstract element to scene
	 * @param sceneElement
	 * @return
	 */
	public boolean addSceneElement(ISceneElements sceneElement) {
		return sceneElements.add(sceneElement);
	}


	/**
	 * @return the backgroundColor
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}


	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}


	/**
	 * @return the camera
	 */
	public Camera getCamera() {
		return camera;
	}


	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 */
	private void initLocalCamera(Camera camera2, int width, int height){
		float[] center = {50,50,50};
		float[] eye = {50,50,300};

		camera2.addCameraListener(new CameraListener() {

			@Override
			public void viewMatrixUpdate(Matrix4 matrix) {

				//update all views
				for(ISceneElements element : sceneElements){
					element.setView(matrix);
				}
			}

			@Override
			public void projectionMatrixUpdate(Matrix4 matrix) {

				//update all projections
				for(ISceneElements element : sceneElements){
					element.setProjection(matrix);
				}
			}
		});
		camera2.setAlpha(45);
		camera2.setWidth(width);
		camera2.setHeight(height);
		camera2.setZfar(100000);
		camera2.setZnear(0);
		camera2.setLookAtPoint(center);
		camera2.setEyePoint(eye);
		camera2.update();
	}


	/**
	 * @param camera the camera to set
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}


	/**
	 * resizes the scene and the gl shader context
	 * @param gl2
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void resize(GL2 gl2,int x, int y, int width, int height){
		camera.setWidth(width);
		camera.setHeight(height);
		camera.updatePerspectiveMatrix();

		//sub class stuff
		resizeSpecial(gl2, x, y, width, height);
	}


	/**
	 * releases gl resources
	 * @param gl2
	 */
	public void dispose(GL2 gl2){
		for(ISceneElements c : sceneElements){
			c.disposeGL(gl2);
		}

		//subclass stuff
		disposeSpecial(gl2);
	}


	/**
	 * render the scene
	 * @param gl2
	 */
	public void render(GL2 gl2){
		gl2.glClearColor(	backgroundColor.getRed()/255,
				backgroundColor.getGreen()/255, 
				backgroundColor.getBlue()/255, 
				backgroundColor.getAlpha()/255);

		GLErrorHandler.assertGL(gl2);


		gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		//subclass stuff
		renderSpecial(gl2);

		//render elements
		for(ISceneElements element: sceneElements){
			element.render(gl2);
		}
	}


	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	public void init(GL2 gl2, int width, int height){

		initLocalCamera(camera, width, height);

		sceneElements.clear();

		//sub class stuff
		initSpecial(gl2, width, height);
	}


	/**
	 * specialized init for subclasses
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected abstract void initSpecial(GL2 gl2, int width, int height);

	/**
	 * specialized resize for subclasses
	 * @param gl2
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected abstract void resizeSpecial(GL2 gl2,int x, int y, int width, int height);


	/**
	 * specialized dispose for subclasses
	 * @param gl2
	 */
	protected abstract void disposeSpecial(GL2 gl2);


	/**
	 * specialized render for subclasses, actual rendering is performed by render, so should not be done here
	 * @param gl2
	 */
	protected abstract void renderSpecial(GL2 gl2);
}
