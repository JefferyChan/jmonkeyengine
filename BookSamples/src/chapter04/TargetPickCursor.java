package chapter04;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This example demonstrates responding to user input, and target picking 
 * with a visible mouse cursor.
 * We added more cubes and we use ray casting to select which cube rotates.
 */
public class TargetPickCursor extends SimpleApplication {

  private Geometry geom, geom2;
  private Trigger trigger_rotate = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);

  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    /** register input mappings to input manager */
    inputManager.addMapping("Rotate", trigger_rotate);
    inputManager.addListener(analogListener, new String[]{"Rotate"});

    inputManager.setCursorVisible(true); // make mouse cursor visible

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
  private AnalogListener analogListener = new AnalogListener() {

    public void onAnalog(String name, float intensity, float tpf) {
      if (name.equals("Rotate")) {
        // Reset results list.
        CollisionResults results = new CollisionResults();
        // Convert screen click to 3d position
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(
                new Vector2f(click2d.getX(), click2d.getY()), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(
                new Vector2f(click2d.getX(), click2d.getY()), 1f).subtractLocal(click3d);
        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(click3d, dir);
        // Collect intersections between ray and all nodes in results list.
        rootNode.collideWith(ray, results);
        // (Print the results so we see what is going on:)
        for (int i = 0; i < results.size(); i++) {
          // (For each “hit”, we know distance, impact point, geometry.)
          float dist = results.getCollision(i).getDistance();
          Vector3f pt = results.getCollision(i).getContactPoint();
          String target = results.getCollision(i).getGeometry().getName();
          System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
        }
        // Use the results -- we rotate the selected geometry.
        if (results.size() > 0) {
          // The closest result is the target that the player picked:
          Geometry target = results.getClosestCollision().getGeometry();
          // Here comes the action:
          if (target.getName().equals("Red Box")) {
            target.rotate(0, -intensity, 0);
          } else if (target.getName().equals("Blue Box")) {
            target.rotate(0, intensity, 0);
          }
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
    TargetPickCursor app = new TargetPickCursor();
    app.start();

  }
}
