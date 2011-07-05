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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 * Rotate transformation
 * @author Jonathan Kaplan 
 */
public class RotateTransform extends ColladaTransform {
    private final Vector3f origAxis;
    private final float origAngle;    
    private Quaternion curQuaternion;

    public RotateTransform(String sid, Vector3f axis, float angle) {
        super (sid);
        
        this.origAxis = axis;
        this.origAngle = normalizeAngle(angle); 
        
        this.curQuaternion = new Quaternion();
        curQuaternion.fromAngleAxis((float) Math.toRadians(angle), axis);
    }
    
    public void set(Vector3f axis, float angle) {
        angle = normalizeAngle(angle);
        this.curQuaternion.fromAngleAxis((float) Math.toRadians(angle), axis);        
    }
    
    public void set(Quaternion quaternion) {
        this.curQuaternion = quaternion;
    }
    
    public Quaternion getCurrentQuaternion() {
        return curQuaternion;
    }
    
    public Vector3f getOriginalAxis() {
        return origAxis.clone();
    }
    
    public float getOriginalAngle() {
        return origAngle;
    }
    
    @Override
    public void update(String member, float[] values) {
        // onyl accept whole matrix updates
        if (values.length != 1) {
            throw new IllegalArgumentException("Cannot update " + getSid() + 
                                               ": wrong number of arguments");
        }
        
        if (!member.equalsIgnoreCase("ANGLE")) {
            throw new IllegalArgumentException("Cannot update " + getSid() +
                                               ": only ANGLE can be modified");
        }
        
        set(origAxis, values[0]);
    }
    
    @Override
    public void reset() {
        curQuaternion = new Quaternion();
        curQuaternion.fromAngleAxis((float) Math.toRadians(origAngle), origAxis);
    }
    
    @Override
    public void apply(Matrix4f transform) {
       transform.multLocal(getCurrentQuaternion());
    }
    
    @Override
    public void interpolate(float percent, ColladaTransform t1, ColladaTransform t2) {
        if (!(t1 instanceof RotateTransform) || 
            !(t2 instanceof RotateTransform))
        {
            throw new IllegalArgumentException("Only rotate interpolation supported");
        }
        
        Quaternion q1 = ((RotateTransform) t1).getCurrentQuaternion();
        Quaternion q2 = ((RotateTransform) t2).getCurrentQuaternion();
        
        Quaternion slerp = new Quaternion();
        slerp.slerp(q1, q2, percent);
        
        set(slerp);
    }
    
    @Override
    public RotateTransform clone() {
        RotateTransform out = new RotateTransform(getSid(), getOriginalAxis(),
                                                  getOriginalAngle());
        out.set(getCurrentQuaternion().clone());
        return out;
    }
    
    // normalize an angle to between 0 and 360
    private static float normalizeAngle(float angle) {
        angle %= 360f;
        if (angle < 0f) {
            angle += 360f;
        }
        
        return angle;
    }
}
