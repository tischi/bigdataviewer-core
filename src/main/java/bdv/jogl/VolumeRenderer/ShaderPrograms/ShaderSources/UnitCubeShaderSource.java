package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources;

import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.*;

import java.util.HashSet;
import java.util.Set;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderCode;

public class UnitCubeShaderSource extends AbstractShaderSource {

	public static final String shaderUniformVariableColor = "inColor";
	
	@Override
	public Set<ShaderCode> getShaderCodes() {
		Set<ShaderCode> codes = new HashSet<ShaderCode>();
		codes.add(new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][]{vertexShaderCode()}));
		codes.add(new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][]{fragmentShaderCode()}));
		return codes;
	}

	private String[] vertexShaderCode(){
		String[] code={
				"#version "+getShaderLanguageVersion(),
				"in vec3 inPosition;",
				"uniform vec4 inColor;",
				"out vec4 fragmentColor;",
				"uniform mat4x4 inView;",
				"uniform mat4x4 inProjection;",
				"uniform mat4x4 inModel;",
				"",
				"void main()",
				"{",
				"	vec4 position4d = vec4(inPosition.xyz,1.f);",
				"",
				"	gl_Position =inProjection *  inView *inModel * position4d;",
				"	//outPosition =inView * inProjection *inView * inModel * position4d;",
				"	fragmentColor = inColor;",
				"	//gl_Position = vec4(inPosition,1.f);",
				"}"
		};
		appendNewLines(code);
		return code;
	}
	
	private String[] fragmentShaderCode(){
		String[] code={
				"#version "+getShaderLanguageVersion(),
				"in vec4 fragmentColor;",
				"out vec4 color;",
				"",
				"void main(void)",
				"{",	
				"	color = fragmentColor;",
				"}"
		};
		appendNewLines(code);
		return code;
	}
}
