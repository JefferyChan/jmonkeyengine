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

import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.system.DisplaySystem;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import java.io.IOException;

/**
 * Trimesh with a transform matrix
 * @author Jonathan Kaplan
 */
public class MatrixTriMesh extends TriMesh implements MatrixGeometry {
    private final MatrixHelper matrix = new MatrixHelper();
    
    public MatrixTriMesh() {
        super();
        
        MatrixHelper.setMatrixDefaults(this);
    }
    
    public MatrixTriMesh(String name) {
        super (name);
        
        MatrixHelper.setMatrixDefaults(this);
    }
    
    public Matrix4f getLocalTransform() {
        return matrix.getLocalTransform();
    }

    public void setLocalTransform(Matrix4f transform) {
        matrix.setLocalTransform(transform);
    }

    public Matrix4f getWorldTransform() {
        return matrix.getWorldTransform();
    }

    @Override
    public void updateWorldBound() {
        if (bound != null) {
            worldBound = bound.transform(getWorldTransform(), worldBound);
        }
    }
    
    @Override
    public Vector3f getLocalTranslation() {
        return matrix.getLocalTranslation();
    }
    
    @Override
    public void setLocalTranslation(Vector3f localTranslation) {
        if (isLive()) {
            DisplaySystem.checkForRenderThread();
        }
        
        matrix.setLocalTranslation(localTranslation);
    }

    @Override
    public void setLocalTranslation(float x, float y, float z) {
        setLocalTranslation(new Vector3f(x, y, z));
    }
    
    @Override
    public Quaternion getLocalRotation() {
        return matrix.getLocalRotation();
    }
    
    @Override
    public void setLocalRotation(Quaternion quaternion) {
        if (isLive()) {
            DisplaySystem.checkForRenderThread();
        }
        
        matrix.setLocalRotation(quaternion);
    }
    
    @Override
    public void setLocalRotation(Matrix3f rotation) {
        if (isLive()) {
            DisplaySystem.checkForRenderThread();
        }
        
        matrix.setLocalRotation(rotation);
    }
    
    @Override
    public Vector3f getLocalScale() {
        return matrix.getLocalScale();
    }
    
    @Override
    public void setLocalScale(Vector3f localScale) {
        if (isLive()) {
            DisplaySystem.checkForRenderThread();
        }
        
        matrix.setLocalScale(localScale);
    }

    @Override
    public void setLocalScale(float localScale) {
        setLocalScale(new Vector3f(localScale, localScale, localScale));
    }
    
    @Override
    public Quaternion getWorldRotation() {
        return matrix.getWorldRotation();
    }

    @Override
    public Vector3f getWorldScale() {
        return matrix.getWorldScale();
    }

    @Override
    public Vector3f getWorldTranslation() {
        return matrix.getWorldTranslation();
    }
    
    @Override
    public Matrix4f getLocalToWorldMatrix(Matrix4f store) {
        return matrix.getWorldTransform();
    }

    @Override
    public Vector3f localToWorld(Vector3f in, Vector3f store) {
        return matrix.getWorldTransform().mult(in, store);
    }

    @Override
    public Vector3f worldToLocal(Vector3f in, Vector3f store) {
        return matrix.getWorldTransform().invert().mult(in, store);
    }    
    
    public void updateWorldTransform() {
        matrix.updateWorldTransform(parent);
    }

    @Override
    public void updateWorldVectors(boolean recurse) {
        if (((lockedMode & Spatial.LOCKED_TRANSFORMS) == 0)) {
            updateWorldTransform();
        }
    }
    
    @Override
    public void read(JMEImporter e) throws IOException {
        super.read(e);
        matrix.read(e.getCapsule(this));
    }

    @Override
    public void write(JMEExporter e) throws IOException {
        super.write(e);
        matrix.write(e.getCapsule(this));
    }
    
    protected MatrixHelper getMatrixHelper() {
        return matrix;
    }
}
