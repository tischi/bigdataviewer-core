#version 130

const int maxNumberOfData = 2;

uniform mat4x4 inView;
uniform mat4x4 inProjection;
uniform mat4x4 inModel;
uniform mat4x4 inScaleGlobal;
uniform mat4x4 inLocalTransformationInverse[maxNumberOfData];

in vec3 inPosition;
out vec3 textureCoord[maxNumberOfData];

void main(){
	
	vec4 position4d = vec4(inPosition.xyz,1.f);
	
	vec4 positionInGlobalSpace = inScaleGlobal * position4d;
	
	//calculate transformed texture coordinates
	for(int i =0; i<maxNumberOfData; i++ ){
		vec4 transformed = inLocalTransformationInverse[i] * positionInGlobalSpace;
		textureCoord[i] = transformed.xyz/transformed.w;
	}	
	
	gl_Position =inProjection * inView * inModel * positionInGlobalSpace;
	
	

}
