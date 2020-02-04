package bdv.io.n5;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

import java.io.File;

public class N5S3LoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		final File file = new File( "src/test/resources/platy-n5s3.xml" );
		BigDataViewer.open( file.getAbsolutePath(), file.getName(), null, ViewerOptions.options() );
	}
}
