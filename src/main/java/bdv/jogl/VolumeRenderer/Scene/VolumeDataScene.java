package bdv.jogl.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.view.Views;
import bdv.BigDataViewer;
import bdv.jogl.shader.UnitCube;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import static bdv.jogl.test.MatrixUtils.*;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;

/**
 * Describes a scene for volume data with multiple scene objects
 * @author michael
 *
 */
public class VolumeDataScene extends AbstractScene{

	private BigDataViewer bigDataViewer;

	private List<UnitCube> volumeBorders = new ArrayList<UnitCube>();

	@Override
	protected void disposeSpecial(GL2 gl2) {}


	@Override
	protected void resizeSpecial(GL2 gl2, int x, int y, int width, int height) {}


	public VolumeDataScene(BigDataViewer bdv){
		bigDataViewer = bdv;
	}


	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected void initSpecial(GL2 gl2, int width, int height){

		int numberOfSources = bigDataViewer.getViewer().getState().getSources().size();
		float colorLinearFactor = 1.f/numberOfSources;
		float r =0, g=1,b=1 ;
		for(int i = 0; i < bigDataViewer.getViewer().getState().getSources().size(); i++){

			UnitCube cubeShader = new UnitCube();
			volumeBorders.add(cubeShader);
			addSceneElement(cubeShader);
			cubeShader.init(gl2);
			cubeShader.setRenderWireframe(true);
			cubeShader.setColor(new Color(r,g,b,1));
			r+=colorLinearFactor;
			b-=colorLinearFactor;
		}
	}



	/**
	 * render the scene
	 * @param gl2
	 */
	protected void renderSpecial(GL2 gl2){

		AffineTransform3D viewerTransform = new AffineTransform3D();
		bigDataViewer.getViewer().getState().getViewerTransform(viewerTransform);

		ViewerState state = bigDataViewer.getViewer().getState();

		state.getViewerTransform(viewerTransform);
		List<SourceState<?>> sources = state.getSources();
		Matrix4 stateTrans = convertToJoglTransform(viewerTransform);

		int currentTimepoint = state.getCurrentTimepoint();
		int midMapLevel = 0;
		int i =0;
		for(SourceState<?> source : sources){
	

			RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, midMapLevel/*source.getSpimSource().getNumMipmapLevels()-1*/);

			//block transform
			AffineTransform3D sourceTransform3D = new AffineTransform3D();
			source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
			Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);

			//	short[][][] values = VolumeDataUtils.getDataBlock(ssource);
		//		VolumeDataUtils.writeParaviewFile(values, "parafile");
		//	if(1==1)
		//	throw new NullPointerException();

			//block size
			long[] min =  new long[3];
			long[] dim =  new long[3];
			ssource.min(min);

			IterableInterval<?> tmp = Views.flatIterable(ssource);
			tmp.dimensions(dim);			

			Matrix4 scale = new Matrix4();
			scale.loadIdentity();
			scale.scale(dim[0], dim[1], dim[2]);

			Matrix4 mat = copyMatrix(stateTrans);
			mat.multMatrix(sourceTransformation);
	
			mat.multMatrix(scale);

			UnitCube cubeShader = volumeBorders.get(i);

			//mat.loadIdentity();
			cubeShader.setModelTransformations(mat);
			i++;
		}
	}
}
