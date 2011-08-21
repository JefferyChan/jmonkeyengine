package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;

public class SunLight extends SimpleApplication {

  private FilterPostProcessor fpp;

  public static void main(String[] args) {
    SunLight app = new SunLight();
    app.start();
  }

  public void simpleInitApp() {
    fpp = new FilterPostProcessor(assetManager);

    Vector3f lightDirection = new Vector3f(-0.39f, -0.32f, -0.74f);

    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(lightDirection);
    sun.setColor(ColorRGBA.White.clone().multLocal(2));
    rootNode.addLight(sun);

    Vector3f lightPos = lightDirection.multLocal(-3000);
    LightScatteringFilter sunLight = new LightScatteringFilter(lightPos);
    fpp.addFilter(sunLight);
    viewPort.addProcessor(fpp);

    initScene();
  }

  private void initScene() {
    // load sky
    rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    /**
     * Add some objects to the scene: A town
     */
    assetManager.registerLocator("town.zip", ZipLocator.class.getName());
    Spatial scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    rootNode.attachChild(scene_geo);

    /**
     * Add some objects to the scene: a tea pot
     */
    Geometry tea_geo = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.j3o");
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Diffuse", ColorRGBA.Pink);
    tea_geo.setMaterial(mat);

    tea_geo.scale(3);
    tea_geo.setLocalTranslation(32, 3, -24);
    rootNode.attachChild(tea_geo);

    /**
     * configure some game properties depending on the scene
     */
    flyCam.setMoveSpeed(30f);
    //cam.lookAt(tea_geo.getLocalTranslation(), Vector3f.UNIT_Y);
  }
}
