package bdv.io.n5;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

import java.io.File;

public class N5UnsignedLongLoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		//final File file = new File( "src/test/resources/mri-stack-n5.xml" );
		final File file = new File( "/g/arendt/EM_6dpf_segmentation/platy-browser-data/data/1.0.1/images/local/sbem-6dpf-1-whole-segmented-nuclei.xml" );
//		final File file = new File( "/g/arendt/EM_6dpf_segmentation/platy-browser-data/data/test_n5/images/export.xml" );


//		final File file = new File( "/Volumes/cba/exchange/s3/constantin/n5/export-n5.xml" );
		final BigDataViewer bigDataViewer = BigDataViewer.open( file.getAbsolutePath(), file.getName(), null, ViewerOptions.options() );
	}
}
