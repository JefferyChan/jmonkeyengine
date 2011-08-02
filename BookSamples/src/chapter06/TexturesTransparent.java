package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/** 
 * How to give an object's surface a material and texture.
 * This class demonstrates opaque and transparent textures, 
 * and textures that let colors "bleed" through.
 * Uses Phong illumination. */
public class TexturesTransparent extends SimpleApplication {

    public static void main(String[] args) {
        TexturesTransparent app = new TexturesTransparent();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        /** A simple textured cube */
        Box boxshape1 = new Box(new Vector3f(-1f, 0f, 0f), 0.5f, 0.5f, 0.5f);
        Geometry cube = new Geometry("Monkey textured box", boxshape1);
        Material mat_opq = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat_opq.setTexture("DiffuseMap", 
                assetManager.loadTexture("Interface/Monkey.png"));
        cube.setMaterial(mat_opq);
        rootNode.attachChild(cube);

        /** This material turns the box into a stained glass window. 
         * The texture has an alpha channel and is partially transparent. */
        Box boxshape3 = new Box(new Vector3f(1f, 0f, 0f), 1f, 1.4f, 0.01f);
        Geometry window_frame = new Geometry("stained glass window", boxshape3);
        Material mat_tt = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat_tt.setTexture("DiffuseMap",
                assetManager.loadTexture("Textures/mucha-window.png"));
        window_frame.setMaterial(mat_tt);
        rootNode.attachChild(window_frame);

        /** A box with its material color "bleeding" through. The texture has 
         * an alpha channel and is partially transparent. */
        Box boxshape4 = new Box(new Vector3f(0f, 0f, -2f), 3f, 1f, 1f);
        Geometry cube_bleed = new Geometry("Bleed-through color", boxshape4);
        Material mat_tl = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat_tl.setBoolean("UseMaterialColors", true);
        mat_tl.setTexture("DiffuseMap", 
                assetManager.loadTexture("Textures/bark.png"));
        mat_tl.setColor("Diffuse", new ColorRGBA(1f, 0.8f, 0.7f, 1f)); // e.g. brown=(1.0,0.9,0.8)
        cube_bleed.setMaterial(mat_tl);
        rootNode.attachChild(cube_bleed);

        /** Must add a light to make the lit object visible! */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

    }
}