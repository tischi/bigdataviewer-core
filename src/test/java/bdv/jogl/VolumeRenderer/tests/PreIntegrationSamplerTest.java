package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.nio.FloatBuffer;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.TransferFunctions.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

public class PreIntegrationSamplerTest {

	private PreIntegrationSampler objectUnderTest =new PreIntegrationSampler();
	
	private TransferFunction1D testTransferFunction = new TransferFunction1D(200, 200);
	
	private float getAlpha(FloatBuffer texture, int begin, int end, float stepsize ){
		
		return 1.f-(float)Math.exp(-stepsize/((float)(end-begin)) * (texture.get(end)-texture.get(begin)));
	}
	

	@Test
	public void shaderLikeExecAlphaCheck() {
		
		FloatBuffer texture = objectUnderTest.sample(testTransferFunction, 1);
		
		//linear ramp tf assumed
		int colorDist = 10; 
		int maxIndex = texture.capacity()-1;
		float a = getAlpha(texture, maxIndex-colorDist, maxIndex, 1);
		float b = getAlpha(texture, maxIndex-2*colorDist,maxIndex-colorDist, 1);
		float c = getAlpha(texture, maxIndex-3*colorDist, maxIndex-2*colorDist, 1);
		
		assertTrue(0 < getAlpha(texture, 0, texture.capacity()-1, 1));
		assertTrue(0 < getAlpha(texture, texture.capacity()/3, maxIndex, 1));
		assertTrue(0 < getAlpha(texture, texture.capacity()/2, maxIndex, 1));
		
		assertTrue(0 < getAlpha(texture, maxIndex, 0, 1));
		assertTrue(0 < getAlpha(texture, maxIndex, maxIndex/3, 1));
		assertTrue(0 < getAlpha(texture, maxIndex, maxIndex/2, 1));
		
		assertTrue(0< a);
		assertTrue(0< b);
		assertTrue(0< c);
		
		assertTrue(b< a);
		//assertTrue(c< b);
	}

}
