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
package jme3test.bullet;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;
import jme3tools.converters.ImageToAwt;

public class TestHoveringTank extends SimpleApplication implements AnalogListener,
        ActionListener {

    private BulletAppState bulletAppState;
    private PhysicsHoverControl hoverControl;
    private Spatial spaceCraft;
    TerrainQuad terrain;
    Node terrainPhysicsNode;
    Material matRock;
    Material matWire;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;

    public static void main(String[] args) {
        TestHoveringTank app = new TestHoveringTank();
        app.start();
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        cam.setFrustumFar(50f);

        setupKeys();
        createTerrain();
        buildPlayer();

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(1.0f, 0.94f, 0.8f, 1f).multLocal(1.3f));
        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(new ColorRGBA(0.7f, 0.85f, 1.0f, 1f));
        dl2.setDirection(lightDir2);
        rootNode.addLight(dl2);
    }

    private void buildPlayer() {
        spaceCraft = assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");

        CollisionShape colShape = CollisionShapeFactory.createDynamicMeshShape(spaceCraft);

        hoverControl = new PhysicsHoverControl(colShape, 1000);
        hoverControl.attachDebugShape(assetManager);
        hoverControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);

        spaceCraft.setLocalTranslation(new Vector3f(-140, 14, -23));
        spaceCraft.setShadowMode(ShadowMode.CastAndReceive);
        spaceCraft.addControl(hoverControl);

        rootNode.attachChild(spaceCraft);
        getPhysicsSpace().add(hoverControl);

        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, spaceCraft, inputManager);
    }

    public void makeMissile() {
        Vector3f pos = spaceCraft.getWorldTranslation().clone();
        Quaternion rot = spaceCraft.getWorldRotation();
        Vector3f dir = rot.getRotationColumn(2);

        Spatial missile = assetManager.loadModel("Models/SpaceCraft/Rocket.mesh.xml");
        missile.scale(0.5f);
        missile.rotate(0, FastMath.PI, 0);
        missile.updateGeometricState();

        BoundingBox box = (BoundingBox) missile.getWorldBound();
        final Vector3f extent = box.getExtent(null);

        BoxCollisionShape boxShape = new BoxCollisionShape(extent);

        PhysicsNode physMissile = new PhysicsNode(missile, boxShape, 20);
        physMissile.setName("Missile");
        physMissile.rotate(rot);
        physMissile.setLocalTranslation(pos.addLocal(0, extent.y * 4.5f, 0));

        physMissile.clearForces();
        physMissile.setLinearVelocity(dir.mult(100));
        physMissile.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);

        physMissile.setShadowMode(ShadowMode.CastAndReceive);

        rootNode.attachChild(physMissile);
        getPhysicsSpace().add(physMissile);
    }

    public void onAnalog(String binding, float value, float tpf) {
        if (binding.equals("Lefts")) {
            hoverControl.steer(50000f * value / tpf);
        } else if (binding.equals("Rights")) {
            hoverControl.steer(-50000f * value / tpf);
        } else if (binding.equals("Ups")) {
            hoverControl.accelerate(8000f * value / tpf);
        } else if (binding.equals("Downs")) {
            hoverControl.accelerate(-8000f * value / tpf);
        }
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                hoverControl.setPhysicsLocation(new Vector3f(-140, 14, -23));
                hoverControl.setPhysicsRotation(new Matrix3f());
                hoverControl.setLinearVelocity(Vector3f.ZERO);
                hoverControl.setAngularVelocity(Vector3f.ZERO);
            } else {
            }
        } else if (binding.equals("Space") && value) {
            makeMissile();
        }
    }

    public void updateCamera() {
        rootNode.updateGeometricState();

        Vector3f pos = spaceCraft.getWorldTranslation().clone();
        Quaternion rot = spaceCraft.getWorldRotation();
        Vector3f dir = rot.getRotationColumn(2);

        // make it XZ only
        Vector3f camPos = new Vector3f(dir);
        camPos.setY(0);
        camPos.normalizeLocal();

        // negate and multiply by distance from object
        camPos.negateLocal();
        camPos.multLocal(15);

        // add Y distance
        camPos.setY(2);
        camPos.addLocal(pos);
        cam.setLocation(camPos);

        Vector3f lookAt = new Vector3f(dir);
        lookAt.multLocal(7); // look at dist
        lookAt.addLocal(pos);
        cam.lookAt(lookAt, Vector3f.UNIT_Y);
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    private void createTerrain() {
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex1", grass);
        matRock.setFloat("Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex2", dirt);
        matRock.setFloat("Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", 128f);
        matWire = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        matWire.setColor("Color", ColorRGBA.Green);
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
            heightmap.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(2, 2, 2));
        terrain.setModelBound(new BoundingBox());
        terrain.updateModelBound();
        terrain.setLocked(false); // unlock it so we can edit the height

        terrainPhysicsNode = new PhysicsNode(CollisionShapeFactory.createMeshShape(terrain), 0);
        terrainPhysicsNode.attachChild(terrain);
        rootNode.attachChild(terrainPhysicsNode);
        getPhysicsSpace().addAll(terrainPhysicsNode);

    }
}
