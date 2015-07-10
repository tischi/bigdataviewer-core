package bdv.jogl.VolumeRenderer.tests;

import net.imglib2.realtransform.AffineTransform3D;

import org.junit.Test;

import static org.junit.Assert.*;
import bdv.jogl.VolumeRenderer.utils.MatrixUtils;

import com.jogamp.opengl.math.Matrix4;

/**
 * Tests for matrix utils
 * @author michael
 *
 */
public class MatrixUtilsTest {
	private float floatAccuracy = 0.0001f;
	
	@Test
	public void identityMatrixTest(){
		Matrix4 identity = new Matrix4();
		identity.loadIdentity();
		
		Matrix4 testMatrix = MatrixUtils.getNewIdentityMatrix();
		assertArrayEquals(identity.getMatrix(),testMatrix.getMatrix(),floatAccuracy);
	}
	
	@Test
	public void copyMatrixTest(){
		Matrix4 matrixToCopy = new Matrix4();
		matrixToCopy.loadIdentity();
		matrixToCopy.makePerspective(23, 42, 0.1f, 1000);
		
		Matrix4 testMatrix = MatrixUtils.copyMatrix(matrixToCopy);
		assertArrayEquals(matrixToCopy.getMatrix(),testMatrix.getMatrix(),floatAccuracy);
	}
	
	@Test
	public void convertMatrixTest(){
		float[] matrixContent = {
				11,21,34,12,
				43,31,44,22,
				12,45,43,12,
				0,0,0,1
		};
		
		//check matrix
		Matrix4 matrixWanted = new Matrix4();
		matrixWanted.loadIdentity();
		matrixWanted.multMatrix(matrixContent);
		matrixWanted.transpose();
		
		//test matrix 4*3
		AffineTransform3D matrixToConvert = new AffineTransform3D();
		matrixToConvert.set(
				matrixContent[0],matrixContent[1],matrixContent[2],matrixContent[3],
				matrixContent[4],matrixContent[5],matrixContent[6],matrixContent[7],
				matrixContent[8],matrixContent[9],matrixContent[10],matrixContent[11]
				);
		
		
		Matrix4 testMatrix = MatrixUtils.convertToJoglTransform(matrixToConvert);
		assertArrayEquals(matrixWanted.getMatrix(), testMatrix.getMatrix(), floatAccuracy);
		
	}
}
