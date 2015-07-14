#version 130
in vec3 inPosition;
out vec4 fragmentColor;
uniform mat4x4 inView;
uniform mat4x4 inProjection;
uniform mat4x4 inModel;
out vec3 textureCoord;


vec3 invertedTexture(vec3 texCoord){
	vec3 outVector = texCoord;
	for(int i =1; i<2 ; i++){
		if(texCoord[i] < 1){
			outVector[i]=1;
		}
		else{
			outVector[i] =0;
		}
	}
	return outVector;
}

void main(){
	textureCoord = invertedTexture(inPosition);
	vec4 position4d = vec4(inPosition.xyz,1.f);
	
	gl_Position =inProjection *  inView *inModel * position4d;
	//unit cube vertex is also the texture coordinate 

	
}
