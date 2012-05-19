package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMeshKey;

/**  */
public class LoadScene extends SimpleApplication {
  
  public static void main(String[] args) {
    LoadScene app = new LoadScene();
    app.start();
  }
  
  @Override
  public void simpleInitApp() {
    viewPort.setBackgroundColor(ColorRGBA.LightGray);
    flyCam.setMoveSpeed(50f);
    
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1f, -1f, -1f));
    rootNode.addLight(sun);
    
    // load a zipped level from an online source
    assetManager.registerLocator(
    "http://jmonkeyengine.googlecode.com/files/quake3level.zip", 
    HttpZipLocator.class);
    MaterialList matList = (MaterialList) assetManager.loadAsset("Scene.material");
    OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
    Node gameLevel = (Node) assetManager.loadAsset(key);
    gameLevel.setLocalScale(0.3f);
    rootNode.attachChild(gameLevel);
    
//    // load sky
//    rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
//    // load a zipped level from the project directory
//    assetManager.registerLocator("town.zip", ZipLocator.class);
//    Spatial sceneModel = assetManager.loadModel("main.scene");
//    sceneModel.setLocalScale(2f);
//    rootNode.attachChild(sceneModel);
    
  }
}
