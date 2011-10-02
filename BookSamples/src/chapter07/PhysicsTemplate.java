package chapter07;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 * Example 9 - Using physics to make walls and floors solid.
 * This sample demos two rigid collision shapes and a custom Action Listener.
 * @author Normen, edited by Zathras
 */
public class PhysicsTemplate extends SimpleApplication {

  private Spatial sceneModel;
  private BulletAppState bulletAppState;
  private RigidBodyControl landscape;
  private CharacterControl player;
  private RigidBodyControl brick_phy;
  private Box box;
  
  public static void main(String[] args) {
    PhysicsTemplate app = new PhysicsTemplate();
    app.start();
  }

  public void simpleInitApp() {
    /** Set up Physics */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    
        /** A white ambient light source. */ 
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White);
    rootNode.addLight(ambient); 

    box = new Box(Vector3f.ZERO, 1f,1f,1f);
    Geometry brick_geo = new Geometry("brick", box);
    Material wall_mat = assetManager.loadMaterial("Materials/brick.j3m");
    brick_geo.setMaterial(wall_mat);
    rootNode.attachChild(brick_geo);
    /** Make brick physical with a mass > 0.0f. */
    brick_phy = new RigidBodyControl(0f);
    /** Add physical brick to physics space. */
    brick_geo.addControl(brick_phy);
    bulletAppState.getPhysicsSpace().add(brick_phy);
    brick_phy.setFriction(1f);

    // We set up collision detection for the player by creating
    // a capsule collision shape and a physics character node.
    // The physics character node offers extra settings for
    // size, stepheight, jumping, falling, and gravity.
    // We also put the player in its starting position.
    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);
    player.setGravity(30);
    player.setPhysicsLocation(new Vector3f(0, 10, 0));
    // We attach the scene and the player to the rootnode and the physics space,
    // to make them appear in the game world.
    rootNode.attachChild(brick_geo);
    bulletAppState.getPhysicsSpace().add(brick_phy);
    bulletAppState.getPhysicsSpace().add(player);
  }
}