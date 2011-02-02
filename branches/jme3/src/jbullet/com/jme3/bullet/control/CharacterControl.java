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
public class CharacterControl extends PhysicsCharacter implements PhysicsControl {

    protected Spatial spatial;
    protected boolean enabled = true;
    protected PhysicsSpace space = null;
    protected Vector3f viewDirection = new Vector3f(Vector3f.UNIT_Z);
    protected boolean useViewDirection = true;

    public CharacterControl() {
    }

    public CharacterControl(CollisionShape shape, float stepHeight) {
        super(shape, stepHeight);
    }

    public Control cloneForSpatial(Spatial spatial) {
        CharacterControl control = new CharacterControl(collisionShape, stepHeight);
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setFallSpeed(getFallSpeed());
        control.setGravity(getGravity());
        control.setJumpSpeed(getJumpSpeed());
        control.setMaxSlope(getMaxSlope());
        control.setPhysicsLocation(getPhysicsLocation());
        control.setUpAxis(getUpAxis());

        control.setSpatial(spatial);
        return control;
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
        setUserObject(spatial);
        if (spatial == null) {
            return;
        }
        setPhysicsLocation(spatial.getWorldTranslation());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setViewDirection(Vector3f vec) {
        viewDirection.set(vec);
    }

    public Vector3f getViewDirection() {
        return viewDirection;
    }

    public boolean isUseViewDirection() {
        return useViewDirection;
    }

    public void setUseViewDirection(boolean viewDirectionEnabled) {
        this.useViewDirection = viewDirectionEnabled;
    }

    public void update(float tpf) {
        if (enabled && spatial != null) {
            Quaternion localRotationQuat = spatial.getLocalRotation();
            Vector3f localLocation = spatial.getLocalTranslation();
            if (spatial.getParent() != null) {
                getPhysicsLocation(localLocation);
                localLocation.subtractLocal(spatial.getParent().getWorldTranslation());
                localLocation.divideLocal(spatial.getParent().getWorldScale());
                tmp_inverseWorldRotation.set(spatial.getParent().getWorldRotation()).inverseLocal().multLocal(localLocation);
                spatial.setLocalTranslation(localLocation);

                localRotationQuat.lookAt(viewDirection, Vector3f.UNIT_Y);
                spatial.setLocalRotation(localRotationQuat);
            } else {
                spatial.setLocalTranslation(getPhysicsLocation());
                localRotationQuat.lookAt(viewDirection, Vector3f.UNIT_Y);
                spatial.setLocalRotation(localRotationQuat);
            }
        }
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (debugShape != null && enabled) {
            debugShape.setLocalTranslation(getPhysicsLocation());
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
        oc.write(useViewDirection, "viewDirectionEnabled", true);
        oc.write(viewDirection, "viewDirection", new Vector3f(Vector3f.UNIT_Z));
        oc.write(spatial, "spatial", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        enabled = ic.readBoolean("enabled", true);
        useViewDirection = ic.readBoolean("viewDirectionEnabled", true);
        viewDirection = (Vector3f) ic.readSavable("viewDirection", new Vector3f(Vector3f.UNIT_Z));
        spatial = (Spatial) ic.readSavable("spatial", null);
    }
}
