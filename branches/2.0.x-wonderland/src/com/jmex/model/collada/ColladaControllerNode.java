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
import com.jme.renderer.Renderer;
import com.jme.util.geom.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/** 
 * A COLLADA controller, the parent of skinned meshes
 * @author Jonathan Kaplan
 */
public class ColladaControllerNode extends ColladaNode {
    private static final Logger LOGGER = 
            Logger.getLogger(ColladaControllerNode.class.getName());
    
    /** maximum number of joints to consider per vertex */
    private static final int MAX_JOINTS = 4;
    
    /** ordered list of joints **/
    private final List<JointInfo> joints = new ArrayList<JointInfo>();
    
    /** ordered list of vertex weights (one per vertex) */
    private final List<VertexInfo> vertices = new ArrayList<VertexInfo>();
    
    /** the bind matrix */
    private final Matrix4f bindMatrix;
    
    /** a joint info for referencing the bind matrix */
    private final BindJointInfo bindJointInfo;
    
    /** the set of skeleton names we reference */
    private final Set<String> skeletonNames = new LinkedHashSet<String>();
    
    /** the last time the matrices were updated */
    private long lastUpdateTime = 0;
    
    public ColladaControllerNode(String name, Matrix4f bindMatrix) {
        super (name);
        
        this.bindMatrix = bindMatrix;
        this.bindJointInfo = new BindJointInfo(bindMatrix);
    }
    
    /**
     * Copy constructor
     * @param copy the controller to copy
     */
    protected ColladaControllerNode(ColladaControllerNode copy) {
        super (copy);
        
        this.bindMatrix = copy.getBindMatrix().clone();
        this.bindJointInfo = new BindJointInfo(bindMatrix);
        
        for (JointInfo ji : copy.getJoints()) {
            joints.add(ji.clone());
        }
        
        for (VertexInfo vi : copy.getVertices()) {
            VertexInfo vic = vi.clone();
            vic.normalizeWeights();
            
            vertices.add(vic);
        }
        
        for (String skeletonName : copy.getSkeletonNames()) {
            skeletonNames.add(skeletonName);
        }
    }
    
    /**
     * Get the bind matrix for this controller
     * @return the bind matrix
     */
    public Matrix4f getBindMatrix() {
        return bindMatrix;
    }
    
    /**
     * Add a new joint to the controller
     * @param sid the sid of the joint to look at
     * @param invBindMatrix the inverse bind matrix for the joint
     */
    public void addJoint(String sid, Matrix4f invBindMatrix) {
        joints.add(new JointInfo(sid, invBindMatrix));
    }
    
    /**
     * Get joint information by index
     * @param index the index of the joint to get, or -1 to reference the
     * bind matrix
     * @return the joint with the given index
     */
    JointInfo getJointInfo(int index) {
        if (index == -1) {
            return bindJointInfo;
        }
        
        return joints.get(index);
    }
    
    List<JointInfo> getJoints() {
        return joints;
    }
    
    /**
     * Add a new vertex, with the given joints and weights
     * @param joints an array of joint indices (-1 means the bind matrix)
     * @param weights the unnormalized weight to assign each vertex
     */
    public void addVertex(int[] joints, float[] weights) {
        if (joints.length != weights.length) {
            throw new IllegalArgumentException("Joint and weight sizes must " +
                                               "match.");
        }
        
        VertexInfo vi = new VertexInfo();
        for (int i = 0; i < joints.length; i++) {
            vi.addJoint(joints[i], weights[i]);
        }
        
        vertices.add(vi);
    }
    
    /**
     * Get information for a particular vertex
     * @param index the index of the vertex to get
     * @return the vertex at the given index
     */
    VertexInfo getVertexInfo(int index) {
        return vertices.get(index);
    }
    
    /**
     * Get all vertex info
     * @return all vertex info
     */
    List<VertexInfo> getVertices() {
        return vertices;
    }
    
    /**
     * Get the skeleton names for this controller
     * @return a set of skeleton names
     */
    public Set<String> getSkeletonNames() {
        return skeletonNames;
    }
    
    /**
     * Add a skeleton name
     * @param skeletonName the name of the skeleton to add
     */
    public void addSkeletonName(String skeletonName) {
        skeletonNames.add(skeletonName);
    }
    
    /**
     * Attach the meshes in this controller to the skeleton by locating
     * the joints referenced in each jointInfo
     * @param finder the finder for locating joints by Sid
     */
    public void attach(JointFinder finder) {
        for (JointInfo ji : getJoints()) {
            ColladaJointNode j = finder.findJoint(ji.getSid());
            if (j == null) {
                LOGGER.warning("Unable to find joint named " + ji.getSid());
            } else {
                ji.setJointNode(j);
            }
        }
        
        // now normalize all weights for all vertices
        for (VertexInfo vi : getVertices()) {
            vi.normalizeWeights();
        }
    }

    /**
     * Update pose matrix for each joint before drawing
     */
    @Override
    public void draw(Renderer r) {
        // record the update time
        lastUpdateTime = System.currentTimeMillis();
        
        for (JointInfo joint : getJoints()) {
            Matrix4f pose = joint.getPoseMatrix();
            pose.loadIdentity();
            joint.apply(pose);
            pose.multLocal(getBindMatrix());
        }
        
        super.draw(r);
    }
    
    /**
     * Get the last time this controller was updated
     * @return the last update time
     */
    long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    @Override
    public ColladaControllerNode clone() {
        return new ColladaControllerNode(this);
    }
       
    /**
     * Interface for finding joints by Sid
     */
    public interface JointFinder {
        /**
         * Find a joint by Sid
         * @param sid the sid of the bone to find
         * @return the joint with the given Sid, or null if no joint can
         * be found with the given id
         */
        public ColladaJointNode findJoint(String sid);
    }
    
    // information we store per joint
    static class JointInfo implements Cloneable {
        private final String sid;
        private final Matrix4f invBindMatrix;
        private final Matrix4f poseMatrix = new Matrix4f();
        
        private ColladaJointNode joint;
        
        public JointInfo(String sid, Matrix4f invBindMatrix) {
            this.sid = sid;
            this.invBindMatrix = invBindMatrix;
        }
        
        protected JointInfo(JointInfo copy) {
            this.sid = copy.getSid();
            this.invBindMatrix = copy.getInvBindMatrix().clone();
        }
        
        public String getSid() {
            return sid;
        }
        
        public Matrix4f getInvBindMatrix() {
            return invBindMatrix;
        }
        
        public ColladaJointNode getJointNode() {
            return joint;
        }
        
        public void setJointNode(ColladaJointNode joint) {
            this.joint = joint;
        }
        
        public Matrix4f getPoseMatrix() {
            return poseMatrix;
        }
        
        /**
         * Multiply the given matrix by the inverse bind matrix and the 
         * corresponding joint's matrix. The matrix is modified locally by
         * this operation.
         * @param matrix the matrix to apply to
         */
        public void apply(Matrix4f matrix) {
            matrix.multLocal(getJointNode().getSkeletonTransform());
            matrix.multLocal(getInvBindMatrix());
        }
        
        @Override
        public JointInfo clone() {
            return new JointInfo(this);
        }
    }

    static class BindJointInfo extends JointInfo {
        public BindJointInfo(Matrix4f bindMatrix) {
            super ("Bind", bindMatrix);
        }
    
        @Override
        public void apply(Matrix4f matrix) {
            matrix.multLocal(getInvBindMatrix());
        }
    }
    
    // information we store per vertex
    static class VertexInfo implements Cloneable {
        private final SortedSet<VertexJointRecord> vertices =
                new TreeSet<VertexJointRecord>();
        
        public VertexInfo() {
        }
         
        protected VertexInfo(VertexInfo copy) {
            for (VertexJointRecord rec : copy.getVertexJointRecords()) {
                vertices.add(rec.clone());
            }
        }
         
        public void addJoint(int jointIndex, float weight) {
            vertices.add(new VertexJointRecord(jointIndex, weight));
        }
        
        SortedSet<VertexJointRecord> getVertexJointRecords() {
            return vertices;
        }
         
        /**
         * Normalize all weights for this vertex, once all joints have
         * been added
         */
        public void normalizeWeights() {
            // make sure there are at most MAX_JOINTS weights
            while (vertices.size() > MAX_JOINTS) {
                vertices.remove(vertices.last());
            }
            
            // find the total weight of all vertices
            float total = 0;
            for (VertexJointRecord record : vertices) {
                total += record.weight;
            }
            
            // now update each record with its new weight by removing
            // and re-adding it
            SortedSet<VertexJointRecord> weighted = new TreeSet<VertexJointRecord>();
            for (VertexJointRecord record : vertices) {
                weighted.add(new VertexJointRecord(record.getJointIndex(), 
                                                   record.getWeight() / total));
            }
            vertices.clear();
            vertices.addAll(weighted);
        }

        /**
         * Apply the transforms for this vertex to the given values. All values
         * will be modified in place.
         * 
         * @param controller the controller to read joint information from
         * @param vertex the vertex to apply to
         * @param normal the normal value to apply to (or null for no normal)
         */
        public void apply(ColladaControllerNode controller, 
                          Vector3f vertex, Vector3f normal) 
        {
            Vector3f v = vertex.clone();
            vertex.set(Vector3f.ZERO);

            Vector3f n = null;
            if (normal != null) {
                n = normal.clone();
                normal.set(Vector3f.ZERO);
            }

            Vector3f tmp = new Vector3f();
            
            for (VertexJointRecord record : getVertexJointRecords()) {
                JointInfo joint = controller.getJointInfo(record.getJointIndex());
                float weight = record.getWeight();

                // start with the joint's pose matrix
                Matrix4f mat = joint.getPoseMatrix();
                
                // apply the matrix to the various vectors and collect the result
                mat.mult(v, tmp);
                tmp.multLocal(weight);
                vertex.addLocal(tmp);
                
                if (n != null) {
                    tmp.set(n);
                    mat.rotateVect(tmp);
                    tmp.multLocal(weight);
                    normal.addLocal(tmp);
                }
            }

            // normalize directions
            if (normal != null) {
                normal.normalizeLocal();
            }
        }

        @Override
        public VertexInfo clone() {
            return new VertexInfo(this);
        }
    }
    
    static class VertexJointRecord implements Cloneable, Comparable<VertexJointRecord> {
        private final int jointIndex;
        private final float weight;
        
        public VertexJointRecord(int jointIndex, float weight) {
            this.jointIndex = jointIndex;
            this.weight = weight;
        }
        
        public int getJointIndex() {
            return jointIndex;
        }
        
        public float getWeight() {
            return weight;
        }
        
        public int compareTo(VertexJointRecord o) {
            // compare by weight
            Float w1 = Float.valueOf(getWeight());
            Float w2 = Float.valueOf(o.getWeight());
            
            return w1.compareTo(w2);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VertexJointRecord other = (VertexJointRecord) obj;
            if (this.jointIndex != other.jointIndex) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + this.jointIndex;
            return hash;
        }
        
        @Override
        public VertexJointRecord clone() {
            return new VertexJointRecord(getJointIndex(), getWeight());
        }
    }
}
