package bdv.jogl.VolumeRenderer.Scene;

import java.nio.Buffer;
import java.util.HashSet;
import java.util.Set;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;

import static bdv.jogl.VolumeRenderer.utils.GLUtils.contextSupportsExtension;

/**
 * Class representing gl textures. Sadly jogl does not support 1D and 3D Textures. 
 * @author michael
 *
 */
public class Texture {

	private static Set<Integer> usedTextureUnits = new HashSet<Integer>();
	
	private int textureUnit;
	
	private int textureObject;
	
	private final int textureType;
	
	private final int variableLocation;

	private final int internalFormat;

	private final int pixelFormat;
	
	private final int pixelDataType;


	/**
	 * Constructor
	 * @param textureType type of the texture (GL_TEXTURE_<N>D)
	 * @param variableLocation Location of the texture variable in the shader program.
	 */
	public Texture(int textureType, int variableLocation, int internalFormat, int pixelFormat, int pixelDataType) {

		this.textureType = textureType;
		this.variableLocation = variableLocation;
		this.internalFormat = internalFormat;
		this.pixelFormat = pixelFormat;
		this.pixelDataType = pixelDataType;
	}
	
	/**
	 * Generates the texture binding for the glsl shaders
	 * @param gl2
	 */
	public void genTexture(GL2 gl2){
		int testUnit = GL2.GL_TEXTURE0; 
		
		//find next free unit
		for(;;){
			if(!usedTextureUnits.contains( testUnit)){
				usedTextureUnits.add(testUnit);
				break;
			}
			testUnit++;
		}
		textureUnit = testUnit;
		
		//activate texture
		gl2.glActiveTexture(textureUnit);

		//generate texture object
		int[] textures = new int[1];
		gl2.glGenTextures(textures.length, textures,0);
		textureObject = textures[0];

		rebindTexture(gl2);

	}
	
	private void rebindTexture(GL2 gl2){
		gl2.glBindTexture(textureType, textureObject);

		int logicalTextureUnit = textureUnit-GL2.GL_TEXTURE0;

		//activate texture unit
		gl2.glUniform1i(variableLocation,logicalTextureUnit);
	}
	
	public static boolean isSparseTextureSupported(GL context){
		return contextSupportsExtension(context, "GL_ARB_sparse_texture");
	}
	
	/**
	 * 
	 * @param gl2
	 * @param midmapLevel
	 * @param data
	 * @param virtualDimensions
	 * @param offsets
	 * @param sizes
	 * @throws UnsupportedOperationException if sparse textures are not supported.
	 */
	public void updateSparse(GL2 gl2,int midmapLevel, Buffer data, int[] virtualDimensions, int[] offsets, int[] sizes) {
		if(isSparseTextureSupported(gl2)){
			throw new UnsupportedOperationException("sparse textures are not supported on your system!");
		}
		//activate context
		gl2.glActiveTexture(textureUnit);
		rebindTexture(gl2);
		setTexParameteri(gl2, GL4.GL_TEXTURE_SPARSE_ARB, GL2.GL_TRUE);
		switch (virtualDimensions.length) {
			case 1:
				gl2.glTexStorage1D(this.textureType, 1, this.internalFormat, virtualDimensions[0]);
				//release all commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, 0, 0, 0, virtualDimensions[0], 1, 1,false);
				//add commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, offsets[0], 0, 0, sizes[0], 1, 1,true);
				gl2.glTexSubImage1D(this.textureType, 0, offsets[0], sizes[0], this.pixelFormat, this.pixelDataType, data);
				break;
			case 2:
				gl2.glTexStorage2D(this.textureType, 1, this.internalFormat, virtualDimensions[0],virtualDimensions[2]);
				//release all commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, 0, 0, 0, virtualDimensions[0], virtualDimensions[1], 1,false);
				//add commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, offsets[0], offsets[1], 0, sizes[0], sizes[1], 1,true);
				gl2.glTexSubImage2D(this.textureType, 0, offsets[0],offsets[1], sizes[0],sizes[1], this.pixelFormat, this.pixelDataType, data);
				break;
			case 3:
				gl2.glTexStorage3D(this.textureType, 1, this.internalFormat, virtualDimensions[0],virtualDimensions[2],virtualDimensions[3]);
				//release all commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, 0, 0, 0, virtualDimensions[0], virtualDimensions[1], virtualDimensions[2],false);
				//add commitments
				gl2.glTexPageCommitmentARB(this.textureType, 1, offsets[0], offsets[1], offsets[2], sizes[0], sizes[1], sizes[2],true);
				gl2.glTexSubImage3D(this.textureType, 0, offsets[0],offsets[1],offsets[2], sizes[0],sizes[1],sizes[2], this.pixelFormat, this.pixelDataType, data);
				break;
			default:
			break;
		}
	}
	
	/**
	 * Updates the data for the texture
	 * @param gl2
	 * @param midmapLevel
	 * @param data
	 * @param dimensions
	 */
	public void update(GL2 gl2,int midmapLevel, Buffer data, int[] dimensions){
		//activate context
		gl2.glActiveTexture(textureUnit);
		rebindTexture(gl2);
		
		switch (dimensions.length) {
		case 1:

			gl2.glTexImage1D(textureType, 
					midmapLevel, 
					internalFormat, 
					dimensions[0], 
					0,
					pixelFormat, 
					pixelDataType, 
					data);
			break;
	
		case 2:
			gl2.glTexImage2D(textureType, 
					midmapLevel, 
					internalFormat, 
					dimensions[0],dimensions[1], 
					0,
					pixelFormat, 
					pixelDataType, 
					data);
			break;

		case 3:
			gl2.glTexImage3D(textureType, 
					midmapLevel, 
					internalFormat, 
					dimensions[0],dimensions[1],dimensions[2], 
					0,
					pixelFormat, 
					pixelDataType, 
					data);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Sets texture parameters with glTexParameteri
	 * @param gl2
	 * @param parameter 
	 * @param value
	 */
	public void setTexParameteri(GL2 gl2, int parameter, int value){
		gl2.glTexParameteri(textureType, parameter, value);
	}
	
	public void setTexParameterfv(GL2 gl2, int parameter, float[] values){
		gl2.glTexParameterfv(textureType, parameter, values, 0);
	}
	
	/**
	 * Clears the current texture context of the object
	 * @param gl2
	 */
	public void delete(GL2 gl2){
		
		
		int textBuffer[] = {textureObject};
		gl2.glDeleteTextures(1, textBuffer, 0);
		
		usedTextureUnits.remove(textureUnit);
	}
	
}
