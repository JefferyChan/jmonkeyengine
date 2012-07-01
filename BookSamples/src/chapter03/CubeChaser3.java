package chapter03;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * A Control changes the locations of the cubes that the player looks at. 
 * The control is only attached to some cubes, only they are affected. 
 * Note that the simpleUpdate() method is empty, and
 * all updates happen in the CubeChaserControl's update loop.
 */
public class CubeChaser3 extends SimpleApplication {

    private static Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);
    
    @Override
    /** initialize the scene here */
    public void simpleInitApp() {
        flyCam.setMoveSpeed(100f);
        makeCubes(40);
    }

    private void makeCubes(int number) {
        for (int i = 0; i < number; i++) {
            // randomize 3D coordinates
            Vector3f loc = new Vector3f(
                    FastMath.nextRandomInt(-20, 20),
                    FastMath.nextRandomInt(-20, 20),
                    FastMath.nextRandomInt(-20, 20));
            Geometry geom = myBox("Cube" + i, loc, ColorRGBA.randomColor());
            // make random cubes chasable
            if (FastMath.nextRandomInt(1, 4) == 4) {
                geom.addControl(new CubeChaser3Control(cam, rootNode));
            }
            rootNode.attachChild(geom);
        }
    }

    public Geometry myBox(String name, Vector3f loc, ColorRGBA color) {
        Geometry geom = new Geometry(name, mesh);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(loc);
        return geom;
    }

    @Override
    /** Interact with update loop here */
    public void simpleUpdate(float tpf) {
    }

    @Override
    /** (optional) Advanced renderer/frameBuffer modifications */
    public void simpleRender(RenderManager rm) {
    }

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        CubeChaser3 app = new CubeChaser3();
        app.start();

    }
}
