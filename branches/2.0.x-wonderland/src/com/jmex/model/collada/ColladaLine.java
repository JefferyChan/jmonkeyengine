/*
 * Copyright (c) 2011 jMonkeyEngine
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

import com.jme.scene.MatrixLine;

/**
 * Cloneable line
 * @author Jonathan Kaplan
 */
public class ColladaLine extends MatrixLine 
    implements ColladaGeometry, ColladaCloneable 
{
    private String material;
    
    public ColladaLine() {
        super ();
    }
    
    /**
     * Copy constructor
     * @param copy the colladaline to copy
     */
    protected ColladaLine(ColladaLine copy) {
        super (copy.getName());
        
        // copy spatial attributes
        ColladaNode.cloneSpatial(copy, this);
        
        // copy material
        this.material = copy.getMaterial();
        
        // shallow copy of geometry data
        this.setIndexBuffer(copy.getIndexBuffer());
        this.setVertexBuffer(copy.getVertexBuffer());
        this.setNormalBuffer(copy.getNormalBuffer());
        this.setTangentBuffer(copy.getTangentBuffer());
        this.setBinormalBuffer(copy.getBinormalBuffer());
        this.setTextureCoords(copy.getTextureCoords());
        
        this.setDefaultColor(copy.getDefaultColor());
        this.setLocalTransform(copy.getLocalTransform().clone());
        
        if (copy.getModelBound() != null) {
            copy.getModelBound().clone(this.bound);
        }
        
        // copy other line data
        this.setLineWidth(copy.getLineWidth());
        this.setMode(copy.getMode());
        this.setStipplePattern(copy.getStipplePattern());
        this.setAntialiased(copy.isAntialiased());
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
  
    @Override
    public ColladaLine clone() {
        return new ColladaLine(this);
    }
}
