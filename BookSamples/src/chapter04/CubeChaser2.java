package chapter04;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This demo uses the simpleUpdate() loop to change the location 
 * of any cube the player looks at. When the player is closer than
 * 10 wu, the cube moves further away.
 */
public class CubeChaser2 extends SimpleApplication {

    private Geometry cube;
    private Ray ray = new Ray();

    @Override
    /** initialize the scene here */
    public void simpleInitApp() {


        makeCubes(40);

        Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);
        cube = new Geometry("white cube", mesh);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        cube.setMaterial(mat);
        rootNode.attachChild(cube);
    }

    private void makeCubes(int max) {
        // fill the space with some random colored cubes
        for (int i = 0; i < max; i++) {
            Vector3f loc = new Vector3f(
                    FastMath.nextRandomInt(-10, 10),
                    FastMath.nextRandomInt(-10, 10),
                    FastMath.nextRandomInt(-10, 10));
            Box mesh = new Box(Vector3f.ZERO, .5f, .5f, .5f);
            Geometry geom = new Geometry("Box"+i, mesh);
            geom.setLocalTranslation(loc);
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.randomColor());
            geom.setMaterial(mat);
            rootNode.attachChild(geom);
        }
    }

    @Override
    /** Interact with update loop here */
    public void simpleUpdate(float tpf) {
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from camera location in camera direction.
        ray.setOrigin(cam.getLocation());
        ray.setDirection(cam.getDirection());
        // 3. Collect intersections between ray and all nodes in results list.
        rootNode.collideWith(ray, results);
        // 4. Use the result
        if (results.size() > 0) {
            // The closest result is the target that the player picked:
            Geometry target = results.getClosestCollision().getGeometry();
            // if camera closer than 10...
            if (cam.getLocation().distance(target.getLocalTranslation()) < 10) {
                // ... move the cube in the direction that camera is facing
                target.setLocalTranslation(target.getLocalTranslation().
                        addLocal(cam.getDirection().normalizeLocal()));
            }
        }
    }

    @Override
    /** (optional) Advanced renderer/frameBuffer modifications */
    public void simpleRender(RenderManager rm) {
    }

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        CubeChaser2 app = new CubeChaser2();
        app.start();

    }
}
