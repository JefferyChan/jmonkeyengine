package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;

/**
 * A demo of a town with sunlight coming down in visible beams ("god beams")
 */
public class SunLight extends SimpleApplication {

  private FilterPostProcessor fpp;

  public static void main(String[] args) {
    SunLight app = new SunLight();
    app.start();
  }

  public void simpleInitApp() {
    flyCam.setMoveSpeed(30f);
    fpp = new FilterPostProcessor(assetManager);
    viewPort.addProcessor(fpp);

    Vector3f lightDirection = new Vector3f(-0.39f, -0.32f, -0.74f);
    Vector3f lightPos = lightDirection.multLocal(-3000);

    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(lightDirection);
    sun.setColor(ColorRGBA.White.clone().multLocal(2));
    rootNode.addLight(sun);

    LightScatteringFilter sunLight = new LightScatteringFilter(lightPos);
    fpp.addFilter(sunLight);

    initScene();
  }

  private void initScene() {
    // load sky
    rootNode.attachChild(SkyFactory.createSky(assetManager,
            "Textures/Sky/Bright/BrightSky.dds", false));
    // load scene content: a town
    assetManager.registerLocator("town.zip", ZipLocator.class.getName());
    Spatial scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    rootNode.attachChild(scene_geo);
  }
}
