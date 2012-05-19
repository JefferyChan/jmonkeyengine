package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/**
 * A town with fog.
 */
public class Fog extends SimpleApplication {

  private FilterPostProcessor fpp;
  private FogFilter fog;

  public static void main(String[] args) {
    Fog app = new Fog();
    app.start();
  }

  public void simpleInitApp() {
    // activate fog
    fpp = new FilterPostProcessor(assetManager);
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
  }

  private void initScene() {
    flyCam.setMoveSpeed(30f);
    viewPort.setBackgroundColor(ColorRGBA.Cyan);

    // Add some objects to the scene: A town
    assetManager.registerLocator("town.zip", ZipLocator.class);
    Spatial scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    rootNode.attachChild(scene_geo);

    // Add some objects to the scene: a tea pot
    Geometry tea_geo = (Geometry) assetManager.loadModel(
            "Models/Teapot/Teapot.j3o");
    Material mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Diffuse", ColorRGBA.Pink);
    tea_geo.setMaterial(mat);

    tea_geo.scale(3);
    tea_geo.setLocalTranslation(32, 3, -24);
    rootNode.attachChild(tea_geo);
    cam.lookAt(tea_geo.getLocalTranslation(), Vector3f.UNIT_Y);
  }
}
