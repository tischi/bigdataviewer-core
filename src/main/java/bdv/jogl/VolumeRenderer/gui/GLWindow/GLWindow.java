package bdv.jogl.VolumeRenderer.gui.GLWindow;


import javax.swing.JFrame;
import bdv.jogl.VolumeRenderer.Scene.AbstractScene;
import bdv.jogl.VolumeRenderer.Scene.SceneEventListener;
import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;


/**
 * Main gl supporting widget
 * @author michael
 *
 */
public class GLWindow extends JFrame {

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private final GLCanvas glCanvas;

	private AbstractScene renderScene;


	private void adaptScene(){
		
		renderScene.addSceneEventListener(new SceneEventListener() {
			
			@Override
			public void needsUpdate() {
				glCanvas.repaint();
				
			}
		});
		CameraUpdater cUpdater = new CameraUpdater(renderScene.getCamera());
		glCanvas.addMouseListener(cUpdater.getMouseListener());
		glCanvas.addMouseMotionListener(cUpdater.getMouseMotionListener());
		glCanvas.addMouseWheelListener(cUpdater.getMouseWheelListener());
	}
	
	/**
	 * @param scenes the scenes to set
	 */
	public void setScene(AbstractScene scenes) {
		this.renderScene = scenes;
		adaptScene();
	}

	/**
	 * @return the glCanvas
	 */
	public GLCanvas getGlCanvas() {
		return glCanvas;
	}

	/**
	 * constructor
	 */
	public GLWindow(final VolumeDataScene scene){		
		// create render area
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities( glprofile );


		glCanvas = new GLCanvas(glcapabilities );
		glCanvas.addGLEventListener(new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				//resizes available scene
			    renderScene.resize(gl2, x, y, width, height);;
				
			}

			/**
			 * init the test context
			 */
			@Override
			public void init(GLAutoDrawable drawable) {

				GL gl = drawable.getGL();
				//gl =drawable.setGL(new TraceGL2(drawable.getGL().getGL2(), System.err));
				GL2 gl2 = gl.getGL2();

				//init available scene
				renderScene.init(gl2, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				//disposes available scene
				renderScene.dispose(gl2);
			}



			@Override
			public void display(GLAutoDrawable drawable) {		

				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				//renders available scene
				renderScene.render(gl2);

			}
		});
		initWindowElements();
		setScene(scene);
	}

	/**
	 * Does define the layout of the Window
	 */
	private void initWindowElements(){
		setTitle("Open GL Window");


		//sample size
		setSize(640,580);

		getContentPane().add(glCanvas);



	}
}
