package chapter02;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Sample 2.4
 * This example shows a blue and a yellow cube. The cubes
 * are both rotated.
 */
public class Rotate extends SimpleApplication {

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

        Vector3f v = new Vector3f(2.0f, 1.0f, -3.0f);

        Box b2 = new Box(Vector3f.ZERO, 1, 1, 1);    // create box mesh
        Geometry geom2 = new Geometry("Box", b2);    // create geometry from mesh

        Material mat2 = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat2.setColor("Color", ColorRGBA.Yellow);      // color the material YELLOW
        geom2.setMaterial(mat2);                       // assign the material to geometry
        geom2.setLocalTranslation(v);                  // position it

        float r = FastMath.DEG_TO_RAD * 45f;

        //geom2.rotate(r, 0.0f, 0.0f);                      // test relative rotation
        //geom.rotate(0.0f, r, 0.0f);                       // test relative rotation

        /* test absolute rotation */
//        Quaternion roll045 = new Quaternion();
//        roll045.fromAngleAxis(45 * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
//        geom2.setLocalRotation(roll045);

        /* test absolute rotation with slerp interpolation */
//        Quaternion q1 = new Quaternion();
//        q1.fromAngleAxis(50 * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
//        Quaternion q2 = new Quaternion();
//        q2.fromAngleAxis(40 * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
//        Quaternion q3 = new Quaternion();
//        q3.slerp(q1, q2, 0.5f);
//        geom2.setLocalRotation(q3);

        rootNode.attachChild(geom2);                   // make geometry appear in scene
        
        Node pivot = new Node("pivot node");
        pivot.attachChild(geom);
        pivot.attachChild(geom2);
        pivot.rotate(0, 0, FastMath.DEG_TO_RAD * 45);
        rootNode.attachChild(pivot);

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
        Rotate app = new Rotate();
        app.start();
    }
}
