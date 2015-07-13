package bdv.jogl.VolumeRenderer.utils;

/**
 * Utils for standard geometry objects
 * @author michael
 *
 */
public class GeometryUtils {

	/**
	 * Returns the Vertices of a unit cube, renderable by GL_Triangles_Strip
	 * (http://doc.qt.io/qt-5/qtopengl-cube-example.html)
	 * @return
	 */
	public static float[] getUnitCubeVertices(){
		float [] array = {
				0,0,0,
				1,0,0,
				0,1,0,
				1,1,0,
				
				1,0,0,
				1,0,1,
				1,1,0,
				1,1,1,
				
				1,0,1,
				0,0,1,
				1,1,1,
				0,1,1,
		
				0,0,1,
				0,0,0,
				0,1,1,
				0,1,0,
				
				0,0,1,
				1,0,1,
				0,0,0,
				1,0,0,
				
				
				0,1,0,
				1,1,0,
				0,1,1,
				1,1,1
		};
		
		
		return array;
	}
}
