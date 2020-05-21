package bdv.io.n5;

import bdv.BigDataViewer;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

import java.io.File;

public class N5S3LoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		// test secure loading
//		BigDataViewer.open( "https://git.embl.de/pape/covid-em/-/raw/master/data/Covid19-S4-Area2/images/remote/sbem-6dpf-1-whole-raw.xml", "", null, ViewerOptions.options().numRenderingThreads( 3 ) );

		// test default loading
		BigDataViewer.open( "https://raw.githubusercontent.com/platybrowser/platybrowser/master/data/1.0.1/images/remote/prospr-6dpf-1-whole-ache.xml", "", null, ViewerOptions.options().numRenderingThreads( 3 ) );
	}
}
