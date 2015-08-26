package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.nio.FloatBuffer;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.TransferFunctions.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

public class PreIntegrationSamplerTest {

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
	
}
