package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

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
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

public class PreIntegrationSamplerTest {

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
	private MultiVolumeRenderer testVolumeRenderer;
	private float [][] volumeDataArrays = {{0,1,2,
		3,4,5},
		{6,5,4,
			3,2,1}};

	
	private PreIntegrationSampler objectUnderTest =new PreIntegrationSampler();
	
	private TransferFunction1D testTransferFunction = new TransferFunction1D(200, 200);
	
	private float getAlpha(FloatBuffer texture, int begin, int end, float stepsize ){
		
		//TODO in shader
		if(begin == end){
			begin--;
		}
		
		return 1.f-(float)Math.exp(-stepsize/(end-begin) * (texture.get(end*4+3)-texture.get(begin*4+3)));
	}
	
	private float[] getRGB(FloatBuffer texture, int begin, int end, float stepsize){
		float[] rgb = new float[3];
		if(begin==end){
			end++;
		}
		
		for(int i=0; i< rgb.length; i++ ){
			rgb[i]=stepsize/(end-begin) * (texture.get(end*4+i)-texture.get(begin*4+i));
		}
		return rgb;
	}

	@Test
	public void shaderLikeExecAlphaCheck() {
		
		FloatBuffer texture = objectUnderTest.sample(testTransferFunction, 1);
		
		//linear ramp tf assumed
		int colorDist = 10; 
		int maxIndex = texture.capacity()/4-1;
		float a = getAlpha(texture, maxIndex-colorDist, maxIndex, 1);
		float b = getAlpha(texture, maxIndex-5*colorDist,maxIndex-4*colorDist, 1);
		float c = getAlpha(texture, maxIndex-10*colorDist, maxIndex-9*colorDist, 1);
		
		assertTrue(0 < getAlpha(texture, 0, maxIndex, 1));
		assertTrue(0 < getAlpha(texture, maxIndex/3, maxIndex, 1));
		assertTrue(0 < getAlpha(texture, maxIndex/2, maxIndex, 1));
		
		assertTrue(0 < getAlpha(texture, maxIndex, 0, 1));
		assertTrue(0 < getAlpha(texture, maxIndex, maxIndex/3, 1));
		assertTrue(0 < getAlpha(texture, maxIndex, maxIndex/2, 1));
		
		assertTrue(0< a);
		assertTrue(0< b);
		assertTrue(0< c);
		
		assertTrue(b< a);
		assertTrue(c< b);
		
		assertTrue(getAlpha(texture, 0, 10, 100000000) > getAlpha(texture, 0, 10, 100) );
		//big slice through opaque
		float alpha = getAlpha(texture, maxIndex, maxIndex, 1000000);
		assertTrue("Assumed long dist to be near 1 but was "+alpha,alpha > 0.9);
	}
	
	@Test
	public void shaderLikeColorTest(){
		FloatBuffer texture = objectUnderTest.sample(testTransferFunction, 1);
		
		float[] a = getRGB(texture, 0, 0, 100);
		float[] b = getRGB(texture,0,0,1000);
		
		//very likely blue default of tf
		assertTrue(a[0] <0.1);
		assertTrue(a[1] <0.1);
		assertTrue(a[2] >a[0]&& a[2] > a[1]);
		
		//b more blue
		assertTrue(a[2]< b[2]);
	}
	
	
	private void initTestWindow(){
		testTransferFunction.setSampler(objectUnderTest);
		
		testVolumeRenderer = new MultiVolumeRenderer(testTransferFunction);
		renderCanvas.addGLEventListener(renderListener);
		
		testWindow.setSize(100, 100);
		testWindow.getContentPane().add(renderCanvas);
		
		
		testScene.getCamera().setEyePoint(testEye);
		testScene.getCamera().setLookAtPoint(testCenter);
		testScene.addSceneElement(testVolumeRenderer);
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
	
		Map<Integer,VolumeDataBlock> dataBlocks = testVolumeRenderer.getVolumeDataMap();
	
		
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
