package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.Map;
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
import com.jogamp.opengl.math.Matrix4;

import bdv.jogl.VolumeRenderer.FrameBufferRedirector;
import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.Scene.SimpleScene;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

public class MultiVolumeRendererTest {
	
	private FrameBufferRedirector redirector = new FrameBufferRedirector(); 
	
	private long[] testdim = {3,2,1};
	
	private float[] testEye = {0,0,3};
	
	private float[] testCenter = {0,0,0};
	
	private float[][][] result;
	
	private SimpleScene testScene = new SimpleScene();
	
	private BlockingQueue<Boolean> sync = new ArrayBlockingQueue<Boolean>(1);
	
	private GLEventListener renderListener =  new GLEventListener() {

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
	};
	
	private JFrame testWindow = new JFrame("Test window");
	
	private GLCanvas renderCanvas = new GLCanvas();
	private MultiVolumeRenderer classUnderTest;
	private float [][] volumeDataArrays = {{0,1,2,
		3,4,5},
		{6,5,4,
			3,2,1}};

	@Before
	public void setUp(){
		classUnderTest = new MultiVolumeRenderer();
	}
	
	@Test
	public void dataAddingTest() {
		
		Map<Integer,VolumeDataBlock> data = classUnderTest.getVolumeDataMap();
		
		assertNotEquals(null, data);
		data.put(0,new VolumeDataBlock());
		assertEquals(1, classUnderTest.getVolumeDataMap().size());

		data.put(1,new VolumeDataBlock());
		assertEquals(2, classUnderTest.getVolumeDataMap().size());
		

		data.remove(0);
		assertEquals(1, classUnderTest.getVolumeDataMap().size());
		assertEquals(null,classUnderTest.getVolumeDataMap().get(0));
		assertNotEquals(null, classUnderTest.getVolumeDataMap().get(1));
		
		data.put(0,new VolumeDataBlock());
		assertEquals(2, classUnderTest.getVolumeDataMap().size());
		assertNotEquals(null, classUnderTest.getVolumeDataMap().get(0));
		assertNotEquals(null, classUnderTest.getVolumeDataMap().get(1));
		
	}

	private void initTestWindow(){
		
		renderCanvas.addGLEventListener(renderListener);
		
		testWindow.setSize(100, 100);
		testWindow.getContentPane().add(renderCanvas);
		
		
		testScene.getCamera().setEyePoint(testEye);
		testScene.getCamera().setLookAtPoint(testCenter);
		testScene.addSceneElement(classUnderTest);
		testScene.setBackgroundColor(Color.RED);

		redirector.setScene(testScene);
		redirector.setHeight(100);
		redirector.setWidth(100);
	}
	
	
	
	@Test
	public void renderSomethingTest(){
		initTestWindow();
		VolumeDataBlock[] blocks = {new VolumeDataBlock(),new VolumeDataBlock()};

		Matrix4 loc1 = new Matrix4();
		loc1.rotate(45, 0, 0, 1);
		Matrix4 loc2 = new Matrix4();
		loc2.rotate(-45, 0, 0, 1);
		blocks[0].data = volumeDataArrays[0];
		blocks[0].localTransformation  = loc1;
		blocks[0].dimensions = testdim.clone();
	
		blocks[1].data = volumeDataArrays[1];
		blocks[1].dimensions = testdim.clone();
		blocks[1].localTransformation = loc2;
	
		Map<Integer,VolumeDataBlock> dataBlocks = classUnderTest.getVolumeDataMap();
		
		dataBlocks.put(0, blocks[0]);
		dataBlocks.put(1, blocks[1]);
			
		testWindow.setVisible(true);
		
		Boolean syncValue = null;
		try {
			syncValue=sync.poll(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		renderCanvas.destroy();
		testWindow.dispose();
		assertEquals(true, syncValue);
		try {
			syncValue = sync.poll(10, TimeUnit.SECONDS);
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
