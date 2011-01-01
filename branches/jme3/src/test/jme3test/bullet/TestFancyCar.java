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

import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.bullet.nodes.PhysicsVehicleWheel;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.texture.Texture.WrapMode;

public class TestFancyCar extends SimpleApplication implements ActionListener {
    
    private BulletAppState bulletAppState;
    private PhysicsVehicleNode player;
    private PhysicsVehicleWheel fr, fl, br, bl;
    private Node node_fr, node_fl, node_br, node_bl;
    private float wheelRadius;
    private float steeringValue=0;
    private float accelerationValue=0;

    public static void main(String[] args) {
        TestFancyCar app = new TestFancyCar();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this,"Lefts");
        inputManager.addListener(this,"Rights");
        inputManager.addListener(this,"Ups");
        inputManager.addListener(this,"Downs");
        inputManager.addListener(this,"Space");
        inputManager.addListener(this,"Reset");
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        if (settings.getRenderer().startsWith("LWJGL")){
            BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 512);
            bsr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
            viewPort.addProcessor(bsr);
        }
        cam.setFrustumFar(150f);
        flyCam.setMoveSpeed(10);

        setupKeys();
        setupFloor();
        setupRamp();
        buildPlayer();

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(0.5f, -0.1f, 0.3f).normalizeLocal());
        rootNode.addLight(dl);
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    public void setupRamp(){
        Box ramp = new Box(5,10,1);
        Geometry rampGeom = new Geometry("Floor", ramp);
        rampGeom.setShadowMode(ShadowMode.Receive);

        Material mat = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m");
        rampGeom.setMaterial(mat);

        PhysicsNode tb = new PhysicsNode(rampGeom, new MeshCollisionShape(rampGeom.getMesh()), 0);
        tb.move(0, -5, -10);
        tb.rotate(-FastMath.HALF_PI, 0, 0);
        tb.rotate(FastMath.HALF_PI / 4f, 0, 0);
        rootNode.attachChild(tb);
        getPhysicsSpace().add(tb);
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

        PhysicsNode tb=new PhysicsNode(floorGeom,new MeshCollisionShape(floorGeom.getMesh()),0);
        tb.setLocalTranslation(new Vector3f(0f,-6,0f));
        rootNode.attachChild(tb);
        getPhysicsSpace().add(tb);
    }

    private Geometry findGeom(Spatial spatial, String name){
        if (spatial instanceof Node){
            Node node = (Node) spatial;
            for (int i = 0; i < node.getQuantity(); i++){
                Spatial child = node.getChild(i);
                Geometry result = findGeom(child, name);
                if (result != null)
                    return result;
            }
        }else if (spatial instanceof Geometry){
            if (spatial.getName().startsWith(name))
                return (Geometry) spatial;
        }
        return null;
    }

    private void buildPlayer() {
        float stiffness=120.0f;//200=f1 car
        float compValue=0.2f; //(lower than damp!)
        float dampValue=0.3f;
        final float mass = 400;

        Spatial car = assetManager.loadModel("Models/Ferrari/Car.scene");
        Node carNode = (Node) car;
        final Geometry chasis = findGeom(carNode, "Car");
        BoundingBox box = (BoundingBox) chasis.getModelBound();

        final Vector3f extent = box.getExtent(null);

        // put chasis in center, so that physics box matches up with it
        // also remove from parent to avoid transform issues
        chasis.removeFromParent();
        chasis.setShadowMode(ShadowMode.Cast);


        HullCollisionShape carHull=new HullCollisionShape(chasis.getMesh());

        player = new PhysicsVehicleNode(chasis, carHull, mass);

        //setting default values for wheels
        player.setSuspensionCompression(compValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionDamping(dampValue*2.0f*FastMath.sqrt(stiffness));
        player.setSuspensionStiffness(stiffness);
        player.setMaxSuspensionForce(10000);

        //Create four wheels and add them at their locations
        //note that our fancy car actually goes backwards..
        Vector3f wheelDirection = new Vector3f(0,-1,0);
        Vector3f wheelAxle = new Vector3f(-1,0,0);

        Geometry wheel_fr = findGeom(carNode, "WheelFrontRight");
        wheel_fr.removeFromParent();
        wheel_fr.center();
        node_fr = new Node("wheel_node");
        node_fr.setShadowMode(ShadowMode.Cast);
        node_fr.attachChild(wheel_fr);
        Node primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_fr);
        box = (BoundingBox) wheel_fr.getModelBound();
        wheelRadius = box.getYExtent();
        float back_wheel_h = (wheelRadius * 1.7f)-1f;
        float front_wheel_h = (wheelRadius * 1.9f)-1f;
        player.addWheel(primaryNode, box.getCenter().add(0, -front_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, true);


        Geometry wheel_fl = findGeom(carNode, "WheelFrontLeft");
        wheel_fl.removeFromParent();
        wheel_fl.center();
        node_fl = new Node("wheel_node");
        node_fl.setShadowMode(ShadowMode.Cast);
        node_fl.attachChild(wheel_fl);
        primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_fl);
        box = (BoundingBox) wheel_fl.getModelBound();
        player.addWheel(primaryNode, box.getCenter().add(0, -front_wheel_h, 0),
                        wheelDirection, wheelAxle, 0.2f, wheelRadius, true);

        Geometry wheel_br = findGeom(carNode, "WheelBackRight");
        wheel_br.removeFromParent();
        wheel_br.center();
        node_br = new Node("wheel_node");
        node_br.setShadowMode(ShadowMode.Cast);
        node_br.attachChild(wheel_br);
        primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_br);
        box = (BoundingBox) wheel_br.getModelBound();
        player.addWheel(primaryNode, box.getCenter().add(0, -back_wheel_h, 0),
                        wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        Geometry wheel_bl = findGeom(carNode, "WheelBackLeft");
        wheel_bl.removeFromParent();
        wheel_bl.center();
        node_bl = new Node("wheel_node");
        node_bl.setShadowMode(ShadowMode.Cast);
        node_bl.attachChild(wheel_bl);
        primaryNode = new Node("primary_wheel_node");
        primaryNode.attachChild(node_bl);
        box = (BoundingBox) wheel_bl.getModelBound();
        player.addWheel(primaryNode, box.getCenter().add(0, -back_wheel_h, 0),
                        wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        player.attachDebugShape(assetManager);
        player.getWheel(2).setFrictionSlip(4);
        player.getWheel(3).setFrictionSlip(4);
        rootNode.attachChild(player);
        getPhysicsSpace().add(player);
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            if(value)
                steeringValue+=.5f;
            else
                steeringValue+=-.5f;
            player.steer(steeringValue);
        } else if (binding.equals("Rights")) {
            if(value)
                steeringValue+=-.5f;
            else
                steeringValue+=.5f;
            player.steer(steeringValue);
        }
        //note that our fancy car actually goes backwards..
        else if (binding.equals("Ups")) {
            if(value)
                accelerationValue-=800;
            else
                accelerationValue+=800;
            player.accelerate(accelerationValue);
        } else if (binding.equals("Downs")) {
            if(value)
                player.brake(40f);
            else
                player.brake(0f);
        } else if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                player.setLocalTranslation(0, 0, 0);
                player.setLocalRotation(new Quaternion());
                player.setLinearVelocity(Vector3f.ZERO);
                player.setAngularVelocity(Vector3f.ZERO);
                player.resetSuspension();
            } else {
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        cam.lookAt(player.getWorldTranslation(), Vector3f.UNIT_Y);
    }

}