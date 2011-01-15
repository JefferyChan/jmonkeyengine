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

package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOConfig;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Box;

public class TestSSAO extends SimpleApplication {

    float angle;
    Spatial lightMdl;
    Spatial teapot;
    Geometry frustumMdl;
    WireFrustum frustum;

    private FilterPostProcessor fpp;
    private Vector3f[] points;

    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) points[i] = new Vector3f();
    }

    public static void main(String[] args){
        TestSSAO app = new TestSSAO();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
        cam.setLocation(new Vector3f(-2.336393f, 11.91392f, -7.139601f));
        cam.setRotation(new Quaternion(0.23602544f, 0.11321983f, -0.027698677f, 0.96473104f));
        //cam.setFrustumFar(1000);

        Material mat = assetManager.loadMaterial("Common/Materials/WhiteColor.j3m");
        Material matSoil = new Material(assetManager,"Common/MatDefs/Misc/SolidColor.j3md");
        matSoil.setColor("Color", ColorRGBA.LightGray);


        teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        teapot.setLocalTranslation(0,0,10);

        teapot.setMaterial(mat);
        teapot.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(teapot);

         for (int i = 0; i < 30; i++) {
            Spatial t = teapot.clone();
            rootNode.attachChild(t);
            teapot.setLocalTranslation((float) Math.random() * 3, (float) Math.random() * 3, (i + 2));
        }

        Geometry soil=new Geometry("soil", new Box(new Vector3f(0, -13, 550), 800, 10, 700));
        soil.setMaterial(matSoil);
        soil.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(soil);

        for (int i = 0; i < 30; i++) {
            Spatial t = teapot.clone();
            t.setLocalScale(10.0f);
            rootNode.attachChild(t);
            teapot.setLocalTranslation((float) Math.random() * 300, (float) Math.random() * 30, 30 * (i + 2));
        }

        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        fpp.setNumSamples(8);
        SSAOFilter ssaoFilter= new SSAOFilter(0.92f,2.2f,0.29000017f,0.21200025f);
        fpp.addFilter(ssaoFilter);
        SSAOUI ui=new SSAOUI(inputManager, ssaoFilter);

        viewPort.addProcessor(fpp);

    }

    @Override
    public void simpleUpdate(float tpf){

    }

}
