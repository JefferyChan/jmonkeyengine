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
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

/**
 *
 * @author normenhansen
 */
public class TestCollisionListener extends SimpleApplication implements ActionListener, PhysicsCollisionListener, PhysicsCollisionGroupListener {

    private BulletAppState bulletAppState;
    private Material mat;
    private static final Sphere bullet;
    private static final SphereCollisionShape bulletCollisionShape;

    static {
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.4f);
    }

    public static void main(String[] args) {
        TestCollisionListener app = new TestCollisionListener();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "shoot");
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        setupKeys();

        mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);

        // Add a physics box to the world
        PhysicsNode physicsBox = new PhysicsNode(new BoxCollisionShape(new Vector3f(1, 1, 1)), 1);
        physicsBox.setName("box");
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f, 4, .5f));
        physicsBox.attachDebugShape(assetManager);
        rootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        // An obstacle mesh, does not move (mass=0)
        PhysicsNode node2 = new PhysicsNode(new MeshCollisionShape(new Sphere(16, 16, 1.2f)), 0);
        node2.setName("mesh");
        node2.setLocalTranslation(new Vector3f(2.5f, -4, 0f));
        node2.attachDebugShape(assetManager);
        //setting collision group to group 2, collide with groups is still 1!
        node2.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        rootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // The floor, does not move (mass=0)
        PhysicsNode node3 = new PhysicsNode(new BoxCollisionShape(new Vector3f(100, 1, 100)), 0);
        node3.setLocalTranslation(new Vector3f(0f, -6, 0f));
        node3.attachDebugShape(assetManager);
        rootNode.attachChild(node3);
        getPhysicsSpace().add(node3);

        // add ourselves as collision listener
        getPhysicsSpace().addCollisionListener(this);
        // add ourselves as group collision listener for group 2
        getPhysicsSpace().addCollisionGroupListener(this, PhysicsCollisionObject.COLLISION_GROUP_02);
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void collision(PhysicsCollisionEvent event) {
        if ("box".equals(event.getNodeA().getName()) || "box".equals(event.getNodeB().getName())) {
            if ("bullet".equals(event.getNodeA().getName()) || "bullet".equals(event.getNodeB().getName())) {
                fpsText.setText("You hit the box!");
            }
        }
        if ("mesh".equals(event.getNodeA().getName()) || "mesh".equals(event.getNodeB().getName())) {
            if ("bullet".equals(event.getNodeA().getName()) || "bullet".equals(event.getNodeB().getName())) {
                fpsText.setText("You hit the mesh!");
            }
        }
    }

    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB) {
        //group 2 only randomly collides
        if (Math.random() < 0.5f) {
            return true;
        } else {
            return false;
        }
    }
    
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("shoot") && !value) {
            Geometry bulletg = new Geometry("bullet", bullet);
            bulletg.setMaterial(mat);
            PhysicsNode bulletNode = new PhysicsNode(bulletg, bulletCollisionShape, 1);
            bulletNode.setName("bullet");
            bulletNode.setLocalTranslation(cam.getLocation());
            bulletNode.setShadowMode(ShadowMode.CastAndReceive);
            bulletNode.setLinearVelocity(cam.getDirection().mult(25));
            rootNode.attachChild(bulletNode);
            getPhysicsSpace().add(bulletNode);
        }
    }

}
