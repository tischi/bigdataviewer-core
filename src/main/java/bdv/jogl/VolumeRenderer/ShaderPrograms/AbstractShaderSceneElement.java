package bdv.jogl.VolumeRenderer.ShaderPrograms;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.AbstractShaderSource.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bdv.jogl.VolumeRenderer.Scene.ISceneElements;
import bdv.jogl.VolumeRenderer.utils.MatrixUtils;

import com.jogamp.common.nio.Buffers;
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

	protected Set<ShaderCode> shaderCodes = new HashSet<ShaderCode>();

	private Matrix4 projection = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 view = MatrixUtils.getNewIdentityMatrix();

	private Matrix4 modelTransformations = MatrixUtils.getNewIdentityMatrix();

	private Map<String, Integer> shaderVariableMapping = new HashMap<String, Integer>();

	private ShaderProgram shaderProgram= new ShaderProgram();

	private VertexAttribute position;
	
	private boolean needsRebuild = false;


	/**
	 * @param needsRebuild the needsRebuild to set
	 */
	public void setNeedsRebuild(boolean needsRebuild) {
		this.needsRebuild = needsRebuild;
	}

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
		
		position.delete(gl2);

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
		if(needsRebuild){
			disposeGL(gl);
			init(gl);
			needsRebuild = false;
		}
		
		updateShaderAttributes(gl);

		updateVertexBuffer(gl);
	}

	/**
	 * Sub class implemented vertex upload.
	 * @param gl2
	 */
	protected abstract void updateVertexBufferSubClass(GL2 gl2, VertexAttribute position);


	/**
	 * Activates vertex buffer and uploads data by calling updateVertexBufferSubClass.
	 * @param gl2
	 */
	private void updateVertexBuffer(GL2 gl2){

		shaderProgram.useProgram(gl2, true);

		updateVertexBufferSubClass(gl2,position);

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
	 * Creates a name location mapping for attribute variables
	 * @param gl2 gl context
	 * @param uniforms List of uniform names to map 
	 */
	protected void mapAvertexAttributs(GL2 gl2, final String[] attributes){
		int location = -1;
		for(String attribute:attributes){
			location = gl2.glGetAttribLocation(shaderProgram.program(), attribute);
			shaderVariableMapping.put(attribute, location);
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
	 */
	protected abstract void  updateShaderAttributesSubClass(GL2 gl2);


	/**
	 * uploads uniform shader variables to the graphic device 
	 * @param gl2
	 */
	private void updateShaderAttributes(GL2 gl2){

		shaderProgram.useProgram(gl2, true);

		//memcopy
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(suvProjectionMatrix), 1, false, projection.getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(suvViewMatrix), 1, false, view.getMatrix(),0);
		gl2.glUniformMatrix4fv(shaderVariableMapping.get(suvModelMatrix), 1, false, modelTransformations.getMatrix(),0);
		updateShaderAttributesSubClass(gl2);

		shaderProgram.useProgram(gl2, false);
	}


	/**
	 * Sub class id mapping for special ids.
	 * @param gl2
	 */
	protected abstract void generateIdMappingSubClass(GL2 gl2);


	/**
	 * Mapps all Shader variables to the shaderVariableMapping, also does for sub classes
	 * @param gl2
	 */
	private void generateIdMapping(GL2 gl2){
		shaderProgram.useProgram(gl2, true);

		//get ids
		mapUniforms(gl2, new String[]{
				suvProjectionMatrix,
				suvViewMatrix,
				suvModelMatrix});

		mapAvertexAttributs(gl2, new String[]{satPosition});

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
		for(ShaderCode code:shaderCodes){
			code.compile(gl2,System.err);
			if(!code.isValid()){
				throw new IllegalArgumentException("No valid shader code in "+ code);
			}
			shaderProgram.add(code);
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

		position = new VertexAttribute(
				gl2, 
				getLocation(satPosition), 
				GL2.GL_FLOAT, 3, Buffers.SIZEOF_FLOAT);
		
		position.allocateAttributes(gl2, getVertexBufferSize());
	}


	/**
	 * render call, updates data if needed an delivers the program context for sub classes
	 */
	public void render(GL2 gl2){

		update(gl2);

		shaderProgram.useProgram(gl2, true);

		position.bind(gl2);
		
		renderSubClass(gl2);

		position.unbind(gl2);

		shaderProgram.useProgram(gl2, false);
	}


	/**
	 * Function containing the actual render call. program and Vertex buffer are bound in there.
	 * @param gl2
	 */
	protected abstract void renderSubClass(GL2 gl2);

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
