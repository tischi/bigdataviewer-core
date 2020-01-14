package bdv.export.n5;

import bdv.BigDataViewer;
import bdv.img.n5.XmlIoN5ImageLoader;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;

import java.io.File;

public class N5LoaderDemo
{
	public static void main( String[] args ) throws SpimDataException
	{
		final File file = new File( "src/test/resources/mri-stack-n5.xml" );
		BigDataViewer.open( file.getAbsolutePath(), file.getName(), null, ViewerOptions.options() );
	}
}
