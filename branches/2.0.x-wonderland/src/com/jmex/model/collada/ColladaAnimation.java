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

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents a Collada animation
 * @author Jonathan Kaplan
 */
public class ColladaAnimation {
    /**
     * Modes for playback
     */
    public enum PlayDirection { FORWARD, BACKWARD };
    
    // the node this animation targets
    private final String targetNodeName;
    
    // the sid of the transform to target
    private final String targetSid;
    
    // the optional property name within the transform to target
    private final String targetProperty;
   
    // the keyframes for this animation
    private final SortedMap<Float, float[]> keyFrames = 
            new TreeMap<Float, float[]>();
    
    // child animations
    private final List<ColladaAnimation> children =
            new LinkedList<ColladaAnimation>();
    
    // total time for all animations
    private float totalTime = 0f;
    
    // the target node (set when the animation is hooked up)
    private ColladaNode targetNode = null;
    
    // the target transform (set when the animation is hooked up)
    private ColladaTransform targetTransform = null;
    
    // the current time of this animation
    private float currentTime = 0f;
    
    public ColladaAnimation(String targetNode, String targetSid,
                            String targetProperty)
    {
        this.targetNodeName = targetNode;
        this.targetSid = targetSid;
        this.targetProperty = targetProperty;
    }
    
    /**
     * Get the name of the target node
     * @return the name of the target node
     */
    public String getTargetNodeName() {
        return targetNodeName;
    }
    
    /**
     * Get the SID of the target transform
     * @return the SID of the target transform
     */
    public String getTargetTransformSID() {
        return targetSid;
    }
    
    /**
     * Get the target property, if any
     * @return the name of the target property,  or null if none is set
     */
    public String getTargetProperty() {
        return targetProperty;
    }
    
    /**
     * Get the target node
     * @return the target node
     */
    public ColladaNode getTargetNode() {
        return targetNode;
    }
    
    /**
     * Get the target transform
     * @return the target transform
     */
    public ColladaTransform getTargetTransform() {
        return targetTransform;
    }

    /**
     * Returns key frames for this animation.
     * 
     * @return key frames map
     */
    public SortedMap<Float, float[]> getKeyFrames() {
        return keyFrames;
    }
    
    /**
     * Hook up this animation by connecting the target node and transform
     * @param node the node to connect to
     */
    public void setTargetNode(ColladaNode target) {
        this.targetNode = target;
        this.targetTransform = target.getTransform(targetSid);
    }
    
    /**
     * Get the time this animation takes. For an animaton with no children, 
     * this is the same as the value of the last keyframe. For an animation
     * with children, this is the longer of the last keyframe time or the
     * longest child time.
     * 
     * @return the time of the last keyframe in this animation or any of
     * its children
     */
    public float getAnimationTime() {
        return totalTime;
    }
    
    /**
     * Update the total time. Call once all children are added.
     * @return the updated total time
     */
    public float updateAnimationTime() {
        totalTime = keyFrames.lastKey();
        
        for (ColladaAnimation child : children) {
            float childTime = child.updateAnimationTime();
            if (childTime > totalTime) {
                totalTime = childTime;
            }
        }
        
        return totalTime;
    }
    
    /**
     * Add a child animation
     * @param anim the child animation to add
     */
    public void addChild(ColladaAnimation anim) {
        children.add(anim);
    }
    
    /**
     * Get all child animations
     * @return the list of child animations
     */
    public List<ColladaAnimation> getChildren() {
        return children;
    }
    
    /**
     * Add a new keyframe to the map
     * @param time the time for this keyframe
     * @param values the values of the various outputs at the given time
     */
    public void addKeyframe(float time, float[] values) {
        keyFrames.put(time, values);
    }
    
    /**
     * Get the closest values to the given time. For any given time,
     * this method will return either 1 or 2 values:
     * 
     * if the given time is before the first keyframe, it will return the
     * first keyframe
     * 
     * if the given time is after the last keyframe, it will return the
     * last keyframe
     * 
     * if the given time is exactly the value of a keyframe, it will return
     * only that keyframe
     * 
     * if the given time is in between two keyframes, it will return both,
     * first the lower time value and then the higher time value
     *
     * @param time the time to search for
     * @return an array containing either one or two entries, as described
     * above
     */
    protected KeyFrame[] getKeyFrame(float time) {
        // find the closes keys using headMap and tailMap
        SortedMap<Float, float[]> head = keyFrames.headMap(time);
        SortedMap<Float, float[]> tail = keyFrames.tailMap(time);
        
        // upper and lower values
        Float lower = null;
        Float upper = null;
               
        if (head.isEmpty()) {
            // before the first key
            upper = tail.firstKey();
        } else if (tail.isEmpty()) {
            // after last key
            lower = head.lastKey();
        } else if (time == tail.firstKey()) {
            // exact match
            upper = tail.firstKey();
        } else {
            // in between
            lower = head.lastKey();
            upper = tail.firstKey();
        }
        
        KeyFrame[] out;
        if (lower == null) {
            out = new KeyFrame[1];
            out[0] = new KeyFrame(upper, keyFrames.get(upper));
        } else if (upper == null) {
            out = new KeyFrame[1];
            out[0] = new KeyFrame(lower, keyFrames.get(lower));
        } else {
           out = new KeyFrame[2];
           out[0] = new KeyFrame(lower, keyFrames.get(lower));
           out[1] = new KeyFrame(upper, keyFrames.get(upper));
        }
        return out;
    } 
    
    /** 
     * Get the current animation time
     * @return the current animation time
     */
    public float getCurrentTime() {
        return currentTime;
    }
    
    /**
     * Set the current animation time
     * @param currentTime the current time to set
     */
    public void setCurrentTime(float currentTime) {
        this.currentTime = currentTime;
        
        // set time for children
        for (ColladaAnimation child : children) {
            child.setCurrentTime(currentTime);
        }
    }
    
    /**
     * Change the current time by the given amount.
     * @param deltaTime the amount of time to change by
     * @param dir the play mode
     * @return the updated current time
     */
    protected float updateCurrentTime(float deltaTime, PlayDirection dir) {
        switch (dir) {
            case FORWARD:
                currentTime += deltaTime;
                break;
            case BACKWARD:
                currentTime -= deltaTime;
                break;
        }
        
        return currentTime;
    }
    
    /**
     * Update the animation by incrementing time by the given amount
     * in seconds.
     * @param deltaTime the time increment (in seconds)
     * @param dir the playback mode
     * @return true if the animation is complete (meaning the current time
     * is either less than 0 or more than the animation time)
     */
    public boolean update(float deltaTime, PlayDirection dir) {
        // update the current time
        float frameTime = updateCurrentTime(deltaTime, dir);
        
        // is there anything to animate?
        if (getTargetTransform() != null) {
            // find the appropriate frames
            KeyFrame[] frames = getKeyFrame(frameTime);
        
            if (frames.length == 2) {
                // interpolate between two frames
                interpolate(frames[0], frames[1], frameTime, dir);
            } else {
                // apply the frame value directly
                getTargetTransform().update(getTargetProperty(), frames[0].getValues());
            }
        }
        
        // update all children
        for (ColladaAnimation child : getChildren()) {
            child.update(deltaTime, dir);
        }
        
        // determine if we are finished with the animation
        return (dir == PlayDirection.BACKWARD && frameTime < 0f) || 
               (dir == PlayDirection.FORWARD && frameTime > getAnimationTime());
    }
    
    /**
     * Interpolate between two keyframes
     * @param f1 the first frame for interpolation
     * @param f2 the second frame for interpolation
     * @param time the current time
     * @param dir the playback mode
     */
    protected void interpolate(KeyFrame f1, KeyFrame f2, float time,
                               PlayDirection dir) 
    {
        // find the time difference between frame one and two (two will always
        // be later)
        float frameDiff = f2.getTime() - f1.getTime();
        
        // find the distance into that difference that time represents
        float timeDiff = time - f1.getTime();
        
        // turn timeDiff into a percentage
        float timePercent = timeDiff / frameDiff;
        
        // get the start and end times depending on the playback mode
        float[] start;
        float[] end;
        
        switch (dir) {
            case BACKWARD:
                start = f2.getValues();
                end = f1.getValues();
                
                // make sure to reverse the percentage too
                timePercent = 1.0f - timePercent;
                break;
            default:
                // forward playback by default
                start = f1.getValues();
                end = f2.getValues();
                break;
                
        }
        
        // create two new transforms
        ColladaTransform startTransform = getTargetTransform().clone();
        startTransform.update(getTargetProperty(), start);
        
        ColladaTransform endTransform = getTargetTransform().clone();
        endTransform.update(getTargetProperty(), end);
        
        // update the target to the interpolation between the two new
        // transforms
        getTargetTransform().interpolate(timePercent, startTransform, endTransform);
    }
    
    /**
     * Reset this animation and its target transform back to the default state
     */
    public void reset() {
        // reset the target transform
        getTargetTransform().reset();
        
        // reset all children
        for (ColladaAnimation child : children) {
            child.reset();
        }
    }
    
    /**
     * Holder for a single keyframe
     */
    protected static class KeyFrame {
        private final float time;
        private final float[] values;
        
        public KeyFrame(float time, float[] values) {
            this.time = time;
            this.values = values;
        }
        
        public float getTime() {
            return time;
        }
        
        public float[] getValues() {
            return values;
        }
    }
}
