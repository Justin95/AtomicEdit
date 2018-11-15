#version 330
#extension GL_ARB_explicit_uniform_location : enable

in vec4 inColor;
in vec2 texCoord;

layout (location = 0) out vec4 outColor;

uniform sampler2D texture1;

void main(){
	outColor = inColor * texture(texture1, texCoord);
}
