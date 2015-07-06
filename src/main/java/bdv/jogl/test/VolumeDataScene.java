package bdv.jogl.test;

import java.awt.Color;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
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
			sceneElements.add(cubeShader);
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

		int currentTimepoint = state.getCurrentTimepoint();
		int midMapLevel = 0;
		int i =0;
		for(SourceState<?> source : sources){
			Matrix4 mat = convertToJoglTransform(viewerTransform);

			RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, midMapLevel);

			//block transform
			AffineTransform3D sourceTransform3D = new AffineTransform3D();
			source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
			Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);

			//block size
			long[] min =  new long[3];
			long[] max =  new long[3];
			ssource.min(min);
			ssource.max(max);

			Matrix4 scale = new Matrix4();
			scale.loadIdentity();
			scale.scale(max[0] - min[0],max[1] - min[1] ,max[2] - min[2]);

			mat.multMatrix(sourceTransformation);
			mat.multMatrix(scale);

			ISceneElements cubeShader = sceneElements.get(i);

			//mat.loadIdentity();
			cubeShader.setModelTransformations(mat);
			i++;
		}
	}
}
