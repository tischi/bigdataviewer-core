package bdv.jogl.test;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;


/**
 * Test scene
 * @author michael
 *
 */
public class OneTriangle {


	/**
	 * render triangle code
	 * @param gl2 gl context
	 * @param width view width
	 * @param height view height
	 */
	public static void render( GL2 gl2) {
		gl2.glClear( GL.GL_COLOR_BUFFER_BIT );

		// draw a triangle filling the window
		//gl2.glLoadIdentity();
		gl2.glBegin( GL.GL_TRIANGLES );
		gl2.glColor3f( 1, 0, 0 );
		gl2.glVertex3f( 0, 0,0 );
		gl2.glColor3f( 0, 1, 0 );
		gl2.glVertex3f( 1, 0,0 );
		gl2.glColor3f( 0, 0, 1 );
		gl2.glVertex3f( 0.5f,1,0 );
		gl2.glEnd();
	}
}
