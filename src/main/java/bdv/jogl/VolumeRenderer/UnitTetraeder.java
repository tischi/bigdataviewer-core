package bdv.jogl.VolumeRenderer;

import com.jogamp.opengl.GL2;


/**
 * A single tetraeder to render
 * @author michael
 *
 */
public class UnitTetraeder{
	/**
	 * render the unit tetraeder
	 * @param gl2 gl context
	 */
	public static void render(GL2 gl2){
		float[][] positions = {{0,0,0},
				{1,0,0},
				{0.5f,0,1},
				{0.5f,1,0.5f}};
		// color to white
		gl2.glColor3f(1,1,1);
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		gl2.glVertex3fv(positions[0], 0);
		gl2.glVertex3fv(positions[1], 0);
		gl2.glVertex3fv(positions[3], 0);
		gl2.glVertex3fv(positions[2], 0);
		gl2.glVertex3fv(positions[0], 0);
		gl2.glVertex3fv(positions[1], 0);
		gl2.glEnd();
	}
}

