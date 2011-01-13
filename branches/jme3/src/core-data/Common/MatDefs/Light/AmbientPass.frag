uniform vec4 m_Ambient;
uniform sampler2D m_DiffuseMap;
varying vec2 texCoord;

#ifdef HAS_VERTEXCOLOR
    varying vec4 vertColor;
#endif

void main(){
    vec4 color = m_Ambient;

    #ifdef HAS_DIFFUSEMAP
        color *= texture2D(m_DiffuseMap, texCoord);
    #endif

    #ifdef HAS_VERTEXCOLOR
        color *= vertColor;
    #endif

    gl_FragColor = color;
}