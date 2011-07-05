/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jmex.model.collada;

import java.util.ArrayList;
import java.util.HashMap;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.image.Texture.WrapMode;
import com.jme.scene.Controller;
import com.jme.scene.state.RenderState;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ColladaMaterial is designed to hold all the material attributes of a Collada
 * object. This may include many RenderState objects. ColladaMaterial is a
 * container object for jME RenderStates needed.
 * 
 * @author Mark Powell
 */
public class ColladaMaterial implements Cloneable {
    private MinificationFilter minFilter = MinificationFilter.Trilinear;
    private MagnificationFilter magFilter = MagnificationFilter.Bilinear;
    private WrapMode wrapS = WrapMode.Repeat;
    private WrapMode wrapT = WrapMode.Repeat;
    
    private final RenderState[] stateList;
    
    private final Map<String, String> glslCode = new LinkedHashMap<String, String>();
    private final Map<String, Object> glslParams = new LinkedHashMap<String, Object>();
  
    // map from texture coordinates name to texture unit number
    private final Map<String, Integer> textureUnitMap = new LinkedHashMap<String, Integer>();
    
    public ColladaMaterial() {
        stateList = new RenderState[RenderState.StateType.values().length];
    }

    public void setState(RenderState ss) {
    	if(ss == null) return;
        stateList[ss.getStateType().ordinal()] = ss;
    }

    /**
     * @deprecated As of 2.0, use {@link #getState(com.jme.scene.state.RenderState.StateType)} instead.
     */
    public RenderState getState(int index) {
        return stateList[index];
    }

    public RenderState getState(RenderState.StateType type) {
        return stateList[type.ordinal()];
    }
    
    public MagnificationFilter getMagFilterConstant() {
        return magFilter;
    }
    
    public void setMagFilterConstant(String magFilterStr) {
        magFilter = getMagFilter(magFilterStr);
    }
    
    public void setMagFilterConstant(MagnificationFilter filter) {
        magFilter = filter;
    }
    
    public static MagnificationFilter getMagFilter(String magFilterStr) {
        if (magFilterStr.equals("NEAREST")) {
            return MagnificationFilter.NearestNeighbor;
        } else if(magFilterStr.equals("LINEAR")) {
            return MagnificationFilter.Bilinear;
        } else {
            return MagnificationFilter.Bilinear;
        }
    }
    
    public MinificationFilter getMinFilterConstant() {
        return minFilter;
    }
    
    public void setMinFilterConstant(String minFilterStr) {
        minFilter = getMinFilter(minFilterStr);
    }

    public void setMinFilterConstant(MinificationFilter filter) {
        minFilter = filter;
    }
    
    public static MinificationFilter getMinFilter(String minFilterStr) {
        if (minFilterStr.equals("NEAREST")) {
            return MinificationFilter.NearestNeighborNoMipMaps;
        } else if (minFilterStr.equals("LINEAR")) {
            return MinificationFilter.BilinearNoMipMaps;
        } else if (minFilterStr.equals("NEAREST_MIPMAP_NEAREST")) {
            return MinificationFilter.NearestNeighborNearestMipMap;
        } else if (minFilterStr.equals("NEAREST_MIPMAP_LINEAR")) {
            return MinificationFilter.NearestNeighborLinearMipMap;
        } else if (minFilterStr.equals("LINEAR_MIPMAP_NEAREST")) {
            return MinificationFilter.BilinearNearestMipMap;
        } else if (minFilterStr.equals("LINEAR_MIPMAP_LINEAR")) {
            return MinificationFilter.Trilinear;
        } else {
            return MinificationFilter.Trilinear;
        }
    }
    
    public WrapMode getWrapSConstant() {
        return wrapS;
    }
    
    public void setWrapSConstant(String wrapSStr) {
        wrapS = getWrapMode(wrapSStr);
    }    
    
    public void setWrapSConstant(WrapMode wrap) {
        wrapS = wrap;
    }

    public WrapMode getWrapTConstant() {
        return wrapT;
    }
    
    public void setWrapTConstant(String wrapTStr) {
        wrapT = getWrapMode(wrapTStr);
    }    
    
    public void setWrapTConstant(WrapMode wrap) {
        wrapT = wrap;
    }
    
    public static WrapMode getWrapMode(String wrapStr) {
        if (wrapStr.equals("WRAP")) {
            return WrapMode.Repeat;
        } else if (wrapStr.equals("NONE")) {
            return WrapMode.Clamp;
        } else {
            return WrapMode.Repeat;
        }
    }
    
    public void putGLSLCode(String key, String code) {
        glslCode.put(key, code);
    }
    
    public String getGLSLCode(String key) {
        return glslCode.get(key);
    }
    
    public Map<String, String> getGLSLCode() {
        return glslCode;
    }
    
    public void putGLSLParam(String key, Object param) {
        glslParams.put(key, param);
    }
    
    public Object getGLSLParam(String key) {
        return glslParams.get(key);
    }
    
    public Map<String, Object> getGLSLParams() {
        return glslParams;
    }
    
    public void addTextureRef(String name, int textureUnit) {
        textureUnitMap.put(name, textureUnit);
    }
    
    public int getTextureRef(String name) {
        Integer res = textureUnitMap.get(name); 
        if (res == null) {
            return 0;
        }
        
        return res.intValue();
    }
}
