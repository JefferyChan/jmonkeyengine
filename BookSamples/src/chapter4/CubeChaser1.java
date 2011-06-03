package solutions.chapter4;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This demo uses the simpleUpdate() loop to change the location 
 * of one cube if the camera is close to it.
 */
public class CubeChaser1 extends SimpleApplication {

  private Geometry cube;
  private Vector3f g = new Vector3f(0, 0, 0);
  private Vector3f v = new Vector3f(0, 0, 0);

  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    makeCubes(40);

    Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);
    cube = new Geometry("Box", mesh);
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.White);
    cube.setMaterial(mat);
    rootNode.attachChild(cube);
  }

  private void makeCubes(int max) {
    // fill the space with some random colored cubes
    for (int i = 0; i < max; i++) {
      Vector3f loc = new Vector3f(
              FastMath.nextRandomInt(-10, 10),
              FastMath.nextRandomInt(-10, 10),
              FastMath.nextRandomInt(-10, 10));
      Box mesh = new Box(loc, .5f, .5f, .5f);
      Geometry geom = new Geometry("Box", mesh);
      Material mat = new Material(assetManager,
              "Common/MatDefs/Misc/Unshaded.j3md");
      mat.setColor("Color", ColorRGBA.randomColor());
      geom.setMaterial(mat);
      rootNode.attachChild(geom);
    }
  }

  @Override
  /** Interact with update loop here */
  public void simpleUpdate(float tpf) {
    // if camera closer than 10...
    if (cam.getLocation().distance(cube.getLocalTranslation()) < 10) {
      // ... move the cube in the direction that camera is facing
      cube.setLocalTranslation(cube.getLocalTranslation().addLocal(cam.getDirection()));
    }
  }

  @Override
  /** (optional) Advanced renderer/frameBuffer modifications */
  public void simpleRender(RenderManager rm) {
  }

  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
    CubeChaser1 app = new CubeChaser1();
    app.start();

  }
}
