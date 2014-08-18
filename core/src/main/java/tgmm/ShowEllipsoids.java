package tgmm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.display.RealARGBColorConverter.Imp1;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.jdom2.JDOMException;

import tgmm.TgmmXmlReader.Gaussian;
import bdv.BigDataViewer;
import bdv.export.ProgressWriterConsole;
import bdv.tools.brightness.RealARGBColorConverterSetup;
import bdv.util.IntervalBoundingBox;
import bdv.util.ModifiableInterval;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.state.ViewerState;

public class ShowEllipsoids
{
	public static class EllipsoidsSource implements Source< UnsignedShortType >
	{
		private final ModifiableInterval interval;

		private final HashMap< Integer, EllipsoidRealRandomAccessible > timepointIndexToEllipsoidAccessible;

		private final String name;

		private final UnsignedShortType type;

		protected final AffineTransform3D identity;

		public EllipsoidsSource( final HashMap< Integer, EllipsoidRealRandomAccessible > timepointIndexToEllipsoidAccessible, final String name )
		{
			this.timepointIndexToEllipsoidAccessible = timepointIndexToEllipsoidAccessible;
			this.name = name;
			this.interval = new ModifiableInterval( Intervals.createMinMax( -100, -100, -100, 100, 100, 100 ) );
			this.type = new UnsignedShortType();
			this.identity = new AffineTransform3D();
		}

		public final ModifiableInterval getInterval( final int t, final int level )
		{
			return interval;
		}

		@Override
		public boolean isPresent( final int t )
		{
			return timepointIndexToEllipsoidAccessible.containsKey( t );
		}

		@Override
		public RandomAccessibleInterval< UnsignedShortType > getSource( final int t, final int level )
		{
			return Views.interval( Views.raster( timepointIndexToEllipsoidAccessible.get( t ) ), interval );
		}

		@Override
		public RealRandomAccessible< UnsignedShortType > getInterpolatedSource( final int t, final int level, final Interpolation method )
		{
			return timepointIndexToEllipsoidAccessible.get( t );
		}

		@Override
		public AffineTransform3D getSourceTransform( final int t, final int level )
		{
			return identity;
		}

		@Override
		public UnsignedShortType getType()
		{
			return type;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public int getNumMipmapLevels()
		{
			return 1;
		}
	}

	public static void main( final String[] args ) throws IOException, JDOMException, SpimDataException
	{
		final int timepointIndex = 245;
		final String fn = "/Users/pietzsch/desktop/data/BDV130418A325/BDV130418A325_NoTempReg.xml";

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final BigDataViewer bdv = new BigDataViewer( fn, new File( fn ).getName(), new ProgressWriterConsole() );
		bdv.getViewer().setTimepoint( timepointIndex );

		final ViewerState viewerState = bdv.getViewer().getState();
		final int currentSourceId = viewerState.getCurrentSource();
		final Source< ? > imgSource = viewerState.getSources().get( currentSourceId ).getSpimSource();
		final Interval imgSourceInterval = imgSource.getSource( timepointIndex, 0 );

		final HashMap< Integer, EllipsoidRealRandomAccessible > timepointIndexToEllipsoidAccessible = new HashMap< Integer, EllipsoidRealRandomAccessible >();
		for ( int tp = 240; tp < 248; ++tp )
		{
			final int timepointId = tp + 1;
			final String tgmmFilename = "/Users/pietzsch/Desktop/data/Fernando/extract/GMEMfinalResult_frame0" + timepointId + ".xml";
			System.out.println( tgmmFilename );
			final ArrayList< Gaussian > gaussians = TgmmXmlReader.read( tgmmFilename );
			final ArrayList< Ellipsoid > ellipsoids = new ArrayList< Ellipsoid >();
			final AffineTransform3D imgSourceTransform = imgSource.getSourceTransform( tp, 0 );
			for ( final Gaussian g : gaussians )
				ellipsoids.add( new Ellipsoid( g, imgSourceTransform ) );
			final EllipsoidRealRandomAccessible ellipsoidsAccessible = new EllipsoidRealRandomAccessible( ellipsoids );
			timepointIndexToEllipsoidAccessible.put( tp, ellipsoidsAccessible );
		}

		final EllipsoidsSource ellipsoidsSource = new EllipsoidsSource( timepointIndexToEllipsoidAccessible, "ellipsoids" );
		final Imp1< UnsignedShortType > converter = new RealARGBColorConverter.Imp1< UnsignedShortType >( 0, 3000 );
		converter.setColor( new ARGBType( ARGBType.rgba( 255, 0, 0, 0 ) ) );
		final SourceAndConverter< UnsignedShortType > ellipsoidsSourceAndConverter = new SourceAndConverter< UnsignedShortType >( ellipsoidsSource, converter );

		final ArrayList< RealPoint > sourceCorners = new ArrayList< RealPoint >();
		final AffineTransform3D imgSourceTransform = imgSource.getSourceTransform( timepointIndex, 0 );
		for ( final RealLocalizable corner : IntervalBoundingBox.getCorners( imgSourceInterval ) )
		{
			final RealPoint sourceCorner = new RealPoint( 3 );
			imgSourceTransform.apply( corner, sourceCorner );
			sourceCorners.add( sourceCorner );
		}
		final Interval bb = Intervals.smallestContainingInterval( IntervalBoundingBox.getBoundingBox( sourceCorners ) );
		ellipsoidsSource.getInterval( timepointIndex, 0 ).set( bb );

		final int ellipsoidSetupId = 12469; // some non-existing setup id
		final RealARGBColorConverterSetup cs = new RealARGBColorConverterSetup( ellipsoidSetupId, converter );
		cs.setViewer( bdv.getViewer() );
		bdv.getSetupAssignments().addSetup( cs );
		bdv.getViewer().addSource( ellipsoidsSourceAndConverter );
	}
}
