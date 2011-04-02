package chapter3;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * Basic jMonkeyEngine game template.
 */
public class Main extends SimpleApplication {

    @Override
    /** initialize the scene here */
    public void simpleInitApp() {
        Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);   // create box mesh
        Geometry geom = new Geometry("Box", mesh);    // create object from mesh

        Material mat = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple material
        mat.setColor("m_Color", ColorRGBA.Blue);      // color the material blue
        geom.setMaterial(mat);                        // give object the blue material
        rootNode.attachChild(geom);                   // make object appear in scene
    }

    @Override
    /** (optional) Interact with update loop here */
    public void simpleUpdate(float tpf) {} 

    @Override
    /** (optional) Advanced renderer/frameBuffer modifications */
    public void simpleRender(RenderManager rm) {}

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}
