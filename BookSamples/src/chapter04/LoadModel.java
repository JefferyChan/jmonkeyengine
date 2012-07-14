package chapter04;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;

/**
 * This example demonstrates loading a 3D model.
 * Various formats are supported, OBJ, Ogre, and binary .j3o.
 */
public class LoadModel extends SimpleApplication {

    @Override
    /** initialize the scene here */
    public void simpleInitApp() {

        //Spatial mymodel = assetManager.loadModel("Textures/MyModel/mymodel.obj");      // OBJ or
        //Spatial mymodel = assetManager.loadModel("Textures/MyModel/mymodel.mesh.xml"); // Ogre or
        Spatial mymodel = assetManager.loadModel("Models/MyModel/mymodel.j3o");   // j3o

        rootNode.attachChild(mymodel);

        /** A white, directional light source */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
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
