package bdv.jogl.test;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
public class UnitCube {

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

	private Queue<Matrix4> modelTransformations = new LinkedList<Matrix4>();

	private Map<Integer, ShaderCode> shaderCodes = new HashMap<Integer, ShaderCode>();
	
	private Map<String, Integer> shaderVariableMapping = new HashMap<String, Integer>();

	private ShaderProgram shaderProgram= new ShaderProgram();

	private float[] coordinates = getBufferVertices(); 
	
	private Camera camera = new Camera();
	
	private int vertexBufferId = 0;
	
	private int vertexArrayId = 0;
	
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
	public void update(GL2 gl){
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
		
		int positionID =gl2.glGetAttribLocation(shaderProgram.program(), shaderVariablePosition);
		shaderVariableMapping.put(shaderVariablePosition, positionID);
		
		shaderProgram.useProgram(gl2, false);
	}

	private void updateShaderAttributes(GL2 gl2){
		
		shaderProgram.useProgram(gl2, true);
		
		//memcopy
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableProjectionMatrix), 1, false, camera.getProjectionMatix().getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableViewMatrix), 1, false, camera.getViewMatrix().getMatrix(),0);
		if(!modelTransformations.isEmpty()){
			gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableModelMatrix), 1, false, modelTransformations.peek().getMatrix(),0);
		}

		shaderProgram.useProgram(gl2, false);
	}

	private List<Float> createVertex(Float x,Float y,Float z){
		List<Float> vertex = new LinkedList<Float>();

		vertex.add(x);
		vertex.add(y);
		vertex.add(z);

		return vertex;
	}
	private float[] getBufferVertices(){
		List<Float> vertices = new LinkedList<Float>();

		//2 faces
		for(Integer z=0;z < 2;z++){
			vertices.addAll(createVertex(0f, 0f, z.floatValue()));
			vertices.addAll(createVertex(1f, 0f, z.floatValue()));
			vertices.addAll(createVertex(1f, 0f, z.floatValue()));
			vertices.addAll(createVertex(1f, 1f, z.floatValue()));
			vertices.addAll(createVertex(1f, 1f, z.floatValue()));
			vertices.addAll(createVertex(0f, 1f, z.floatValue()));
			vertices.addAll(createVertex(0f, 1f, z.floatValue()));
			vertices.addAll(createVertex(0f, 0f, z.floatValue()));
		}

		
		
		//connections of faces
		for(Integer y=0; y <2; y++){
			for(Integer x=0; x <2; x++){
				for(Integer z=0; z <2; z++){
					vertices.addAll(createVertex(x.floatValue(), y.floatValue(), z.floatValue()));
				}
			}
		}
		
		float [] array = new float[vertices.size()];
		int i = 0;
		for(Float f : vertices){
			array[i] = f.floatValue();
			i++;
		}
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
	public Queue<Matrix4> getModelTransformations() {
		return modelTransformations;
	}

	public void render(GL2 gl2){
		
		shaderProgram.useProgram(gl2, true);
		
		gl2.glBindVertexArray(vertexArrayId);
		
		gl2.glDrawArrays(GL2.GL_LINES, 0,coordinates.length);
			
		gl2.glBindVertexArray(0);
		
		shaderProgram.useProgram(gl2, false);
	}
	

	/**
	 * set the scene camera
	 * @param camera
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * rendering methode
	 * @param gl2 gl2 context
	 */
	/*public static void render(GL gl2){
		gl2.glColor3f(1, 1, 1);

		//2 faces
		for(int z=0;z < 2;z++){
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glVertex3f(0, 0, z);
			gl2.glVertex3f(1, 0, z);
			gl2.glVertex3f(1, 1, z);
			gl2.glVertex3f(0, 1, z);
			gl2.glVertex3f(0, 0, z);
			gl2.glEnd();
		}

		//connections of faces
		gl2.glBegin(GL2.GL_LINES);
		for(int y=0; y <2; y++){
			for(int x=0; x <2; x++){
				for(int z=0; z <2; z++){
					gl2.glVertex3f(x, y, z);
				}
			}
		}
		gl2.glEnd();
	}*/
}
