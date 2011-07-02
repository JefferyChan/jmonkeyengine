package chapter5;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
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
  
  private AnimChannel channel;
  private AnimControl control;
  private Node player;
  
  public static void main(String[] args) {
    LoadScene app = new LoadScene();
    app.start();
  }
  
  @Override
  public void simpleInitApp() {
    viewPort.setBackgroundColor(ColorRGBA.LightGray);
    
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
    
    assetManager.registerLocator(
    "http://jmonkeyengine.googlecode.com/files/quake3level.zip", 
    HttpZipLocator.class.getName());
    MaterialList matList = (MaterialList) assetManager.loadAsset("Scene.material");
    OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
    Node gameLevel = (Node) assetManager.loadAsset(key);
    gameLevel.setLocalScale(0.1f);
    rootNode.attachChild(gameLevel);
    
//    assetManager.registerLocator("assets/Models/town.zip",
//            ZipLocator.class.getName());
//    assetManager.registerLocator("town.zip", ZipLocator.class.getName());
//    Spatial sceneModel = assetManager.loadModel("main.scene");
//    sceneModel.setLocalScale(2f);
//    rootNode.attachChild(sceneModel);
    
  }
}
