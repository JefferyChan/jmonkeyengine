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

package jme3tools.optimize;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.texture.FrameBuffer;
import java.util.HashSet;
import java.util.Set;


public class TestOctree extends SimpleApplication implements SceneProcessor {

    private Octree tree;
    private Set<Geometry> renderSet = new HashSet<Geometry>(300);
    private Material mat, mat2;
    private WireBox box = new WireBox(1,1,1);

    public static void main(String[] args){
        TestOctree app = new TestOctree();
        app.start();
    }

    public void simpleInitApp() {
        this.flyCam.setMoveSpeed(2000);
        this.cam.setFrustumFar(10000);
        MeshLoader.AUTO_INTERLEAVE = false;

//        mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
//        mat.setColor("m_Color", ColorRGBA.White);

//        mat2 = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");

        assetManager.registerLocator("quake3level.zip", "com.jme3.asset.plugins.ZipLocator");
        MaterialList matList = (MaterialList) assetManager.loadAsset("Scene.material");
        OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
        Spatial scene = assetManager.loadModel(key);


        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        scene.addLight(dl);

        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1, -1, 1).normalize());
        scene.addLight(dl);

//        scene.setMaterial(mat2);
//        scene.setLocalScale(0.2f);
//        Spatial scene = manager.loadModel("models/teapot.obj");
//        Material mat = new Material(manager, "Common/MatDefs/Misc/ShowNormals.j3md");
//        scene.setMaterial(mat);

        // generate octree
        tree = new Octree(scene, 20000);
//        tree = new Octree(scene, 50);
        tree.construct();

        viewPort.addProcessor(this);
    }

    public void initialize(RenderManager rm, ViewPort vp) {
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return true;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
        renderSet.clear();
        tree.generateRenderSet(renderSet, cam);
//        System.out.println("Geoms: "+renderSet.size());
        int tris = 0;

        for (Geometry geom : renderSet){
            tris += geom.getTriangleCount();
//            geom.setMaterial(mat2);
            rq.addToQueue(geom, geom.getQueueBucket());
        }

//        Matrix4f transform = new Matrix4f();
//        transform.setScale(0.2f, 0.2f, 0.2f);
//        System.out.println("Tris: "+tris);
        
//        tree.renderBounds(rq, transform, box, mat);

//        renderManager.flushQueue(viewPort);
    }

    public void postFrame(FrameBuffer out) {
    }

    public void cleanup() {
    }
}
