package bdv.io.n5;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

import java.io.File;

public class N5S3LoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		// works nice
//		BigDataViewer.open( "https://git.embl.de/tischer/platy-browser-tables/raw/master/data/test_n5/1.0.0/images/remote/sbem-6dpf-1-whole-raw-1.xml", "", null, ViewerOptions.options() );

		// even nicer (but only when in the right orientation)
//		BigDataViewer.open( "https://git.embl.de/tischer/platy-browser-tables/raw/master/data/test_n5/1.0.0/images/remote/sbem-6dpf-1-whole-raw-2.xml", "", null, ViewerOptions.options() );

		// feels slower than above
//		BigDataViewer.open( "https://git.embl.de/tischer/platy-browser-tables/raw/master/data/test_n5/1.0.0/images/remote/sbem-6dpf-1-whole-raw-3.xml", "", null, ViewerOptions.options().numRenderingThreads( 20 ) );

		// feels even slower
//		BigDataViewer.open( "https://git.embl.de/tischer/platy-browser-tables/raw/master/data/test_n5/1.0.0/images/remote/sbem-6dpf-1-whole-raw-4.xml", "", null, ViewerOptions.options().numRenderingThreads( 3 ) );

		// feels even slower
		BigDataViewer.open( "https://git.embl.de/tischer/platy-browser-tables/raw/master/data/test_n5/1.0.0/images/remote/sbem-6dpf-1-whole-segmented-cells.xml", "", null, ViewerOptions.options().numRenderingThreads( 3 ) );


//		BigDataViewer.open( "https://git.embl.de/tischer/platy-browser-tables/raw/master/data/test_n5/1.0.0/images/remote/sbem-6dpf-1-whole-raw.xml", "", null, ViewerOptions.options() );

	}
}
