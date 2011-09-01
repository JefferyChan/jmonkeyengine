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

import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.MatrixNode;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Collada node stores an ordered stack of transforms that can be used to
 * calculate the final transform matrix.
 * @author Jonathan Kaplan
 */
public class ColladaNode extends MatrixNode implements ColladaCloneable {
    private static final Logger LOGGER =
            Logger.getLogger(ColladaNode.class.getName());
    
    /**
     * map of transform elements by sid. This is a LinkedHashMap because
     * ordering is important.
     */
    private final Map<String, ColladaTransform> transforms =
            new LinkedHashMap<String, ColladaTransform>();

    /**
     * map of nodes this node instantiates. Used for delayed instantiation.
     */
    private List<String> instanceNodes;
    
    /**
     * Create a new ColladaNode with the given name
     */
    public ColladaNode(String name) {
        super (name);
    }
    
    /**
     * Protected copy constructor
     * @param node the node to copy
     */
    protected ColladaNode(ColladaNode node) {
        super (node.getName());
        
        // copy spatial properties
        cloneSpatial(node, this);
        
        // copy transforms
        for (ColladaTransform t : node.getAllTransforms()) {
            transforms.put(t.getSid(), t);
        }
        
        if (node.getInstanceNodes() != null) {
            instanceNodes = new LinkedList<String>();
            for (String instanceNode : node.getInstanceNodes()) {
                instanceNodes.add(instanceNode);
            }
        }
    }
    
    /**
     * Add a transform to the list of transforms.
     * @param transform the transform to add
     */
    public void addTransform(ColladaTransform transform) {
        transforms.put(transform.getSid(), transform);
    }
    
    /**
     * Get a transform by name. If the transform is updated, the results will
     * not be calculated until the next time updateWorldTransform() is called.
     * @param sid the sid of the transform to get
     * @return the transform with the given sid, or null if no transform exists
     * with the given id
     */
    public ColladaTransform getTransform(String sid) {
        return transforms.get(sid);
    }
    
    /**
     * Get all transforms for this node
     * @return all transforms for this node, in the order they should be 
     * applied
     */
    protected Collection<ColladaTransform> getAllTransforms() {
        return transforms.values();
    }
     
    /**
     * Add a reference to an instance node
     * @param instanceNode the name of the node to reference
     */
    void addInstanceNode(String instanceNode) {
        if (instanceNodes == null) {
            instanceNodes = new LinkedList<String>();
        }
        
        instanceNodes.add(instanceNode);
    }
    
    /**
     * Get the list of instance node references
     * @return the list of instance node references
     */
    List<String> getInstanceNodes() {
        return instanceNodes;
    }
    
    /**
     * Recalculate transform by first combining all transforms in the stack
     * and then calculating the world transform.
     */
    @Override
    public void updateWorldTransform() {
        // make any changes to the local transform
        updateLocalTransform();
        
        super.updateWorldTransform();
    }
    
    /**
     * Apply transforms to set the correct local transform.
     */
    protected void updateLocalTransform() {
        // first combine all transforms
        Matrix4f matrix = new Matrix4f();
        for (ColladaTransform transform : getAllTransforms()) {
            transform.apply(matrix);
        }
        
        // now update the local transform
        super.setLocalTransform(matrix);
    }

    @Override
    public void setLocalRotation(Quaternion quaternion) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }

    @Override
    public void setLocalRotation(Matrix3f rotation) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }

    @Override
    public void setLocalScale(Vector3f localScale) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }

    @Override
    public void setLocalScale(float localScale) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }

    @Override
    public void setLocalTransform(Matrix4f transform) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }

    @Override
    public void setLocalTranslation(Vector3f localTranslation) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }

    @Override
    public void setLocalTranslation(float x, float y, float z) {
        throw new UnsupportedOperationException("Cannot set transform of collada node");
    }
    
    @Override
    public ColladaNode clone() {
        return new ColladaNode(this);
    }
    
    /**
     * A deep clone of this node and all children
     * @return a clone of this node with clones of all children attached.
     */
    public ColladaNode cloneTree() {
        ColladaNode out = clone();

        if (getChildren() != null) {
            for (Spatial child : getChildren()) {
                if (child instanceof ColladaNode) {
                    ColladaNode clone = ((ColladaNode) child).cloneTree();
                    out.attachChild(clone);
                } else if (child instanceof ColladaCloneable) {
                    Spatial clone = ((ColladaCloneable) child).clone();
                    out.attachChild(clone);
                } else {
                    LOGGER.warning("Non cloneable child " + child);
                }
            }
        }
        
        return out;
    }
    
    public static void cloneSpatial(Spatial src, Spatial dest) {
        dest.setCullHint(src.getLocalCullHint());
        dest.setLightCombineMode(src.getLocalLightCombineMode());
        dest.setRenderQueueMode(src.getLocalRenderQueueMode());
        dest.setTextureCombineMode(src.getLocalTextureCombineMode());
        dest.setZOrder(src.getZOrder());
        
        for (RenderState.StateType type : RenderState.StateType.values()) {
            RenderState state = src.getRenderState(type);
            if (state != null) {
                dest.setRenderState(state);
            }
        }
    }
}
