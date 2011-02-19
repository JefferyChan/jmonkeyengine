/*
 * Copyright (c) 2009-2010 jMonkeyEngine
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

package com.jme3.animation;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.io.IOException;


/**
 * Contains a list of transforms and times for each keyframe.
 */
public final class BoneTrack implements Savable {

    /**
     * Bone index in the skeleton which this track effects.
     */
    private int targetBoneIndex;

    /**
     * Transforms and times for track.
     */
    private Vector3f[] translations;
    private Quaternion[] rotations;
    private float[] times;

    // temp vectors for interpolation
    private transient final Vector3f tempV = new Vector3f();
    private transient final Quaternion tempQ = new Quaternion();

    /**
     * Serialization-only. Do not use.
     */
    public BoneTrack(){
    }

    public BoneTrack(int targetBoneIndex, float[] times, Vector3f[] translations, Quaternion[] rotations){
        this.targetBoneIndex = targetBoneIndex;
        setKeyframes(times, translations, rotations);
    }

    public BoneTrack(int targetBoneIndex){
        this.targetBoneIndex = targetBoneIndex;
    }

    public int getTargetBoneIndex() {
        return targetBoneIndex;
    }

    public Quaternion[] getRotations() {
        return rotations;
    }

    public float[] getTimes() {
        return times;
    }

    public Vector3f[] getTranslations() {
        return translations;
    }

    public void setKeyframes(float[] times, Vector3f[] translations, Quaternion[] rotations){
        if (times.length == 0)
            throw new RuntimeException("BoneTrack with no keyframes!");

        assert (times.length == translations.length) && (times.length == rotations.length);

        this.times = times;
        this.translations = translations;
        this.rotations = rotations;
    }  

    /**
     * Modify the bone which this track modifies in the skeleton to contain
     * the correct animation transforms for a given time.
     * The transforms can be interpolated in some method from the keyframes.
     */
    public void setTime(float time, Skeleton skeleton, float weight) {
        Bone target = skeleton.getBone(targetBoneIndex);

        int lastFrame = times.length - 1;
        if (time < 0 || lastFrame == 0){
            tempQ.set(rotations[0]);
            tempV.set(translations[0]);
        }else if (time >= times[lastFrame]){
            tempQ.set(rotations[lastFrame]);
            tempV.set(translations[lastFrame]);
        }else{
            int startFrame = 0;
            int endFrame   = 1;
            // use len-1 so we never overflow the array
            for (int i = 0; i < times.length-1; i++){
                if (times[i] < time){
                    startFrame = i;
                    endFrame   = i + 1;
                }
            }
            float blend =  (time - times[startFrame])
                         / (times[endFrame] - times[startFrame]);

            tempQ.slerp(rotations[startFrame], rotations[endFrame], blend);
            tempV.interpolate(translations[startFrame], translations[endFrame], blend);
        }

        if (weight != 1f){
//            tempQ.slerp(Quaternion.IDENTITY, 1f - weight);
//            tempV.multLocal(weight);
            target.blendAnimTransforms(tempV, tempQ, weight);
//            target.setAnimTransforms(tempV, tempQ);
        }else{
            target.setAnimTransforms(tempV, tempQ);
        }

        
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(targetBoneIndex, "boneIndex", 0);
        oc.write(translations, "translations", null);
        oc.write(rotations, "rotations", null);
        oc.write(times, "times", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        targetBoneIndex = ic.readInt("boneIndex", 0);

        Savable[] sav = ic.readSavableArray("translations", null);
        if (sav != null){
            translations = new Vector3f[sav.length];
            System.arraycopy(sav, 0, translations, 0, sav.length);
        }

        sav = ic.readSavableArray("rotations", null);
        if (sav != null){
            rotations = new Quaternion[sav.length];
            System.arraycopy(sav, 0, rotations, 0, sav.length);
        }
        times = ic.readFloatArray("times", null);
    }

}
