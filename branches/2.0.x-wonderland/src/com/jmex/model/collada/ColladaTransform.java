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
 * Abstract superclass of collada transform types
 * @author Jonathan Kaplan
 */
public abstract class ColladaTransform implements Cloneable {
    private final String sid;
    
    public ColladaTransform(String sid) {
        this.sid = sid;
    }
    
    /**
     * Get the name of this transform
     * @return the name
     */
    public String getSid() {
        return sid;
    }

    /**
     * Modify this transform by changing the given member to the given
     * values.
     * @param member the name of the member to target, or null to target
     * the entire transform
     * @param values the updated values
     */
    public abstract void update(String member, float[] values);
    
    /** 
     * Modify this transform by setting it to the result of interpolating by 
     * percent amount between two transforms
     * @param percent the percent to interpolate. The value is between 0.0 and
     * 1.0, where 0.0 is t1 and 1.0 is t2.
     * @param t1 the first transform to interpolate
     * @param t2 the second transform to interpolate.
     */
    public abstract void interpolate(float percent, ColladaTransform t1,
                                     ColladaTransform t2);
    
    /** Reset this transform back to its original value */
    public abstract void reset();

    /** Apply this transform to a transform matrix */
    public abstract void apply(Matrix4f transform);
    
    /** Clone */
    @Override
    public abstract ColladaTransform clone();
}
