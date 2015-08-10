package bdv.jogl.VolumeRenderer.Scene;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Map;
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
	
	private final int variableOffset=0;

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

		gl2.glBindTexture(textureType, textureObject);


		//activate texture unit
		gl2.glUniform1i(variableLocation,textureUnit-GL2.GL_TEXTURE0);
		//gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		/*gl2.glTexParameteri(type, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
		gl2.glTexParameteri(type, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);*/
		//gl2.glBindTexture(type, 0);

	}
	
	public void update(GL2 gl2,int midmapLevel, Buffer data, int[] dimensions){
		//activate context
		gl2.glActiveTexture(textureUnit);
		gl2.glBindTexture(textureType, textureObject);
		gl2.glUniform1i(variableLocation ,textureUnit-GL2.GL_TEXTURE0);
		
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
	
	public void setTexParameteri(GL2 gl2, int flag, int value){
		gl2.glTexParameteri(textureType, flag, value);
	}
	
}
