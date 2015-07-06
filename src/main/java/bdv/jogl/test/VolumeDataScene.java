package bdv.jogl.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import bdv.BigDataViewer;
import bdv.jogl.shader.UnitCube;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import static bdv.jogl.test.MatrixUtils.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;

/**
 * Describes a scene for volume data with multiple scene objects
 * @author michael
 *
 */
public class VolumeDataScene {

	private Camera camera = new Camera();

	private BigDataViewer bigDataViewer;

	private List<UnitCube> sceneElements = new ArrayList<UnitCube>();

	public VolumeDataScene(BigDataViewer bdv){
		bigDataViewer = bdv;
	}

	/**
	 * @return the camera
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * does std gl camera initializations
	 * @param camera2 camera to init
	 */
	private void initLocalCamera(Camera camera2, int width, int height){
		float[] center = {50,50,50};
		float[] eye = {50,50,300};

		camera2.addCameraListener(new CameraListener() {

			@Override
			public void viewMatrixUpdate(Matrix4 matrix) {

				//update all views
				for(UnitCube element : sceneElements){
					element.setView(matrix);
				}
			}

			@Override
			public void projectionMatrixUpdate(Matrix4 matrix) {

				//update all projections
				for(UnitCube element : sceneElements){
					element.setProjection(matrix);
				}
			}
		});
		camera2.setAlpha(45);
		camera2.setWidth(width);
		camera2.setHeight(height);
		camera2.setZfar(100000);
		camera2.setZnear(0);
		camera2.setLookAtPoint(center);
		camera2.setEyePoint(eye);
		camera2.update();
	}


	/**
	 * @param camera the camera to set
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	
	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	public void init(GL2 gl2, int width, int height){

		initLocalCamera(camera,width,height);
		sceneElements.clear();
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
	public void render(GL2 gl2){
		gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);


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



			UnitCube cubeShader = sceneElements.get(i);

			//mat.loadIdentity();
			cubeShader.setModelTransformations(mat);
			cubeShader.update(gl2);
			cubeShader.render(gl2);

			i++;
		}

	}

	/**
	 * resizes the scene and the gl shader context
	 * @param gl2
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void resize(GL2 gl2,int x, int y, int width, int height){
		camera.setWidth(width);
		camera.setHeight(height);
		camera.updatePerspectiveMatrix();
	}

	/**
	 * releases gl resources
	 * @param gl2
	 */
	public void dispose(GL2 gl2){
		for(UnitCube c : sceneElements){
			c.disposeGL(gl2);
		}
	}
}
