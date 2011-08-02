package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
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
    sun.setDirection(new Vector3f(1, -1.2f, -1.5f).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

    PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 3);
    pssmRenderer.setDirection(new Vector3f(1, -1.2f, -1.5f).normalizeLocal());
    pssmRenderer.setLambda(0.65f);
    pssmRenderer.setShadowIntensity(0.8f);
    pssmRenderer.setCompareMode(CompareMode.Software);
    pssmRenderer.setFilterMode(FilterMode.Bilinear);
    pssmRenderer.setShadowZExtend(10f);
    //pssmRenderer.displayDebug();
    viewPort.addProcessor(pssmRenderer);

  }

  // set default for applets
  public static void main(String[] args) {
    ShadowPSSM app = new ShadowPSSM();
    app.start();
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
    cam.setLocation(new Vector3f(0, 8f, 0));
    cam.lookAt(tea_geo.getWorldTranslation(), Vector3f.UNIT_Y);
  }
}
