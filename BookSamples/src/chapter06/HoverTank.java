package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * Loading a multimapped model with Phong illumination.
 */
public class HoverTank extends SimpleApplication {

    @Override
    /** initialize the scene here */
    public void simpleInitApp() {
        Node tank = (Node) assetManager.loadModel("Models/HoverTank/Tank.mesh.xml");
        Material mat = new Material(
                assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap",
                assetManager.loadTexture("Models/HoverTank/tank_diffuse.jpg"));
        mat.setTexture("NormalMap",
                assetManager.loadTexture("Models/HoverTank/tank_normals.png"));
        mat.setTexture("SpecularMap",
                assetManager.loadTexture("Models/HoverTank/tank_specular.jpg"));
        mat.setTexture("GlowMap",
                assetManager.loadTexture("Models/HoverTank/tank_glow_map.jpg"));
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setColor("GlowColor", ColorRGBA.White);
        mat.setFloat("Shininess", 8f);
        rootNode.attachChild(tank);

//        Material mat2 = (Material) assetManager.loadAsset( "Materials/Cool.j3m");
//        tank.setMaterial(mat2);
//        
//        DiffuseMap  : Models/HoverTank/tank_diffuse.jpg
//        NormalMap   : Models/HoverTank/tank_normals.png
//        SpecularMap : Models/HoverTank/tank_specular.jpg
//        GlowMap     : Models/HoverTank/tank_glow_map.jpg
//        UseMaterialColors : true
//        Ambient  : 0.0 0.0 0.0 1.0
//        Diffuse  : 1.0 1.0 1.0 1.0
//        Specular : 1.0 1.0 1.0 1.0
//        Shininess : 8

        /** A white, directional light source */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        HoverTank app = new HoverTank();
        app.start();

    }
}
