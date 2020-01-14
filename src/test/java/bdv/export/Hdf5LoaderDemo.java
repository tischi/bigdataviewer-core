package bdv.export;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

import java.io.File;

public class Hdf5LoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		final File file = new File( "src/test/resources/mri-stack-copy.xml" );
		BigDataViewer.open( file.getAbsolutePath(), file.getName(), null, ViewerOptions.options() );
	}
}
