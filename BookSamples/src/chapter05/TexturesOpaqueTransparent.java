package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

/** 
 * How to give an object's surface a material and texture.
 * This class demonstrates opaque and transparent textures, 
 * and textures that let colors "bleed" through.
 * Uses Phong illumination. */
public class TexturesOpaqueTransparent extends SimpleApplication {

    public static void main(String[] args) {
        TexturesOpaqueTransparent app = new TexturesOpaqueTransparent();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.White);
        flyCam.setMoveSpeed(50);

        /** A simple textured sphere */
        Sphere sphere_mesh = new Sphere(16, 16, 1);
        Geometry sphere_geo = new Geometry("lit textured sphere", sphere_mesh);
        Material sphere_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        sphere_mat.setTexture("AlphaMap",
                assetManager.loadTexture("Interface/Monkey.png")); 
        sphere_mat.setTexture("DiffuseMap",
                assetManager.loadTexture("Interface/Monkey.png")); 
        sphere_mat.setBoolean("UseMaterialColors", true);
        sphere_mat.setColor("Diffuse", ColorRGBA.Gray);
        sphere_mat.setColor("Ambient", ColorRGBA.Gray); 
        sphere_mat.getAdditionalRenderState().setAlphaTest(true);
        sphere_mat.getAdditionalRenderState().setAlphaFallOff(.0005f);
        sphere_geo.setQueueBucket(Bucket.Transparent);
        sphere_geo.setMaterial(sphere_mat);
        sphere_geo.move(-2f, 0f, 0f);
        sphere_geo.rotate(FastMath.DEG_TO_RAD * -90, FastMath.DEG_TO_RAD * 120, 0f);
        rootNode.attachChild(sphere_geo);

        /** This material turns the box into a stained glass window. 
         * The texture has an alpha channel and is partially transparent. */
        Box window_mesh = new Box(new Vector3f(1f, 0f, 0f), 1f, 1.4f, 0.01f);
        Geometry window_geo = new Geometry("stained glass window", window_mesh);
        Material window_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        window_mat.setTexture("DiffuseMap",
                assetManager.loadTexture("Textures/mucha-window.png"));
        window_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        window_geo.setMaterial(window_mat);
        window_geo.setQueueBucket(Bucket.Transparent);
        window_geo.setMaterial(window_mat);
        rootNode.attachChild(window_geo);

        /** A box with its material color "bleeding" through. The texture has 
         * an alpha channel and is partially transparent. */
        Cylinder log_mesh = new Cylinder(32, 32, 1, 8, true);
        TangentBinormalGenerator.generate(log_mesh);
        Geometry log_geo = new Geometry("Bleed-through color", log_mesh);
        Material log_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        log_mat.setTexture("DiffuseMap",
                assetManager.loadTexture("Textures/Bark/bark_diffuse.png"));
        log_mat.setTexture("NormalMap",
                assetManager.loadTexture("Textures/Bark/bark_normal.png"));
        log_mat.setBoolean("UseMaterialColors", true);
        log_mat.setColor("Diffuse", ColorRGBA.Orange);
        log_mat.setColor("Ambient", ColorRGBA.Gray); 
        log_mat.setBoolean("UseAlpha",true);
        log_geo.setMaterial(log_mat);
        log_geo.move(0f, 0f, -2f);
        log_geo.rotate(0f, FastMath.DEG_TO_RAD * 90, 0f);
        rootNode.attachChild(log_geo);

        /** Must add a light to make the lit object visible! */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
    }
}