// Water pixel shader
// Copyright (C) JMonkeyEngine 3.0
// by Remy Bouquet (nehon) for JMonkeyEngine 3.0
// original HLSL version by Wojciech Toman 2009

uniform sampler2D m_HeightMap;
uniform sampler2D m_Texture;
uniform sampler2D m_DepthTexture;
uniform sampler2D m_PositionBuffer;
uniform sampler2D m_NormalMap;
uniform sampler2D m_FoamMap;
uniform sampler2D m_ReflectionMap;
uniform mat4 m_ViewProjectionMatrixInverse;
uniform mat4 m_TextureProjMatrix;
uniform  vec3 m_CameraPosition;
uniform float m_WaterHeight;
uniform float m_Time;
uniform float m_WaterTransparency;
uniform float m_NormalScale;
uniform float m_R0;
uniform float m_MaxAmplitude;
uniform vec3 m_LightDir;
uniform vec4 m_LightColor;
uniform float m_ShoreHardness;
uniform float m_FoamHardness;
uniform float m_RefractionStrength;
uniform vec3 m_FoamExistence;
uniform vec3 m_ColorExtinction;
uniform float m_Shininess;
uniform vec4 m_WaterColor;
uniform vec4 m_DeepWaterColor;
uniform vec2 m_WindDirection;
uniform float m_SunScale;
uniform float m_WaveScale;
vec2 scale = vec2(m_WaveScale, m_WaveScale);
float refractionScale = m_WaveScale;

// Modifies 4 sampled normals. Increase first values to have more
// smaller "waves" or last to have more bigger "waves"
const vec4 normalModifier = vec4(3.0, 2.0, 4.0, 10.0);
// Strength of displacement along normal.
const float displace = 1.7;
// Water transparency along eye vector.
const float visibility = 3.0;

varying vec2 texCoord;

mat3 MatrixInverse(in mat3 inMatrix)
{  
   float det = dot(cross(inMatrix[0], inMatrix[1]), inMatrix[2]);
   mat3 T = transpose(inMatrix);
   return mat3(cross(T[1], T[2]),
               cross(T[2], T[0]),
               cross(T[0], T[1])) / det;
}

mat3 computeTangentFrame(in vec3 N,in vec3 P,in vec2 UV)
{
	vec3 dp1 = dFdx(P);
	vec3 dp2 = dFdy(P);
	vec2 duv1 = dFdx(UV);
	vec2 duv2 = dFdy(UV);
	 
	   
	// solve the linear system
	mat3 M = mat3(dp1, dp2, cross(dp1, dp2));
	mat3 inverseM = MatrixInverse(M);
	vec3 T = inverseM * vec3(duv1.x, duv2.x, 0.0);
	vec3 B = inverseM * vec3(duv1.y, duv2.y, 0.0);
	   
	// construct tangent frame  
	float maxLength = max(length(T), length(B));
	T = T / maxLength;
	B = B / maxLength;
	
	vec3 tangent = normalize(T);
	vec3 binormal = normalize(B);  
	
	return mat3(T, B, N);	
}

float saturate(in float val){
	return clamp(val,0.0,1.0);
}

vec3 saturate(in vec3 val){
	return clamp(val,0.0,1.0);
}


vec3 getPosition(in vec2 uv){
/*  float depth = texture2D(m_DepthTexture, uv).r;

  vec4 pos;
  pos.xy = (uv * vec2(2.0)) - vec2(1.0);
  
  pos.z  = depth;
  pos.w  = 1.0;
  pos = m_ViewProjectionMatrixInverse * pos;
  pos/=pos.w;

  return pos.xyz;
*/
return texture2D(m_PositionBuffer,uv).rgb;

}

// Function calculating fresnel term.
// - normal - normalized normal vector
// - eyeVec - normalized eye vector
float fresnelTerm(in vec3 normal,in vec3 eyeVec){
		float angle = 1.0 - saturate(dot(normal, eyeVec));
		float fresnel = angle * angle;
		fresnel = fresnel * fresnel;
		fresnel = fresnel * angle;
		return saturate(fresnel * (1.0 - saturate(m_R0)) + m_R0 - m_RefractionStrength);
}

void main(void){

	vec3 color2 = texture2D(m_Texture, texCoord).rgb;
	vec3 color = color2;
        
	
	vec3 position = getPosition(texCoord);

	float level = m_WaterHeight;
	float depth = 0.0;


	// If we are underwater let's leave out complex computations
	if(level >= m_CameraPosition.y){
		gl_FragColor = vec4(color2, 1.0);
                
        }else{
            if(position.y <= level + m_MaxAmplitude){
                    vec3 eyeVec = position - m_CameraPosition;
                    float diff = level - position.y;
                    float cameraDepth = m_CameraPosition.y - position.y;

                    // Find intersection with water surface
                    vec3 eyeVecNorm = normalize(eyeVec);
                    float t = (level - m_CameraPosition.y) / eyeVecNorm.y;
                    vec3 surfacePoint = m_CameraPosition + eyeVecNorm * t;

                    vec2 texC;
                    for(int i = 0; i < 10; ++i){

                            texC = (surfacePoint.xz + eyeVecNorm.xz * 0.1) * scale + m_Time * 0.03 * m_WindDirection;
                            
                            float bias = texture2D(m_HeightMap, texC).r;

                            bias *= 0.1;
                            level += bias * m_MaxAmplitude;
                            t = (level - m_CameraPosition.y) / eyeVecNorm.y;
                            surfacePoint = m_CameraPosition + eyeVecNorm * t;
                    }

                    depth = length(position - surfacePoint);
                    float depth2 = surfacePoint.y - position.y;

                    eyeVecNorm = normalize(m_CameraPosition - surfacePoint);
                   
                    float normal1 = texture2D(m_HeightMap, (texC + vec2(-1.0, 0.0) / 256.0)).r;
                    float normal2 = texture2D(m_HeightMap, (texC + vec2(1.0, 0.0) / 256.0)).r;
                    float normal3 = texture2D(m_HeightMap, (texC + vec2(0.0, -1.0) / 256.0)).r;
                    float normal4 = texture2D(m_HeightMap, (texC + vec2(0.0, 1.0) / 256.0)).r;

                    vec3 myNormal = normalize(vec3((normal1 - normal2) * m_MaxAmplitude,m_NormalScale,(normal3 - normal4) * m_MaxAmplitude));

                    texC = surfacePoint.xz * 0.8 + m_WindDirection * m_Time* 1.6;
                    mat3 tangentFrame = computeTangentFrame(myNormal, eyeVecNorm, texC);
                    vec3 normal0a = normalize(tangentFrame*(2.0f * texture2D(m_NormalMap, texC).xyz - 1.0));

                    texC = surfacePoint.xz * 0.4 + m_WindDirection * m_Time* 0.8;
                    tangentFrame = computeTangentFrame(myNormal, eyeVecNorm, texC);
                    vec3 normal1a = normalize(tangentFrame*(2.0f * texture2D(m_NormalMap, texC).xyz - 1.0));

                    texC = surfacePoint.xz * 0.2 + m_WindDirection * m_Time * 0.4;
                    tangentFrame = computeTangentFrame(myNormal, eyeVecNorm, texC);
                    vec3 normal2a = normalize(tangentFrame*(2.0f * texture2D(m_NormalMap, texC).xyz - 1.0));

                    texC = surfacePoint.xz * 0.1 + m_WindDirection * m_Time * 0.2;
                    tangentFrame = computeTangentFrame(myNormal, eyeVecNorm, texC);
                    vec3 normal3a = normalize(tangentFrame*(2.0f * texture2D(m_NormalMap, texC).xyz - 1.0));

                    vec3 normal = normalize(normal0a * normalModifier.x + normal1a * normalModifier.y +normal2a * normalModifier.z + normal3a * normalModifier.w);

                    texC = texCoord.xy;
                    texC += sin(m_Time*1.8  + 3.0f * abs(position.y)) * (refractionScale * min(depth2, 1.0));
 
                    vec3 refraction = texture2D(m_Texture, texC).rgb;
                    if(getPosition(texC).y > level)
                            refraction = color2;
                    

                    vec3 waterPosition = surfacePoint.xyz;
                    waterPosition.y -= (level - m_WaterHeight);
                    vec4 texCoordProj =m_TextureProjMatrix* vec4(waterPosition, 1.0);
       
                    texCoordProj.x = texCoordProj.x + displace * normal.x;
                    texCoordProj.z = texCoordProj.z + displace * normal.z;
                    texCoordProj/=texCoordProj.w;
                    texCoordProj.y=1.0-texCoordProj.y;

                    vec3 reflection =texture2D(m_ReflectionMap, texCoordProj.xy).rgb;

                   
                    float fresnel = fresnelTerm(normal, eyeVecNorm);
                    float depthN = depth * m_WaterTransparency;
                    float waterCol = saturate(length(m_LightColor.rgb) / m_SunScale);
                    refraction = mix(mix(refraction, m_WaterColor.rgb * waterCol, saturate(depthN / visibility)),
                                                      m_DeepWaterColor.rgb * waterCol, saturate(depth2 / m_ColorExtinction));

                    vec3 foam = vec3(0.0);
                
                    texC = (surfacePoint.xz + eyeVecNorm.xz * 0.1) * 0.05 + m_Time * 0.05f * m_WindDirection + sin(m_Time * 0.001 + position.x) * 0.005;
                   
                    vec2 texCoord2 = (surfacePoint.xz + eyeVecNorm.xz * 0.1) * 0.05 + m_Time * 0.1f * m_WindDirection + sin(m_Time * 0.001 + position.z) * 0.005;
                   
                    if(depth2 < m_FoamExistence.x)
                            foam = (texture2D(m_FoamMap, texC).r + texture2D(m_FoamMap, texCoord2)).rgb * 0.4;
                    else if(depth2 < m_FoamExistence.y)
                    {
                            foam = mix((texture2D(m_FoamMap, texC) + texture2D(m_FoamMap, texCoord2)) * 0.4, vec4(0.0),
                                                     (depth2 - m_FoamExistence.x) / (m_FoamExistence.y - m_FoamExistence.x)).rgb;

                    }

                    if(m_MaxAmplitude - m_FoamExistence.z > 0.0001)
                    {
                            foam += ((texture2D(m_FoamMap, texC) + texture2D(m_FoamMap, texCoord2)) * 0.4 *
                                    saturate((level - (m_WaterHeight + m_FoamExistence.z)) / (m_MaxAmplitude - m_FoamExistence.z))).rgb;
                    }
                    foam *= m_LightColor.rgb;
                 

                    vec3 specular =vec3(0.0);
                    vec3 lightDir=normalize(m_LightDir);
                    vec3 mirrorEye = (2.0f * dot(eyeVecNorm, normal) * normal - eyeVecNorm);
                    float dotSpec = saturate(dot(mirrorEye.xyz, -lightDir) * 0.5 + 0.5);
                    specular = vec3((1.0 - fresnel) * saturate(-lightDir.y) * ((pow(dotSpec, 512.0)) * (m_Shininess * 1.8 + 0.2)));
                    specular += specular * 25 * saturate(m_Shininess - 0.05);
                    //foam does not shine
                    specular=specular * m_LightColor.rgb - (5.0 * foam);

                    color = mix(refraction, reflection, fresnel);
                    color = mix(refraction, color, saturate(depth * m_ShoreHardness));
                    color = saturate(color + max(specular, foam ));
                    color = mix(refraction, color, saturate(depth* m_FoamHardness));
                  
            }

            if(position.y > level){
                    color = color2;
            }

            gl_FragColor = vec4(color,0.0);
        }


 /*   if(position.y>0.0){
        gl_FragColor = vec4(color,1.0);
    }else{
        gl_FragColor = vec4(0.0,0.0,1.0,1.0);
    }
*/
//gl_FragColor = texture2D(m_ReflectionMap, texCoord);
}