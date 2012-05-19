package chapter07;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

/**
 * Click to shoot cannon balls at a brick wall. 
 * @author double1984, modified by zathras.
 */
public class PhysicsFallingBricks extends SimpleApplication {

  public static void main(String args[]) {
    PhysicsFallingBricks app = new PhysicsFallingBricks();
    app.start();
  }
  
  /** Physics application state (jBullet) */
  private BulletAppState bulletAppState;
  
  /** Materials for bricks, cannon balls, floor. */
  Material brick_mat, stone_mat, wood_mat;
  /** Mesh shapes for bricks, cannon balls, floor. */
  private static final Box box;
  private static final Sphere sphere;
  private static final Box floor;
  private static Node wall_node;
  /** PhysicsControls for bricks, cannon balls, floor. */
  private RigidBodyControl brick_phy;
  private RigidBodyControl ball_phy;
  private RigidBodyControl floor_phy;
  /** The dimensions used for bricks and wall */
  private static final float brickLength = 0.4f;
  private static final float brickWidth  = 0.3f;
  private static final float brickHeight = 0.25f;
  private static final float wallWidth=12; 
  private static final float wallHeight=6;

  static {
    /** Initialize reusable mesh shapes. */
    floor  = new Box(Vector3f.ZERO, 10f, 0.5f, 5f);
    box    = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
    sphere = new Sphere(32,32, 0.25f, true, false);
    sphere.setTextureMode(TextureMode.Projected);
  }

  @Override
  public void simpleInitApp() {
    /** Make this a jBullet Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    
    /** Initialize the scene using helper methods (keeps code readable). */
    initMaterials();
    initLight(); 
    initUserInterface();
    initFloor();
    initBrickwall();

    /** Add InputManager action: Left click triggers shooting. */
    inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addListener(actionListener, "shoot");
    
    /** Move camera to look at scene */
    cam.setLocation(new Vector3f(0f,2f,6f));
    cam.lookAt(new Vector3f(0f,2f,0f),Vector3f.UNIT_Y);
  }

  /** Keep default navigation inputs, add shoot action.
   *  Each shot introduces a new cannon ball. */
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("shoot") && !keyPressed) {
        shootCannonBall();
      }
    }
  };

  /** Make a solid floor and add it to the scene. */
  public void initFloor() {
    /* Create and attach floor geometry */
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(wood_mat);
    floor_geo.setLocalTranslation(0, -brickHeight*2, 0); // don't collide with bricks
    rootNode.attachChild(floor_geo);
    /* Make the floor physical and static (mass zero!) */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
  }
  

  /** This loop builds a wall out of individual bricks. */
  public void initBrickwall() {
    wall_node=new Node("wall");
    float offset_h = brickLength / 3;
    float offset_v = 0;
    for (int j = 0; j < wallHeight; j++) { 
      for (int i = 0; i < wallWidth; i++) {
        Vector3f brick_pos = new Vector3f(
                offset_h + brickLength*2*i -(brickLength*wallWidth), 
                offset_v + brickHeight, 
                0f );
        layBrick(brick_pos);
      }
      offset_h = -offset_h;
      offset_v += 2 * brickHeight;
    }
    rootNode.attachChild(wall_node);
  }

  /** This method creates one individual physical brick. */
  public void layBrick(Vector3f loc) {
    /** Create a brick geometry and attach to scene graph. */
    Geometry brick_geo = new Geometry("brick", box);
    brick_geo.setMaterial(brick_mat);
    wall_node.attachChild(brick_geo);
    brick_geo.setLocalTranslation(loc);
    /** Create physical brick and add to physics space. */
    brick_phy = new RigidBodyControl(5f);
    brick_geo.addControl(brick_phy);
    bulletAppState.getPhysicsSpace().add(brick_phy);
    brick_phy.setFriction(10f);
  }

  /** This method creates one individual physical cannon ball.
   * By defaul, the ball is accelerated and flies
   * from the camera position in the camera direction.*/
  public void shootCannonBall() {
    /** Create a cannon ball geometry and attach to scene graph. */
    Geometry ball_geo = new Geometry("cannon ball", sphere);
    ball_geo.setMaterial(stone_mat);
    ball_geo.setLocalTranslation(cam.getLocation());
    rootNode.attachChild(ball_geo);
    /** Create physical cannon ball and add to physics space. */
    ball_phy = new RigidBodyControl(5f);
    ball_geo.addControl(ball_phy);
    bulletAppState.getPhysicsSpace().add(ball_phy);
    
    ball_phy.setCcdSweptSphereRadius(0.25f);
    ball_phy.setCcdMotionThreshold(100);
    
    /** Accelerate the physical ball in camera direction to shoot it! */
    ball_phy.setLinearVelocity(cam.getDirection().mult(50));
  }
  
  /** Create reusable materials. */
  private void initMaterials() {
    brick_mat  = assetManager.loadMaterial("Materials/brick.j3m");
    stone_mat  = assetManager.loadMaterial("Materials/pebbles.j3m");
    wood_mat   = assetManager.loadMaterial("Materials/bark.j3m");
  }
  
  /** Create light sources. */
  private void initLight() {
    /** A directional light -- for simple shading. */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1.1f, -1.3f, -2.1f));
    rootNode.addLight(sun);
    /** An overall ambient illumination. */
    rootNode.addLight(new AmbientLight());
  }
  
  /** User interface: Crosshairs to help with aiming. */
  protected void initUserInterface() {
    // remove the default UI that displays statistics
    setDisplayStatView(false);
    // add custom UI that displays crosshairs
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        // Fake crosshairs made of a plus sign :-)
    ch.setLocalTranslation( // Move crosshairs to center of screen
            (settings.getWidth() / 2) , (settings.getHeight() / 2)  , 0);
    guiNode.attachChild(ch);
  }
}
