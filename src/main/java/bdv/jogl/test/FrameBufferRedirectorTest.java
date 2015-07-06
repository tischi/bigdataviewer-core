package bdv.jogl.test;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;

import bdv.jogl.shader.UnitCube;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Matrix4;

/**
 * Test of frame buffer wrapper
 * @author michael
 *
 */
public class FrameBufferRedirectorTest {

	//camera defaults
	private final float[] eye = {0,0,4};

	private final float[] center = {0,0,0};

	private final float[] up = {0,1,0};

	private final float znear = 0.1f;

	private final float zfar = 10f;

	private float[][][] result;

	private JFrame testwindow = new JFrame();

	private GLCanvas glCanvas = new GLCanvas();

	private Camera camera = new Camera();

	private UnitCube cube = new UnitCube();

	private Color cubeColor = new Color(1.f,1.f,1.f,1.f); 

	private FrameBufferRedirector redirector = new FrameBufferRedirector(); 

	private final int testSize = 10;

	private BlockingQueue<Boolean> syncQueue = new ArrayBlockingQueue<Boolean>(1);

	@Before
	public void setup(){
		testwindow.getContentPane().add(glCanvas);


		GLEventListener listener = new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) { 
				camera.setWidth(width);
				camera.setHeight(height);
				camera.update();
			}

			@Override
			public void init(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();
				int width = drawable.getSurfaceWidth();
				int height = drawable .getSurfaceHeight();

				//camera init
				camera.setWidth(width);
				camera.setHeight(height);
				camera.setZnear(znear);
				camera.setZfar(zfar);
				camera.setEyePoint(eye);
				camera.setLookAtPoint(center);
				camera.setLookAtPoint(up);
				camera.update();


				//redirector
				redirector.setHeight(height);
				redirector.setWidth(width);;
				redirector.init(gl2);

				cube.setProjection(camera.getProjectionMatix());
				cube.setView(camera.getViewMatrix());
				//cube.update(gl2);

				GLErrorHandler.assertGL(gl2);

			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				result = redirector.getFrameBufferContent(gl2);
				//redirector.disposeGL(gl2);
				cube.disposeGL(gl2);

				GLErrorHandler.assertGL(gl2);
				try {
					syncQueue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void display(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();
				
				gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

				//cube.render(gl2);
				redirector.render(gl2);

				GLErrorHandler.assertGL(gl2);
				try {
					syncQueue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} ;
		Matrix4 mat = MatrixUtils.getNewIdentityMatrix();


		cube.setColor(cubeColor);
		cube.setModelTransformations(mat);

		redirector.getRenderElements().add(cube);

		testwindow.setSize(testSize, testSize);
		glCanvas.addGLEventListener(listener);
	}

	@Test
	public void readTest() throws InterruptedException {
		testwindow.setVisible(true);

		syncQueue.poll(2, TimeUnit.MINUTES);

		glCanvas.destroy();
		testwindow.dispose();

		syncQueue.poll(2, TimeUnit.MINUTES);

		boolean notNull = false;
		for(int y =0; y < redirector.getHeight(); y++){
			for(int x =0; x < redirector.getWidth(); x++){

				Color currentColor = new Color(result[x][y][0],result[x][y][1],result[x][y][2],result[x][y][3]);
				if(currentColor.equals(cubeColor)){
					notNull = true;
				}

				if(notNull){
					break;
				}
			}
			if(notNull){
				break;
			}
		}

		assertTrue("The framebuffer was empty", notNull);;
	}

}
