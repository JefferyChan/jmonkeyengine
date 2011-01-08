/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.bullet;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author normenhansen
 */
public class PhysicsHoverControl extends PhysicsRigidBody implements PhysicsControl, PhysicsTickListener {

    protected Spatial spatial;
    protected boolean enabled = true;
    protected PhysicsSpace space = null;

    protected float steeringValue = 0;
    protected float accelerationValue = 0;

    protected int xw = 4;
    protected int zw = 6;
    protected int yw = 4;
    protected Vector3f HOVER_HEIGHT_LF_START = new Vector3f(xw, 2, zw);
    protected Vector3f HOVER_HEIGHT_RF_START = new Vector3f(-xw, 2, zw);
    protected Vector3f HOVER_HEIGHT_LR_START = new Vector3f(xw, 2, -zw);
    protected Vector3f HOVER_HEIGHT_RR_START = new Vector3f(-xw, 2, -zw);
    protected Vector3f HOVER_HEIGHT_LF = new Vector3f(xw, -yw, zw);
    protected Vector3f HOVER_HEIGHT_RF = new Vector3f(-xw, -yw, zw);
    protected Vector3f HOVER_HEIGHT_LR = new Vector3f(xw, -yw, -zw);
    protected Vector3f HOVER_HEIGHT_RR = new Vector3f(-xw, -yw, -zw);
    protected Vector3f HOVER_FORCE = new Vector3f(0, 6000f, 0);

    public PhysicsHoverControl() {
    }

    /**
     * Creates a new PhysicsNode with the supplied collision shape
     * @param child
     * @param shape
     */
    public PhysicsHoverControl(CollisionShape shape) {
        super(shape);
    }

    public PhysicsHoverControl(CollisionShape shape, float mass) {
        super(shape, mass);
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

    public void physicsTick(PhysicsSpace space, float f) {
        List<PhysicsRayTestResult> results = space.rayTest(spatial.localToWorld(HOVER_HEIGHT_LF_START, null), spatial.localToWorld(HOVER_HEIGHT_LF, null));
        if (results.size() > 0) {
            applyForce(HOVER_FORCE, HOVER_HEIGHT_LF.mult(results.get(0).getHitFraction()));
        }
        results = space.rayTest(spatial.localToWorld(HOVER_HEIGHT_RF_START, null), spatial.localToWorld(HOVER_HEIGHT_RF, null));
        if (results.size() > 0) {
            applyForce(HOVER_FORCE, HOVER_HEIGHT_RF.mult(results.get(0).getHitFraction()));
        }
        results = space.rayTest(spatial.localToWorld(HOVER_HEIGHT_LR_START, null), spatial.localToWorld(HOVER_HEIGHT_LR, null));
        if (results.size() > 0) {
            applyForce(HOVER_FORCE, HOVER_HEIGHT_LR.mult(results.get(0).getHitFraction()));
        }
        results = space.rayTest(spatial.localToWorld(HOVER_HEIGHT_RR_START, null), spatial.localToWorld(HOVER_HEIGHT_RR, null));
        if (results.size() > 0) {
            applyForce(HOVER_FORCE, HOVER_HEIGHT_RR.mult(results.get(0).getHitFraction()));
        }

        Vector3f angVel = getAngularVelocity();
        float velocity = angVel.getY();
        Quaternion q = new Quaternion().fromRotationMatrix(motionState.getWorldRotation());
        Vector3f dir = q.getRotationColumn(2);
        dir.y = 0;
        dir.normalizeLocal();
        Vector3f vel = getLinearVelocity();

        if (steeringValue != 0) {
            if (velocity < 1 && velocity > -1) {
                applyTorque(new Vector3f(0, steeringValue, 0));
            }
            steeringValue = 0;
        } else {
            // counter the steering value!
            if (velocity > 0.2f) {
                applyTorque(new Vector3f(0, -10000, 0));
            } else if (velocity < -0.2f) {
                applyTorque(new Vector3f(0, 10000, 0));
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
            applyForce(counter.mult(2000), Vector3f.ZERO);

            if (vel.length() < 10) {
                applyForce(dir.mult(accelerationValue), Vector3f.ZERO);
            }

            accelerationValue = 0;
        } else {
            // counter the acceleration value
            if (vel.length() > FastMath.ZERO_TOLERANCE) {
                vel.normalizeLocal().negateLocal();
                applyForce(vel.mult(2000), Vector3f.ZERO);
            }
        }
        //counter too much rotation
        float[] angles = new float[3];
        q.toAngles(angles);
        if (angles[0] > FastMath.QUARTER_PI/2f) {
            applyTorque(new Vector3f(-30000, 0, 0));
        }
        if (angles[0] < -FastMath.QUARTER_PI/2f) {
            applyTorque(new Vector3f(30000, 0, 0));
        }
        if (angles[2] > FastMath.QUARTER_PI/2f) {
            applyTorque(new Vector3f(0, 0, -30000));
        }
        if (angles[2] < -FastMath.QUARTER_PI/2f) {
            applyTorque(new Vector3f(0, 0, 30000));
        }
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
                this.space.removeTickListener(this);
            }
        } else {
            space.addCollisionObject(this);
            space.addTickListener(this);
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

    /**
     * @param steeringValue the steeringValue to set
     */
    public void steer(float steeringValue) {
        this.steeringValue = steeringValue;
    }

    /**
     * @param accelerationValue the accelerationValue to set
     */
    public void accelerate(float accelerationValue) {
        this.accelerationValue = accelerationValue;
    }

}
