package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;

/**
 *
 * @author normenhansen
 */
public class SimpleWater extends SimpleApplication {

  Material mat;
  Spatial waterPlane;
  SimpleWaterProcessor waterProcessor;
  Node sceneNode;
  private Vector3f lightPos = new Vector3f(33, 12, -29);

  public static void main(String[] args) {
    SimpleWater app = new SimpleWater();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50f);
    cam.setLocation(new Vector3f(0, 10, 10));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

    initScene();

    waterProcessor = new SimpleWaterProcessor(assetManager);
    waterProcessor.setReflectionScene(sceneNode); // !
    waterProcessor.setDebug(true);
    waterProcessor.setLightPosition(lightPos);
    viewPort.addProcessor(waterProcessor);

    //create water quad
    waterPlane = waterProcessor.createWaterGeometry(100, 100);
    waterPlane.setMaterial(waterProcessor.getMaterial());
    waterPlane.setLocalTranslation(-50, 0, 50);
    rootNode.attachChild(waterPlane);
  }

  private void initScene() {
    sceneNode = new Node("Scene");
    
    mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Monkey.png"));
    Box b = new Box(2, 2, 2);
    Geometry geom = new Geometry("Box", b);
    geom.setMaterial(mat);
    sceneNode.attachChild(geom);

    sceneNode.attachChild(SkyFactory.createSky(assetManager,
            "Textures/Sky/Bright/BrightSky.dds", false));
    
    rootNode.attachChild(sceneNode);
  }

  @Override
  public void simpleUpdate(float tpf) { }
}
