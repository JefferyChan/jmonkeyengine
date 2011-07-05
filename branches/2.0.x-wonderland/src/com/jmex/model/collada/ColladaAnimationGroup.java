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

import com.jmex.model.collada.ColladaAnimation.PlayDirection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A named group of animations
 * @author Jonathan Kaplan
 */
public class ColladaAnimationGroup {
    private static final Logger LOGGER =
            Logger.getLogger(ColladaAnimationGroup.class.getName());
    
    public enum LoopMode { NONE, REPEAT, REVERSE };
    
    private final String name;
    private final List<ColladaAnimation> animations =
            new LinkedList<ColladaAnimation>();

    // the time of the last frame
    private long prevTime;
    
    // whether or not we are playing
    private boolean playing;
    
    // the playback direction
    private PlayDirection direction = PlayDirection.FORWARD;
    
    // the loop mode 
    private LoopMode loopMode = LoopMode.REPEAT;
    
    // the playback speed
    private float speed = 1.0f;
    
    // time of longest animation
    private float totalTime;
    
    // the set of all target nodes that will be modified by these animations
    private final Set<ColladaNode> nodes = new LinkedHashSet<ColladaNode>();
    
    public ColladaAnimationGroup(String name) {
        this.name = name;
    }
    
    /**
     * Get the name of this group
     * @return the group's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Whether or not this animation is playing
     * @return true if the animation is playing, and false if not
     */
    public boolean isPlaying() {
        return playing;
    }
    
    /**
     * Start or stop playing
     * @param playing true if the animation is playing
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
        this.prevTime = System.currentTimeMillis();
    }
    
    /**
     * Get the current play direction
     * @return the play direction
     */
    public PlayDirection getPlayDirection() {
        return direction;
    }
    
    /**
     * Set the current play direction
     * @param direction the play direction
     */
    public void setPlayDirection(PlayDirection direction) {
        this.direction = direction;
    }
    
    /**
     * Get the current loop mode for playback
     * @return the current loop mode
     */
    public LoopMode getLoopMode() {
        return loopMode;
    }
    
    /**
     * Set the current loop mode
     * @param loopMode the loopMode to set
     */
    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }
    
    /**
     * Set the playback speed
     * @param speed the speed to playback
     */
    public void setPlaybackSpeed(float speed) {
        this.speed = speed;
    }
    
    /**
     * Get the playback speed
     * @return the playback speed
     */
    public float getPlaybackSpeed() {
        return speed;
    }
    
    /**
     * Add an animation to the group
     * @param anim the animation to add
     */
    public void addAnimation(ColladaAnimation anim) {
        animations.add(anim);
    }
    
    /**
     * Get all animations in the group
     * @return the list of animations in the group
     */
    public List<ColladaAnimation> getAnimations() {
        return animations;
    }
    
    /**
     * Update the animation group
     */
    public void update() {
        // update the time
        long curTime = System.currentTimeMillis();
        long millisDiff = curTime - prevTime;
        float timeDiff = millisDiff / 1000f;
        prevTime = curTime;
        
        // if the animation isn't playing, we can quit now
        if (!isPlaying()) {
            return;
        }
        
        // factor in the speed
        millisDiff *= getPlaybackSpeed();
        timeDiff *= getPlaybackSpeed();
        
        // update each child, recording whether all the children are done
        boolean finished = true;
        for (ColladaAnimation child : getAnimations()) {
            finished &= child.update(timeDiff, getPlayDirection());
        }
        
        // if all children are finished, it is time to apply the loop mode
        if (finished) {
            switch (getLoopMode()) {
                case NONE:
                    // no repeate, stop playback
                    setPlaying(false);
                    break;
                case REPEAT:
                    // reset time to the start
                    float time = 0f;
                    if (getPlayDirection() == PlayDirection.BACKWARD) {
                        // start at the end
                        time = totalTime;
                    }
                    
                    // reset all animations
                    for (ColladaAnimation child : getAnimations()) {
                        child.setCurrentTime(time);
                    }
                    break;
                case REVERSE:
                    // change the direction
                    switch (getPlayDirection()) {
                        case FORWARD:
                            setPlayDirection(PlayDirection.BACKWARD);
                            break;
                        case BACKWARD:
                            setPlayDirection(PlayDirection.FORWARD);
                            break;
                    }
                    break;
            }
        }
        
        // now that we have updated everyone, make sure to update the world
        // transform to apply the changes
        for (ColladaNode node : nodes) {
            node.updateGeometricState(millisDiff, true);
        }
    }
    
    /**
     * Attach animation to a set of nodes
     * @param finder the node finder to locate target nodes
     */
    public void attach(NodeFinder finder) {
        // reset total time & node list
        totalTime = 0;
        nodes.clear();
        
        for (ColladaAnimation child : getAnimations()) {
            // recursively perform attachment
            attach(child, finder);
            
            // update total time
            float childTime = child.updateAnimationTime();
            if (childTime > totalTime) {
                totalTime = childTime;
            }
        }
    }
    
    /**
     * Recursive attach method
     * @param animthe animation to attach
     * @param finder the node finder
     */
    public void attach(ColladaAnimation anim, NodeFinder finder) {
        if (anim.getTargetNodeName() != null) {
            // find the node
            ColladaNode node = finder.findNode(anim.getTargetNodeName());
            if (node == null) {
                LOGGER.warning("Unable to find target node " + anim.getTargetNodeName());
            } else {
                anim.setTargetNode(node);
                nodes.add(node);
            }
        }
        
        // attach children
        for (ColladaAnimation child : anim.getChildren()) {
            attach(child, finder);
        }
    }
    
    /**
     * Get the total time of all animations. Valid after the group is attached.
     * @return the total time for all animations
     */
    public float getTotalTime() {
        return totalTime;
    }
    
    /**
     * Interface used to find nodes to attach animations to
     */
    public interface NodeFinder {
        /**
         * Find a node by name
         * @param name the name of the node to find
         * @return node 
         */
        public ColladaNode findNode(String name);
    }
}
