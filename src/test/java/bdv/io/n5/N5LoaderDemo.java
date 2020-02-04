package bdv.io.n5;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

import java.io.File;

public class N5LoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		//final File file = new File( "src/test/resources/mri-stack-n5.xml" );
		final File file = new File( "/g/arendt/EM_6dpf_segmentation/platy-browser-data/data/test_n5/images/sbem-6dpf-1-whole-raw.xml" );
//		final File file = new File( "/g/arendt/EM_6dpf_segmentation/platy-browser-data/data/test_n5/images/io.xml" );


//		final File file = new File( "/Volumes/cba/exchange/s3/constantin/n5/io-n5.xml" );
		BigDataViewer.open( file.getAbsolutePath(), file.getName(), null, ViewerOptions.options() );
	}
}
