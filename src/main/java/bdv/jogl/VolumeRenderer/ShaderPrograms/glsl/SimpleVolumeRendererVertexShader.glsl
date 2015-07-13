#version 130
in vec3 inPosition;
out vec4 fragmentColor;
uniform mat4x4 inView;
uniform mat4x4 inProjection;
uniform mat4x4 inModel;
out vec3 textureCoord;
void main(){
	
	vec4 position4d = vec4(inPosition.xyz,1.f);
	
	gl_Position =inProjection *  inView *inModel * position4d;
	
	//unit cube vertex is also the texture coordinate 
	textureCoord = inPosition;
}
