package chapter07;

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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Example 9 - Using physics to make walls and floors solid.
 * This sample demos two rigid collision shapes and a custom Action Listener.
 * @author Normen, edited by Zathras
 */
public class PhysicsTown extends SimpleApplication implements ActionListener {

  private BulletAppState bulletAppState;
  private Spatial scene_geo;
  private RigidBodyControl scene_phy;
  private CollisionShape scene_col;
  private CharacterControl player_phy;
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
    
    viewPort.setBackgroundColor(ColorRGBA.Blue); // set up blue sky
    initLight();      // set up sun light
    initNavigation();  // set up first-person camera controls
    
    // You load a model with floors and walls and make them solid:
    // 1. Load the scene
    assetManager.registerLocator("town.zip", ZipLocator.class);
    scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f); // adjust the size a bit
    // 2. Attach scene geometry to rootNode
    rootNode.attachChild(scene_geo);
    // 3. Generate a Compound CollisionShape for the scene geometry
    scene_col = CollisionShapeFactory.createMeshShape((Node) scene_geo);
    // 4. From the Collision Shape, create a RigidBody PhysicsControl with mass zero
    scene_phy = new RigidBodyControl(scene_col, 0f);
    // 5. Add scene PhysicsControl to the scene geometry
    scene_geo.addControl(scene_phy);
    // 6. Add scene PhysicsControl to PhysicsSpace
    bulletAppState.getPhysicsSpace().add(scene_phy);
    
    // Set up collision detection for 1st-person camera. Player has no visible geometry.
    // 1. Create a Capsule Collision Shape as big as the player (radius, height, axis).
    CapsuleCollisionShape player_col = new CapsuleCollisionShape(1f, 3f, 1);
    // 2. From the Collision Shape, create a Character Physics Control (with stepheight).
    player_phy = new CharacterControl(player_col, 0.5f);
    // 3. Set properties of Character Physics Control
    player_phy.setJumpSpeed(20);
    player_phy.setFallSpeed(30);
    // Move the player in its starting position.
    player_phy.setPhysicsLocation(new Vector3f(0, 10, 0));
    // Add the first-person player to PhysicsSpace
    bulletAppState.getPhysicsSpace().add(player_phy);

  }

  private void initLight() {
    // We add light so we see the scene
    AmbientLight al = new AmbientLight();
    rootNode.addLight(al);
    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
    rootNode.addLight(dl);
  }

  /** Override default navigational key mappings here, so you can
   *  add physics-controlled walking and jumping to the camera. */
  private void initNavigation() {
    flyCam.setMoveSpeed(100);
    inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Left",    new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Back",    new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Right",   new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Jump",    new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(this, "Forward");
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Back");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Jump");
  }

  /** These custom navigation actions are triggered by user input.
   * No walking happens yet -- here you keep track of the direction the user wants to go. */
  public void onAction(String binding, boolean intensity, float tpf) {
    if      (binding.equals("Left"))    { left    = intensity; }
    else if (binding.equals("Right"))   { right   = intensity; }
    else if (binding.equals("Forward")) { forward = intensity; }
    else if (binding.equals("Back"))    { back    = intensity; }
    else if (binding.equals("Jump"))    { player_phy.jump(); }
  }

  /**
   * First-person walking is handled here in the update loop.
   */
  @Override
  public void simpleUpdate(float tpf) {
    // Check in which direction the player is walking by interpreting
    // the camera direction forward (camDir) and to the side (camLeft).
    Vector3f camDir  = cam.getDirection();
    Vector3f camLeft = cam.getLeft();

    // Calculate final walk direction:
    walkDirection.set(0, 0, 0); // reset
    if (left)    { walkDirection.addLocal(camLeft); }
    if (right)   { walkDirection.addLocal(camLeft.negate()); }
    if (forward) { walkDirection.addLocal(camDir); }
    if (back)    { walkDirection.addLocal(camDir.negate().clone().multLocal(0.5f)); }
    // Use setWalkDirection() to move the physics-controlled player.
    player_phy.setWalkDirection(walkDirection);
    // Make sure to move the first-person camera with the player.
    cam.setLocation(player_phy.getPhysicsLocation());
  }
}
