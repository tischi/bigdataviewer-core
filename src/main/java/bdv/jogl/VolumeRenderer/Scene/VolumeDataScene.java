package bdv.jogl.VolumeRenderer.Scene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.view.Views;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.Camera;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.SimpleVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.UnitCube;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AverageVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MinimumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunctionAdapter;
import bdv.jogl.VolumeRenderer.gui.SceneControlsWindow;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.AggregatorManager;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.IVolumeAggregationListener;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;
import static bdv.jogl.VolumeRenderer.utils.GeometryUtils.*;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.geom.AABBox;

/**
 * Describes a scene for volume data with multiple scene objects
 * @author michael
 *
 */
public class VolumeDataScene extends AbstractScene{

	private BigDataViewer bigDataViewer;

	private List<UnitCube> volumeBorders = new ArrayList<UnitCube>();

	private List<SimpleVolumeRenderer> volumeRenderes = new ArrayList<SimpleVolumeRenderer>();

	private final TransferFunction1D transferFunction = new TransferFunction1D();
	
	private final VolumeDataManager dataManager = new VolumeDataManager();
	
	private final MultiVolumeRenderer multiVolumeRenderer = new MultiVolumeRenderer(transferFunction,dataManager);

	private Matrix4 globalModelTransformation = getNewIdentityMatrix();

	private int latestRenderTimePoint = -1;

	private int currentActiveSource = -1;

	//private UnitCube boundingVolume =new UnitCube();



	private SceneControlsWindow controls;

	private final boolean single = false;

	private void cleanUpSceneElements(){
		//	volumeBorders.clear();
		volumeRenderes.clear();
	}

	@Override
	protected void disposeSpecial(GL2 gl2) {
		controls.destroyTFWindow();
		controls = null;
		cleanUpSceneElements();

	}

	@Override
	protected void resizeSpecial(GL2 gl2, int x, int y, int width, int height) {}


	public VolumeDataScene(BigDataViewer bdv){
		bigDataViewer = bdv;
		transferFunction.addTransferFunctionListener( new TransferFunctionAdapter() {

			@Override
			public void colorChanged(final TransferFunction1D function) {

				//trigger scene update
				fireNeedUpdateAll();
			}
			
			@Override
			public void samplerChanged(TransferFunction1D transferFunction1D) {
				fireNeedUpdateAll();
			}
		});
	}

	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 */
	private void initLocalCamera(Camera camera2, int width, int height, AABBox volumeBoundingBox){

		float[] center = volumeBoundingBox.getCenter();

		float[] eye = {center[0],center[1],	center[2] - 7f * (volumeBoundingBox.getDepth())};


		camera2.setAlpha(45);
		camera2.setWidth(width);
		camera2.setHeight(height);
		camera2.setZfar(5000);
		camera2.setZnear(1);
		camera2.setLookAtPoint(center);
		camera2.setEyePoint(eye);
		camera2.setUpVector(new float[]{0,-1,0});
		camera2.init();
	}

	private int getMidmapLevel(final SourceState<?> source){
		return source.getSpimSource().getNumMipmapLevels()-1;
	}

	private void initBlending(GL2 gl2){
		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected void initSpecial(GL2 gl2, int width, int height){

		initBlending(gl2);
		
		int currentRenderTimePoint = bigDataViewer.getViewer().getState().getCurrentTimepoint();
		float[][] minMaxDimensions = {
				{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
				{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}
				};

		int j =-1;

		if(!single){
			addSceneElement(multiVolumeRenderer);
			multiVolumeRenderer.setTransferFunction(transferFunction);
			multiVolumeRenderer.init(gl2);
			initBoundingVolumeCube(gl2);
		}
		for(SourceState<?> source: bigDataViewer.getViewer().getState().getSources()){


			j++;

			//create borders
			UnitCube cubeShader = new UnitCube();
			volumeBorders.add(cubeShader);
			addSceneElement(cubeShader);
			cubeShader.init(gl2);
			cubeShader.setRenderWireframe(true);
			cubeShader.setColor(getColorOfVolume(j));

			if(!source.isCurrent()){
				continue;
			}
			currentActiveSource = j;

			//create vRenderer
			if(single ){
				SimpleVolumeRenderer vRenderer = new SimpleVolumeRenderer();
				vRenderer.setTransferFunction(transferFunction);
				volumeRenderes.add(vRenderer);
				addSceneElement(vRenderer);
				vRenderer.init(gl2);
			}

			int midMapLevel = getMidmapLevel(source);
			AffineTransform3D sourceTransformation = new AffineTransform3D();
			source.getSpimSource().getSourceTransform(currentRenderTimePoint, midMapLevel, sourceTransformation);
			Matrix4 transformation = convertToJoglTransform(sourceTransformation);
			RandomAccessibleInterval<?> data = source.getSpimSource().getSource(currentRenderTimePoint, midMapLevel);
			
			
			//long[] dim = new long[3];
			long[] dim = new long[]{1,1,1};
			data.dimensions(dim);
			AABBox currentBox = getAABBOfTransformedBox(dim, transformation);
			for(int i = 0; i<dim.length; i++){
				minMaxDimensions[0][i]  =Math.min(minMaxDimensions[0][i], currentBox.getLow()[i]);
				minMaxDimensions[1][i]  =Math.max(minMaxDimensions[1][i], currentBox.getHigh()[i]);
			}
		}
		AABBox volumeBoundingBox = new AABBox(minMaxDimensions[0],minMaxDimensions[1]);
		initLocalCamera(camera, width, height, volumeBoundingBox);

		
		//window
		Set<AbstractVolumeAccumulator> acc =  new HashSet<AbstractVolumeAccumulator>();
		AverageVolumeAccumulator avg = new AverageVolumeAccumulator();
		acc.add(avg);
		acc.add(new MaximumVolumeAccumulator());
		acc.add(new MinimumVolumeAccumulator());
		acc.add(new MaxDifferenceAccumulator());
		AggregatorManager aggm = new AggregatorManager(acc);
		aggm.setActiveAcumulator(avg.getFunctionName());
		aggm.addListener(new IVolumeAggregationListener() {
			
			@Override
			public void aggregationChanged(AbstractVolumeAccumulator acc) {
				multiVolumeRenderer.getSource().setAccumulator(acc);
				fireNeedUpdateAll();
			}
		});
		if(!single){
			addSceneElement(multiVolumeRenderer);
			multiVolumeRenderer.setTransferFunction(transferFunction);
			multiVolumeRenderer.init(gl2);
			initBoundingVolumeCube(gl2);
		}
		controls =new SceneControlsWindow(transferFunction,aggm, dataManager);
	}


	private void initBoundingVolumeCube(GL2 gl2) {
		/*	addSceneElement(boundingVolume);

		boundingVolume.init(gl2);
		boundingVolume.setRenderWireframe(true);
		boundingVolume.setColor(Color.yellow);*/
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
		final float offsetFactor = 1.01f;

		for(SourceState<?> source : sources){
			
			i++;
			UnitCube cubeShader = volumeBorders.get(i);

			//block transform
			int midMapLevel = getMidmapLevel(source);
			AffineTransform3D sourceTransform3D = new AffineTransform3D();
			source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
			Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);


			//block size
			RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, midMapLevel);
			long[] min =  new long[3];
			long[] dim =  new long[3];
			ssource.min(min);

			IterableInterval<?> tmp = Views.flatIterable(ssource);
			tmp.dimensions(dim);	
			Matrix4 scale = new Matrix4();
			scale.loadIdentity();
			scale.scale((float)(dim[0])*offsetFactor, (float)(dim[1])*offsetFactor, (float)(dim[2])*offsetFactor);



			Matrix4 modelMatrix = getNewIdentityMatrix();
			modelMatrix=copyMatrix(globalModelTransformation);
			modelMatrix.multMatrix(copyMatrix(sourceTransformation));
			modelMatrix.multMatrix(copyMatrix(scale));	
			cubeShader.setModelTransformation(modelMatrix);


			//no inactive sources
			if(single){
				if(!source.isCurrent()){
					continue;
				}
			}

			if(latestRenderTimePoint != currentTimepoint || 
					!dataManager.getVolumeKeys().contains(i)){
				/*float[] values = VolumeDataUtils.getDataBlock(ssource);
			VolumeDataUtils.writeParaviewFile(values, dim,"parafile");
			if(1==1)
				throw new NullPointerException();*/
			
				VolumeDataBlock data = getDataBlock(source.getSpimSource().getSource(currentTimepoint, midMapLevel));




				data.localTransformation =sourceTransformation;
				if(single){
					volumeRenderes.get(0).setModelTransformation(modelMatrix);
					if( i == currentActiveSource){
						volumeRenderes.get(0).setData(data);	
						currentActiveSource = i;
					}
				}else{
					dataManager.setVolume(i, data);		
				}
			}
		}
		latestRenderTimePoint = currentTimepoint;
		if(!single){
			multiVolumeRenderer.setModelTransformation(globalModelTransformation);
			/*	multiVolumeRenderer.update(gl2);
			Matrix4 model = copyMatrix(multiVolumeRenderer.getModelTransformation());
			model.multMatrix(multiVolumeRenderer.getDrawCubeTransformation());
			boundingVolume.setModelTransformation(model);*/
		}
	}


	/**
	 * @param globalModelTransformation the globalModelTransformation to set
	 */
	public void setGlobalModelTransformation(Matrix4 globalModelTransformation) {
		this.globalModelTransformation = globalModelTransformation;
	}
}
