#version 150

in vec2 textureCoords;

out vec4 out_Colour;

uniform sampler2D colourTexture;
uniform sampler2D highlightTexture2;
uniform sampler2D highlightTexture4;
uniform sampler2D highlightTexture8;

void main(void) {

    vec4 sceneColour = texture(colourTexture, textureCoords);
    vec4 highlightColour2 = texture(highlightTexture2, textureCoords);
    vec4 highlightColour4 = texture(highlightTexture4, textureCoords);
    vec4 highlightColour8 = texture(highlightTexture8, textureCoords);
    out_Colour = sceneColour + highlightColour2 + highlightColour4 + highlightColour8;

}