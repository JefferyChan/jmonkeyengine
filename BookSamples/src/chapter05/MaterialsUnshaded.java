package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

/** 
 * How to give an object's surface a material and texture.
 * This class demonstrates opaque and transparent textures, 
 * and textures that let colors "bleed" through. 
 * No Phong illumination. */
public class MaterialsUnshaded extends SimpleApplication {

    public static void main(String[] args) {
        MaterialsUnshaded app = new MaterialsUnshaded();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(50);
        viewPort.setBackgroundColor(ColorRGBA.White);

        /** A simple textured sphere */
        Sphere sphere_mesh = new Sphere(16, 16, 1);
        Geometry sphere_geo = new Geometry("Unshaded textured sphere", sphere_mesh);
        Material sphere_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        sphere_mat.setTexture("ColorMap",
                assetManager.loadTexture("Interface/Monkey.png"));
        sphere_mat.setTexture("LightMap",
                assetManager.loadTexture("Interface/Monkey_light.png"));
        sphere_geo.setMaterial(sphere_mat);
        sphere_geo.move(-2f, 0f, 0f);
        sphere_geo.rotate(FastMath.DEG_TO_RAD * -90, FastMath.DEG_TO_RAD * 120, 0f);
        rootNode.attachChild(sphere_geo);

        /** This material turns the box into a stained glass window. 
         * The texture uses an alpha channel and is partially transparent. */
        Box window_mesh = new Box(Vector3f.ZERO, 1f, 1.4f, 0.01f);
        Geometry window_geo = new Geometry("a transparent window frame", window_mesh);
        window_geo.move(2f, 0f, 0f);
        Material window_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        window_mat.setTexture("ColorMap",
                assetManager.loadTexture("Textures/mucha-window.png"));
        window_geo.setMaterial(window_mat);
        window_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        window_geo.setQueueBucket(Bucket.Translucent);
        // window_mat.getAdditionalRenderState().setDepthTest(true);
        rootNode.attachChild(window_geo);

        /** A cylinder with its material color "bleeding" through the texture. 
         * The texture uses an alpha channel and is partially transparent. */
        Cylinder log_mesh = new Cylinder(32, 32, 1, 8, true);
        //log_mesh.scaleTextureCoordinates(new Vector2f(5,1));
        Geometry log_geo = new Geometry("A textured log with brown color", log_mesh);
        log_geo.move(0f, 0f, -2f);
        log_geo.rotate(0f, FastMath.DEG_TO_RAD * 90, 0f);
        Material log_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture log_tex = assetManager.loadTexture("Textures/Bark/bark_diffuse.png");
        log_mat.setTexture("ColorMap", log_tex);
        log_mat.setColor("Color", ColorRGBA.Orange);
        log_geo.setMaterial(log_mat);
        rootNode.attachChild(log_geo);
    }
}