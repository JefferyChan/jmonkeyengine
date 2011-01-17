/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.bullet.objects.PhysicsVehicleWheel;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.Arrow;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author normenhansen
 */
public class PhysicsVehicleControl extends PhysicsVehicle implements PhysicsControl {

    protected Spatial spatial;
    private boolean enabled = true;
    protected PhysicsSpace space = null;

    public PhysicsVehicleControl() {
    }

    /**
     * Creates a new PhysicsNode with the supplied collision shape
     * @param child
     * @param shape
     */
    public PhysicsVehicleControl(CollisionShape shape) {
        super(shape);
    }

    public PhysicsVehicleControl(CollisionShape shape, float mass) {
        super(shape, mass);
    }

    public Control cloneForSpatial(Spatial spatial) {
        PhysicsVehicleControl control = new PhysicsVehicleControl(collisionShape, mass);
        control.setAngularSleepingThreshold(getAngularSleepingThreshold());
        control.setAngularVelocity(getAngularVelocity());
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setDamping(getLinearDamping(), getAngularDamping());
        control.setFriction(getFriction());
        control.setGravity(getGravity());
        control.setKinematic(isKinematic());
        control.setLinearSleepingThreshold(getLinearSleepingThreshold());
        control.setLinearVelocity(getLinearVelocity());
        control.setPhysicsLocation(getPhysicsLocation());
        control.setPhysicsRotation(getPhysicsRotation());
        control.setRestitution(getRestitution());

        control.setFrictionSlip(getFrictionSlip());
        control.setMaxSuspensionTravelCm(getMaxSuspensionTravelCm());
        control.setSuspensionStiffness(getSuspensionStiffness());
        control.setSuspensionCompression(tuning.suspensionCompression);
        control.setSuspensionDamping(tuning.suspensionDamping);
        control.setMaxSuspensionForce(getMaxSuspensionForce());

        for (Iterator<PhysicsVehicleWheel> it = wheels.iterator(); it.hasNext();) {
            PhysicsVehicleWheel wheel = it.next();
            PhysicsVehicleWheel newWheel = control.addWheel(wheel.getLocation(), wheel.getDirection(), wheel.getAxle(), wheel.getRestLength(), wheel.getRadius(), wheel.isFrontWheel());
            newWheel.setFrictionSlip(wheel.getFrictionSlip());
            newWheel.setMaxSuspensionTravelCm(wheel.getMaxSuspensionTravelCm());
            newWheel.setSuspensionStiffness(wheel.getSuspensionStiffness());
            newWheel.setWheelsDampingCompression(wheel.getWheelsDampingCompression());
            newWheel.setWheelsDampingRelaxation(wheel.getWheelsDampingRelaxation());
            newWheel.setMaxSuspensionForce(wheel.getMaxSuspensionForce());

            //TODO: bad way finding children!
            if (spatial instanceof Node) {
                Node node = (Node) spatial;
                Spatial wheelSpat = node.getChild(wheel.getWheelSpatial().getName());
                if (wheelSpat != null) {
                    newWheel.setWheelSpatial(wheelSpat);
                }
            }
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
            this.spatial = null;
            this.collisionShape = null;
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
            if (getMotionState().applyTransform(spatial)) {
                spatial.getWorldTransform();
                applyWheelTransforms();
            }
        } else if (enabled) {
            applyWheelTransforms();
        }
    }

    @Override
    protected Spatial getDebugShape() {
        return super.getDebugShape();
    }

    public void render(RenderManager rm, ViewPort vp) {
        if (debugShape != null && enabled) {
            Node debugNode = (Node) debugShape;
            debugShape.setLocalTranslation(motionState.getWorldLocation());
            debugShape.setLocalRotation(motionState.getWorldRotation());
            int i = 0;
            for (Iterator<PhysicsVehicleWheel> it = wheels.iterator(); it.hasNext();) {
                PhysicsVehicleWheel physicsVehicleWheel = it.next();
                Vector3f location = physicsVehicleWheel.getLocation().clone();
                Vector3f direction = physicsVehicleWheel.getDirection().clone();
                Vector3f axle = physicsVehicleWheel.getAxle().clone();
                float restLength = physicsVehicleWheel.getRestLength();
                float radius = physicsVehicleWheel.getRadius();
                float skid = physicsVehicleWheel.getSkidInfo();

                Geometry locGeom = (Geometry) debugNode.getChild("WheelLocationDebugShape" + i);
                Geometry dirGeom = (Geometry) debugNode.getChild("WheelDirectionDebugShape" + i);
                Geometry axleGeom = (Geometry) debugNode.getChild("WheelAxleDebugShape" + i);
                Geometry wheelGeom = (Geometry) debugNode.getChild("WheelRadiusDebugShape" + i);

                Arrow locArrow = (Arrow) locGeom.getMesh();
                locArrow.setArrowExtent(location);
                Arrow axleArrow = (Arrow) axleGeom.getMesh();
                axleArrow.setArrowExtent(axle.normalizeLocal().multLocal(0.3f));
                Arrow wheelArrow = (Arrow) wheelGeom.getMesh();
                wheelArrow.setArrowExtent(direction.normalizeLocal().multLocal(radius));
                Arrow dirArrow = (Arrow) dirGeom.getMesh();
                dirArrow.setArrowExtent(direction.normalizeLocal().multLocal(restLength));

                if (skid < 0.9f) {
                    wheelGeom.setMaterial(debugMaterialRed);
                } else {
                    wheelGeom.setMaterial(debugMaterialGreen);
                }

                dirGeom.setLocalTranslation(location);
                axleGeom.setLocalTranslation(location.addLocal(direction));
                wheelGeom.setLocalTranslation(location);
                i++;
            }
            debugShape.updateLogicalState(0);
            debugShape.updateGeometricState();
            rm.renderScene(debugShape, vp);
        }
    }

    public void setPhysicsSpace(PhysicsSpace space) {
        createVehicle(space);
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
