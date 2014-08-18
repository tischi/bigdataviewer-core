package tgmm;

import java.util.List;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.collection.KDTree;
import net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class EllipsoidRealRandomAccessible implements RealRandomAccessible< UnsignedShortType >
{
	final static double nSigmas = 2;

	final static double nSigmasSquared = nSigmas * nSigmas;

	final KDTree< Ellipsoid > tree;

	final double maxRadius;

	public EllipsoidRealRandomAccessible( final List< Ellipsoid > ellipsoids )
	{
		tree = new KDTree< Ellipsoid >( ellipsoids, ellipsoids );
		double max = 0;
		for ( final Ellipsoid e : ellipsoids )
			max = Math.max( max, e.boundingSphereRadiusSquared );
		maxRadius = Math.sqrt( max );
	}

	@Override
	public int numDimensions()
	{
		return 3;
	}

	@Override
	public RealRandomAccess< UnsignedShortType > realRandomAccess( final RealInterval interval )
	{
		return realRandomAccess();
	}

	class Access extends RealPoint implements RealRandomAccess< UnsignedShortType >
	{
		private final RadiusNeighborSearchOnKDTree< Ellipsoid > search;

		private final UnsignedShortType t = new UnsignedShortType();

		final double[] tmp1 = new double[ 3 ];

		final double[] tmp2 = new double[ 3 ];

		public Access()
		{
			super( 3 );
			search = new RadiusNeighborSearchOnKDTree< Ellipsoid >( tree );
		}

		private KDTree< Ellipsoid > tree()
		{
			return tree;
		}

		protected Access( final Access a )
		{
			super( a );
			search = new RadiusNeighborSearchOnKDTree< Ellipsoid >( a.tree() );
		}

		@Override
		public UnsignedShortType get()
		{
//			boolean inBoundingSphere = false;
			search.search( this, maxRadius, false );
			final int numNeighbors = search.numNeighbors();
			for ( int i = 0; i < numNeighbors; ++i )
			{
				final Ellipsoid ell = search.getSampler( i ).get();
				if ( search.getSquareDistance( i ) < ell.boundingSphereRadiusSquared )
				{
//					inBoundingSphere = true;
					if ( Ellipsoid.squaredMahalanobis( this.position, ell.m, ell.W, tmp1, tmp2 ) < nSigmasSquared )
					{
						t.set( 1000 );
						return t;
					}
				}
			}
			t.set( 0 );
			return t;
		}

		@Override
		public Access copy()
		{
			return new Access( this );
		}

		@Override
		public Access copyRealRandomAccess()
		{
			return copy();
		}
	}

	@Override
	public RealRandomAccess< UnsignedShortType > realRandomAccess()
	{
		return new Access();
	}
}
