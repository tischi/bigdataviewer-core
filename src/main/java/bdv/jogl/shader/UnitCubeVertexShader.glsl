#version 130
in vec3 inPosition;
uniform vec4 inColor;
out vec4 fragmentColor;
uniform mat4x4 inView;
uniform mat4x4 inProjection;
uniform mat4x4 inModel;


void main()
{
	vec4 position4d = vec4(inPosition.xyz,1.f);

	gl_Position =inProjection *  inView *inModel * position4d;
	//outPosition =inView * inProjection *inView * inModel * position4d;
	fragmentColor = inColor;
	//gl_Position = vec4(inPosition,1.f);
}
