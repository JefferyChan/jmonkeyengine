package chapter02;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * Sample 2.1 
 * Basic jMonkeyEngine game template. This code renders a blue 3D cube.
 */
public class Main extends SimpleApplication {

    @Override
    /** Initialize the scene here: 
     *  Create Geometries and attach them to the rootNode. */
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);   // create box mesh
        Geometry geom = new Geometry("Box", b);    // create geometry from mesh

        Material mat = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);        // color the material blue
        geom.setMaterial(mat);                        // assign the material to geometry
        rootNode.attachChild(geom);                   // make geometry appear in scene
    }

    @Override
    /** (optional) Interact with update loop here. 
     *  This is where the action will happen in your game. */
    public void simpleUpdate(float tpf) {
      /* Nothing yet. */
    } 

    @Override
    /** (optional) Advanced renderer/frameBuffer modifications.  */
    public void simpleRender(RenderManager rm) {
      /* Not used in this example. */
    }

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        Main app = new Main();
        app.start();   
    }
}
