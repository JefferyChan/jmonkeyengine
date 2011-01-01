uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;


varying vec4 pos;

void main(){
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition,1.0);

    pos = g_WorldMatrix*vec4(inPosition,0.0);
}
