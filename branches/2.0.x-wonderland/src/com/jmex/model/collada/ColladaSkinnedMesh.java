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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import com.jmex.model.collada.ColladaControllerNode.VertexInfo;
import java.nio.FloatBuffer;

/**
 * Node that represents a skinned collada mesh.
 * @author Jonathan Kaplan
 */
public class ColladaSkinnedMesh extends ColladaTriMesh {
    // mapping from our vertex number to a vertex number in the controller's
    // list of vertices
    private final int[] vertexIndices;
    
    private FloatBuffer skinnedVertices;
    private FloatBuffer skinnedNormals;

    // the last time the controller was updated (to avoid duplicate updates)
    private long lastUpdateTime = 0;
    
    // the minimum and maximum points for updating bounds
    private FloatBuffer minMaxPoints;
    
    /**
     * Create a new ColladaSkinnedMesh
     * @param name
     * @param target
     * @param parent 
     */
    public ColladaSkinnedMesh(String name, TriMesh target, 
                              int[] vertexIndices) 
    {
        super (name, target);

        this.vertexIndices = vertexIndices;
    }
    
    /**
     * Copy constructor
     * @param copy the skinned mesh to copy
     */
    protected ColladaSkinnedMesh(ColladaSkinnedMesh copy) {
        super (copy);
        
        // shallow copy
        this.vertexIndices = copy.vertexIndices;
    }
    
    @Override
    public ColladaControllerNode getParent() {
        return (ColladaControllerNode) super.getParent();
    }

    @Override
    public void draw(Renderer r) {
        if (!r.isProcessingQueue()) {
            if (r.checkAndAdd(this))
                return;
        }
        
        TriMesh target = getTarget();
        ColladaControllerNode controller = getParent();

        // determine whether we need to recalculate vertices
        boolean recalculate = (lastUpdateTime != controller.getLastUpdateTime());
        
        // make sure the buffers have been created -- if not, create them
        if (skinnedVertices == null) {
            createBuffers();
        }
        
        // replace the target buffers with our own
        FloatBuffer origVertices = target.getVertexBuffer();
        target.setVertexBuffer(skinnedVertices);
        origVertices.rewind();
        
        FloatBuffer origNormals = target.getNormalBuffer();
        if (skinnedNormals != null) {
            target.setNormalBuffer(skinnedNormals);
            origNormals.rewind();
        }
        
        // now update all buffers if necessary
        if (recalculate) {
            skinnedVertices.clear();
            if (skinnedNormals != null) {
                skinnedNormals.clear();
            }
            
            Vector3f minBounds = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            Vector3f maxBounds = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            
            Vector3f vertex = new Vector3f();
            Vector3f normal = new Vector3f();
            for (int i = 0; i < getVertexCount(); i++) {
                // read the next values from the original buffers
                read(origVertices, vertex);
                read(origNormals, normal);

                // transform
                VertexInfo vi = controller.getVertexInfo(vertexIndices[i]);
                vi.apply(controller, vertex, normal);

                // track minimum and maximum bounds
                if (vertex.x > maxBounds.x) {
                    maxBounds.x = vertex.x;
                }
                if (vertex.x < minBounds.x) {
                    minBounds.x = vertex.x;
                }
                if (vertex.y > maxBounds.y) {
                    maxBounds.y = vertex.y;
                }
                if (vertex.y < minBounds.y) {
                    minBounds.y = vertex.y;
                }
                if (vertex.z > maxBounds.z) {
                    maxBounds.z = vertex.z;
                }
                if (vertex.z < minBounds.z) {
                    minBounds.z = vertex.z;
                }
                
                // write skinned buffers
                write(vertex, skinnedVertices);
                write(normal, skinnedNormals);
            }
            
            // record the new update time
            lastUpdateTime = controller.getLastUpdateTime();
            
            // update model bounds
            write(minBounds, minMaxPoints);
            write(maxBounds, minMaxPoints);
            updateModelBound();
        }
        
        super.draw(r);
        
        // reset buffers
        target.setVertexBuffer(origVertices);
        target.setNormalBuffer(origNormals);
    }
    
    protected void createBuffers() {
        TriMesh t = getTarget();
        skinnedVertices = BufferUtils.createVector3Buffer(t.getVertexCount());
        
        if (t.getNormalBuffer() != null) {
            skinnedNormals = BufferUtils.createVector3Buffer(t.getVertexCount());
        }
        
        minMaxPoints = BufferUtils.createVector3Buffer(2);
    }
    
    private static void read(FloatBuffer buffer, Vector3f vec) {
        if (buffer == null) {
            return;
        }
        
        vec.set(buffer.get(), buffer.get(), buffer.get());
    }
    
    private static void write(Vector3f vector, FloatBuffer buffer) {
        if (buffer == null || vector == null) {
            return;
        }
        
        buffer.put(vector.x);
        buffer.put(vector.y);
        buffer.put(vector.z);
    }

    @Override
    public void updateWorldBound() {
        if (getModelBound() != null) {
            worldBound = getModelBound().transform(getWorldTransform());
        }
    }

    @Override
    public BoundingVolume getModelBound() {
        if (bound == null) {
            return super.getModelBound();
        }
        
        return bound;
    }

    @Override
    public void updateModelBound() {
        if (minMaxPoints == null) {
            super.updateModelBound();
        } else {
            // update bounds from points
            if (bound == null) {
                bound = new BoundingBox();
            }
            
            bound.computeFromPoints(minMaxPoints);
            updateWorldBound();
        }
    }
    
    @Override
    public ColladaTriMesh clone() {
        return new ColladaSkinnedMesh(this);
    }    
}
