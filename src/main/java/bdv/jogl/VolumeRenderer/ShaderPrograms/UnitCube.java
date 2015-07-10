package bdv.jogl.shader;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.test.AbstractShaderSceneElement;

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
		aMap.put(GL2.GL_VERTEX_SHADER, "UnitCubeVertexShader.glsl");
		aMap.put(GL2.GL_FRAGMENT_SHADER, "UnitCubeFragmentShader.glsl");
		shaderFiles = Collections.unmodifiableMap(aMap);
	}	

	private float[] coordinates = getBufferVertices(); 

	private Color color = new Color(1f, 1f, 1f, 1f);

	protected void updateShaderAttributesSubClass(GL2 gl2,final Map<String, Integer> shaderVariableMapping){
		gl2.glUniform4f(shaderVariableMapping.get(shaderVariableColor), color.getRed()/255,color.getGreen()/255,color.getBlue()/255,color.getAlpha()/255);
	}

	
	protected void generateIdMappingSubClass(GL2 gl2,Map<String, Integer> shaderVariableMapping,final ShaderProgram shaderProgram){
		int colorID = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableColor);
		shaderVariableMapping.put(shaderVariableColor, colorID);
	}


	private float[] getBufferVertices(){
		float [] array = {
				0,0,0,
				1,0,0,
				1,1,0,
				0,1,0,

				0,0,0,
				0,1,0,
				0,1,1,
				0,0,1,

				0,0,0,
				1,0,0,
				1,0,1,
				0,0,1,

				1,0,0,
				1,0,1,
				1,1,1,
				1,1,0,

				1,0,1,
				0,0,1,
				0,1,1,
				1,1,1,

				0,1,0,
				1,1,0,
				1,1,1,
				0,1,1,
		};
		return array;
	}

	
	protected void renderSubClass(GL2 gl2){
		gl2.glDrawArrays(GL2.GL_QUADS, 0,coordinates.length/3);
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
