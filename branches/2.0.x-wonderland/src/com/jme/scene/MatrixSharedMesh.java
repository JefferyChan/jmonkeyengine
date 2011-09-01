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
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Shared mesh with a transform matrix
 * @author Jonathan Kaplan
 */
public class MatrixSharedMesh extends SharedMesh implements MatrixGeometry {
    private final MatrixHelper matrix = new MatrixHelper();
    
    public MatrixSharedMesh() {
        super();
        
        MatrixHelper.setMatrixDefaults(this);
    }
    
    public MatrixSharedMesh(String name, TriMesh target) {
        super();
        MatrixHelper.setMatrixDefaults(this);

        setName(name);
        
        defaultColor = null;

        if (target instanceof MatrixSharedMesh) {
            setTarget(((MatrixSharedMesh) target).getTarget());
            this.setName(target.getName());
            this.setCullHint(target.cullHint);
            this.setLightCombineMode(target.lightCombineMode);
            this.setRenderQueueMode(target.renderQueueMode);
            this.setTextureCombineMode(target.textureCombineMode);
            this.setZOrder(target.getZOrder());
            this.setDefaultColor(target.getDefaultColor());
            for (RenderState.StateType type : RenderState.StateType.values()) {
                RenderState state = target.getRenderState( type );
                if (state != null) {
                    this.setRenderState(state );
                }
            }
        } else {
            setTarget(target);
        }
        
        // if we get here, we know it was a MatrixTriMesh or MatrixSharedMesh
        matrix.setLocalTransform(((MatrixGeometry) target).getLocalTransform());
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
    public void setTarget(TriMesh target) {
        if (!(target instanceof MatrixTriMesh)) {
            throw new IllegalArgumentException("Only MatrixTriMesh allowed");
        }
        
        super.setTarget(target);
    }
    
    @Override
    public MatrixTriMesh getTarget() {
        return (MatrixTriMesh) super.getTarget();
    }   
    
    @Override
    public boolean hasTriangleCollision(TriMesh toCheck) {
        getTarget().setLocalTransform(getWorldTransform());
        getTarget().updateWorldBound();
        return getTarget().hasTriangleCollision(toCheck);
    }

    @Override
    public void findTriangleCollision(TriMesh toCheck,
            ArrayList<Integer> thisIndex, ArrayList<Integer> otherIndex) 
    {
        getTarget().setLocalTransform(getWorldTransform());
        getTarget().updateWorldBound();
        getTarget().findTriangleCollision(toCheck, thisIndex, otherIndex);
    }

    @Override
    public void findTrianglePick(Ray toTest, ArrayList<Integer> results) {
        getTarget().setLocalTransform(getWorldTransform());
        getTarget().updateWorldBound();
        getTarget().findTrianglePick(toTest, results);
    }
    
    @Override
    public void draw(Renderer r) {
        if (!r.isProcessingQueue()) {
            if (r.checkAndAdd(this))
                return;
        }
        
        getTarget().getWorldTransform().set(getWorldTransform());
        getTarget().setDefaultColor(getDefaultColor());
        getTarget().setGlowColor(getGlowColor());
        getTarget().setGlowEnabled(isGlowEnabled());
        getTarget().setGlowScale(getGlowScale());
        getTarget().setRenderQueueMode(getRenderQueueMode());
        getTarget().setLightState(getLightState());
        System.arraycopy(this.states, 0, getTarget().states, 0, states.length);

        r.draw(getTarget());
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
    public void updateWorldBound() {
        if (getTarget().getModelBound() != null) {
            worldBound = getTarget().getModelBound().transform(getWorldTransform(), worldBound);
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
