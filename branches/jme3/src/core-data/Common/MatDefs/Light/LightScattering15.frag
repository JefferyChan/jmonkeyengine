#import "Common/ShaderLib/MultiSample.glsllib"

uniform TEXTURE m_Texture;
uniform TEXTURE m_DepthTexture;

uniform int m_NbSamples;
uniform float m_BlurStart;
uniform float m_BlurWidth;
uniform float m_LightDensity;
uniform bool m_Display;

in vec2 lightPos;
in vec2 texCoord;

void main(void)
{
   if(m_Display){

       vec4 colorRes= getColor(m_Texture,texCoord);
       float factor=(m_BlurWidth/float(m_NbSamples-1.0));
       float scale;
       vec2 texCoo=texCoord-lightPos;
       vec2 scaledCoord;
       vec4 res = vec4(0.0);
       for(int i=0; i<m_NbSamples; i++) {
            scale = i * factor + m_BlurStart ;
            scaledCoord=texCoo*scale+lightPos;
            if(getColor(m_DepthTexture,scaledCoord).r==1.0){
                res += getColor(m_Texture,scaledCoord);
            }
        }
        res /= m_NbSamples;

        //Blend the original color with the averaged pixels
        gl_FragColor =mix( colorRes, res, m_LightDensity);
    }else{
        gl_FragColor= getColor(m_Texture,texCoord);
    }
}
