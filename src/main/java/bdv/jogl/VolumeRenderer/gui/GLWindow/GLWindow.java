package bdv.jogl.VolumeRenderer.gui.GLWindow;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;

import bdv.jogl.VolumeRenderer.Scene.AbstractScene;
import bdv.jogl.VolumeRenderer.Scene.SceneEventListener;
import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
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
	
	private CameraUpdater cUpdater;

	//TODO bench
	private final int maxStamps = 1000; 
	private final int startSteps = 1000;
	
	private boolean measurementInProgress = false;
	private boolean startPhaseInProgress = false;
	private long timeStamp[] = new long[maxStamps];
	
	
	private int startStepsTaken =  0;
	private int measureStepsTaken = 0 ;

	//TODO bench
			
	private void adaptScene(){
		
		renderScene.addSceneEventListener(new SceneEventListener() {
			
			@Override
			public void needsUpdate() {
				glCanvas.repaint();
				
			}
		});
		cUpdater = new CameraUpdater(renderScene.getCamera());
		glCanvas.addMouseListener(cUpdater.getMouseListener());
		glCanvas.addMouseMotionListener(cUpdater.getMouseMotionListener());
		glCanvas.addMouseWheelListener(cUpdater.getMouseWheelListener());
	}
	
	private void storeResults(){
		PrintWriter paraWriter = null;
		try {
			paraWriter = new PrintWriter("benchmark.txt","UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long latestTimeStep = timeStamp[0];
		for(int i = 1;i< maxStamps; i++){
			paraWriter.write(""+(  timeStamp[i]-latestTimeStep)+System.lineSeparator());
			latestTimeStep = timeStamp[i];
		}
		paraWriter.close();
	}
	
	/**
	 * @param scenes the scenes to set
	 */
	public void setScene(AbstractScene scenes) {
		this.renderScene = scenes;
		adaptScene();
	}

	/**
	 * @return the renderScene
	 */
	public AbstractScene getScene() {
		return renderScene;
	}


	/**
	 * @return the glCanvas
	 */
	public GLCanvas getGlCanvas() {
		return glCanvas;
	}

	//TODO bench
	/**
	 * Benchmark interface only!
	 */
	public void startBenchmark(){
		startStepsTaken = 0;
		measureStepsTaken = 0;
		startPhaseInProgress = true;
		measurementInProgress = false;
		System.out.println("Benchmark started!");
		glCanvas.repaint();
	}
	
	private void doMeasurement(){
		if(startStepsTaken < startSteps){
			startStepsTaken++;
			return;
		}	
		if(startPhaseInProgress||measurementInProgress){
		startPhaseInProgress = false;
		measurementInProgress = true;
		if(measureStepsTaken < maxStamps){
			
			timeStamp[measureStepsTaken] = System.nanoTime();
			measureStepsTaken++;
			if(measureStepsTaken >= maxStamps){
				measurementInProgress= false;
				storeResults();
				System.out.println("Benchmark done!");
			}
		}
		}
	}
	
	private void prepareNextMeasurement(){
		if(measurementInProgress||startPhaseInProgress ){
			if(measurementInProgress&&measureStepsTaken %100 ==0){
				System.out.print(".");
			}
			glCanvas.repaint();
		}
	}
	//TODO bench
	
	/**
	 * constructor
	 */
	public GLWindow(final VolumeDataScene scene){		
		// create render area
		//GLProfile glprofile = GLProfile.getDefault();
		GLProfile glprofile = GLProfile.get(GLProfile.GL4);
		GLCapabilities glcapabilities = new GLCapabilities( glprofile );


		glCanvas = new GLCanvas(glcapabilities );
		glCanvas.addGLEventListener(new GLEventListener() {

			@Override
			public synchronized void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//resizes available scene
			    renderScene.resize(gl2, x, y, width, height);
			}

			/**
			 * init the test context
			 */
			@Override
			public synchronized void init(GLAutoDrawable drawable) {

				GL gl = drawable.getGL();
				//gl =drawable.setGL(new TraceGL2(drawable.getGL().getGL2(), System.err));
				GL4 gl2 = gl.getGL4();

				//init available scene
				renderScene.init(gl2, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
			}

			@Override
			public synchronized void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//disposes available scene
				renderScene.dispose(gl2);
			}



			@Override
			public synchronized void display(GLAutoDrawable drawable) {		

				//TODO bench
				//doMeasurement();
				//TODO bench
				GL gl = drawable.getGL();
				GL4 gl2 = gl.getGL4();

				//renders available scene
				renderScene.render(gl2);
				//TODO bench
				//prepareNextMeasurement();
				//TODO bench
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

	/**
	 * @return the camera updater
	 */
	public CameraUpdater getCameraUpdater() {
		return cUpdater;
	}

}
