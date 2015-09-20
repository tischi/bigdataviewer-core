package bdv.jogl.VolumeRenderer;

import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getDataBlock;

import java.util.List;

import com.jogamp.opengl.math.geom.AABBox;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.utils.TestDataBlockSphere;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;


/**
 * Bridging class between bdv and the data manager
 * @author michael
 *
 */
public class BigDataViewerAdapter {

	private static int getMidmapLevel(final SourceState<?> source){
		int levels= source.getSpimSource().getNumMipmapLevels();
		return levels -1;
	}

	/**
	 * connects the bdv to the data manger
	 * @param bdv
	 * @param manager
	 */
	public static synchronized void connect(final BigDataViewer bdv,final VolumeDataManager manager){
		updateData(bdv,manager);
		bdv.getViewer().addRenderTransformListener(new TransformListener<AffineTransform3D>() {

			@Override
			public synchronized void transformChanged(AffineTransform3D transform) {

					updateData(bdv,manager);
				
			}
		});
	}
	
	private static void updateData(final BigDataViewer bdv,final VolumeDataManager manager){
		//mainly for new time points and data not realy for transform
		ViewerState state = bdv.getViewer().getState();
		List<SourceState<?>> sources = state.getSources();

		
		int currentTimepoint = state.getCurrentTimepoint();
		if(manager.getCurrentTime() == currentTimepoint){
			return;
		}
		int i =-1;
		for(SourceState<?> source : sources){

			i++;

			//block transform
			int midMapLevel = getMidmapLevel(source);
			VolumeDataBlock data = getDataBlock(bdv,new AABBox(new float[]{0,0,0,0},new float[]{2000,2000,2000}),i,midMapLevel);
			manager.setVolume(i,currentTimepoint, data);
		//	manager.setVolume(i,currentTimepoint, new TestDataBlockSphere(50));
		}
	}
}
