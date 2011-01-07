/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.sceneexplorer.nodes;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.PhysicsRigidBodyControl;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import java.awt.Image;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service=SceneExplorerNode.class)
public class JmePhysicsRigidBodyControl extends AbstractSceneExplorerNode {

    private static Image smallImage =
            ImageUtilities.loadImage("com/jme3/gde/core/sceneexplorer/nodes/icons/physicsnode.gif");
    private PhysicsRigidBodyControl geom;

    public JmePhysicsRigidBodyControl() {
    }

    public JmePhysicsRigidBodyControl(PhysicsRigidBodyControl spatial) {
        super(Children.LEAF);
        getLookupContents().add(spatial);
        this.geom = spatial;
        setName("PhysicsControl");
    }

    @Override
    public Image getIcon(int type) {
        return smallImage;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return smallImage;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("PhysicsRigidBodyControl");
        set.setName(PhysicsRigidBodyControl.class.getName());
        PhysicsRigidBodyControl obj = geom;//getLookup().lookup(Spatial.class);
        if (obj == null) {
            return sheet;
        }

        set.put(makeProperty(obj, Vector3f.class, "getPhysicsLocation", "setPhysicsLocation", "Physics Location"));
        set.put(makeProperty(obj, Matrix3f.class, "getPhysicsRotation", "setPhysicsRotation", "Physics Rotation"));

        set.put(makeProperty(obj, CollisionShape.class, "getCollisionShape", "setCollisionShape", "Collision Shape"));
        set.put(makeProperty(obj, int.class, "getCollisionGroup", "setCollisionGroup", "Collision Group"));
        set.put(makeProperty(obj, int.class, "getCollideWithGroups", "setCollideWithGroups", "Collide With Groups"));
        
        set.put(makeProperty(obj, float.class, "getFriction", "setFriction", "Friction"));
        set.put(makeProperty(obj, float.class, "getMass", "setMass", "Mass"));
        set.put(makeProperty(obj, boolean.class, "isKinematic", "setKinematic", "Kinematic"));
        set.put(makeProperty(obj, Vector3f.class, "getGravity", "setGravity", "Gravity"));
        set.put(makeProperty(obj, float.class, "getLinearDamping", "setLinearDamping", "Linear Damping"));
        set.put(makeProperty(obj, float.class, "getAngularDamping", "setAngularDamping", "Angular Damping"));
        set.put(makeProperty(obj, float.class, "getRestitution", "setRestitution", "Restitution"));

        set.put(makeProperty(obj, float.class, "getLinearSleepingThreshold", "setLinearSleepingThreshold", "Linear Sleeping Threshold"));
        set.put(makeProperty(obj, float.class, "getAngularSleepingThreshold", "setAngularSleepingThreshold", "Angular Sleeping Threshold"));

        sheet.put(set);
        return sheet;

    }

    public Class getExplorerObjectClass() {
        return PhysicsRigidBodyControl.class;
    }

    public Class getExplorerNodeClass() {
        return JmePhysicsRigidBodyControl.class;
    }

    public org.openide.nodes.Node[] createNodes(Object key, DataObject key2, boolean cookie) {
        return new org.openide.nodes.Node[]{new JmePhysicsRigidBodyControl((PhysicsRigidBodyControl) key).setReadOnly(cookie)};
    }
}
