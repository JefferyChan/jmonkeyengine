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
import com.jme3.bullet.collision.PhysicsRayTestResult;
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
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
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
    private PhysicsNode player;
    private Spatial spaceCraft;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private BitmapText angular;
    private BitmapText linear;
    int xw = 4;
    int zw = 6;
    int yw = 4;
    private Vector3f HOVER_HEIGHT_LF_START = new Vector3f(xw, 4, zw);
    private Vector3f HOVER_HEIGHT_RF_START = new Vector3f(-xw, 4, zw);
    private Vector3f HOVER_HEIGHT_LR_START = new Vector3f(xw, 4, -zw);
    private Vector3f HOVER_HEIGHT_RR_START = new Vector3f(-xw, 4, -zw);
    private Vector3f HOVER_HEIGHT_LF = new Vector3f(xw, -yw, zw);
    private Vector3f HOVER_HEIGHT_RF = new Vector3f(-xw, -yw, zw);
    private Vector3f HOVER_HEIGHT_LR = new Vector3f(xw, -yw, -zw);
    private Vector3f HOVER_HEIGHT_RR = new Vector3f(-xw, -yw, -zw);
    private Vector3f HOVER_FORCE = new Vector3f(0, 2500, 0);
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

//        if (settings.getRenderer().startsWith("LWJGL")){
////            BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 1024);
////            bsr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
////            viewPort.addProcessor(bsr);
//
//            PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 512, 3);
//            pssmRenderer.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
//            viewPort.addProcessor(pssmRenderer);
//        }
        cam.setFrustumFar(50f);

        setupKeys();
        createTerrain();
//        setupFloor();
        buildPlayer();

//        DirectionalLight dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
//        rootNode.addLight(dl);
//
//        dl = new DirectionalLight();
//        dl.setDirection(new Vector3f(0.5f, -0.1f, 0.3f).normalizeLocal());
//        rootNode.addLight(dl);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(1.0f, 0.94f, 0.8f, 1f).multLocal(1.3f));
        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(new ColorRGBA(0.7f, 0.85f, 1.0f, 1f));
        dl2.setDirection(lightDir2);
        rootNode.addLight(dl2);

        angular = new BitmapText(guiFont);
        linear = new BitmapText(guiFont);

        linear.setLocalTranslation(0, settings.getHeight(), 0);
        angular.setLocalTranslation(0, settings.getHeight() - linear.getLineHeight(), 0);

        guiNode.attachChild(angular);
        guiNode.attachChild(linear);

        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        mat.setTexture("m_ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        for (int j = 0; j < 4; j++) {
            Geometry geom = new Geometry("Box", b);
            geom.setMaterial(mat);
            boxes[j] = geom;
            rootNode.attachChild(geom);
        }
    }

    public void setupFloor() {
        Material mat = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m");
        mat.getTextureParam("m_DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_NormalMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("m_ParallaxMap").getTextureValue().setWrap(WrapMode.Repeat);

        Box floor = new Box(Vector3f.ZERO, 40, 1f, 40);
        floor.scaleTextureCoordinates(new Vector2f(12.0f, 12.0f));
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setShadowMode(ShadowMode.Receive);
        floorGeom.setMaterial(mat);

        BoxCollisionShape colShape = new BoxCollisionShape(new Vector3f(40, 1, 40));
        PhysicsNode tb = new PhysicsNode(floorGeom, colShape, 0);
        tb.setName("Floor");
        tb.setLocalTranslation(new Vector3f(0f, -7, 0f));
        tb.attachDebugShape(assetManager);

        rootNode.attachChild(tb);
        getPhysicsSpace().add(tb);
    }

    private void buildPlayer() {
        spaceCraft = assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");

        CollisionShape colShape = CollisionShapeFactory.createDynamicMeshShape(spaceCraft);

        player = new PhysicsNode(spaceCraft, colShape, 1000);
        player.setName("Player");
        player.setSleepingThresholds(0.75f, 0.2f);
        player.setRestitution(0); // do not bounce
        player.attachDebugShape(assetManager);
        player.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);

        player.setLocalTranslation(new Vector3f(-140, 14, -23));

        spaceCraft.setShadowMode(ShadowMode.CastAndReceive);

        rootNode.attachChild(player);
        getPhysicsSpace().add(player);

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
//        physMissile.applyContinuousForce(true, new Vector3f(0, 9.81f, 0));
        physMissile.applyForce(dir.mult(50000), Vector3f.ZERO);
        physMissile.setRestitution(0); // do not bounce
//        physMissile.attachDebugShape(assetManager);
        physMissile.setLinearDamping(0);
        physMissile.setAngularDamping(1); // prevent rotation
        physMissile.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);

        physMissile.setShadowMode(ShadowMode.CastAndReceive);

        rootNode.attachChild(physMissile);
        getPhysicsSpace().add(physMissile);
        physMissile.setGravity(Vector3f.ZERO);
    }

    Geometry[] boxes = new Geometry[4];
    private void setBox(int i, Vector3f loc) {
        boxes[i].setLocalTranslation(loc);
    }

    public void updatePlayer() {
        List<PhysicsRayTestResult> results = bulletAppState.getPhysicsSpace().rayTest(player.localToWorld(HOVER_HEIGHT_LF_START, null), player.localToWorld(HOVER_HEIGHT_LF, null));
        if (results.size() > 0) {
            player.applyForce(HOVER_FORCE, HOVER_HEIGHT_LF_START);
        }
        results = bulletAppState.getPhysicsSpace().rayTest(player.localToWorld(HOVER_HEIGHT_RF_START, null), player.localToWorld(HOVER_HEIGHT_RF, null));
        if (results.size() > 0) {
            player.applyForce(HOVER_FORCE, HOVER_HEIGHT_RF_START);
        }
        results = bulletAppState.getPhysicsSpace().rayTest(player.localToWorld(HOVER_HEIGHT_LR_START, null), player.localToWorld(HOVER_HEIGHT_LR, null));
        if (results.size() > 0) {
            player.applyForce(HOVER_FORCE, HOVER_HEIGHT_LR_START);
        }
        results = bulletAppState.getPhysicsSpace().rayTest(player.localToWorld(HOVER_HEIGHT_RR_START, null), player.localToWorld(HOVER_HEIGHT_RR, null));
        if (results.size() > 0) {
            player.applyForce(HOVER_FORCE, HOVER_HEIGHT_RR_START);
        }

        //debug view
        setBox(0, player.localToWorld(HOVER_HEIGHT_LF, null));
        setBox(1, player.localToWorld(HOVER_HEIGHT_RF, null));
        setBox(2, player.localToWorld(HOVER_HEIGHT_LR, null));
        setBox(3, player.localToWorld(HOVER_HEIGHT_RR, null));

        Vector3f angVel = player.getAngularVelocity();
        float velocity = angVel.getY();
        Quaternion q = player.getWorldRotation();
        Vector3f dir = q.getRotationColumn(2);
        dir.y = 0;
        dir.normalizeLocal();
        Vector3f vel = player.getLinearVelocity();
        linear.setText("Linear Velocity: " + ((int) vel.length()));
        angular.setText("Angular Velocity: " + ((int) velocity));

//         Quaternion newRot = new Quaternion();
//         float pitch = (velocity / 3f) * FastMath.HALF_PI;
//         float yaw   = (vel.length() / 5f / 10f) * FastMath.HALF_PI * 1f;
//         newRot.fromAngles(yaw, 0/*FastMath.PI*/, pitch);
//         spaceCraft.setLocalRotation(newRot);

        if (steeringValue != 0) {
            if (velocity < 1 && velocity > -1) {
                player.applyTorque(new Vector3f(0, steeringValue, 0));
            }
            steeringValue = 0;
        } else {
            // counter the steering value!
            if (velocity > 0.2f) {
                player.applyTorque(new Vector3f(0, -10000, 0));
            } else if (velocity < -0.2f) {
                player.applyTorque(new Vector3f(0, 10000, 0));
            }
        }
        if (accelerationValue > 0) {

            // counter force that will adjust velocity
            // if we are not going where we want to go.
            // this will prevent "drifting" and thus improve control
            // of the vehicle
            float d = dir.dot(vel.normalize());
            Vector3f counter = dir.project(vel).normalizeLocal().negateLocal().multLocal(1 - d);
            // adjust the "2000" value to increase or decrease counter-drifting
            player.applyForce(counter.mult(2000), Vector3f.ZERO);

            if (vel.length() < 10) {
                player.applyForce(dir.mult(accelerationValue), Vector3f.ZERO);
            }

            accelerationValue = 0;
        } else {
            // counter the acceleration value
            if (vel.length() > FastMath.ZERO_TOLERANCE) {
                vel.normalizeLocal().negateLocal();
                player.applyForce(vel.mult(2000), Vector3f.ZERO);
            }
        }

        //counter too much rotation
        float[] angles = new float[3];
        player.getWorldRotation().toAngles(angles);
        if (angles[0] > FastMath.QUARTER_PI/2f) {
            player.applyTorque(new Vector3f(3000, 0, 0));
        }
        if (angles[0] < -FastMath.QUARTER_PI/2f) {
            player.applyTorque(new Vector3f(-3000, 0, 0));
        }
        if (angles[2] > FastMath.QUARTER_PI/2f) {
            player.applyTorque(new Vector3f(0, 0, 3000));
        }
        if (angles[2] < -FastMath.QUARTER_PI/2f) {
            player.applyTorque(new Vector3f(0, 0, -3000));
        }
    }

    public void onAnalog(String binding, float value, float tpf) {
        if (binding.equals("Lefts")) {
            steeringValue = 50000f * value / tpf;
        } else if (binding.equals("Rights")) {
            steeringValue = -50000f * value / tpf;
        } else if (binding.equals("Ups")) {
            accelerationValue = 8000f * value / tpf;
        } else if (binding.equals("Downs")) {
        }
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                player.setLocalTranslation(new Vector3f(-140, 14, -23));
                player.setLocalRotation(new Quaternion());
                player.setLinearVelocity(Vector3f.ZERO);
                player.setAngularVelocity(Vector3f.ZERO);
            } else {
            }
        } else if (binding.equals("Space") && value) {
            makeMissile();
        }
    }

    public void updateCamera() {
        rootNode.updateGeometricState();

        Vector3f pos = player.getWorldTranslation().clone();
        Quaternion rot = player.getWorldRotation();
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
        updatePlayer();
//        updateCamera();
    }

    private void createTerrain() {

        // First, we load up our textures and the heightmap texture for the terrain

        // TERRAIN TEXTURE material
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

        // ALPHA map (for splat textures)
        matRock.setTexture("m_Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        // HEIGHTMAP image (for the terrain heightmap)
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex1", grass);
        matRock.setFloat("m_Tex1Scale", 64f);

        // DIRT texture
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex2", dirt);
        matRock.setFloat("m_Tex2Scale", 32f);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex3", rock);
        matRock.setFloat("m_Tex3Scale", 128f);

        // WIREFRAME material
        matWire = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        matWire.setColor("m_Color", ColorRGBA.Green);


        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

            heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
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
        rootNode.attachChild(terrain);


        /**
         * Now we use the TerrainPhysicsShapeFactory to generate a heightfield
         * collision shape for us, and then add it to the physics node.
         */
        terrainPhysicsNode = new PhysicsNode(CollisionShapeFactory.createMeshShape(terrain), 0);
//        terrainPhysicsNode.attachChild(terrain);
        rootNode.attachChild(terrainPhysicsNode);
        getPhysicsSpace().addAll(terrainPhysicsNode);

    }
}
