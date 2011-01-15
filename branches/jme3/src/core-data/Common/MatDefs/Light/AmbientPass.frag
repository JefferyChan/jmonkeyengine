uniform vec4 g_AmbientLightColor;

uniform vec4 m_Ambient;
uniform vec4 m_Diffuse;
uniform sampler2D m_DiffuseMap;
varying vec2 texCoord;

#ifdef HAS_VERTEXCOLOR
    varying vec4 vertColor;
#endif

void main(){
    #ifdef MATERIAL_COLORS
        vec4 color = m_Ambient;
        color.a = m_Diffuse.a;
    #else
        vec4 color = vec4(0.2, 0.2, 0.2, 1.0);
    #endif

    #ifdef HAS_DIFFUSEMAP
        color *= texture2D(m_DiffuseMap, texCoord);
    #endif

    #ifdef HAS_VERTEXCOLOR
        color *= vertColor;
    #endif

    gl_FragColor = color * g_AmbientLightColor;
}