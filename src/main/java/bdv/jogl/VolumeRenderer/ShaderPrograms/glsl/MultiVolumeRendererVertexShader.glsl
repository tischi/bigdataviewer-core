#version 130

const int maxNumberOfData = 6;

uniform mat4x4 inView;
uniform mat4x4 inProjection;
uniform mat4x4 inModel;
uniform mat4x4 inScaleGlobal;
uniform mat4x4 inLocalTransformation[maxNumberOfData];

in vec3 inPosition;
in vec3 inTextureCoord[maxNumberOfData];
out vec3 textureCoord[maxNumberOfData];

void main(){
	
	vec4 position4d = vec4(inPosition.xyz,1.f);
	
	//calculate transformed texture coordinates
	for(int i =0; i<maxNumberOfData; i++ ){
		vec4 transformed = inLocalTransformation[i] * position4d;
		textureCoord[i] = transformed.xyz/transformed.w;
	}
	
	gl_Position =inProjection * inView * inModel * inScaleGlobal * position4d;	
}
