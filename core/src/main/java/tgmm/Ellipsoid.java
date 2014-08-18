package tgmm;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import tgmm.TgmmXmlReader.Gaussian;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class Ellipsoid extends RealPoint
{
	final double[] m;

	final double[][] W;

	final double[][] S;

	final double boundingSphereRadiusSquared;

	public Ellipsoid( final Gaussian gaussian, final AffineTransform3D transform )
	{
		super( 3 );
		m = position;

		final double[] wtmp = new double[9];
		LinAlgHelpers.scale( gaussian.getW(), gaussian.getNu(), wtmp );

		final Matrix precMat = new Matrix( wtmp, 3 );
		final Matrix covMat = precMat.inverse();
		S = covMat.getArray();

		transform.apply( gaussian.getM(), m );

		final double[][] T = new double[3][3];
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[r][c] = transform.get( r, c );

		final double[][] TS = new double[3][3];
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, S );

		W = covMat.inverse().getArray();

		final EigenvalueDecomposition eig = covMat.eig();
		final double[] eigVals = eig.getRealEigenvalues();
		double max = 0;
		for ( int k = 0; k < eigVals.length; k++ )
			max = Math.max( max, eigVals[ k ] );
		boundingSphereRadiusSquared = max * EllipsoidRealRandomAccessible.nSigmasSquared;
	}

	public static double squaredMahalanobis( final double[] x, final double[] m, final double[][] W, final double[] tmp1, final double[] tmp2 )
	{
		LinAlgHelpers.subtract( x, m, tmp1 );
		LinAlgHelpers.mult( W, tmp1, tmp2 );
		return LinAlgHelpers.dot( tmp1, tmp2 );
	}
}