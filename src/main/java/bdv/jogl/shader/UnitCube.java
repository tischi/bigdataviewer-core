package bdv.jogl.shader;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bdv.jogl.test.AbstractShaderSceneElement;
import bdv.jogl.test.MatrixUtils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 * class to render a cube in gl
 * @author michael
 *
 */
public class UnitCube extends AbstractShaderSceneElement{
	
	public static final String shaderVariablePosition = "inPosition";

	public static final String shaderVariableProjectionMatrix = "inProjection";

	public static final String shaderVariableViewMatrix = "inView";

	public static final String shaderVariableModelMatrix = "inModel";

	public static final String shaderVariableColor = "inColor";

	private static final Map<Integer, String> shaderFiles;
	static {
		Map<Integer, String> aMap = new HashMap<Integer, String>();
		aMap.put(GL2.GL_VERTEX_SHADER, "UnitCubeVertexShader.glsl");
		aMap.put(GL2.GL_FRAGMENT_SHADER, "UnitCubeFragmentShader.glsl");
		shaderFiles = Collections.unmodifiableMap(aMap);
	}

	private Matrix4 projection = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 view = MatrixUtils.getNewIdentityMatrix();
	
	private Matrix4 modelTransformations = MatrixUtils.getNewIdentityMatrix();
	
	private Map<Integer, ShaderCode> shaderCodes = new HashMap<Integer, ShaderCode>();
	
	private Map<String, Integer> shaderVariableMapping = new HashMap<String, Integer>();

	private ShaderProgram shaderProgram= new ShaderProgram();

	private float[] coordinates = getBufferVertices(); 
	
	//private Camera camera = new Camera();
	
	private int vertexBufferId = 0;
	
	private int vertexArrayId = 0;

	private boolean renderWireframe = false;
	
	private Color color = new Color(1f, 1f, 1f, 1f);
	
	/**
	 * releases all gl resources
	 * @param gl2
	 */
	public void disposeGL(GL2 gl2){
		int[] buffers = {vertexBufferId};
		gl2.glDeleteBuffers(1, buffers,0);
		
		int[] arrays = {vertexArrayId};
		gl2.glDeleteVertexArrays(1, arrays, 0);
		
		shaderProgram.destroy(gl2);
	}
	
	/**
	 * initializes the shader program
	 * @param gl
	 */
	public void init(GL2 gl){
		
		initProgram(gl);

		generateIdMapping(gl);
		
		generateVertexBuffer(gl);
		
		updateVertexBuffer(gl);
	}
	
	/**
	 * update all shader variables 
	 */
	private void update(GL2 gl){
		updateShaderAttributes(gl);
		
		updateVertexBuffer(gl);
	}

	private void initProgram(GL2 gl2){

		//create program id 
		shaderProgram.init(gl2);
		
		//compile and attache shaders from files
		for(int key : shaderFiles.keySet()){
			String[] files = {shaderFiles.get(key)};
			shaderCodes.put(key,ShaderCode.create(gl2,key , 
					1, this.getClass(), files,
					false));
			
			shaderCodes.get(key).compile(gl2,System.err);
			
			//valid shader?
			if(!shaderCodes.get(key).isValid()){
				throw new IllegalArgumentException("No valid shader code in "+ shaderFiles.get(key));
			}
			
			shaderProgram.add(shaderCodes.get(key));
			
		}

		//link program
		shaderProgram.link(gl2, System.err);
		
		shaderProgram.useProgram(gl2, false);
	}
	
	private void generateVertexBuffer(GL2 gl2){

		//gen vertex array
		int[] vertexArrays  = new int[1];
		gl2.glGenVertexArrays(1, vertexArrays, 0);
		vertexArrayId = vertexArrays[0];
		
		//bind
		gl2.glBindVertexArray(vertexArrayId);
		
		//vertex buffer
		int[] vertexBufferObject = new int[1];
		gl2.glGenBuffers(1,vertexBufferObject,0 );
		vertexBufferId =  vertexBufferObject[0];
		
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBufferId);

		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, 
				coordinates.length * Buffers.SIZEOF_FLOAT,
				null, 
				GL2.GL_STATIC_DRAW);
	
		gl2.glEnableVertexAttribArray(shaderVariableMapping.get(shaderVariablePosition));
		
		gl2.glVertexAttribPointer(
				shaderVariableMapping.get(shaderVariablePosition), 
				3, 
				GL2.GL_FLOAT, 
				false, 
				0,//Buffers.SIZEOF_FLOAT*3, 
				0);
		
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		//release array
		gl2.glBindVertexArray(0);
		
	}
	
	private void generateIdMapping(GL2 gl2){
		shaderProgram.useProgram(gl2, true);
		
		//get ids
		int projectID = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableProjectionMatrix);
		shaderVariableMapping.put(shaderVariableProjectionMatrix, projectID);

		int viewID = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableViewMatrix);
		shaderVariableMapping.put(shaderVariableViewMatrix, viewID);
		
		int modelID = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableModelMatrix);
		shaderVariableMapping.put(shaderVariableModelMatrix, modelID);
		
		int colorID = gl2.glGetUniformLocation(shaderProgram.program(), shaderVariableColor);
		shaderVariableMapping.put(shaderVariableColor, colorID);
		
		int positionID =gl2.glGetAttribLocation(shaderProgram.program(), shaderVariablePosition);
		shaderVariableMapping.put(shaderVariablePosition, positionID);
		
		shaderProgram.useProgram(gl2, false);
	}

	private void updateShaderAttributes(GL2 gl2){
		
		shaderProgram.useProgram(gl2, true);
		
		//memcopy
		gl2.glUniform4f(shaderVariableMapping.get(shaderVariableColor), color.getRed()/255,color.getGreen()/255,color.getBlue()/255,color.getAlpha()/255);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableProjectionMatrix), 1, false, projection.getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableViewMatrix), 1, false, view.getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableModelMatrix), 1, false, modelTransformations.getMatrix(),0);
		
		shaderProgram.useProgram(gl2, false);
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

	private void updateVertexBuffer(GL2 gl2){
	
		shaderProgram.useProgram(gl2, true);
		
		gl2.glBindVertexArray(vertexArrayId);
		
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER,vertexBufferId);
	
		FloatBuffer bufferData = Buffers.newDirectFloatBuffer(coordinates);
		bufferData.rewind();
		
		gl2.glBufferSubData(
				GL2.GL_ARRAY_BUFFER,
				0, 
				bufferData.capacity() * Buffers.SIZEOF_FLOAT, 
				bufferData);		
		
		//clear state after unbind
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
		
		gl2.glBindVertexArray(0);
		
		shaderProgram.useProgram(gl2, false);
	}


	/**
	 * @return the modelTransformations
	 */
	public Matrix4 getModelTransformations() {
		return modelTransformations;
	}

	/**
	 * @param modelTransformations the modelTransformations to set
	 */
	public void setModelTransformations(Matrix4 modelTransformations) {
		this.modelTransformations = modelTransformations;
	}

	public void render(GL2 gl2){
		
		update(gl2);
		
		shaderProgram.useProgram(gl2, true);
		
		gl2.glBindVertexArray(vertexArrayId);
		if(isRenderWireframe()){
			gl2.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_LINE );
		}
		
		gl2.glDrawArrays(GL2.GL_QUADS, 0,coordinates.length/3);
		if(isRenderWireframe()){
			gl2.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_FILL );
		}
		gl2.glBindVertexArray(0);
		
		shaderProgram.useProgram(gl2, false);
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
	
	/**
	 * @param projection the projection to set
	 */
	public void setProjection(Matrix4 projection) {
		this.projection = projection;
	}

	/**
	 * @param view the view to set
	 */
	public void setView(Matrix4 view) {
		this.view = view;
	}
}
