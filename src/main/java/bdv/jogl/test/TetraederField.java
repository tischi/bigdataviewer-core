package bdv.jogl.test;

import java.awt.image.renderable.RenderableImage;

import com.jogamp.opengl.GL2;

/**
 * Class to render a cubic field of normal tetraedars to test the view transformations
 * @author michael
 *
 */
public class TetraederField {
	/**
	 * render field
	 * @param gl2
	 */
	public static void render(GL2 gl2){
		final int count =10;
		
		//3 dim
		gl2.glPushMatrix();
		for (int z =0; z< count ;z++){

		
			gl2.glPushMatrix();
			for (int y =0; y< count ;y++){
				
				gl2.glPushMatrix();
				for (int x =0; x< count ;x++){
					UnitTetraeder.render(gl2);
					gl2.glTranslatef(2, 0, 0);
					
				}
				gl2.glPopMatrix();
				gl2.glTranslatef(0, 2, 0);
				
			}
			gl2.glPopMatrix();
			gl2.glTranslatef(0, 0, 2);
		}
		gl2.glPopMatrix();
		
	}
}
