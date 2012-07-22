package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

/**
 * An automated platform that starts rising when a special dynamic object
 * arrives (collision listener).
 * When the platform is on top, a force is applied to its passenger 
 * (physics tick listener)
 * @author zathras.
 */
public class PhysicsKinematic extends SimpleApplication
        implements PhysicsCollisionListener, PhysicsTickListener {

    public static void main(String args[]) {
        PhysicsKinematic app = new PhysicsKinematic();
        app.start();
    }
    /** Physics application state (jBullet) */
    private BulletAppState bulletAppState;
    /** Materials for bricks, cannon balls, floor. */
    Material brickMat, stoneMat, woodMat;
    private boolean isBallOnPlatform = false;
    private boolean isPlatformOnTop = false;
    private Geometry platformGeo;
    private RigidBodyControl ballPhy;
    private RigidBodyControl floorPhy;

    @Override
    public void simpleInitApp() {
        /** Make this a jBullet Physics Game */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        bulletAppState.getPhysicsSpace().addTickListener(this);

        /** Initialize the scene using helper methods (keeps code readable). */
        initMaterials();
        initLight();
        initFloor();
        initPlatform();
        dropBall();

        /** Move camera to look at scene */
        cam.setLocation(new Vector3f(0f, 3f, 15f));


    }

    /** Make a solid floor and add it to the scene. */
    public void initPlatform() {
        /* Create and attach floor geometry */
        Box platformMesh = new Box(Vector3f.ZERO, 2f, 0.5f, 5f);
        platformGeo = new Geometry("Elevator", platformMesh);
        platformGeo.setMaterial(woodMat);
        platformGeo.move(0, 2, 0);
        rootNode.attachChild(platformGeo);
        RigidBodyControl platformPhy = new RigidBodyControl(10.0f);
        platformGeo.addControl(platformPhy);
        platformPhy.setKinematic(true);
        bulletAppState.getPhysicsSpace().add(platformPhy);
    }

    /** Make a solid floor and add it to the scene. */
    public void initFloor() {
        Node floorNode = new Node("Scene");
        /* Create and attach floor geometry */
        Box floorMesh = new Box(Vector3f.ZERO, 10f, 0.5f, 10f);
        floorMesh.scaleTextureCoordinates(new Vector2f(4f, 4f));
        Geometry floorGeo = new Geometry("Floor", floorMesh);
        floorGeo.setMaterial(stoneMat);
        floorGeo.move(0, -.1f, 0);
        floorNode.attachChild(floorGeo);

        Box slopeBottomMesh = new Box(Vector3f.ZERO, 5.5f, 0.2f, 5f);
        Geometry slopeBottomGeo = new Geometry("Slop Bottom", slopeBottomMesh);
        slopeBottomGeo.setMaterial(brickMat);
        slopeBottomGeo.rotate(0, 0, FastMath.DEG_TO_RAD * 50);
        slopeBottomGeo.move(5, 4f, 0);
        floorNode.attachChild(slopeBottomGeo);

        Box wallMesh = new Box(Vector3f.ZERO, 5f, 0.5f, 5f);
        Geometry wallGeo = new Geometry("Slop Bottom", wallMesh);
        wallGeo.setMaterial(brickMat);
        wallGeo.rotate(0, 0, FastMath.DEG_TO_RAD * 90);
        wallGeo.move(-2.5f, 2, 0);
        floorNode.attachChild(wallGeo);

        floorPhy = new RigidBodyControl(0.0f);
        floorNode.addControl(floorPhy);
        bulletAppState.getPhysicsSpace().add(floorPhy);

        rootNode.attachChild(floorNode);
    }

    /** This method creates one individual physical cannon ball.
     * By defaul, the ball is accelerated and flies
     * from the camera position in the camera direction.*/
    public void dropBall() {
        /** Create a cannon ball geometry and attach to scene graph. */
        Sphere ballMesh = new Sphere(32, 32, .75f, true, false);
        ballMesh.setTextureMode(TextureMode.Projected);
        Geometry ballGeo = new Geometry("Ball", ballMesh);

        ballGeo.setMaterial(stoneMat);
        rootNode.attachChild(ballGeo);
        /** Create physical cannon ball and add to physics space. */
        ballPhy = new RigidBodyControl(5f);
        ballGeo.addControl(ballPhy);
        bulletAppState.getPhysicsSpace().add(ballPhy);
        ballPhy.setPhysicsLocation(new Vector3f(0, 10, 0));
    }

    /** Create reusable materials. */
    private void initMaterials() {
        brickMat = assetManager.loadMaterial("Materials/brick.j3m");
        stoneMat = assetManager.loadMaterial("Materials/pebbles.j3m");
        woodMat = assetManager.loadMaterial("Materials/bark.j3m");
    }

    /** Create light sources. */
    private void initLight() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1.1f, -1.3f, -2.1f));
        rootNode.addLight(sun);
        rootNode.addLight(new AmbientLight());
    }

    public void collision(PhysicsCollisionEvent event) {
        if ((event.getNodeA().getName().equals("Ball")
                && event.getNodeB().getName().equals("Elevator"))
                || (event.getNodeA().getName().equals("Elevator")
                && event.getNodeB().getName().equals("Ball"))) {
            if (!isBallOnPlatform) {
                System.out.println("Ball is on platform");
            }
            isBallOnPlatform = true;
        } else if ((event.getNodeA().getName().equals("Ball")
                && event.getNodeB().getName().equals("Scene"))
                || (event.getNodeA().getName().equals("Scene")
                && event.getNodeB().getName().equals("Ball"))) {
            if (isBallOnPlatform) {
                System.out.println("Ball is off platform");
            }
            isBallOnPlatform = false;

        }

    }

    @Override
    public void simpleUpdate(float tpf) {
        if (isBallOnPlatform && platformGeo.getLocalTranslation().getY() < 6.25f) {
            platformGeo.move(0f, tpf, 0f);
        }
        if (isBallOnPlatform && platformGeo.getLocalTranslation().getY() >= 6.25f) {
            isPlatformOnTop = true;
        }
        if (!isBallOnPlatform) {
            platformGeo.setLocalTranslation(0f, 0f, 0f);
            isPlatformOnTop = false;
        }
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        if (isBallOnPlatform && isPlatformOnTop) {
            ballPhy.applyImpulse(Vector3f.UNIT_X.clone(), Vector3f.ZERO);
            isPlatformOnTop = false;
        }
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
    }
}
