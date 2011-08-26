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
 * A demo of the basic water simulation, with reflections but no underwater effects.
 * @author normenhansen
 */
public class WaterSimple extends SimpleApplication {
  Node reflectedScene;

  public static void main(String[] args) {
    WaterSimple app = new WaterSimple();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50f);
    initScene();
    initWater();
  }

  private void initScene() {
    // mainscene has everything that reflects in water, 
    // including sky and light, but not the water itself.
    reflectedScene = new Node("Scene");
    rootNode.attachChild(reflectedScene);
    // Add sky
    reflectedScene.attachChild(SkyFactory.createSky(assetManager,
            "Textures/Sky/Bright/BrightSky.dds", false));
    // Add some scene content, e.g. an unshaded box
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Monkey.png"));
    Box b = new Box(2, 2, 2);
    Geometry box_geo = new Geometry("Box", b);
    box_geo.setMaterial(mat);
    reflectedScene.attachChild(box_geo);

    rootNode.attachChild(reflectedScene);
  }

  private void initWater() {
    // create water post-processor
    SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
    waterProcessor.setReflectionScene(reflectedScene); // !
    waterProcessor.setLightPosition(new Vector3f(33, 12, -29));
    viewPort.addProcessor(waterProcessor);
    // create water geometry with material
    Spatial waterPlane = waterProcessor.createWaterGeometry(100, 100);
    waterPlane.setMaterial(waterProcessor.getMaterial());
    waterPlane.setLocalTranslation(-50, 0, 50);
    rootNode.attachChild(waterPlane);
  }

  @Override
  public void simpleUpdate(float tpf) { }
}
