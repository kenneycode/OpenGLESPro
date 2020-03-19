#version 300 es

precision mediump float;

layout(location = 0) out vec4 fragColor;

in vec2 texCoord;
in vec3 fragPos;
in vec3 normal;

uniform sampler2D imageTex;
uniform sampler2D normalTex;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;

float myPow(float x, int r) {
    float result = 1.0;
    for (int i = 0; i < r; i = i + 1) {
        result = result * x;
    }
    return result;
}

void main()
{

    vec3 texColor = texture(imageTex, texCoord).rgb;

    // ambient
    float ambientStrength = 0.5;
    vec3 ambient = ambientStrength * lightColor;

    vec3 spotDir = vec3(-2.0, 0.0, 0.0);
    vec3 lightDir = normalize(lightPos - fragPos);
    float theta = dot(lightDir, normalize(-spotDir));

    // diffuse
    vec3 norm = normalize(normal);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // specular
    float specularStrength = 2.0;
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = myPow(max(dot(viewDir, reflectDir), 0.0), 16);
    vec3 specular = specularStrength * spec * lightColor;

    // spotlight (soft edges)
    float epsilon = 0.15;
    float intensity = clamp((theta - 0.85) / epsilon, 0.0, 1.0);
    diffuse  *= intensity;
    specular *= intensity;

    // attenuation
    float dist = length(lightPos - fragPos);
    float kc = 0.1;
    float kl = 0.1;
    float kq = 0.1;
    float attenuation = 1.0 / (kc + kl * dist + kq * (dist * dist));
    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;

    vec3 result = (ambient + diffuse + specular) * texColor;
    fragColor = vec4(result, 1.0);

}