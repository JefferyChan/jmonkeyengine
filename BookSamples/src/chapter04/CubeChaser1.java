package chapter04;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This demo uses the simpleUpdate() loop to change the location 
 * of one white cube if the camera is close to it.
 */
public class CubeChaser1 extends SimpleApplication {

  private Geometry myCube;

  /** Fill space with random static cubes. You will notice
   *  myCube moves in relation to these other non-moving ones. */
  private void makeCubes(int number) {
    for (int i = 0; i < number; i++) {                // ... A loop that spawns cubes:
      Vector3f loc = new Vector3f(
              FastMath.nextRandomInt(-10, 10),
              FastMath.nextRandomInt(-10, 10),
              FastMath.nextRandomInt(-10, 10));       // randomize 3D coordinates
      Box mesh = new Box(Vector3f.ZERO, .5f, .5f, .5f);         // create cube shape
      Geometry geom = new Geometry("white cube", mesh);      // create geometry from shape
      geom.setLocalTranslation(loc);
      Material mat = new Material(assetManager, 
              "Common/MatDefs/Misc/Unshaded.j3md");   // create a material
      mat.setColor("Color", ColorRGBA.randomColor()); // give material a random color
      geom.setMaterial(mat);                          // apply material to geometry
      rootNode.attachChild(geom);                     // add geometry to the scene
    }                                                 // ... repeat.
  }

  @Override
  /** initialize the scene here. */
  public void simpleInitApp() {
    // Create one white cube -- we want to chase this cube.
    Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);   // create cube shape
    myCube = new Geometry("Box", mesh);           // create geometry from shape
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md"); // create a material
    mat.setColor("Color", ColorRGBA.White);       // make material white
    myCube.setMaterial(mat);                      // apply white material to geometry
    rootNode.attachChild(myCube);                 // add geometry to the scene

    makeCubes(40); // Add some more random colorful cubes as background.
  }

  @Override
  /** This update loop controls the game and moves the cube. */
  public void simpleUpdate(float tpf) {
    // If camera is closer than 10 units to myCube...
    if (cam.getLocation().distance(myCube.getLocalTranslation()) < 10) {
      // ... then move myCube away, in the direction that camera is facing.
      myCube.setLocalTranslation(myCube.getLocalTranslation().addLocal(
              cam.getDirection().normalizeLocal()));
    }
  }

  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
    CubeChaser1 app = new CubeChaser1();
    app.start();

  }
}
