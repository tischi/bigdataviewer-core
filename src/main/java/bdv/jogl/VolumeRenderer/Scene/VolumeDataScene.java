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

	private Map<Integer,UnitCube> volumeBorders = new HashMap<Integer, UnitCube>();

	private VolumeDataManager dataManager;

	private boolean showVolumes = true;
	//private UnitCube boundingVolume =new UnitCube();

	@Override
	protected void disposeSpecial(GL2 gl2) {
	}

	public void enableVolumeBorders(boolean flag){
		showVolumes = flag;
		for(UnitCube c : volumeBorders.values()){
			c.setEnabled(flag);
		}
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
		Matrix4 modelMatrix = calcVolumeTransformation(data);
		cubeShader.setProjection(getCamera().getProjectionMatix());
		cubeShader.setView(getCamera().getViewMatrix());
		cubeShader.setModelTransformation(modelMatrix);
		cubeShader.setEnabled(showVolumes);
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


	public VolumeDataScene( VolumeDataManager dataManager, MultiVolumeRenderer renderer){
		setDataManager(dataManager);
		addSceneElement(renderer);
	}

	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 */
	public void initLocalCamera(Camera camera2, int width, int height){

		camera2.setAlpha(45);
		camera2.setWidth(width);
		camera2.setHeight(height);
		camera2.setZfar(5000);
		camera2.setZnear(1);
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
		




		initBoundingVolumeCube(gl2);

		
		initLocalCamera(camera, width,height);
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
}
