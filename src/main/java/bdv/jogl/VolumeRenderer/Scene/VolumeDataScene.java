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
	
	private int latestRenderTimePoint = 0;
	
	private int currentActiveSource = 0;

	private void cleanUpSceneElements(){
		volumeBorders.clear();
		volumeRenderes.clear();
	}
	
	
	@Override
	protected void disposeSpecial(GL2 gl2) {
		cleanUpSceneElements();
	}


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
	
	private int getMidmapLevel(final SourceState<?> source){
		return source.getSpimSource().getNumMipmapLevels()-1;
	}
	
	
	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected void initSpecial(GL2 gl2, int width, int height){

		int numberOfSources = bigDataViewer.getViewer().getState().getSources().size();
		latestRenderTimePoint = bigDataViewer.getViewer().getState().getCurrentTimepoint();
		float colorLinearFactor = 1.f/numberOfSources;
		float r =0, g=1,b=1 ;
		int[] dimensions = {0,0,0};

		int j =-1;
		for(SourceState<?> source: bigDataViewer.getViewer().getState().getSources()){

			j++;
			if(!source.isCurrent()){
				continue;
			}
			currentActiveSource = j;
			
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

		
			int midMapLevel = getMidmapLevel(source);
			VolumeDataBlock vData =VolumeDataUtils.getDataBlock(source.getSpimSource().getSource(latestRenderTimePoint, midMapLevel));
			vRenderer.setData(vData);
		
			RandomAccessibleInterval<?> data = source.getSpimSource().getSource(latestRenderTimePoint, midMapLevel);
			long[] dim = new long[3];
			int[] dimI = new int[3];
			data.dimensions(dim);
			for(int i = 0; i<dim.length; i++){
				dimI[i] = (int)dim[i];
				dimensions[i]  =Math.max(dimensions[i], dimI[i]);

			}
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

		int i =-1;
		
		for(SourceState<?> source : sources){

			i++;
			//no inactive sources
			if(!source.isCurrent()){
				continue;
			}
			
			int midMapLevel = getMidmapLevel(source);
			RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, midMapLevel);

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

			UnitCube cubeShader = volumeBorders.get(0);

			Matrix4 modelMatrix = getNewIdentityMatrix();
			modelMatrix=copyMatrix(globalModelTransformation);
			modelMatrix.multMatrix(copyMatrix(sourceTransformation));
			modelMatrix.multMatrix(copyMatrix(scale));	

			volumeRenderes.get(0).setModelTransformations(modelMatrix);

			cubeShader.setModelTransformations(modelMatrix);
			if(latestRenderTimePoint != currentTimepoint|| i != currentActiveSource){
				
				latestRenderTimePoint = currentTimepoint;
				currentActiveSource = i;
				
				volumeRenderes.get(0).setData(VolumeDataUtils.getDataBlock(source.getSpimSource().getSource(latestRenderTimePoint, midMapLevel)));
			}
			

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
