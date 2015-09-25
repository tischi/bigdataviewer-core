package bdv.jogl.VolumeRenderer.utils;

import net.imglib2.realtransform.AffineTransform3D;

import com.jogamp.opengl.math.Matrix4;

/**
 * Class to provide matrix operations for affine and matrix4
 * @author michael
 *
 */
public class MatrixUtils {

	/**
	 * Returns a new matrix instance intialized with the identity
	 * @return
	 */
	public static Matrix4 getNewIdentityMatrix(){
		Matrix4 matrix = new Matrix4();
		matrix.loadIdentity();
		return matrix;
	}
	
	/**
	 * converts a AffineTransform3D matrix to a matrix 4 in opengl format
	 * @param viewerTransform
	 * @return
	 */
	public static Matrix4 convertToJoglTransform(final AffineTransform3D viewerTransform) {
		Matrix4 matrix = getNewIdentityMatrix();

		double [] viewerArray = new double[4*3];
		viewerTransform.toArray(viewerArray);

		for(int i = 0; i < viewerArray.length; i++){
			Double data = viewerArray[i];
			matrix.getMatrix()[i] = data.floatValue();
		}
		//matrix.invert();
		matrix.transpose();

		return matrix;
	}
	
	/**
	 * Create a new instance as copy of a given matrix
	 * @param matrixToCopy
	 * @return
	 */
	public static Matrix4 copyMatrix(final Matrix4 matrixToCopy){
		Matrix4 matrix = getNewIdentityMatrix();
		matrix.multMatrix(matrixToCopy);
		return matrix;
	}
	
	/**
	 * Get the normalized eye position in the current space.
	 * @param modelViewMatrix model view matrix defining the current space in gl format (transposed)
	 * @return
	 */
	public static float[] getEyeInCurrentSpace(final Matrix4 modelViewMatrix){
		float eyePositionInCurrentSpace[] = new float[3];
		Matrix4 modelViewMatrixInverse = copyMatrix(modelViewMatrix);
		modelViewMatrixInverse.invert();
	
		//translation part of model view
		for(int i= 0; i < eyePositionInCurrentSpace.length; i++){
			eyePositionInCurrentSpace[i] = modelViewMatrixInverse.getMatrix()[12 + i];
		}
		return eyePositionInCurrentSpace;

	}
}
