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
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;

public class Fog extends SimpleApplication {

  private FilterPostProcessor fpp;
  private boolean enabled = true;
  private FogFilter fog;

  // set default for applets
  public static void main(String[] args) {
    Fog app = new Fog();
    app.start();
  }

  public void simpleInitApp() {
    fpp = new FilterPostProcessor(assetManager);
    //fpp.setNumSamples(4);
    fog = new FogFilter();
    fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
    fog.setFogDistance(155);
    fog.setFogDensity(2.0f);
    fpp.addFilter(fog);
    viewPort.addProcessor(fpp);


    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(1f, -1f, -1f));
    rootNode.addLight(dl);
    initScene();


    initInputs();
  }

  private void initScene() {
    // load sky
    rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    /**
     * Add some objects to the scene: A town
     */
    assetManager.registerLocator("assets/Scenes/town.zip", ZipLocator.class.getName());
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
    cam.lookAt(tea_geo.getLocalTranslation(), Vector3f.UNIT_Y);
  }

  private void initInputs() {
    inputManager.addMapping("toggle", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addMapping("DensityUp", new KeyTrigger(KeyInput.KEY_Y));
    inputManager.addMapping("DensityDown", new KeyTrigger(KeyInput.KEY_H));
    inputManager.addMapping("DistanceUp", new KeyTrigger(KeyInput.KEY_U));
    inputManager.addMapping("DistanceDown", new KeyTrigger(KeyInput.KEY_J));


    ActionListener acl = new ActionListener() {

      public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("toggle") && keyPressed) {
          if (enabled) {
            enabled = false;
            viewPort.removeProcessor(fpp);
          } else {
            enabled = true;
            viewPort.addProcessor(fpp);
          }
        }

      }
    };

    AnalogListener anl = new AnalogListener() {

      public void onAnalog(String name, float isPressed, float tpf) {
        if (name.equals("DensityUp")) {
          fog.setFogDensity(fog.getFogDensity() + 0.01f);
          System.out.println("Fog density : " + fog.getFogDensity());
        }
        if (name.equals("DensityDown")) {
          fog.setFogDensity(fog.getFogDensity() - 0.10f);
          System.out.println("Fog density : " + fog.getFogDensity());
        }
        if (name.equals("DistanceUp")) {
          fog.setFogDistance(fog.getFogDistance() + 1f);
          System.out.println("Fog Distance : " + fog.getFogDistance());
        }
        if (name.equals("DistanceDown")) {
          fog.setFogDistance(fog.getFogDistance() - 1f);
          System.out.println("Fog Distance : " + fog.getFogDistance());
        }

      }
    };

    inputManager.addListener(acl, "toggle");
    inputManager.addListener(anl, "DensityUp", "DensityDown", "DistanceUp", "DistanceDown");

  }
}
