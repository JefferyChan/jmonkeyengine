package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.shadow.BasicShadowRenderer;

/**
 * The demo shows a simple way of adding dynamic shadows to a scene
 */
public class ShadowSimple extends SimpleApplication {

  private BasicShadowRenderer bsr;

  @Override
  public void simpleInitApp() {
    bsr = new BasicShadowRenderer(assetManager, 1024);
    bsr.setDirection(new Vector3f(.5f, -.5f, -.5f).normalizeLocal());
    viewPort.addProcessor(bsr);

    /** Add light */
    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(.5f, -.5f, -.5f));
    rootNode.addLight(dl);

    initScene(); // load some models, e.g. a town, tea pot
    viewPort.setBackgroundColor(ColorRGBA.Blue);
  }

  private void initScene() {
    /** Add some objects to the scene: A town */
    assetManager.registerLocator("town.zip", ZipLocator.class.getName());
    Spatial scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    rootNode.attachChild(scene_geo);
    scene_geo.setShadowMode(ShadowMode.CastAndReceive);

    /** Add some objects to the scene: a tea pot */
    Geometry tea_geo = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.j3o");
    tea_geo.scale(3);
    tea_geo.setLocalTranslation(32, 3, -24);
    rootNode.attachChild(tea_geo);
    tea_geo.setShadowMode(ShadowMode.CastAndReceive);

    /** configure some game properties depending on the scene*/
    flyCam.setMoveSpeed(30f);
    cam.setLocation(new Vector3f(0f,15f,-30f));
    cam.lookAt(tea_geo.getLocalTranslation(), Vector3f.UNIT_Y);
  }

  public static void main(String[] args) {
    ShadowSimple app = new ShadowSimple();
    app.start();

  }
}
