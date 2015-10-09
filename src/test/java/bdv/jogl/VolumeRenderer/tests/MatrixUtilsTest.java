package bdv.jogl.VolumeRenderer.tests;

import java.beans.Expression;

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

		float[] s = {
				10,0,0,0,
				0,20,0,0,
				0,0,30,0,
				0,0,0,1
		};

		float[] t = {
				1,0,0,10,
				0,1,0,20,
				0,0,1,30,
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

		float[]testVec = {1,1,1,1};
		float[]testTrans = new float[4];
		AffineTransform3D sa = new AffineTransform3D();
		sa.set(
				s[0],s[1],s[2],s[3],
				s[4],s[5],s[6],s[7],
				s[8],s[9],s[10],s[11]
				);
		MatrixUtils.convertToJoglTransform(sa).multVec(testVec, testTrans);
		assertArrayEquals(new float[]{10,20,30,1}, testTrans,0.01f);

		AffineTransform3D ta = new AffineTransform3D();
		ta.set(
				t[0],t[1],t[2],t[3],
				t[4],t[5],t[6],t[7],
				t[8],t[9],t[10],t[11]
				);
		MatrixUtils.convertToJoglTransform(ta).multVec(testVec, testTrans);
		assertArrayEquals(new float[]{11,21,31,1}, testTrans,0.01f);		
	}

	@Test
	public void zeroMatrixTest(){
		Matrix4 zeroMatrix = MatrixUtils.getNewNullMatrix();
		for(int i = 0; i < 16;i++){
			assertEquals(0, zeroMatrix.getMatrix()[0], 0.000f);
		}
	} 

	@Test
	public void addMatrixTest(){
		Matrix4 a = MatrixUtils.createMatrix4X4(new float []{
				1,1,1,1,
				2,2,2,2,
				3,3,3,3,
				4,4,4,4});
		Matrix4 b = MatrixUtils.createMatrix4X4(new float []{
				1,2,3,4,
				1,2,3,4,
				1,2,3,4,
				1,2,3,4});
		Matrix4 expected = MatrixUtils.createMatrix4X4(new float []{
				2,3,4,5,
				3,4,5,6,
				4,5,6,7,
				5,6,7,8
		});

		Matrix4 r = MatrixUtils.addMatrix(a, b);
		
		assertArrayEquals(expected.getMatrix(), r.getMatrix(), 0.0001f);
	}
	
	@Test
	public void subMatrixTest(){
		Matrix4 a = MatrixUtils.createMatrix4X4(new float []{
				1,1,1,1,
				2,2,2,2,
				3,3,3,3,
				4,4,4,4});
		Matrix4 b = MatrixUtils.createMatrix4X4(new float []{
				1,2,3,4,
				1,2,3,4,
				1,2,3,4,
				1,2,3,4});
		Matrix4 expected = MatrixUtils.createMatrix4X4(new float []{
				0,-1,-2,-3,
				1,0,-1,-2,
				2,1,0,-1,
				3,2,1,0
		});

		Matrix4 r = MatrixUtils.subMatrix(a, b);
		
		assertArrayEquals(expected.getMatrix(), r.getMatrix(), 0.0001f);
	}
	
}
