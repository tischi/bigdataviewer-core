package bdv.jogl.VolumeRenderer.Scene;

import java.nio.Buffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

/**
 * Class encapsulating gl vbo routines
 * @author michael
 *
 */
public class VertexBuffer {

	private int vbo; 
	
	private void generateBuffer(GL2 gl2){
		//vertex buffer
		int[] vertexBufferObject = new int[1];
		gl2.glGenBuffers(1,vertexBufferObject,0 );
		vbo =  vertexBufferObject[0];
	}
	
	public VertexBuffer(GL2 gl2){
		generateBuffer(gl2);
	}
	
	/**
	 * Allocates gpu memory for the buffer
	 * @param gl2
	 * @param sizeInBytes Memory size
	 */
	public void allocateMemory(GL2 gl2, int sizeInBytes){
		
		bind(gl2);
		
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, 
				sizeInBytes,
				null, 
				GL2.GL_STATIC_DRAW);
		
		unbind(gl2);
	}
	
	public void memcopyData(GL2 gl2, final Buffer data, int elementSize, int offset){
		bind(gl2);
		
		gl2.glBufferSubData(
				GL2.GL_ARRAY_BUFFER,
				offset, 
				data.capacity() * elementSize, 
				data);
		
		unbind(gl2);
	}
	
	/**
	 * Binds the buffer to the current context
	 * @param gl2
	 */
	public void bind(GL2 gl2){
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo);
	}
	
	/**
	 * Unbinds the buffer
	 * @param gl2
	 */
	public void unbind(GL2 gl2){
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	public void delete(GL2 gl2){
		int[] buffers = {vbo};
		gl2.glDeleteBuffers(1, buffers,0);
	}

}
