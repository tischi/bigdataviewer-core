package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.awt.Color;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.utils.GeometryUtils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 * class to render a cube in gl
 * @author michael
 *
 */
public class UnitCube extends AbstractShaderSceneElement{

	public static final String shaderVariableColor = "inColor";
	static {
		Map<Integer, String> aMap = new HashMap<Integer, String>();
		aMap.put(GL2.GL_VERTEX_SHADER, "glsl"+File.separator+"UnitCubeVertexShader.glsl");
		aMap.put(GL2.GL_FRAGMENT_SHADER, "glsl"+File.separator+"UnitCubeFragmentShader.glsl");
		shaderFiles = Collections.unmodifiableMap(aMap);
	}	

	private boolean renderWireframe = false;
	
	private float[] coordinates = GeometryUtils.getUnitCubeVerticesQuads(); 

	private Color color = new Color(1f, 1f, 1f, 1f);

	protected void updateShaderAttributesSubClass(GL2 gl2,final Map<String, Integer> shaderVariableMapping){
		gl2.glUniform4f(shaderVariableMapping.get(shaderVariableColor), color.getRed()/255,color.getGreen()/255,color.getBlue()/255,color.getAlpha()/255);
	}

	
	protected void generateIdMappingSubClass(GL2 gl2,Map<String, Integer> shaderVariableMapping,final ShaderProgram shaderProgram){
		int colorID = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableColor);
		shaderVariableMapping.put(shaderVariableColor, colorID);
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
	
	
	protected void renderSubClass(GL2 gl2,Map<String, Integer> shaderVariableMapping){
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

	
	protected void updateVertexBufferSubClass(GL2 gl2){
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();

		gl2.glBufferSubData(
				GL2.GL_ARRAY_BUFFER,
				0, 
				bufferData.capacity() * Buffers.SIZEOF_FLOAT, 
				bufferData);		
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
