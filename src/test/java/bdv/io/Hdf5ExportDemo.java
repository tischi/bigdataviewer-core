package bdv.io;

import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.MipmapInfo;
import bdv.img.hdf5.Util;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.sequence.SequenceDescription;

import java.io.File;
import java.util.HashMap;

public class Hdf5ExportDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		SpimData spimData = new XmlIoSpimData().load( "src/test/resources/mri-stack.xml");

		final SequenceDescription sequenceDescription = spimData.getSequenceDescription();
		final HashMap< Integer, ExportMipmapInfo > perSetupMipmapInfo = getMipMapInfos( sequenceDescription );

		final int numThreads = Math.max( 1, Runtime.getRuntime().availableProcessors() - 2 );
		final File hdf5File = new File( "src/test/resources/mri-stack-copy.h5" );

		WriteSequenceToHdf5.writeHdf5File( sequenceDescription, perSetupMipmapInfo, true, hdf5File, null, null, numThreads, null );

		final SpimData spimData2 = new SpimData( new File("src/test/resources/"), sequenceDescription, spimData.getViewRegistrations() );
		new XmlIoSpimData().save( spimData2, "src/test/resources/mri-stack-copy.xml" );
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
