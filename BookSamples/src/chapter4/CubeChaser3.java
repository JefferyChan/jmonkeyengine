package chapter4;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This demo uses the a) a Control, and b) an AppState to change the locations 
 * of cubes that the player looks at. 
 * The control is only attached to two white cubes, only they are affected. 
 * The AppState demo affects all cubes.
 * Note that the simpleUpdate() method is empty -- all updates happen in the Control/AppState.
 */
public class CubeChaser3 extends SimpleApplication {

  private Geometry cubeA, cubeB;
    
  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    makeCubes(40);

    // create two white cubes
    Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);
    cubeA = new Geometry("Box", mesh);
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.White);
    cubeA.setMaterial(mat);
    cubeA.setLocalTranslation(-1,-1,-1);
    rootNode.attachChild(cubeA);
    
    cubeB = new Geometry("Box", mesh);
    cubeB.setMaterial(mat);
    cubeB.setLocalTranslation(1,1,1);
    rootNode.attachChild(cubeB);
    
    /** Demo 1: Add the CubeChaseControl to two white cubes */
    CubeChaserControl c = new CubeChaserControl(cam, rootNode);
    cubeA.addControl(c);
    cubeB.addControl(c);
    
    /** Demo 2: Activate a Chaser AppState on all cubes. */
    //MyAppState state = new MyAppState(cam,rootNode);
    //stateManager.attach(state);

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
    
  }

  @Override
  /** (optional) Advanced renderer/frameBuffer modifications */
  public void simpleRender(RenderManager rm) {
  }

  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
    CubeChaser3 app = new CubeChaser3();
    app.start();

  }
}
