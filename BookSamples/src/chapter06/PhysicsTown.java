package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Example 9 - Using physics to make walls and floors solid.
 * This sample demos two rigid collision shapes and a custom Action Listener.
 * @author Normen, edited by Zathras
 */
public class PhysicsTown extends SimpleApplication implements ActionListener {

    private Node sceneNode;
    private BulletAppState bulletAppState;
    private RigidBodyControl scenePhy;
    private CharacterControl playerPhy;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, forward = false, back = false;

    public static void main(String[] args) {
        PhysicsTown app = new PhysicsTown();
        app.start();
    }

    public void simpleInitApp() {
        /** Set up Physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        initLight();      
        initNavigation(); 
        initScene();
        initCharacter();
    }

    private void initCharacter() {
        // Set up collision detection for 1st-person camera. Player has no visible geometry.
        // 1. Create a Capsule Collision Shape as big as the player (radius, height, axis).
        CapsuleCollisionShape playerCol = new CapsuleCollisionShape(1f, 2f, 1);
        // 2. From the Collision Shape, create a Character Physics Control (with stepheight).
        playerPhy = new CharacterControl(playerCol, 0.5f);
        // 3. Set properties of Character Physics Control
        playerPhy.setJumpSpeed(20);
        playerPhy.setFallSpeed(30);
        // Move the player in its starting position.
        playerPhy.setPhysicsLocation(new Vector3f(0, 2, 0));
        // Add the first-person player to PhysicsSpace
        bulletAppState.getPhysicsSpace().add(playerPhy);
    }

    private void initScene() {
        // You load a model with floors and walls and make them solid:
        viewPort.setBackgroundColor(ColorRGBA.Blue); // set up blue sky
        // 1. Load the scene
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneNode = (Node)assetManager.loadModel("main.scene");
        sceneNode.scale(1.5f);
        rootNode.attachChild(sceneNode);
        // 1. Create a RigidBody PhysicsControl with mass zero
        // 2. Add scene PhysicsControl to the scene geometry
        // 3. Add scene PhysicsControl to PhysicsSpace
        scenePhy = new RigidBodyControl(0f);
        sceneNode.addControl(scenePhy);
        bulletAppState.getPhysicsSpace().add(scenePhy);
    }

    private void initLight() {
        AmbientLight ambient = new AmbientLight();
        rootNode.addLight(ambient);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1.4f, -1.4f, -1.4f));
        rootNode.addLight(sun);
    }

    /** Override default navigational key mappings here, so you can
     *  add physics-controlled walking and jumping to the camera. */
    private void initNavigation() {
        flyCam.setMoveSpeed(100);
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Forward", "Left", "Back", "Right");
        inputManager.addListener(this, "Jump");
    }

    /** These custom navigation actions are triggered by user input.
     * No walking happens yet -- here you keep track of the direction the user wants to go. */
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Left")) {
            left = isPressed;
        } else if (binding.equals("Right")) {
            right = isPressed;
        } else if (binding.equals("Forward")) {
            forward = isPressed;
        } else if (binding.equals("Back")) {
            back = isPressed;
        } else if (binding.equals("Jump")) {
            playerPhy.jump();
        }
    }

    /**
     * First-person walking is handled here in the update loop.
     */
    @Override
    public void simpleUpdate(float tpf) {
        // Check in which direction the player is walking by interpreting
        // the camera direction forward (camDir) and to the side (camLeft).
        Vector3f camDir = cam.getDirection();
        Vector3f camLeft = cam.getLeft();

        // Calculate final walk direction:
        walkDirection.set(0, 0, 0); // reset
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (forward) {
            walkDirection.addLocal(camDir);
        }
        if (back) {
            walkDirection.addLocal(camDir.negate().clone().multLocal(0.5f));
        }
        // Use setWalkDirection() to move the physics-controlled player.
        playerPhy.setWalkDirection(walkDirection);
        
        // Make sure to move the first-person camera with the player.
        //cam.setLocation(playerPhy.getPhysicsLocation());
        cam.setLocation(playerPhy.getPhysicsLocation().add(0, 1f, 0));
    }
}
