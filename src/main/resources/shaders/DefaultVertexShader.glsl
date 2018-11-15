#version 330
#extension GL_ARB_explicit_uniform_location : enable

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoord;
layout (location = 2) in vec4 color;

out vec4 inColor;
out vec2 texCoord;

layout (location = 0) uniform mat4 model;
layout (location = 1) uniform mat4 view;
layout (location = 2) uniform mat4 proj;

void main(){
	inColor = color;
	texCoord = textureCoord;
	gl_Position = proj * view * model * vec4(position,1.0);
}
