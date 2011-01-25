/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.bullet;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.BulletGhostObject;
import com.jme3.bullet.objects.BulletRigidBody;
import com.jme3.math.Vector3f;
import java.util.Iterator;

/**
 *
 * @author normenhansen
 */
public class BombControl extends RigidBodyControl implements PhysicsCollisionListener, PhysicsTickListener {

    private float explosionRadius = 10;
    private BulletGhostObject ghostObject;
    private Vector3f vector = new Vector3f();
    private Vector3f vector2 = new Vector3f();
    private float forceFactor = 1;

    public BombControl() {
    }

    public BombControl(float mass) {
        super(mass);
        createGhostObject();
    }

    public BombControl(CollisionShape shape, float mass) {
        super(shape, mass);
        createGhostObject();
    }

    public BombControl(CollisionShape shape) {
        super(shape);
        createGhostObject();
    }

    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            if (this.space != null) {
                this.space.removeCollisionListener(this);
            }
        } else {
            space.addCollisionListener(this);
        }
        super.setPhysicsSpace(space);
    }

    protected void createGhostObject() {
        ghostObject = new BulletGhostObject(new SphereCollisionShape(explosionRadius));
    }

    public void collision(PhysicsCollisionEvent event) {
        if (event.getObjectA() == this || event.getObjectB() == this) {
            space.add(ghostObject);
            ghostObject.setPhysicsLocation(getPhysicsLocation(vector));
            space.addTickListener(this);
            space.removeCollisionObject(this);
            spatial.removeFromParent();
        }
    }

    public void physicsTick(PhysicsSpace space, float f) {
        //get all overlapping objects and apply impulse to them
        for (Iterator<PhysicsCollisionObject> it = ghostObject.getOverlappingObjects().iterator(); it.hasNext();) {
            PhysicsCollisionObject physicsCollisionObject = it.next();
            if (physicsCollisionObject instanceof BulletRigidBody) {
                BulletRigidBody rBody = (BulletRigidBody) physicsCollisionObject;
                rBody.getPhysicsLocation(vector2);
                vector2.subtractLocal(vector);
                float force = explosionRadius - vector2.length();
                force *= forceFactor;
                vector2.normalizeLocal();
                vector2.multLocal(force);
                ((BulletRigidBody) physicsCollisionObject).applyImpulse(vector2, Vector3f.ZERO);
            }
        }
        space.removeTickListener(this);
        space.remove(ghostObject);
    }

    /**
     * @return the explosionRadius
     */
    public float getExplosionRadius() {
        return explosionRadius;
    }

    /**
     * @param explosionRadius the explosionRadius to set
     */
    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
        createGhostObject();
    }
}
