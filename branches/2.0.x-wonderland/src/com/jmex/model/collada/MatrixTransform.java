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

/**
 * Matrix transformation
 * @author Jonathan Kaplan 
 */
public class MatrixTransform extends ColladaTransform {
    private final Matrix4f orig;
    private Matrix4f cur;

    public MatrixTransform(String sid, Matrix4f value) {
        super (sid);
        
        this.orig = value;
        this.cur = orig;
    }
    
    public void set(Matrix4f matrix) {
        this.cur = matrix;
    }
    
    public Matrix4f getCurrentValue() {
        return cur.clone();
    }
    
    public Matrix4f getOriginalValue() {
        return orig.clone();
    }
    
    @Override
    public void update(String member, float[] values) {
        // onyl accept whole matrix updates
        if (values.length != 16) {
            throw new IllegalArgumentException("Cannot update " + getSid() + 
                                               ": wrong number of arguments");
        }
        
        // values are in row major order
        Matrix4f updated = new Matrix4f();
        updated.set(values, true);
        set(updated);
    }
    
    @Override
    public void reset() {
        cur = orig;
    }
    
    @Override
    public void apply(Matrix4f transform) {
        transform.multLocal(getCurrentValue());
    }
    
    @Override
    public void interpolate(float percent, ColladaTransform t1, ColladaTransform t2) {
        if (!(t1 instanceof MatrixTransform) || 
            !(t2 instanceof MatrixTransform))
        {
            throw new IllegalArgumentException("Only matrix interpolation supported");
        }
        
        Matrix4f m1 = ((MatrixTransform) t1).getCurrentValue();
        Matrix4f m2 = ((MatrixTransform) t2).getCurrentValue();
        
        // no JME method for this, need to do it by hand
        float[] f1 = new float[16];
        m1.get(f1);
        
        float[] f2 = new float[16];
        m2.get(f2);
        
        for (int i = 0; i < 12; i++)
        {
            if (f1[i] != f2[i]) {
                f1[i] = (f1[i] * (1.0f - percent)) + (f2[i] * percent);
            }
        }
        
        Matrix4f interpolated = new Matrix4f();
        interpolated.set(f1, true);
        set(interpolated);
    }
    
    @Override
    public MatrixTransform clone() {
        MatrixTransform out = new MatrixTransform(getSid(), getOriginalValue());
        out.set(getCurrentValue());
        return out;
    }
}
