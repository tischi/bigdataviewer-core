package bdv.jogl.VolumeRenderer.Scene;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.view.Views;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.Camera;
import bdv.jogl.VolumeRenderer.CameraListener;
import bdv.jogl.VolumeRenderer.ShaderPrograms.SimpleVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.UnitCube;

import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataUtils;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;

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
	
	private List<SimpleVolumeRenderer> volumeRenderes = new ArrayList<SimpleVolumeRenderer>();

	private Matrix4 globalModelTransformation = getNewIdentityMatrix();
	
	@Override
	protected void disposeSpecial(GL2 gl2) {}


	@Override
	protected void resizeSpecial(GL2 gl2, int x, int y, int width, int height) {}


	public VolumeDataScene(BigDataViewer bdv){
		bigDataViewer = bdv;
	}

	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 */
	private void initLocalCamera(Camera camera2, int width, int height, int[] dim){
		
		 
		
		float[] center = {dim[0] ,dim[1],dim[2]};
		
		
		float[] eye = {center[0],center[1],	center[2] - 30f * (dim[2])};
		
		
		camera2.addCameraListener(new CameraListener() {

			@Override
			public void viewMatrixUpdate(Matrix4 matrix) {

				//update all views
				for(ISceneElements element : sceneElements){
					element.setView(matrix);
				}
			}

			@Override
			public void projectionMatrixUpdate(Matrix4 matrix) {

				//update all projections
				for(ISceneElements element : sceneElements){
					element.setProjection(matrix);
				}
			}
		});
		camera2.setAlpha(45);
		camera2.setWidth(width);
		camera2.setHeight(height);
		camera2.setZfar(5000);
		camera2.setZnear(1);
		camera2.setLookAtPoint(center);
		camera2.setEyePoint(eye);
		camera2.setUpVector(new float[]{0,-1,0});
		camera2.update();
	}
	

	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected void initSpecial(GL2 gl2, int width, int height){
	
		sceneElements.clear();
		int numberOfSources = bigDataViewer.getViewer().getState().getSources().size();
		float colorLinearFactor = 1.f/numberOfSources;
		float r =0, g=1,b=1 ;
		int[] dimensions = {0,0,0};
		
		for(SourceState<?> source: bigDataViewer.getViewer().getState().getSources()){
			
			
			
			//create borders
			UnitCube cubeShader = new UnitCube();
			volumeBorders.add(cubeShader);
			addSceneElement(cubeShader);
			cubeShader.init(gl2);
			cubeShader.setRenderWireframe(true);
			cubeShader.setColor(new Color(r,g,b,1));
			r+=colorLinearFactor;
			b-=colorLinearFactor;
			
			//create vRenderer
			SimpleVolumeRenderer vRenderer = new SimpleVolumeRenderer();
			volumeRenderes.add(vRenderer);
			addSceneElement(vRenderer);
			vRenderer.init(gl2);
			
			RandomAccessibleInterval<?> data = source.getSpimSource().getSource(0, source.getSpimSource().getNumMipmapLevels()-1);
			long[] dim = new long[3];
			int[] dimI = new int[3];
			data.dimensions(dim);
			for(int i = 0; i<dim.length; i++){
				dimI[i] = (int)dim[i];
				dimensions[i]  =Math.max(dimensions[i], dimI[i]);
	
			}
			vRenderer.setDimension(dimI);
			VolumeDataBlock vData =VolumeDataUtils.getDataBlock(source.getSpimSource().getSource(0, source.getSpimSource().getNumMipmapLevels()-1));
			vRenderer.setData(vData);
			break;
		}
		
		initLocalCamera(camera, width, height, dimensions);
	
	}


	/**
	 * render the scene
	 * @param gl2
	 */
	protected void renderSpecial(GL2 gl2){

	

		ViewerState state = bigDataViewer.getViewer().getState();

		List<SourceState<?>> sources = state.getSources();

		int currentTimepoint = state.getCurrentTimepoint();
		
		int i =0;
		for(SourceState<?> source : sources){
	
			int midMapLevel = source.getSpimSource().getNumMipmapLevels()-1;
			RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, midMapLevel/*source.getSpimSource().getNumMipmapLevels()-1*/);

			//block transform
			AffineTransform3D sourceTransform3D = new AffineTransform3D();
			source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
			Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);

	
			//block size
			long[] min =  new long[3];
			long[] dim =  new long[3];
			ssource.min(min);

			IterableInterval<?> tmp = Views.flatIterable(ssource);
			tmp.dimensions(dim);			

			/*float[] values = VolumeDataUtils.getDataBlock(ssource);
			VolumeDataUtils.writeParaviewFile(values, dim,"parafile");
			if(1==1)
				throw new NullPointerException();*/
			
			Matrix4 scale = new Matrix4();
			scale.loadIdentity();
			scale.scale(dim[0], dim[1], dim[2]);

			UnitCube cubeShader = volumeBorders.get(i);
			
			Matrix4 modelMatrix = getNewIdentityMatrix();
			modelMatrix=copyMatrix(globalModelTransformation);
			modelMatrix.multMatrix(copyMatrix(sourceTransformation));
			modelMatrix.multMatrix(copyMatrix(scale));	
			
			volumeRenderes.get(i).setModelTransformations(modelMatrix);
			
			//mat.loadIdentity();
			cubeShader.setModelTransformations(modelMatrix);
			i++;
			break;
		}
	}


	/**
	 * @param globalModelTransformation the globalModelTransformation to set
	 */
	public void setGlobalModelTransformation(Matrix4 globalModelTransformation) {
		this.globalModelTransformation = globalModelTransformation;
	}
}
