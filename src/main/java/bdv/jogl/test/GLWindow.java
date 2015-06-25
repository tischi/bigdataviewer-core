package bdv.jogl.test;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLPipelineFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.TraceGL2;
import com.jogamp.opengl.TraceGLES1;
import com.jogamp.opengl.TraceGLES2;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Matrix4;

/**
 * Main gl supporting widget
 * @author michael
 *
 */
public class GLWindow extends JFrame {

	private final GLCanvas glCanvas;

	private BigDataViewer bigDataViewer;

	private Camera camera = new Camera();
	
	private List<UnitCube> spaces =  new LinkedList< UnitCube>();

	private boolean useOwnCameraForNavigation = true;

	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 */
	private static void initLocalCamera(Camera camera2, int width, int height){
		float[] center = {50,50,50};
		float[] eye = {50,50,-300};

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
	 * @return the bigDataViewer
	 */
	public BigDataViewer getBigDataViewer() {
		return bigDataViewer;
	}

	/**
	 * @param bigDataViewer the bigDataViewer to set
	 */
	public void setBigDataViewer(BigDataViewer bigDataViewer) {
		this.bigDataViewer = bigDataViewer;
		this.bigDataViewer.getViewer().addTransformListener(new TransformListener<AffineTransform3D>() {

			@Override
			public void transformChanged(AffineTransform3D transform) {

				glCanvas.repaint();

			}
		});

		//close listener
		this.bigDataViewer.getViewerFrame().addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) { }

			@Override
			public void windowIconified(WindowEvent e) { }

			@Override
			public void windowDeiconified(WindowEvent e) { }

			@Override
			public void windowDeactivated(WindowEvent e) { }

			@Override
			public void windowClosing(WindowEvent e) {
				glCanvas.destroy();
				dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) { }

			@Override
			public void windowActivated(WindowEvent e) { }
		});
		
		spaces.clear();

		
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
				camera.setWidth(width);
				camera.setHeight(height);
				camera.updatePerspectiveMatrix();
			}

			/**
			 * init the test context
			 */
			@Override
			public void init(GLAutoDrawable drawable) {

				GL gl = drawable.getGL();
				//gl =drawable.setGL(new TraceGL2(drawable.getGL().getGL2(), System.err));
				GL2 gl2 = gl.getGL2();

				initLocalCamera(camera,drawable.getSurfaceWidth(),drawable.getSurfaceHeight());
				
				spaces.clear();
				int numberOfSources = bigDataViewer.getViewer().getState().getSources().size();
				float colorLinearFactor = 1.f/numberOfSources;
				float r =0, g=1,b=1 ;
				for(SourceState<?> source:bigDataViewer.getViewer().getState().getSources()){
					
					UnitCube cubeShader = new UnitCube();
					cubeShader.setCamera(camera);
					spaces.add(cubeShader);
					cubeShader.init(gl2);
					cubeShader.setRenderWireframe(true);
					cubeShader.setColor(new Color(r,g,b,1));
					r+=colorLinearFactor;
					b-=colorLinearFactor;
				}
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				GL2 gl2 = drawable.getGL().getGL2();
				
				for(UnitCube c : spaces){
					c.disposeGL(gl2);
				}
			}



			@Override
			public void display(GLAutoDrawable drawable) {		
			
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();
				
				gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);


				AffineTransform3D viewerTransform = new AffineTransform3D();
				bigDataViewer.getViewer().getState().getViewerTransform(viewerTransform);

		
				ViewerState state = bigDataViewer.getViewer().getState();
				state.getViewerTransform(viewerTransform);
				List<SourceState<?>> sources = state.getSources();
				
				int currentTimepoint = state.getCurrentTimepoint();
				int midMapLevel = 0;
				int i =0;
				for(SourceState<?> source : sources){
					Matrix4 mat = convertToJoglTransform(viewerTransform);
					
					//gl2.glPushMatrix();
					
					RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, midMapLevel);

					//block transform
					AffineTransform3D sourceTransform3D = new AffineTransform3D();
					source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
					Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);
					//gl2.glMultMatrixf(sourceTransformation.getMatrix(), 0);

					//block size
					long[] min =  new long[3];
					long[] max =  new long[3];
					ssource.min(min);
					ssource.max(max);

					Matrix4 scale = new Matrix4();
					scale.loadIdentity();
					scale.scale(max[0] - min[0],max[1] - min[1] ,max[2] - min[2]);
					
					mat.multMatrix(sourceTransformation);
					mat.multMatrix(scale);
					
					 
					
					UnitCube cubeShader = spaces.get(i);
				
					//mat.loadIdentity();
					cubeShader.getModelTransformations().clear();
					cubeShader.getModelTransformations().add(mat);
					cubeShader.update(gl2);
					cubeShader.render(gl2);

					i++;
				}

			}
		});
		initWindowElements();
	}



	protected Matrix4 convertToJoglTransform(AffineTransform3D viewerTransform) {
		Matrix4 matrix = new Matrix4();
		matrix.loadIdentity();


		double [] viewerArray = new double[4*3];
		viewerTransform.toArray(viewerArray);

		for(int i = 0; i < viewerArray.length; i++){
			Double data = viewerArray[i];
			matrix.getMatrix()[i] = data.floatValue();
		}
		matrix.invert();
		matrix.transpose();

		return matrix;
	}


	/**
	 * creates a GLWidget and connects it to the viewer 
	 * @param parent The BigDataViewer to connect to
	 * @return The new created GLWidget
	 */
	public static GLWindow createAndConnect3DView(BigDataViewer parent){
		GLWindow window = new GLWindow();
		window.setBigDataViewer(parent);
		window.setVisible(true);
		return window;
	}

	/**
	 * Does define the layout of the Window
	 */
	private void initWindowElements(){
		setTitle("Open GL Window");

		getContentPane().add(glCanvas);

		//sample size
		setSize(640,480);
	}

	/**
	 * @return the useOwnCameraForNavigation
	 */
	public boolean isUseOwnCameraForNavigation() {
		return useOwnCameraForNavigation;
	}

	/**
	 * @param useOwnCameraForNavigation the useOwnCameraForNavigation to set
	 */
	public void setUseOwnCameraForNavigation(boolean useOwnCameraForNavigation) {
		this.useOwnCameraForNavigation = useOwnCameraForNavigation;
	}
}
