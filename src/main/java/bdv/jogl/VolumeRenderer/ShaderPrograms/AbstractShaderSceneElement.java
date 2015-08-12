package bdv.jogl.VolumeRenderer.ShaderPrograms;

import java.util.HashMap;
import java.util.Map;

import bdv.jogl.VolumeRenderer.Scene.ISceneElements;
import bdv.jogl.VolumeRenderer.utils.MatrixUtils;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 * Defines standard behavior for shader elements.
 * @author michael
 *
 */
public abstract class AbstractShaderSceneElement implements ISceneElements{

	public static final String shaderVariablePosition = "inPosition";

	public static final String shaderVariableProjectionMatrix = "inProjection";

	public static final String shaderVariableViewMatrix = "inView";

	public static final String shaderVariableModelMatrix = "inModel";

	protected static Map<Integer, String> shaderFiles;

	private Matrix4 projection = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 view = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 modelTransformations = MatrixUtils.getNewIdentityMatrix();

	private Map<Integer, ShaderCode> shaderCodes = new HashMap<Integer, ShaderCode>();

	private Map<String, Integer> shaderVariableMapping = new HashMap<String, Integer>();

	private ShaderProgram shaderProgram= new ShaderProgram();

	private int vertexBufferId = 0;

	private int vertexArrayId = 0;


	/**
	 * @param projection the projection to set
	 */
	public void setProjection(final Matrix4 projection) {
		this.projection = MatrixUtils.copyMatrix(projection);
	}
	
	/**
	 * @return the view
	 */
	public Matrix4 getView() {
		return MatrixUtils.copyMatrix(view);
	}

	/**
	 * @param view the view to set
	 */
	public void setView(final Matrix4 view) {
		this.view = MatrixUtils.copyMatrix(view);
	}

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

	/**
	 * Sub class implemented vertex upload.
	 * @param gl2
	 */
	protected abstract void updateVertexBufferSubClass(GL2 gl2);

	
	/**
	 * Activates vertex buffer and uploads data by calling updateVertexBufferSubClass.
	 * @param gl2
	 */
	private void updateVertexBuffer(GL2 gl2){

		shaderProgram.useProgram(gl2, true);

		gl2.glBindVertexArray(vertexArrayId);

		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER,vertexBufferId);

		updateVertexBufferSubClass(gl2);

		//clear state after unbind
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);

		gl2.glBindVertexArray(0);

		shaderProgram.useProgram(gl2, false);
	}

	/**
	 * Creates a name location mapping for uniform variables
	 * @param gl2 gl context
	 * @param uniforms List of uniform names to map 
	 */
	protected void mapUniforms(GL2 gl2, final String[] uniforms){
		int location = -1;
		for(String uniform:uniforms){
			location = gl2.glGetUniformLocation(shaderProgram.program(), uniform);
			shaderVariableMapping.put(uniform, location);
		}
	}

	/**
	 * Returns the location of a certain variable 
	 * @param variableName Name of the shader variable
	 * @return
	 */
	protected int getLocation(final String variableName){
		return shaderVariableMapping.get(variableName);
	}
	
	/**
	 * Sub class uniform upload
	 * @param gl2
	 * @param shaderVariableMapping
	 */
	protected abstract void  updateShaderAttributesSubClass(GL2 gl2,final Map<String, Integer> shaderVariableMapping);

	
	/**
	 * uploads uniform shader variables to the graphic device 
	 * @param gl2
	 */
	private void updateShaderAttributes(GL2 gl2){

		shaderProgram.useProgram(gl2, true);

		//memcopy
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableProjectionMatrix), 1, false, projection.getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableViewMatrix), 1, false, view.getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(shaderVariableModelMatrix), 1, false, modelTransformations.getMatrix(),0);
		updateShaderAttributesSubClass(gl2, shaderVariableMapping);

		shaderProgram.useProgram(gl2, false);
	}

	/**
	 * Sub class id mapping for special ids.
	 * @param gl2
	 * @param shaderVariableMapping
	 * @param shaderProgram
	 */
	protected abstract void generateIdMappingSubClass(GL2 gl2);

	
	/**
	 * Mapps all Shader variables to the shaderVariableMapping, also does for sub classes
	 * @param gl2
	 */
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

		generateIdMappingSubClass(gl2);

		shaderProgram.useProgram(gl2, false);
	}

	/**
	 * Creates a shader program
	 * @param gl2
	 */
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

	/**
	 * Returns the size of the Vertex buffer needed by the sub class
	 * @return
	 */
	protected abstract int getVertexBufferSize();


	/**
	 * Creates a vertex buffer for the current program
	 * @param gl2
	 */
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
				getVertexBufferSize(),
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


	/**
	 * render call, updates data if needed an delivers the program context for sub classes
	 */
	public void render(GL2 gl2){

		update(gl2);

		shaderProgram.useProgram(gl2, true);

		gl2.glBindVertexArray(vertexArrayId);


		renderSubClass(gl2,shaderVariableMapping);


		gl2.glBindVertexArray(0);

		shaderProgram.useProgram(gl2, false);
	}


	/**
	 * Function containing the actual render call. program and Vertex buffer are bound in there.
	 * @param gl2
	 * @param shaderVariableMapping 
	 */
	protected abstract void renderSubClass(GL2 gl2, Map<String, Integer> shaderVariableMapping);

	/**
	 * @return the modelTransformations
	 */
	public Matrix4 getModelTransformation() {
		return MatrixUtils.copyMatrix(modelTransformations);
	}


	/**
	 * @param modelTransformations the modelTransformations to set
	 */
	public void setModelTransformation(final Matrix4 modelTransformations) {
		this.modelTransformations = MatrixUtils.copyMatrix(modelTransformations);
	}
}
