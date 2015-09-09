package bdv.jogl.VolumeRenderer.Scene;


import java.util.HashMap;

import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.Camera;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.UnitCube;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;
import static bdv.jogl.VolumeRenderer.utils.GeometryUtils.*;
import bdv.viewer.state.SourceState;
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

	private Map<Integer,UnitCube> volumeBorders = new HashMap<Integer, UnitCube>();

	private VolumeDataManager dataManager;
	
	private final MultiVolumeRenderer multiVolumeRenderer;

	private Matrix4 globalModelTransformation = getNewIdentityMatrix();



	//private UnitCube boundingVolume =new UnitCube();



	@Override
	protected void disposeSpecial(GL2 gl2) {
	}

	private void addNewCubeBorderShader(Integer id){
		UnitCube cubeShader = new UnitCube();
		volumeBorders.put(id,cubeShader);
		addSceneElement(cubeShader);
		cubeShader.setRenderWireframe(true);
		cubeShader.setColor(getColorOfVolume(id));
		

	}
	
	private void updateCubeBorderShader(Integer id, final VolumeDataBlock data){
		UnitCube cubeShader = volumeBorders.get(id);
		Matrix4 modelMatrix = getNewIdentityMatrix();
		modelMatrix.multMatrix(copyMatrix(data.localTransformation));
		long dim[] = data.dimensions.clone();
		modelMatrix.scale(dim[0], dim[1], dim[2]);	
		cubeShader.setProjection(getCamera().getProjectionMatix());
		cubeShader.setView(getCamera().getViewMatrix());
		cubeShader.setModelTransformation(modelMatrix);
	}
	
	private void setDataManager(final VolumeDataManager manager){
		dataManager = manager;
		
		dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
			
			@Override
			public void addedData(Integer id) {
				//add cubes
				addNewCubeBorderShader(id);
			}
			
			@Override 
			public void dataUpdated(Integer id) {
				updateCubeBorderShader(id, dataManager.getVolume(id));
				fireNeedUpdateAll();
			}
		});
	}
	
	@Override
	protected void resizeSpecial(GL2 gl2, int x, int y, int width, int height) {}


	public VolumeDataScene(BigDataViewer bdv, VolumeDataManager dataManager, MultiVolumeRenderer renderer){
		
		bigDataViewer = bdv;
		setDataManager(dataManager);
		multiVolumeRenderer = renderer;
		
		addSceneElement(multiVolumeRenderer);
		
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
	
	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected void initSpecial(GL2 gl2, int width, int height){
		
		int currentRenderTimePoint = bigDataViewer.getViewer().getState().getCurrentTimepoint();
		float[][] minMaxDimensions = {
				{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
				{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}
				};



			initBoundingVolumeCube(gl2);
		for(SourceState<?> source: bigDataViewer.getViewer().getState().getSources()){

			if(!source.isCurrent()){
				continue;
			}
			//create vRenderer
	

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

	}


	/**
	 * @param globalModelTransformation the globalModelTransformation to set
	 */
	public void setGlobalModelTransformation(Matrix4 globalModelTransformation) {
		this.globalModelTransformation = globalModelTransformation;
	}
}
