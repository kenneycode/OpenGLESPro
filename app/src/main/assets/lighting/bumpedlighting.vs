#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec3 tangent;
layout (location = 4) in vec3 bitangent;

out vec3 fragPos;
out vec2 texCoord;
out vec3 tangentLightPos;
out vec3 tangentViewPos;
out vec3 tangentFragPos;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    texCoord = aTexCoord;
    fragPos = vec3(model * vec4(aPos, 1.0));
    mat3 normalMatrix = transpose(inverse(mat3(model)));
    vec3 T = normalize(normalMatrix * tangent);
    vec3 B = normalize(normalMatrix * bitangent);
    vec3 N = normalize(normalMatrix * aNormal);
    mat3 TBN = transpose(mat3(T, B, N));
    tangentLightPos = TBN * lightPos;
    tangentViewPos  = TBN * viewPos;
    tangentFragPos  = TBN * fragPos;
    gl_Position = projection * view * vec4(fragPos, 1.0);
}