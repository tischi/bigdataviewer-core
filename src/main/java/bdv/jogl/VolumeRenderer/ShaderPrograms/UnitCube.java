package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.awt.Color;
import java.nio.FloatBuffer;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.UnitCubeShaderSource.*;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.UnitCubeShaderSource;
import bdv.jogl.VolumeRenderer.utils.GeometryUtils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

/**
 * class to render a cube in gl
 * @author michael
 *
 */
public class UnitCube extends AbstractShaderSceneElement{

	private boolean renderWireframe = false;
	
	private float[] coordinates = GeometryUtils.getUnitCubeVerticesQuads(); 

	private Color color = new Color(1f, 1f, 1f, 1f);
	
	private UnitCubeShaderSource source = new UnitCubeShaderSource();

	public UnitCube(){
		for(ShaderCode code: source.getShaderCodes()){
			shaderCodes.add(code);
		}
	}
	
	protected void updateShaderAttributesSubClass(GL2 gl2){
		gl2.glUniform4f(getLocation(suvColor), color.getRed()/255,color.getGreen()/255,color.getBlue()/255,color.getAlpha()/255);
	}

	
	protected void generateIdMappingSubClass(GL2 gl2){
		mapUniforms(gl2, new String[]{suvColor});
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
	
	
	protected void renderSubClass(GL2 gl2){
		int[] oldFrontBack={GL2.GL_FILL,GL2.GL_FILL};
		
		if(isRenderWireframe()){
			gl2.glGetIntegerv(GL2.GL_POLYGON_MODE, oldFrontBack,0);
			gl2.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_LINE );
		}
		gl2.glDrawArrays(GL2.GL_QUADS, 0,coordinates.length/3);
		if(isRenderWireframe()){
			gl2.glPolygonMode(GL2.GL_FRONT, oldFrontBack[0]);
			gl2.glPolygonMode( GL2.GL_BACK, oldFrontBack[1] );
		}
	}

	
	protected void updateVertexBufferSubClass(GL2 gl2, VertexAttribute position){
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
	}

	
	protected int getVertexBufferSize(){
		return coordinates.length * Buffers.SIZEOF_FLOAT;
	}
}
