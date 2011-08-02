package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;

/**
 * This example demonstrates loading a 3D model.
 */
public class LoadModel extends SimpleApplication {

  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    //Spatial mymodel = assetManager.loadModel("Textures/MyModel/MyModel.obj");
    //Spatial mymodel = assetManager.loadModel("Textures/MyModel/MyModel.mesh.xml");
    Spatial mymodel = assetManager.loadModel("Models/MyModel/MyModel.j3o");
    
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/ShowNormals.j3md"); // create a simple material
    mymodel.setMaterial(mat);                     // give object the blue wireframe
    
    rootNode.attachChild(mymodel);
    
        /** A white ambient light source. */ 
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White);
    rootNode.addLight(ambient);
  }

  @Override
  /** (optional) Interact with update loop here */
  public void simpleUpdate(float tpf) {
  }

  @Override
  /** (optional) Advanced renderer/frameBuffer modifications */
  public void simpleRender(RenderManager rm) {
  }

  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
    LoadModel app = new LoadModel();
    app.start();

  }
}
