#version 130
uniform sampler3D inVolumeTexture;
out vec4 color;
in vec3 textureCoord;
void main(void)
{	
	//float volume = textureCoord.x; //texture(inVolumeTexture,textureCoord ).x ;
	color = vec4(texture(inVolumeTexture,textureCoord ).xyz,1.f);//vec4(textureCoord,1.f);
	//color = vec4(1.f,1.f,/*1.f*/ volume/*255.f*/,1.f);
}