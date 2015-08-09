package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.jogamp.opengl.math.Matrix4;

import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;

public class MultiVolumeRendererTest {

	private float [][] volumeDataArrays = {{0,1,2,
		3,4,5},
		{6,5,4,
			3,2,1}};

	@Test
	public void dataAddingTest() {
		MultiVolumeRenderer classUnderTest = new MultiVolumeRenderer();
		
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
	
	@Test
	public void TransformationTest(){
		MultiVolumeRenderer classUnderTest = new MultiVolumeRenderer();
		Map<Integer,Matrix4> transformations = classUnderTest.getModelTransformations();
		
		assertNotEquals(null, transformations);
		transformations.put(0,new Matrix4());
		assertEquals(1, classUnderTest.getModelTransformations().size());

		transformations.put(1,new Matrix4());
		assertEquals(2, classUnderTest.getModelTransformations().size());
		

		transformations.remove(0);
		assertEquals(1, classUnderTest.getModelTransformations().size());
		assertEquals(null,classUnderTest.getModelTransformations().get(0));
		assertNotEquals(null, classUnderTest.getModelTransformations().get(1));
		
		transformations.put(0,new Matrix4());
		assertEquals(2, classUnderTest.getModelTransformations().size());
		assertNotEquals(null, classUnderTest.getModelTransformations().get(0));
		assertNotEquals(null, classUnderTest.getModelTransformations().get(1));
	}

}
