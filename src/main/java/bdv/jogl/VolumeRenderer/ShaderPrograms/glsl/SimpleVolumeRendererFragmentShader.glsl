#version 130
uniform sampler3D inVolumeTexture;
out vec4 color;
smooth in vec3 textureCoord;
float inMaxVolumeValue =9000;
void main(void)
{	
	//float volume = textureCoord.x; //texture(inVolumeTexture,textureCoord ).x ;
	color  = vec4(textureCoord,1);
	color = vec4(texture(inVolumeTexture,textureCoord ).x/ inMaxVolumeValue,0.1f,0.1f ,1.f);//vec4(textureCoord,1.f);
	//color = vec4(1.f,1.f,/*1.f*/ volume/*255.f*/,1.f);
}