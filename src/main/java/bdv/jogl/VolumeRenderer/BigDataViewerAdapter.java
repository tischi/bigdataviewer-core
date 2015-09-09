package bdv.jogl.VolumeRenderer;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.convertToJoglTransform;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getDataBlock;

import java.util.List;

import com.jogamp.opengl.math.Matrix4;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
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
		return source.getSpimSource().getNumMipmapLevels()-1;
	}

	/**
	 * connects the bdv to the data manger
	 * @param bdv
	 * @param manager
	 */
	public static void connect(final BigDataViewer bdv,final VolumeDataManager manager){
		bdv.getViewer().addRenderTransformListener(new TransformListener<AffineTransform3D>() {

			@Override
			public void transformChanged(AffineTransform3D transform) {


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
					AffineTransform3D sourceTransform3D = new AffineTransform3D();
					source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
					Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);

					VolumeDataBlock data = getDataBlock(source.getSpimSource().getSource(currentTimepoint, midMapLevel));
					data.localTransformation =sourceTransformation;
					manager.setVolume(i,currentTimepoint, data);
				}
			}
		});
	}
}
