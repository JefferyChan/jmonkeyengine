package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;

public class ShadowPSSM extends SimpleApplication {

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50);

    initScene();

    AmbientLight al = new AmbientLight();
    al.setColor(new ColorRGBA(ColorRGBA.White));
    rootNode.addLight(al);
    
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(.5f,-.5f,-.5f).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

    PssmShadowRenderer pssm = new PssmShadowRenderer(assetManager, 1024, 2);
    pssm = new PssmShadowRenderer(assetManager, 1024, 2);
    pssm.setDirection(new Vector3f(.5f, -.5f, -.5f).normalizeLocal());
    pssm.setLambda(0.55f);
    pssm.setShadowIntensity(0.6f);
    pssm.setCompareMode(CompareMode.Hardware);
    pssm.setFilterMode(FilterMode.PCF4);
    //pssm.displayDebug();
    viewPort.addProcessor(pssm);
    rootNode.setShadowMode(ShadowMode.Off); // reset first

  }

  // set default for applets
  public static void main(String[] args) {
    ShadowPSSM app = new ShadowPSSM();
    app.start();
  }

  private void initScene() {
    // load sky
    rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    
    // Add some objects to the scene: A town
    assetManager.registerLocator("town.zip", ZipLocator.class);
    Spatial scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    scene_geo.setShadowMode(ShadowMode.CastAndReceive);
    rootNode.attachChild(scene_geo);

    // Add some objects to the scene: a tea pot
    Geometry tea_geo = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.j3o");
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Diffuse", ColorRGBA.Pink);
    tea_geo.setMaterial(mat);
    tea_geo.scale(3);
    tea_geo.setLocalTranslation(32, 3, -24);
    rootNode.attachChild(tea_geo);
    tea_geo.setShadowMode(ShadowMode.CastAndReceive);
    cam.setLocation(new Vector3f(0, 8f, 0));
    cam.lookAt(tea_geo.getWorldTranslation(), Vector3f.UNIT_Y);
  }
}
