package chapter8;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;

public class SSAO extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(68.45442f, 8.235511f, 7.9676695f));
        cam.setRotation(new Quaternion(0.046916496f, -0.69500375f, 0.045538206f, 0.7160271f));

        flyCam.setMoveSpeed(50);

        initScene();

        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(1.8f, 1.8f, 1.8f, 1.0f));
        rootNode.addLight(al);
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        SSAOFilter ssaoFilter = new SSAOFilter(12.940201f, 43.928635f, 0.32999992f, 0.6059958f);
        fpp.addFilter(ssaoFilter);
        viewPort.addProcessor(fpp);
    }

  // set default for applets
  public static void main(String[] args) {
    SSAO app = new SSAO();
    app.start();
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

 
}
