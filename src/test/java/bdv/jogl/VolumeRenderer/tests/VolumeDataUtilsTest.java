package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jogamp.opengl.math.VectorUtil;

import bdv.jogl.VolumeRenderer.utils.CurvatureContainer;
import bdv.jogl.VolumeRenderer.utils.GradientContainer;
import bdv.jogl.VolumeRenderer.utils.MatrixUtils;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataUtils;

public class VolumeDataUtilsTest {

	@Test
	public void testCalculateCurvatureOfVolume() {
		VolumeDataBlock testBlock = new VolumeDataBlock();
		testBlock.data = new float[]{0,0,0,
									 0,0,0,
									 0,0,0,
			
									 1,1,1,
									 1,1,1,
									 1,1,1,
									 
									 2,2,2,
									 2,2,2,
									 2,2,2};
		testBlock.memSize = new long[]{3,3,3};
		CurvatureContainer c = VolumeDataUtils.calculateCurvatureOfVolume(testBlock);
		//test center gradient zero curvature on homogene data
		assertEquals(0, c.valueMesh3d[13],0.01);
	}

	@Test
	public void testCalculateGradientOfVolume() {
		VolumeDataBlock testBlock = new VolumeDataBlock();
		testBlock.data = new float[]{0,0,0,
									 0,0,0,
									 0,0,0,
			
									 1,1,1,
									 1,1,1,
									 1,1,1,
									 
									 2,2,2,
									 2,2,2,
									 2,2,2};
		testBlock.memSize = new long[]{3,3,3};
		float wantedDir[] = new float[]{0,0,1};
		testBlock.setLocalTransformation(MatrixUtils.getNewIdentityMatrix());
		
		GradientContainer c = VolumeDataUtils.calculateGradientOfVolume(testBlock);
		
		//test center gradient
		assertEquals(1f, Math.abs(VectorUtil.dotVec3(wantedDir, c.valueMesh3d[13])),0.01);
	}

}
