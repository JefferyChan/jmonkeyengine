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

import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The root of a COLLADA model
 * @author Jonathan Kaplan
 */
public class ColladaRootNode extends Node {
    private final Map<String, ColladaAnimationGroup> animationGroups =
            new LinkedHashMap<String, ColladaAnimationGroup>();
    
    private ColladaAnimationGroup currentGroup;
    
    public ColladaRootNode(String name) {
        super ("ColladaRoot - " + name);
    }
    
    /**
     * Add an animation group
     * @param group the group to add
     */
    public void addAnimationGroup(ColladaAnimationGroup group) {
        animationGroups.put(group.getName(), group);
        
        // if there is no current group, set the current group
        if (currentGroup == null) {
            currentGroup = group;
            currentGroup.setPlaying(true);
        }
    }
    
    /**
     * Get the current animation group
     * @return the current animation group, or null if there is no current
     * group
     */
    public ColladaAnimationGroup getCurrentGroup() {
        return currentGroup;
    }
    
    /**
     * Set the current animation group to the group with the given name
     * @param name the name of the animation group to make current
     * @return the group that was made current
     */
    public ColladaAnimationGroup setCurrentGroup(String name) {
        currentGroup = animationGroups.get(name);
        return currentGroup;
    }
    
    /**
     * Set the current animation group to the given animation group
     * @return the group to set
     */
    public void setCurrentGroup(ColladaAnimationGroup group) {
        currentGroup = group;
    }
    
    /**
     * Get all animation groups
     * @return all animation groups
     */
    public Collection<ColladaAnimationGroup> getAnimationGroups() {
        return animationGroups.values();
    }
    
    /**
     * Get the names of all animation groups
     * @return the names of all groups.
     */
    public Set<String> getAnimationNames() {
        return animationGroups.keySet();
    }

    @Override
    public void draw(Renderer r) {
        // before drawing chilren, update the current animation group (if any)
        ColladaAnimationGroup current = getCurrentGroup();
        if (current != null) {
            current.update();
        }
        
        super.draw(r);
    }
}
