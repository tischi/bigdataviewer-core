package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import bdv.jogl.VolumeRenderer.FrameBufferRedirector;
import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.Scene.SimpleScene;
import bdv.jogl.VolumeRenderer.ShaderPrograms.SimpleVolumeRenderer;
import bdv.jogl.VolumeRenderer.gui.TransferFunction1D;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

public class SimpleVolumeRendererTest {

	private float[] testEye = {0,0,3};
	
	private float[] testCenter = {0,0,0};

	private float[] volumeData = {1,2,3,
			4,5,6};

	private TransferFunction1D testtf = new TransferFunction1D(100, 100);
	
	private SimpleVolumeRenderer objectUnderTest = new SimpleVolumeRenderer();

	private SimpleScene testScene = new SimpleScene();

	private JFrame testwindow = new JFrame();

	private GLCanvas glCanvas = new GLCanvas();

	private FrameBufferRedirector redirector = new FrameBufferRedirector();

	private float[][][] result;

	private BlockingQueue<Boolean> sync = new ArrayBlockingQueue<Boolean>(1);

	@Before
	public void setup(){
		testwindow.setSize(100, 100);
		testwindow.getContentPane().add(glCanvas);
		
		VolumeDataBlock block = new VolumeDataBlock();
		block.data = volumeData;
		objectUnderTest.setData(block);
		objectUnderTest.setTransferFunction(testtf);
		
		testScene.getCamera().setEyePoint(testEye);
		testScene.getCamera().setLookAtPoint(testCenter);
		testScene.addSceneElement(objectUnderTest);
		testScene.setBackgroundColor(Color.RED);

		redirector.setScene(testScene);
		redirector.setHeight(100);
		redirector.setWidth(100);

		glCanvas.addGLEventListener(new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				GLErrorHandler.assertGL(drawable.getGL());

			}

			@Override
			public void init(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				redirector.setHeight(drawable.getSurfaceHeight());
				redirector.setWidth(drawable.getSurfaceWidth());
				redirector.init(gl2);		

				GLErrorHandler.assertGL(gl2);
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				result = redirector.getFrameBufferContent(gl2);

				GLErrorHandler.assertGL(gl2);
				try {
					sync.put(true);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void display(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				GL2 gl2 = gl.getGL2();

				redirector.render(gl2);

				GLErrorHandler.assertGL(gl2);
				try {
					sync.put(true);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		testwindow.setVisible(true);
	}

	@Test
	public void SomeThingRenderedTest() {
		Boolean syncValue =null;
		try {
			syncValue=sync.poll(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		glCanvas.destroy();
		testwindow.dispose();
		assertEquals(true, syncValue);
		try {
			sync.poll(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(true, syncValue);
		
		Boolean isSomethingDrawn = false;
		for(int x = 0; x< result.length; x++){
			for(int y = 0; y < result[x].length; y++){
				Color testColor = new Color(result[x][y][0],result[x][y][1],result[x][y][2],result[x][y][3]);
				if(!(testColor.equals( testScene.getBackgroundColor()))){
					isSomethingDrawn = true;
				}
			}

		}
		assertTrue(isSomethingDrawn);

	}

}
