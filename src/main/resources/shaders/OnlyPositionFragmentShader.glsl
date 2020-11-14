#version 330
#extension GL_ARB_explicit_uniform_location : enable

in vec4 inColor;

layout (location = 0) out vec4 outColor;

void main(){
    vec4 color = inColor;
    if(color.a < 0.01){
        discard;
    }
	outColor = color;
}
