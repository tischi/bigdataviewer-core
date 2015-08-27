package bdv.jogl.VolumeRenderer.Scene;

import java.nio.Buffer;
import java.util.HashSet;
import java.util.Set;

import com.jogamp.opengl.GL2;

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
