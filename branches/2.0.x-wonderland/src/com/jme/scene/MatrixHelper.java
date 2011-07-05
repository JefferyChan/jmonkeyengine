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
package com.jme.scene;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.util.export.InputCapsule;
import com.jme.util.export.OutputCapsule;
import java.io.IOException;

/**
 * Helper class to handle common matrix operations
 * @author Jonathan Kaplan
 */
public class MatrixHelper implements MatrixGeometry {
    private Matrix4f localMatrix = new Matrix4f();
    private Matrix4f worldMatrix = new Matrix4f();

    /**
     * @{inheritdoc}
     */
    public Matrix4f getLocalTransform() {
        return localMatrix;
    }

    /**
     * @{inheritdoc}
     */
    public void setLocalTransform(Matrix4f transform) {
        this.localMatrix = transform.clone();
        this.worldMatrix = transform.clone();
    }

    /**
     * @{inheritdoc}
     */
    public Matrix4f getWorldTransform() {
        return worldMatrix;
    }
    
    /**
     * Get the local translation
     * @return the local transform
     */
    public Vector3f getLocalTranslation() {
        return localMatrix.toTranslationVector();
    }
    
    /**
     * Set the local translation
     * @param translation the local translation
     */
    public void setLocalTranslation(Vector3f translation) {
        localMatrix.setTranslation(translation);
        worldMatrix.setTranslation(translation);
    }
    
    /**
     * Get the local rotation. Note that this may return incorrect results
     * for some rotation matrices.
     * @return the rotation
     */
    public Quaternion getLocalRotation() {
       return getRotation(localMatrix);
    }
    
    /**
     * Set the local rotation
     * @param rotation the rotation to set
     */
    public void setLocalRotation(Quaternion rotation) {
        localMatrix.setRotationQuaternion(rotation);
        worldMatrix.setRotationQuaternion(rotation);
    }
    
    /**
     * Set the local rotation from a matrix
     * @param rotation the rotation matrix to set
     */
    public void setLocalRotation(Matrix3f rotation) {
        // factor out scale
        Vector3f scale = getScale(localMatrix);
        
        // reapply scale to new rotation
        localMatrix.m00 = worldMatrix.m00 = rotation.m00 * scale.x;
        localMatrix.m01 = worldMatrix.m01 = rotation.m01 * scale.y;
        localMatrix.m02 = worldMatrix.m02 = rotation.m02 * scale.z;
        localMatrix.m10 = worldMatrix.m10 = rotation.m10 * scale.x;
        localMatrix.m11 = worldMatrix.m11 = rotation.m11 * scale.y;
        localMatrix.m12 = worldMatrix.m12 = rotation.m12 * scale.z;
        localMatrix.m20 = worldMatrix.m20 = rotation.m20 * scale.x;
        localMatrix.m21 = worldMatrix.m21 = rotation.m21 * scale.y;
        localMatrix.m22 = worldMatrix.m22 = rotation.m22 * scale.z;
    }
    
    /**
     * Get the local scale. Not implemented.
     * @return the local scale
     */
    public Vector3f getLocalScale() {
        return getScale(localMatrix);
    }
    
    /**
     * Set the local scale
     * @param scale the local scale
     */
    public void setLocalScale(Vector3f scale) {
        localMatrix.scale(scale);
    }
    
    /**
     * Get the world translation
     * @return the world translation
     */
    public Vector3f getWorldTranslation() {
        return worldMatrix.toTranslationVector();
    }
    
    /**
     * Get the world rotation
     * @return the world rotation
     */
    public Quaternion getWorldRotation() {
        return getRotation(worldMatrix);
    }
    
    /**
     * Get the world scale. Not currently implemented.
     * @return the world scale
     */
    public Vector3f getWorldScale() {
        return getScale(worldMatrix);
    }
       
    /**
     * Update the world matrix based on the given parent node
     * @param parent the parent to update from
     */
    public void updateWorldTransform(Spatial parent) {
        if (parent instanceof MatrixGeometry) {
            // update from parent matrix
            worldMatrix = ((MatrixGeometry) parent).getWorldTransform().clone();
            worldMatrix.multLocal(localMatrix);
        } else if (parent != null) {
            // update from parent rotation, translation and scale
            worldMatrix = new Matrix4f();
            worldMatrix.scale(parent.getWorldScale());
            worldMatrix.multLocal(parent.getWorldRotation());
            worldMatrix.setTranslation(parent.getWorldTranslation());
            worldMatrix.multLocal(localMatrix);
        } else {
            worldMatrix = new Matrix4f(localMatrix);
        }
    }
    
    /**
     * Read from JME
     */
    public void read(InputCapsule capsule) throws IOException {
        Matrix4f mat = (Matrix4f) capsule.readSavable("localMatrix", new Matrix4f());
        setLocalTransform(mat);
    }
    
    /**
     * Write to JME
     */
    public void write(OutputCapsule capsule) throws IOException {
        capsule.write(getLocalTransform(), "localMatrix", new Matrix4f());
    }
    
    /**
     * Get the rotation of a matrix
     * @param matrix the matrix to get the rotation of
     * @return the rotation of the matrix
     * @throws IllegalArgumentException if the rotation of the matrix cannot be
     * determined
     */
    public static Quaternion getRotation(Matrix4f matrix) {
        if (!isAffine(matrix)) {
            throw new IllegalArgumentException("Can't get scale of non-affine matrix");
        }
        
        if (!isOrthogonal(matrix)) {
            throw new IllegalArgumentException("Can't get scale of non-orthonormal matrix");
        }
        
        return matrix.toRotationQuat();
    }
    
    /**
     * Get the scale of a matrix
     * @param matrix the matrix to get the scale of
     * @return the scale of the matrix
     * @throws IllegalArgumentException if the scale of the matrix cannot be
     * determined
     */
    public static Vector3f getScale(Matrix4f matrix) {
        if (!isAffine(matrix)) {
            throw new IllegalArgumentException("Can't get scale of non-affine matrix");
        }
        
        if (!isOrthogonal(matrix)) {
            throw new IllegalArgumentException("Can't get scale of non-orthonormal matrix");
        }
        
        Vector3f out = new Vector3f();
        out.x = (float) Math.sqrt(matrix.m00 * matrix.m00 + 
                                  matrix.m10 * matrix.m10 +
                                  matrix.m20 * matrix.m20);
        out.y = (float) Math.sqrt(matrix.m01 * matrix.m01 + 
                                  matrix.m11 * matrix.m11 +
                                  matrix.m21 * matrix.m21);
        out.z = (float) Math.sqrt(matrix.m02 * matrix.m02 + 
                                  matrix.m12 * matrix.m12 +
                                  matrix.m22 * matrix.m22);
        return out;
    }
    
    /**
     * Determine if the given matrix is affine
     * @param matrix the matrix to compare
     * return true if the matrix is affine, or false if not
     */
    public static boolean isAffine(Matrix4f matrix) {
        // the matrix is affine if the last row is 0, 0, 0, 1
        return epsilonEquals(matrix.m30, 0f) &&
               epsilonEquals(matrix.m31, 0f) &&
               epsilonEquals(matrix.m32, 0f) &&
               epsilonEquals(matrix.m33, 1f);
    }
    
    /**
     * Determine if the given matrix is congruent
     * @param matrix the matrix to check
     * return true if the matrix is congruent, or false if not
     */
    public static boolean isCongruent(Matrix4f matrix) {
        float check1 = matrix.m00 * matrix.m00 + matrix.m10 * matrix.m10 +
                       matrix.m20 * matrix.m20;
        float check2 = matrix.m01 * matrix.m01 + matrix.m11 * matrix.m11 +
                       matrix.m21 * matrix.m21;
        if (!epsilonEquals(check1, check2)) {
            return false;
        }
        
        float check3 = matrix.m02 * matrix.m02 + matrix.m12 * matrix.m12 +
                       matrix.m22 * matrix.m22;
        if (!epsilonEquals(check1, check3)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determine if the given matrix is orthogonal
     * @param matrix the matrix to check
     * @return true if the matrix is orthagonal, or false if not
     */
    public static boolean isOrthogonal(Matrix4f matrix) {
        float check = matrix.m00 * matrix.m02 + matrix.m10 * matrix.m12 +
                      matrix.m20 * matrix.m22;
        if (!epsilonEquals(check, 0)) {
            return false;
        }
        
        check = matrix.m00 * matrix.m01 + matrix.m10 * matrix.m11 +
                matrix.m20 * matrix.m21;
        if (!epsilonEquals(check, 0)) {
            return false;
        }
        
        check = matrix.m01 * matrix.m02 + matrix.m11 * matrix.m12 +
                matrix.m21 * matrix.m22;
        if (!epsilonEquals(check, 0)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Return true if the two values given are within epsilon of each other
     * @param v1 the first value
     * @param v2 the second value
     * @return true if the values are equal, or false if not
     */
    protected static boolean epsilonEquals(float v1, float v2) {
        return Math.abs(v1 - v2) <= FastMath.FLT_EPSILON;
    }
    
    /**
     * Set default values on a matrix node
     * @param node the node to set values on
     */
    public static void setMatrixDefaults(Spatial s) {
        // since we can't always detect the scale, tell OpenGL to deal
        // with it itself
        s.setNormalsMode(Spatial.NormalsMode.AlwaysNormalize);
    }
}
