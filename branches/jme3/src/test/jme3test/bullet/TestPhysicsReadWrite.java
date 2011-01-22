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

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.scene.Node;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a basic Test of jbullet-jme functions
 *
 * @author normenhansen
 */
public class TestPhysicsReadWrite extends SimpleApplication{
    private BulletAppState bulletAppState;
    private Node physicsRootNode;
    public static void main(String[] args){
        TestPhysicsReadWrite app = new TestPhysicsReadWrite();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        physicsRootNode=new Node("PhysicsRootNode");
        rootNode.attachChild(physicsRootNode);

        // Add a physics sphere to the world
        PhysicsNode physicsSphere=new PhysicsNode(new SphereCollisionShape(1),1);
        physicsSphere.setName("physicssphere");
        physicsSphere.setLocalTranslation(new Vector3f(3,6,0));
        physicsSphere.attachDebugShape(getAssetManager());
        physicsRootNode.attachChild(physicsSphere);
        getPhysicsSpace().add(physicsSphere);

        // Add a physics sphere to the world using the collision shape from sphere one
        PhysicsNode physicsSphere2=new PhysicsNode(physicsSphere.getCollisionShape(),1);
        physicsSphere2.setLocalTranslation(new Vector3f(4,8,0));
        physicsSphere2.attachDebugShape(getAssetManager());
        physicsRootNode.attachChild(physicsSphere2);
        getPhysicsSpace().add(physicsSphere2);

        // Add a physics box to the world
        PhysicsNode physicsBox=new PhysicsNode(new BoxCollisionShape(new Vector3f(1,1,1)),1);
        physicsBox.setFriction(0.1f);
        physicsBox.setLocalTranslation(new Vector3f(.6f,4,.5f));
        physicsBox.attachDebugShape(getAssetManager());
        physicsRootNode.attachChild(physicsBox);
        getPhysicsSpace().add(physicsBox);

        // Add a physics cylinder to the world
        PhysicsNode physicsCylinder=new PhysicsNode(new CylinderCollisionShape(new Vector3f(1f,1f,1.5f)));
        physicsCylinder.setLocalTranslation(new Vector3f(2,2,0));
        physicsCylinder.attachDebugShape(getAssetManager());
        physicsRootNode.attachChild(physicsCylinder);
        getPhysicsSpace().add(physicsCylinder);

        // an obstacle mesh, does not move (mass=0)
        CompoundCollisionShape compShape=new CompoundCollisionShape();
        compShape.addChildShape(new MeshCollisionShape(new Sphere(16,16,1.2f)), Vector3f.ZERO);
        PhysicsNode node2=new PhysicsNode(compShape,0);
        node2.setLocalTranslation(new Vector3f(2.5f,-4,0f));
        node2.attachDebugShape(getAssetManager());
        physicsRootNode.attachChild(node2);
        getPhysicsSpace().add(node2);

        // the floor mesh, does not move (mass=0)
        PhysicsNode node3=new PhysicsNode(new MeshCollisionShape(new Box(Vector3f.ZERO,100f,0.2f,100f)),0);
        node3.setLocalTranslation(new Vector3f(0f,-6,0f));
        node3.attachDebugShape(getAssetManager());
        physicsRootNode.attachChild(node3);
        getPhysicsSpace().add(node3);

        // Join the physics objects with a Point2Point joint
//        PhysicsPoint2PointJoint joint=new PhysicsPoint2PointJoint(physicsSphere, physicsBox, new Vector3f(-2,0,0), new Vector3f(2,0,0));
        HingeJoint joint=new HingeJoint(physicsSphere.getRigidBody(), physicsBox.getRigidBody(), new Vector3f(-2,0,0), new Vector3f(2,0,0), Vector3f.UNIT_Z,Vector3f.UNIT_Z);
        getPhysicsSpace().add(joint);

        //save and load the physicsRootNode
        try {
            //remove all physics objects from physics space
            getPhysicsSpace().removeAll(physicsRootNode);
            physicsRootNode.removeFromParent();
            //export to byte array
            ByteArrayOutputStream bout=new ByteArrayOutputStream();
            BinaryExporter.getInstance().save(physicsRootNode, bout);
            //import from byte array
            ByteArrayInputStream bin=new ByteArrayInputStream(bout.toByteArray());
            BinaryImporter imp=BinaryImporter.getInstance();
            imp.setAssetManager(assetManager);
            Node newPhysicsRootNode=(Node)imp.load(bin);
            //add all physics objects to physics space
            getPhysicsSpace().addAll(newPhysicsRootNode);
            rootNode.attachChild(newPhysicsRootNode);
        } catch (IOException ex) {
            Logger.getLogger(TestPhysicsReadWrite.class.getName()).log(Level.SEVERE, null, ex);
        }

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

}
