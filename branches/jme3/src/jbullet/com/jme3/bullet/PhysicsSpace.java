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
package com.jme3.bullet;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.ContactAddedCallback;
import com.bulletphysics.ContactDestroyedCallback;
import com.bulletphysics.ContactProcessedCallback;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.AxisSweep3_32;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.OverlapFilterCallback;
import com.bulletphysics.collision.broadphase.SimpleBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.jme3.app.AppTask;
import com.jme3.math.Vector3f;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionEventFactory;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.Converter;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>PhysicsSpace - The central jbullet-jme physics space</p>
 * @author normenhansen
 */
public class PhysicsSpace {

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;
    private static ThreadLocal<ConcurrentLinkedQueue<AppTask<?>>> pQueueTL =
            new ThreadLocal<ConcurrentLinkedQueue<AppTask<?>>>() {

                @Override
                protected ConcurrentLinkedQueue<AppTask<?>> initialValue() {
                    return new ConcurrentLinkedQueue<AppTask<?>>();
                }
            };
    private ConcurrentLinkedQueue<AppTask<?>> pQueue = new ConcurrentLinkedQueue<AppTask<?>>();
    private static ThreadLocal<PhysicsSpace> physicsSpaceTL = new ThreadLocal<PhysicsSpace>();
    private DiscreteDynamicsWorld dynamicsWorld = null;
    private BroadphaseInterface broadphase;
    private BroadphaseType broadphaseType = BroadphaseType.DBVT;
    private CollisionDispatcher dispatcher;
    private ConstraintSolver solver;
    private DefaultCollisionConfiguration collisionConfiguration;
//    private Map<GhostObject, PhysicsGhostObject> physicsGhostNodes = new ConcurrentHashMap<GhostObject, PhysicsGhostObject>();
    private Map<RigidBody, PhysicsRigidBody> physicsNodes = new ConcurrentHashMap<RigidBody, PhysicsRigidBody>();
    private List<PhysicsJoint> physicsJoints = new LinkedList<PhysicsJoint>();
    private List<PhysicsCollisionListener> collisionListeners = new LinkedList<PhysicsCollisionListener>();
    private List<PhysicsCollisionEvent> collisionEvents = new LinkedList<PhysicsCollisionEvent>();
    private Map<Integer, PhysicsCollisionGroupListener> collisionGroupListeners = new ConcurrentHashMap<Integer, PhysicsCollisionGroupListener>();
    private ConcurrentLinkedQueue<PhysicsTickListener> tickListeners = new ConcurrentLinkedQueue<PhysicsTickListener>();
    private PhysicsCollisionEventFactory eventFactory = new PhysicsCollisionEventFactory();
    private Vector3f worldMin = new Vector3f(-10000f, -10000f, -10000f);
    private Vector3f worldMax = new Vector3f(10000f, 10000f, 10000f);
    private float accuracy = 1f / 60f;
    private boolean deterministic = true;
    private javax.vecmath.Vector3f rayVec1 = new javax.vecmath.Vector3f();
    private javax.vecmath.Vector3f rayVec2 = new javax.vecmath.Vector3f();

    /**
     * Get the current PhysicsSpace <b>running on this thread</b><br/>
     * For parallel physics, this can also be called from the OpenGL thread to receive the PhysicsSpace
     * @return the PhysicsSpace running on this thread
     */
    public static PhysicsSpace getPhysicsSpace() {
        return physicsSpaceTL.get();
    }

    /**
     * Used internally
     * @param space
     */
    public static void setLocalThreadPhysicsSpace(PhysicsSpace space) {
        physicsSpaceTL.set(space);
    }

    public PhysicsSpace() {
        this(new Vector3f(-10000f, -10000f, -10000f), new Vector3f(10000f, 10000f, 10000f), BroadphaseType.DBVT);
    }

    public PhysicsSpace(BroadphaseType broadphaseType) {
        this(new Vector3f(-10000f, -10000f, -10000f), new Vector3f(10000f, 10000f, 10000f), broadphaseType);
    }

    public PhysicsSpace(Vector3f worldMin, Vector3f worldMax) {
        this(worldMin, worldMax, BroadphaseType.AXIS_SWEEP_3);
    }

    public PhysicsSpace(Vector3f worldMin, Vector3f worldMax, BroadphaseType broadphaseType) {
        this.worldMin.set(worldMin);
        this.worldMax.set(worldMax);
        this.broadphaseType = broadphaseType;
        create();
    }

    /**
     * has to be called from the (designated) physics thread
     */
    public void create() {
        pQueueTL.set(pQueue);

        collisionConfiguration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(collisionConfiguration);
        switch (broadphaseType) {
            case SIMPLE:
                broadphase = new SimpleBroadphase();
                break;
            case AXIS_SWEEP_3:
                broadphase = new AxisSweep3(Converter.convert(worldMin), Converter.convert(worldMax));
                break;
            case AXIS_SWEEP_3_32:
                broadphase = new AxisSweep3_32(Converter.convert(worldMin), Converter.convert(worldMax));
                break;
            case DBVT:
                broadphase = new DbvtBroadphase();
                break;
        }

        solver = new SequentialImpulseConstraintSolver();

        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new javax.vecmath.Vector3f(0, -9.81f, 0));

        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);

        physicsSpaceTL.set(this);
        //register filter callback for groups / collision decision
        setOverlapFilterCallback();
        setTickCallback();
        setContactCallbacks();
    }

    private void setOverlapFilterCallback() {
        OverlapFilterCallback callback = new OverlapFilterCallback() {

            public boolean needBroadphaseCollision(BroadphaseProxy bp, BroadphaseProxy bp1) {
                boolean collides = (bp.collisionFilterGroup & bp1.collisionFilterMask) != 0;
                if (collides) {
                    collides = (bp1.collisionFilterGroup & bp.collisionFilterMask) != 0;
                }
                if (collides) {
                    assert (bp.clientObject instanceof com.bulletphysics.collision.dispatch.CollisionObject && bp.clientObject instanceof com.bulletphysics.collision.dispatch.CollisionObject);
                    com.bulletphysics.collision.dispatch.CollisionObject colOb = (com.bulletphysics.collision.dispatch.CollisionObject) bp.clientObject;
                    com.bulletphysics.collision.dispatch.CollisionObject colOb1 = (com.bulletphysics.collision.dispatch.CollisionObject) bp1.clientObject;
                    assert (colOb.getUserPointer() != null && colOb1.getUserPointer() != null);
                    PhysicsCollisionObject collisionObject = (PhysicsCollisionObject) colOb.getUserPointer();
                    PhysicsCollisionObject collisionObject1 = (PhysicsCollisionObject) colOb1.getUserPointer();
                    if ((collisionObject.getCollideWithGroups() & collisionObject1.getCollisionGroup()) > 0
                            || (collisionObject1.getCollideWithGroups() & collisionObject.getCollisionGroup()) > 0) {
                        PhysicsCollisionGroupListener listener = collisionGroupListeners.get(collisionObject.getCollisionGroup());
                        PhysicsCollisionGroupListener listener1 = collisionGroupListeners.get(collisionObject1.getCollisionGroup());
                        if (listener != null) {
                            return listener.collide(collisionObject, collisionObject1);
                        } else if (listener1 != null) {
                            return listener1.collide(collisionObject, collisionObject1);
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
                return collides;
            }
        };
        dynamicsWorld.getPairCache().setOverlapFilterCallback(callback);
    }

    private void setTickCallback() {
        final PhysicsSpace space = this;
        InternalTickCallback callback = new InternalTickCallback() {

            @Override
            public void internalTick(DynamicsWorld dw, float f) {
                for (Iterator<PhysicsTickListener> it = tickListeners.iterator(); it.hasNext();) {
                    PhysicsTickListener physicsTickCallback = it.next();
                    physicsTickCallback.physicsTick(space, f);
                }
                //execute task list
                AppTask task = pQueue.poll();
                task = pQueue.poll();
                while (task != null) {
                    while (task.isCancelled()) {
                        task = pQueue.poll();
                    }
                    try {
                        task.invoke();
                    } catch (Exception ex) {
                        Logger.getLogger(PhysicsSpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    task = pQueue.poll();
                }

            }
        };
        dynamicsWorld.setInternalTickCallback(callback, this);
    }

    private void setContactCallbacks() {
        BulletGlobals.setContactAddedCallback(new ContactAddedCallback() {

            public boolean contactAdded(ManifoldPoint cp, com.bulletphysics.collision.dispatch.CollisionObject colObj0,
                    int partId0, int index0, com.bulletphysics.collision.dispatch.CollisionObject colObj1, int partId1,
                    int index1) {
                System.out.println("contact added");
                return true;
            }
        });

        BulletGlobals.setContactProcessedCallback(new ContactProcessedCallback() {

            public boolean contactProcessed(ManifoldPoint cp, Object body0, Object body1) {
                if (body0 instanceof CollisionObject && body1 instanceof CollisionObject) {
                    PhysicsCollisionObject node = null, node1 = null;
                    CollisionObject rBody0 = (CollisionObject) body0;
                    CollisionObject rBody1 = (CollisionObject) body1;
                    node = (PhysicsCollisionObject) rBody0.getUserPointer();
                    node1 = (PhysicsCollisionObject) rBody1.getUserPointer();
                    collisionEvents.add(eventFactory.getEvent(PhysicsCollisionEvent.TYPE_PROCESSED, node, node1, cp));
                }
                return true;
            }
        });

        BulletGlobals.setContactDestroyedCallback(new ContactDestroyedCallback() {

            public boolean contactDestroyed(Object userPersistentData) {
                System.out.println("contact destroyed");
                return true;
            }
        });
    }

    /**
     * updates the physics space
     * @param time the current time value
     */
    public void update(float time) {
        int subSteps = 1;
        if (deterministic && time > accuracy) {
            subSteps = (int) (Math.ceil(time / accuracy));
        }
        update(time, subSteps);
    }

    /**
     * updates the physics space, uses maxSteps<br>
     * @param time the current time value
     * @param maxSteps
     */
    public void update(float time, int maxSteps) {
        if (getDynamicsWorld() == null) {
            return;
        }
        //step simulation
        dynamicsWorld.stepSimulation(time, maxSteps, accuracy);
    }

    public void distributeEvents() {
        //add collision callbacks
        synchronized (collisionEvents) {
            for (Iterator<PhysicsCollisionEvent> it = collisionEvents.iterator(); it.hasNext();) {
                PhysicsCollisionEvent physicsCollisionEvent = it.next();
                for (PhysicsCollisionListener listener : collisionListeners) {
                    listener.collision(physicsCollisionEvent);
                }
                //recycle events
                eventFactory.recycle(physicsCollisionEvent);
                it.remove();
            }
        }
    }

    public static <V> Future<V> enqueueOnThisThread(Callable<V> callable) {
        AppTask<V> task = new AppTask<V>(callable);
        System.out.println("created apptask");
        pQueueTL.get().add(task);
        return task;
    }

    /**
     * calls the callable on the next physics tick (ensuring e.g. force applying)
     * @param <V>
     * @param callable
     * @return
     */
    public <V> Future<V> enqueue(Callable<V> callable) {
        AppTask<V> task = new AppTask<V>(callable);
        pQueue.add(task);
        return task;
    }

    /**
     * adds an object to the physics space
     * @param obj the PhysicsControl or Spatial with PhysicsControl to add
     */
    public void add(Object obj) {
        if (obj instanceof PhysicsControl) {
            ((PhysicsControl) obj).setPhysicsSpace(this);
        } else if (obj instanceof Spatial) {
            Spatial node = (Spatial) obj;
            PhysicsControl control = node.getControl(PhysicsControl.class);
            control.setPhysicsSpace(this);
        } else if (obj instanceof PhysicsCollisionObject) {
            addCollisionObject((PhysicsCollisionObject) obj);
        } else if (obj instanceof PhysicsJoint) {
            addJoint((PhysicsJoint) obj);
        } else {
            throw (new UnsupportedOperationException("Cannot add this kind of object to the physics space."));
        }
    }

    public void addCollisionObject(PhysicsCollisionObject obj) {
        if (obj instanceof PhysicsGhostObject) {
            addGhostObject((PhysicsGhostObject) obj);
        } else if (obj instanceof PhysicsRigidBody) {
            addRigidBody((PhysicsRigidBody) obj);
        } else if (obj instanceof PhysicsVehicle) {
            addRigidBody((PhysicsVehicle) obj);
        } else if (obj instanceof PhysicsCharacter) {
            addCharacter((PhysicsCharacter) obj);
        }
    }

    /**
     * removes an object from the physics space
     * @param obj the PhysicsControl or Spatial with PhysicsControl to remove
     */
    public void remove(Object obj) {
        if (obj instanceof PhysicsControl) {
            ((PhysicsControl) obj).setPhysicsSpace(null);
        } else if (obj instanceof Spatial) {
            Spatial node = (Spatial) obj;
            PhysicsControl control = node.getControl(PhysicsControl.class);
            control.setPhysicsSpace(null);
        } else if (obj instanceof PhysicsCollisionObject) {
            removeCollisionObject((PhysicsCollisionObject) obj);
        } else if (obj instanceof PhysicsJoint) {
            removeJoint((PhysicsJoint) obj);
        } else {
            throw (new UnsupportedOperationException("Cannot remove this kind of object from the physics space."));
        }
    }

    public void removeCollisionObject(PhysicsCollisionObject obj) {
        if (obj instanceof PhysicsGhostObject) {
            removeGhostObject((PhysicsGhostObject) obj);
        } else if (obj instanceof PhysicsRigidBody) {
            removeRigidBody((PhysicsRigidBody) obj);
        } else if (obj instanceof PhysicsCharacter) {
            removeCharacter((PhysicsCharacter) obj);
        }
    }

    /**
     * adds all physics controls and joints in the given spatial node to the physics space
     * (e.g. after loading from disk) - recursive if node
     * @param spatial the rootnode containing the physics objects
     */
    public void addAll(Spatial spatial) {
        if (spatial.getControl(RigidBodyControl.class) != null) {
            RigidBodyControl physicsNode = spatial.getControl(RigidBodyControl.class);
            if (!physicsNodes.containsValue(physicsNode)) {
                physicsNode.setPhysicsSpace(this);
            }
            //add joints
            List<PhysicsJoint> joints = physicsNode.getJoints();
            for (Iterator<PhysicsJoint> it1 = joints.iterator(); it1.hasNext();) {
                PhysicsJoint physicsJoint = it1.next();
                //add connected physicsnodes if they are not already added
                if (!physicsNodes.containsValue(physicsJoint.getBodyA())) {
                    addRigidBody(physicsJoint.getBodyA());
                }
                if (!physicsNodes.containsValue(physicsJoint.getBodyB())) {
                    addRigidBody(physicsJoint.getBodyB());
                }
                if (!physicsJoints.contains(physicsJoint)) {
                    addJoint(physicsJoint);
                }
            }
        } else if (spatial.getControl(PhysicsControl.class) != null) {
            spatial.getControl(PhysicsControl.class).setPhysicsSpace(this);
        }
        //recursion
        if (spatial instanceof Node) {
            List<Spatial> children = ((Node) spatial).getChildren();
            for (Iterator<Spatial> it = children.iterator(); it.hasNext();) {
                Spatial spat = it.next();
                if (spat instanceof Node) {
                    addAll((Node) spat);
                }
            }
        }
    }

    /**
     * Removes all physics controls and joints in the given spatial from the physics space
     * (e.g. before saving to disk) - recursive if node
     * @param spatial the rootnode containing the physics objects
     */
    public void removeAll(Spatial spatial) {
        if (spatial.getControl(RigidBodyControl.class) != null) {
            RigidBodyControl physicsNode = spatial.getControl(RigidBodyControl.class);
            if (physicsNodes.containsValue(physicsNode)) {
                physicsNode.setPhysicsSpace(null);
            }
            //remove joints
            List<PhysicsJoint> joints = physicsNode.getJoints();
            for (Iterator<PhysicsJoint> it1 = joints.iterator(); it1.hasNext();) {
                PhysicsJoint physicsJoint = it1.next();
                //add connected physicsnodes if they are not already added
                if (physicsNodes.containsValue(physicsJoint.getBodyA())) {
                    removeRigidBody(physicsJoint.getBodyA());
                }
                if (physicsNodes.containsValue(physicsJoint.getBodyB())) {
                    removeRigidBody(physicsJoint.getBodyB());
                }
                if (physicsJoints.contains(physicsJoint)) {
                    removeJoint(physicsJoint);
                }
            }
        } else if (spatial.getControl(PhysicsControl.class) != null) {
            spatial.getControl(PhysicsControl.class).setPhysicsSpace(null);
        }
        //recursion
        if (spatial instanceof Node) {
            List<Spatial> children = ((Node) spatial).getChildren();
            for (Iterator<Spatial> it = children.iterator(); it.hasNext();) {
                Spatial spat = it.next();
                if (spat instanceof Node) {
                    removeAll((Node) spat);
                }
            }
        }
    }

    private void addGhostObject(PhysicsGhostObject node) {
        dynamicsWorld.addCollisionObject(node.getObjectId());
    }

    private void removeGhostObject(PhysicsGhostObject node) {
        dynamicsWorld.removeCollisionObject(node.getObjectId());
    }

    private void addCharacter(PhysicsCharacter node) {
//        dynamicsWorld.addCollisionObject(node.getObjectId());
        dynamicsWorld.addCollisionObject(node.getObjectId(), CollisionFilterGroups.CHARACTER_FILTER, (short) (CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
        dynamicsWorld.addAction(node.getControllerId());
    }

    private void removeCharacter(PhysicsCharacter node) {
        dynamicsWorld.removeAction(node.getControllerId());
        dynamicsWorld.removeCollisionObject(node.getObjectId());
    }

    private void addRigidBody(PhysicsRigidBody node) {
        physicsNodes.put(node.getObjectId(), node);
        dynamicsWorld.addRigidBody(node.getObjectId());
        if (node instanceof PhysicsVehicle) {
            ((PhysicsVehicle) node).createVehicle(this);
            dynamicsWorld.addVehicle(((PhysicsVehicle) node).getVehicle());
        }
    }

    private void removeRigidBody(PhysicsRigidBody node) {
        if (node instanceof PhysicsVehicle) {
            dynamicsWorld.removeVehicle(((PhysicsVehicle) node).getVehicle());
        }
        physicsNodes.remove(node.getObjectId());
        dynamicsWorld.removeRigidBody(node.getObjectId());
    }

    private void addJoint(PhysicsJoint joint) {
        physicsJoints.add(joint);
        dynamicsWorld.addConstraint(joint.getConstraint(), !joint.isCollisionBetweenLinkedBodys());
    }

    private void removeJoint(PhysicsJoint joint) {
        physicsJoints.remove(joint);
        dynamicsWorld.removeConstraint(joint.getConstraint());
    }

    /**
     * Sets the gravity of the PhysicsSpace, set before adding PhysicsNodes!
     * @param gravity
     */
    public void setGravity(Vector3f gravity) {
        dynamicsWorld.setGravity(Converter.convert(gravity));
    }

    /**
     * applies gravity value to all objects
     */
    public void applyGravity(){
        dynamicsWorld.applyGravity();
    }

    /**
     * clears forces of all objects
     */
    public void clearForces(){
        dynamicsWorld.clearForces();
    }

    /**
     * Adds the specified listener to the physics tick listeners.
     * The listeners are called on each physics step, which is not necessarily
     * each frame but is determined by the accuracy of the physics space.
     * @param listener
     */
    public void addTickListener(PhysicsTickListener listener) {
        tickListeners.add(listener);
    }

    public void removeTickListener(PhysicsTickListener listener) {
        tickListeners.remove(listener);
    }

    /**
     * Adds a CollisionListener that will be informed about collision events
     * @param listener the CollisionListener to add
     */
    public void addCollisionListener(PhysicsCollisionListener listener) {
        collisionListeners.add(listener);
    }

    /**
     * Removes a CollisionListener from the list
     * @param listener the CollisionListener to remove
     */
    public void removeCollisionListener(PhysicsCollisionListener listener) {
        collisionListeners.remove(listener);
    }

    /**
     * Adds a listener for a specific collision group, such a listener can disable collisions when they happen.<br>
     * There can be only one listener per collision group.
     * @param listener
     * @param collisionGroup
     */
    public void addCollisionGroupListener(PhysicsCollisionGroupListener listener, int collisionGroup) {
        collisionGroupListeners.put(collisionGroup, listener);
    }

    public void removeCollisionGroupListener(int collisionGroup) {
        collisionGroupListeners.remove(collisionGroup);
    }

    /**
     * Performs a ray collision test and returns the results as a list of PhysicsRayTestResults
     */
    public List<PhysicsRayTestResult> rayTest(Vector3f from, Vector3f to) {
        List<PhysicsRayTestResult> results = new LinkedList<PhysicsRayTestResult>();
        dynamicsWorld.rayTest(Converter.convert(from, rayVec1), Converter.convert(to, rayVec2), new InternalRayListener(results));
        return results;
    }

    public List<PhysicsRayTestResult> rayTest(Vector3f from, Vector3f to, List<PhysicsRayTestResult> results) {
        results.clear();
        dynamicsWorld.rayTest(Converter.convert(from, rayVec1), Converter.convert(to, rayVec2), new InternalRayListener(results));
        return results;
    }

    private class InternalRayListener extends CollisionWorld.RayResultCallback {

        private List<PhysicsRayTestResult> results;

        public InternalRayListener(List<PhysicsRayTestResult> results) {
            this.results = results;
        }

        @Override
        public float addSingleResult(LocalRayResult lrr, boolean bln) {
            PhysicsCollisionObject obj = (PhysicsCollisionObject) lrr.collisionObject.getUserPointer();
            results.add(new PhysicsRayTestResult(obj, Converter.convert(lrr.hitNormalLocal), lrr.hitFraction, bln));
            return lrr.hitFraction;
        }
    }

    /**
     * destroys the current PhysicsSpace so that a new one can be created
     */
    public void destroy() {
        physicsNodes.clear();
        physicsJoints.clear();

        dynamicsWorld.destroy();
        dynamicsWorld = null;
    }

    /**
     * used internally
     * @return the dynamicsWorld
     */
    public DynamicsWorld getDynamicsWorld() {
        return dynamicsWorld;
    }

    public BroadphaseType getBroadphaseType() {
        return broadphaseType;
    }

    public void setBroadphaseType(BroadphaseType broadphaseType) {
        this.broadphaseType = broadphaseType;
    }

    /**
     * Sets the deterministic mode of this physics space.
     * If the physicsSpace is deterministic, low fps values will
     * be compensated by stepping the physics space multiple times per frame.
     * If not, low fps values will make the physics inaccurate. This switch
     * has been added to avoid problems with "overloaded" physics spaces that drive
     * their own fps down.
     * @param deterministic
     */
    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

    /**
     * get the current accuracy of the physics computation
     * @return the current accuracy
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * sets the accuracy of the physics computation, default=1/60s<br>
     * @param accuracy
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Vector3f getWorldMin() {
        return worldMin;
    }

    /**
     * only applies for AXIS_SWEEP broadphase
     * @param worldMin
     */
    public void setWorldMin(Vector3f worldMin) {
        this.worldMin.set(worldMin);
    }

    public Vector3f getWorldMax() {
        return worldMax;
    }

    /**
     * only applies for AXIS_SWEEP broadphase
     * @param worldMax
     */
    public void setWorldMax(Vector3f worldMax) {
        this.worldMax.set(worldMax);
    }

    /**
     * interface with Broadphase types
     */
    public enum BroadphaseType {

        /**
         * basic Broadphase
         */
        SIMPLE,
        /**
         * better Broadphase, needs worldBounds , max Object number = 16384
         */
        AXIS_SWEEP_3,
        /**
         * better Broadphase, needs worldBounds , max Object number = 65536
         */
        AXIS_SWEEP_3_32,
        /**
         * Broadphase allowing quicker adding/removing of physics objects
         */
        DBVT;
    }
}
