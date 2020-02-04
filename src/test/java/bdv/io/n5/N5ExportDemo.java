package bdv.io.n5;

import bdv.export.ExportMipmapInfo;
import bdv.export.n5.WriteSequenceToN5;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.MipmapInfo;
import bdv.img.hdf5.Util;
import bdv.img.n5.N5ImageLoader;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.sequence.SequenceDescription;
import org.janelia.saalfeldlab.n5.GzipCompression;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class N5ExportDemo
{
	public static void main( String[] args ) throws SpimDataException, IOException
	{
		//final String xmlFilename = "src/test/resources/mri-stack.xml";
		final String xmlFilename = "/Volumes/cba/exchange/s3/constantin/h5/export.xml";

		//final String outputBasePath = "src/test/resources";
		final String outputBasePath = "/Volumes/cba/exchange/s3/constantin/n5";


		final String n5OutputPath = outputBasePath + "/export.n5";
		final String n5BasePath = new File( n5OutputPath ).getParent();
		final String n5XmlOutputPath = outputBasePath + "/export-n5.xml";

		SpimData spimData = new XmlIoSpimData().load( xmlFilename );

		final SequenceDescription sequenceDescription = spimData.getSequenceDescription();
		final HashMap< Integer, ExportMipmapInfo > perSetupMipmapInfo = getMipMapInfos( sequenceDescription );

		final int numThreads = Math.max( 1, Runtime.getRuntime().availableProcessors() - 2 );

		final File n5File = new File( n5OutputPath );
		WriteSequenceToN5.writeN5File( sequenceDescription, perSetupMipmapInfo, new GzipCompression(), n5File, null, null, numThreads, null );

		// write xml sequence description
		final N5ImageLoader n5Loader = new N5ImageLoader( n5File, null );
		sequenceDescription.setImgLoader( n5Loader );

		final SpimData spimDataN5 = new SpimData( new File( n5BasePath ), sequenceDescription, spimData.getViewRegistrations() );

		new XmlIoSpimData().save( spimDataN5, n5XmlOutputPath );
	}

	private static HashMap< Integer, ExportMipmapInfo > getMipMapInfos( SequenceDescription sequenceDescription )
	{
		final HashMap< Integer, ExportMipmapInfo > perSetupMipmapInfo = new HashMap<>();
		final Hdf5ImageLoader loader = ( Hdf5ImageLoader ) sequenceDescription.getImgLoader();
		for ( final int setupId : sequenceDescription.getViewSetups().keySet() )
		{
			final MipmapInfo info = loader.getSetupImgLoader( setupId ).getMipmapInfo();;
			if ( info == null )
			{
				throw new UnsupportedOperationException( "" );
			}
			else
				perSetupMipmapInfo.put( setupId, new ExportMipmapInfo(
						Util.castToInts( info.getResolutions() ),
						info.getSubdivisions() ) );
		}
		return perSetupMipmapInfo;
	}
}
