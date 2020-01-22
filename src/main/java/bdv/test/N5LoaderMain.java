package bdv.test;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

import java.io.File;

public class N5LoaderMain
{
	public static void main( String[] args ) throws SpimDataException
	{
		final File file = new File( args[ 0 ] );
		BigDataViewer.open( file.getAbsolutePath(), file.getName(), null, ViewerOptions.options() );
	}
}
