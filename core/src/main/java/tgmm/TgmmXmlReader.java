package tgmm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class TgmmXmlReader
{
	public static class Gaussian
	{
		private final double nu;

		private final double[] m;

		private final double[] W;

		private final int id;

		private final int lineage;

		private final int parent;

		public Gaussian(
				final double nu,
				final double[] m,
				final double[] W,
				final int id,
				final int lineage,
				final int parent )
		{
			this.nu = nu;
			this.m = m;
			this.W = W;
			this.id = id;
			this.lineage = lineage;
			this.parent = parent;
		}

		public double getNu()
		{
			return nu;
		}

		public double[] getM()
		{
			return m;
		}

		public double[] getW()
		{
			return W;
		}

		public int getId()
		{
			return id;
		}

		public int getLineage()
		{
			return lineage;
		}

		public int getParent()
		{
			return parent;
		}

		@Override
		public String toString()
		{
			return "Gaussian( " +
					"nu = " + nu +
					"m = " + net.imglib2.util.Util.printCoordinates( m ) +
					"W = " + net.imglib2.util.Util.printCoordinates( W ) +
					"id = " + id +
					"lineage = " + lineage +
					"parent = " + parent +
					" )";
		}
	}

	public static double getDoubleAttribute( final Element parent, final String name )
	{
		return Double.parseDouble( parent.getAttributeValue( name ) );
	}

	public static double[] getDoubleArrayAttribute( final Element parent, final String name )
	{
		final String text = parent.getAttributeValue( name );
		final String[] entries = text.split( "\\s+" );
		final double[] array = new double[ entries.length ];
		for ( int i = 0; i < entries.length; ++i )
			array[ i ] = Double.parseDouble( entries[ i ] );
		return array;
	}

	public static int getIntAttribute( final Element parent, final String name )
	{
		return Integer.parseInt( parent.getAttributeValue( name ) );
	}

	public static ArrayList< Gaussian > read( final String xmlFilename ) throws IOException, JDOMException
	{
		final SAXBuilder sax = new SAXBuilder();
		final Document doc = sax.build( xmlFilename );
		final Element root = doc.getRootElement();

		final List< Element > gaussianMixtureModels = root.getChildren( "GaussianMixtureModel" );
		final ArrayList< Gaussian > gaussians = new ArrayList< Gaussian >();
		for ( final Element elem : gaussianMixtureModels )
		{
			try
			{
				final double nu = getDoubleAttribute( elem, "nu" );
				final double[] m = getDoubleArrayAttribute( elem, "m" );
				final double[] W = getDoubleArrayAttribute( elem, "W" );
				final int id = getIntAttribute( elem, "id" );
				final int lineage = getIntAttribute( elem, "lineage" );
				final int parent = getIntAttribute( elem, "parent" );

				gaussians.add( new Gaussian( nu, m, W, id, lineage, parent ) );
			}
			catch ( final NumberFormatException e )
			{
				e.printStackTrace();
				System.out.println( "ignoring " + elem );
			}
		}
		return gaussians;
	}
}
