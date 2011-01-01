/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author normenhansen
 */
public class PhysicsCharacterControl extends PhysicsCharacter implements PhysicsControl {

    protected Spatial spatial;
    protected boolean enabled = true;
    protected PhysicsSpace space = null;

    public PhysicsCharacterControl() {
    }

    public PhysicsCharacterControl(CollisionShape shape, float stepHeight) {
        super(shape, stepHeight);
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
        setUserObject(spatial);
        if (spatial == null) {
            return;
        }
        setPhysicsLocation(spatial.getWorldTranslation());
        setPhysicsRotation(spatial.getWorldRotation().toRotationMatrix());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void update(float tpf) {
        if (enabled && spatial != null) {
            Quaternion localRotationQuat = spatial.getLocalRotation();
            Vector3f localLocation = spatial.getLocalTranslation();
            if (spatial.getParent() != null) {
                localLocation.set(getPhysicsLocation()).subtractLocal(spatial.getParent().getWorldTranslation());
                localLocation.divideLocal(spatial.getParent().getWorldScale());
                tmp_inverseWorldRotation.set(spatial.getParent().getWorldRotation()).inverseLocal().multLocal(localLocation);

                localRotationQuat.set(getPhysicsRotation());
                tmp_inverseWorldRotation.set(spatial.getParent().getWorldRotation()).inverseLocal().mult(localRotationQuat, localRotationQuat);

                spatial.setLocalTranslation(localLocation);
                spatial.setLocalRotation(localRotationQuat);
            } else {
                spatial.setLocalTranslation(getPhysicsLocation());
                spatial.setLocalRotation(getPhysicsRotation());
            }
        }
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
                this.space.remove(this);
            }
        } else {
            space.add(this);
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
