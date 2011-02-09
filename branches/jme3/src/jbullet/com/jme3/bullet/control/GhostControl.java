/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Matrix3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 * A GhostControl moves with the spatial it is attached to and can be used to check
 * overlaps with other physics objects (e.g. aggro radius).
 * @author normenhansen
 */
public class GhostControl extends PhysicsGhostObject implements PhysicsControl {

    protected Spatial spatial;
    protected boolean enabled = true;
    protected boolean added = false;
    protected PhysicsSpace space = null;
    private Matrix3f temp_matrix = new Matrix3f();

    public GhostControl() {
    }

    public GhostControl(CollisionShape shape) {
        super(shape);
    }

    public Control cloneForSpatial(Spatial spatial) {
        GhostControl control = new GhostControl(collisionShape);
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setPhysicsLocation(getPhysicsLocation());
        control.setPhysicsRotation(getPhysicsRotation());

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
            return;
        }
        setPhysicsLocation(spatial.getWorldTranslation());
        setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (space != null) {
            if (enabled && !added) {
                if(spatial!=null){
                    setPhysicsLocation(spatial.getWorldTranslation());
                    setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix(temp_matrix));
                }
                space.addCollisionObject(this);
                added = true;
            } else if (!enabled && added) {
                space.removeCollisionObject(this);
                added = false;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void update(float tpf) {
        if (!enabled) {
            return;
        }
        setPhysicsLocation(spatial.getWorldTranslation());
        setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix(temp_matrix));
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (debugShape != null && enabled) {
            debugShape.setLocalTranslation(getPhysicsLocation());
            debugShape.setLocalRotation(getPhysicsRotation());
            debugShape.updateLogicalState(0);
            debugShape.updateGeometricState();
            rm.renderScene(debugShape, vp);
        }
    }

    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            if (this.space != null) {
                this.space.removeCollisionObject(this);
                added = false;
            }
        } else {
            if(this.space==space) return;
            space.addCollisionObject(this);
            added = true;
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
