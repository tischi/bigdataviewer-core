package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.awt.Color;
import java.nio.FloatBuffer;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.UnitCubeShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;
import bdv.jogl.VolumeRenderer.GLErrorHandler;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.UnitCubeShaderSource;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * class to render a cube in gl
 * @author michael
 *
 */
public class UnitCube extends AbstractShaderSceneElement{

	private boolean renderWireframe = false;
	
	private boolean colorNeedsUpdate = true;
	
	private float[] coordinates = GeometryUtils.getUnitCubeVerticesTriangles(); 

	private Color color = new Color(1f, 1f, 1f, 1f);
	
	private UnitCubeShaderSource source = new UnitCubeShaderSource();

	@Override
	protected UnitCubeShaderSource getSource() {
		return source;
	};
	
	protected void updateShaderAttributesSubClass(GL4 gl2){
		if(!colorNeedsUpdate){
			return;
		}
		float[] rgba = getNormalizedColor(color);
		gl2.glUniform4fv(getLocation(suvColor),1,rgba,0);
		colorNeedsUpdate = false;
	}

	
	protected void generateIdMappingSubClass(GL4 gl){
		mapUniforms(gl, new String[]{suvColor});
	}


	/**
	 * @return the renderWireframe
	 */
	public boolean isRenderWireframe() {
		return renderWireframe;
	}

	
	/**
	 * @param renderWireframe the renderWireframe to set
	 */
	public void setRenderWireframe(boolean renderWireframe) {
		this.renderWireframe = renderWireframe;
	}
	
	
	protected void renderSubClass(GL4 gl2){
		GLErrorHandler.assertGL(gl2);
		
		if(isRenderWireframe()){
			gl2.glDisable(GL4.GL_DEPTH_TEST);
			gl2.glPolygonMode( GL4.GL_FRONT_AND_BACK, GL4.GL_LINE );
		}
		GLErrorHandler.assertGL(gl2);
		gl2.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0,coordinates.length/3);
		GLErrorHandler.assertGL(gl2);
		if(isRenderWireframe()){
			gl2.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);
		}
		GLErrorHandler.assertGL(gl2);
	}

	
	protected void updateVertexBufferSubClass(GL4 gl2, VertexAttribute position){
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		position.setAttributeValues(gl2, bufferData);
	}


	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	
	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
		this.colorNeedsUpdate = true;
	}

	@Override
	protected void disposeSubClass(GL4 gl2) {
		colorNeedsUpdate = true;
	};
	
	protected int getVertexBufferSize(){
		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}
}
