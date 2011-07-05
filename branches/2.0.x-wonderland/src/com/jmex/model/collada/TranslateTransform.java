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

import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;

/**
 * Translate transformation
 * @author Jonathan Kaplan 
 */
public class TranslateTransform extends ColladaTransform {
    private final Vector3f orig;
    private Vector3f cur;

    public TranslateTransform(String sid, Vector3f value) {
        super (sid);
        
        this.orig = value;
        this.cur = orig;
    }
    
    public void set(Vector3f vector) {
        this.cur = vector;
    }
    
    public Vector3f getCurrentValue() {
        return cur.clone();
    }
    
    public Vector3f getOriginalValue() {
        return orig.clone();
    }
    
    @Override
    public void update(String member, float[] values) {
        // accept update to entire value or any individual value
        if (values.length == 3) {
            set(new Vector3f(values[0], values[1], values[2]));
        } else if (values.length == 1 && member.equalsIgnoreCase("X")) {
            set(new Vector3f(values[0], cur.y, cur.z));
        } else if (values.length == 1 && member.equalsIgnoreCase("Y")) {
            set(new Vector3f(cur.x, values[0], cur.z));
        } else if (values.length == 1 && member.equalsIgnoreCase("Z")) {
            set(new Vector3f(cur.x, cur.y, values[0]));
        } else {
            throw new IllegalArgumentException("Unable to update " + getSid() + 
                                               ": must update entire transform " +
                                               " or X, Y, or Z");
        }
    }
    
    @Override
    public void reset() {
        cur = orig;
    }
    
    @Override
    public void apply(Matrix4f transform) {
        Matrix4f translate = new Matrix4f();
        translate.setTranslation(getCurrentValue());
        transform.multLocal(translate);
    }

    @Override
    public void interpolate(float percent, ColladaTransform t1, ColladaTransform t2) {
        if (!(t1 instanceof TranslateTransform) || 
            !(t2 instanceof TranslateTransform))
        {
            throw new IllegalArgumentException("Only translate interpolation supported");
        }
        
        Vector3f v1 = ((TranslateTransform) t1).getCurrentValue();
        Vector3f v2 = ((TranslateTransform) t2).getCurrentValue();
        
        v1.interpolate(v2, percent);
        set(v1);
    }
    
    @Override
    public TranslateTransform clone() {
        TranslateTransform out = new TranslateTransform(getSid(), getOriginalValue());
        out.set(getCurrentValue());
        return out;
    }
}
