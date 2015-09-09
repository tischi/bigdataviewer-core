package bdv.jogl.VolumeRenderer.gui.GLWindow;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import bdv.jogl.VolumeRenderer.gui.VolumeRendereActions.*;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.SceneGlobalTransformationListener;
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

	private BigDataViewer bigDataViewer;

	private List<VolumeDataScene> scenes = new LinkedList<VolumeDataScene>();

	/**
	 * @return the bigDataViewer
	 */
	public BigDataViewer getBigDataViewer() {
		return bigDataViewer;
	}

	private void createScene(){
		VolumeDataScene scene =  new VolumeDataScene(bigDataViewer);
		scene.addSceneEventListener(new SceneEventListener() {
			
			@Override
			public void needsUpdate() {
				glCanvas.repaint();
				
			}
		});
		CameraUpdater cUpdater = new CameraUpdater(scene.getCamera());
		glCanvas.addMouseListener(cUpdater.getMouseListener());
		glCanvas.addMouseMotionListener(cUpdater.getMouseMotionListener());
		glCanvas.addMouseWheelListener(cUpdater.getMouseWheelListener());
		scenes.clear();
		scenes.add(scene);
		this.bigDataViewer.getViewer().addTransformListener(new SceneGlobalTransformationListener(scene));
	}

	/**
	 * @param bigDataViewer the bigDataViewer to set
	 */
	public void setBigDataViewer(BigDataViewer bigDataViewer) {

		this.bigDataViewer = bigDataViewer;

		this.bigDataViewer.getViewer().addRenderTransformListener(new TransformListener<AffineTransform3D>() {

			@Override
			public void transformChanged(AffineTransform3D transform) {

				glCanvas.repaint();

			}
		});

		//close listener
		this.bigDataViewer.getViewerFrame().addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				glCanvas.destroy();
				dispose();
			}
		});

	}


	/**
	 * constructor
	 */
	public GLWindow(){
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

				//resizes all available scenes
				for(VolumeDataScene scene: scenes){
					scene.resize(gl2, x, y, width, height);;
				}
			}

			/**
			 * init the test context
			 */
			@Override
			public void init(GLAutoDrawable drawable) {

				GL gl = drawable.getGL();
				//gl =drawable.setGL(new TraceGL2(drawable.getGL().getGL2(), System.err));
				GL2 gl2 = gl.getGL2();

				createScene();

				//init all available scenes
				for(VolumeDataScene scene: scenes){
					scene.init(gl2, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
				}
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				//disposes all available scenes

				for(VolumeDataScene scene: scenes){
					scene.dispose(gl2);
				}
				scenes.clear();
			}



			@Override
			public void display(GLAutoDrawable drawable) {		

				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				//renders all available scenes
				for(VolumeDataScene scene: scenes){
					scene.render(gl2);
				}
			}
		});
		initWindowElements();
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
