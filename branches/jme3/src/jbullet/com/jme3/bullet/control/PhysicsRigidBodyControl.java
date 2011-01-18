/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author normenhansen
 */
public class PhysicsRigidBodyControl extends PhysicsRigidBody implements PhysicsControl {

    protected Spatial spatial;
    protected boolean enabled = true;
    protected PhysicsSpace space = null;

    public PhysicsRigidBodyControl() {
    }

    /**
     * When using this constructor, the CollisionShape for the RigidBody is generated
     * automatically when the Control is added to a Spatial.
     * @param mass When not 0, a HullCollisionShape is generated, otherwise a MeshCollisionShape is used.
     */
    public PhysicsRigidBodyControl(float mass) {
        this.mass = mass;
    }

    /**
     * Creates a new PhysicsNode with the supplied collision shape
     * @param child
     * @param shape
     */
    public PhysicsRigidBodyControl(CollisionShape shape) {
        super(shape);
    }

    public PhysicsRigidBodyControl(CollisionShape shape, float mass) {
        super(shape, mass);
    }

    public Control cloneForSpatial(Spatial spatial) {
        PhysicsRigidBodyControl control = new PhysicsRigidBodyControl(collisionShape, mass);
        control.setAngularFactor(getAngularFactor());
        control.setAngularSleepingThreshold(getAngularSleepingThreshold());
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setDamping(getLinearDamping(), getAngularDamping());
        control.setFriction(getFriction());
        control.setGravity(getGravity());
        control.setKinematic(isKinematic());
        control.setLinearSleepingThreshold(getLinearSleepingThreshold());
        control.setPhysicsLocation(getPhysicsLocation(null));
        control.setPhysicsRotation(getPhysicsRotation(null));
        control.setRestitution(getRestitution());

        if (mass > 0) {
            control.setAngularVelocity(getAngularVelocity());
            control.setLinearVelocity(getLinearVelocity());
        }

        control.setSpatial(spatial);
        return control;
    }

    public void setSpatial(Spatial spatial) {
        if (getUserObject() == null || getUserObject() == this.spatial) {
            setUserObject(spatial);
        }
        this.spatial = spatial;
        if (spatial == null) {
            if (getUserObject() == spatial) {
                setUserObject(null);
            }
            spatial = null;
            collisionShape = null;
            return;
        }
        if (collisionShape == null) {
            createCollisionShape();
            rebuildRigidBody();
        }
        setPhysicsLocation(spatial.getWorldTranslation());
        setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix());
    }

    protected void createCollisionShape() {
        if (spatial == null) {
            return;
        }
        if (mass > 0) {
            Node parent = spatial.getParent();
            if (parent != null) {
                spatial.removeFromParent();
            }
            collisionShape = CollisionShapeFactory.createDynamicMeshShape(spatial);
            if (parent != null) {
                parent.attachChild(spatial);
            }
        } else {
            Node parent = spatial.getParent();
            if (parent != null) {
                spatial.removeFromParent();
            }
            collisionShape = CollisionShapeFactory.createMeshShape(spatial);
            if (parent != null) {
                parent.attachChild(spatial);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void update(float tpf) {
        if (enabled && spatial != null) {
            getMotionState().applyTransform(spatial);
        }
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (debugShape != null && enabled) {
            debugShape.setLocalTranslation(motionState.getWorldLocation());
            debugShape.setLocalRotation(motionState.getWorldRotation());
            debugShape.updateLogicalState(0);
            debugShape.updateGeometricState();
            rm.renderScene(debugShape, vp);
        }
    }

    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            if (this.space != null) {
                this.space.removeCollisionObject(this);
            }
        } else {
            space.addCollisionObject(this);
        }
        this.space = space;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(enabled, "enabled", true);
        oc.write(spatial, "spatial", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        enabled = ic.readBoolean("enabled", true);
        spatial = (Spatial) ic.readSavable("spatial", null);
    }
}
