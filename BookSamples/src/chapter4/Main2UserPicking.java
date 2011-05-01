package chapter4;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This example demonstrates responding to user input, and target picking.
 * We added more cubes and we use ray casting to select which cube rotates.
 */
public class Main2UserPicking extends SimpleApplication {

  private Trigger trigger_pause = new KeyTrigger(KeyInput.KEY_P);
  private Trigger trigger_rotate = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
  private Trigger trigger_pause2 = new KeyTrigger(KeyInput.KEY_ESCAPE);
  private boolean isRunning = true;
  private Geometry geom, geom2;

  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    inputManager.deleteMapping(INPUT_MAPPING_EXIT);
    inputManager.deleteMapping(INPUT_MAPPING_CAMERA_POS);
    inputManager.deleteMapping(INPUT_MAPPING_MEMORY);
    /** register input mappings to input manager */
    inputManager.addMapping("Pause Game", trigger_pause, trigger_pause2);
    inputManager.addMapping("Rotate", trigger_rotate);
    inputManager.addListener(actionListener, new String[]{"Pause Game"});
    inputManager.addListener(analogListener, new String[]{"Rotate"});

    /** Create a blue cube */
    Box mesh = new Box(Vector3f.ZERO, 1, 1, 1); 
    geom = new Geometry("Blue Box", mesh);   
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.Blue);     
    geom.setMaterial(mat);                       
    rootNode.attachChild(geom);                  

    /** Create a red cube */
    Box mesh2 = new Box(new Vector3f(0, 2, 0), 1, 1, 1);
    geom2 = new Geometry("Red Box", mesh2);    
    Material mat2 = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md"); 
    mat2.setColor("Color", ColorRGBA.Red);      
    geom2.setMaterial(mat2);    
    rootNode.attachChild(geom2);
  }
  private ActionListener actionListener = new ActionListener() {

    public void onAction(String name, boolean isPressed, float tpf) {
      if (name.equals("Pause Game") && !isPressed) {

        isRunning = !isRunning;
        System.out.println("isRunning " + isRunning);
      } // else if ...
    }
  };
  private AnalogListener analogListener = new AnalogListener() {

    public void onAnalog(String name, float intensity, float tpf) {
        if (name.equals("Rotate")) {
         // 1. Reset results list.
         CollisionResults results = new CollisionResults();
         // 2. Aim the ray from camera location in camera direction.
         Ray ray = new Ray(cam.getLocation(), cam.getDirection());
         // 3. Collect intersections between ray and all nodes in results list.
         rootNode.collideWith(ray, results);
         // 4. Print the results so we see what is going on
         for (int i = 0; i < results.size(); i++) {
           // For each “hit”, we know distance, impact point, geometry.
           float dist = results.getCollision(i).getDistance();
           Vector3f pt = results.getCollision(i).getContactPoint();
           String target = results.getCollision(i).getGeometry().getName();
           System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
         }
         // 5. Use the results -- we rotate the selected geometry.
         if (results.size() > 0) {
           // The closest result is the target that the player picked:
           Geometry target = results.getClosestCollision().getGeometry();
           // Here comes the action:
           if(target.getName().equals("Red Box"))
             target.rotate(0, - intensity * tpf * 100, 0);
           else if(target.getName().equals("Blue Box"))
             target.rotate(0, intensity * tpf * 100, 0);
         }

        } // else if ...
      
    }
  };

  @Override
  /** (optional) Interact with update loop here */
  public void simpleUpdate(float tpf) {
  }

  @Override
  /** (optional) Advanced renderer/frameBuffer modifications */
  public void simpleRender(RenderManager rm) {
  }

  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
    Main2UserPicking app = new Main2UserPicking();
    app.start();

  }
}
