package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Node;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.TangentBinormalGenerator;

/**
 * Loading a multimapped model with UV textures and Phong illumination.
 */
public class HoverTank extends SimpleApplication {

    private SpotLight spot;

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);

        Node tank = (Node) assetManager.loadModel(
                "Models/HoverTank/Tank.mesh.xml");

        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        TextureKey tank_diffuse = new TextureKey("Models/HoverTank/tank_diffuse.jpg", false);
        mat.setTexture("DiffuseMap", assetManager.loadTexture(tank_diffuse));

        TangentBinormalGenerator.generate(tank);
        TextureKey tank_normal = new TextureKey("Models/HoverTank/tank_normals.png", false);
        mat.setTexture("NormalMap", assetManager.loadTexture(tank_normal));

        TextureKey tank_specular = new TextureKey("Models/HoverTank/tank_specular.jpg", false);
        mat.setTexture("SpecularMap", assetManager.loadTexture(tank_specular));
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 120f);

        TextureKey tank_glow = new TextureKey("Models/HoverTank/tank_glow_map.jpg", false);
        mat.setTexture("GlowMap", assetManager.loadTexture(tank_glow));
        mat.setColor("GlowColor", ColorRGBA.White);

        Material mat2 = assetManager.loadMaterial("Materials/tank.j3m");

        tank.setMaterial(mat);
        rootNode.attachChild(tank);

        /** A cone-shaped spotlight with location, direction, range */
        spot = new SpotLight();
        spot = new SpotLight();
        spot.setSpotRange(100);
        spot.setSpotOuterAngle(20 * FastMath.DEG_TO_RAD);
        spot.setSpotInnerAngle(15 * FastMath.DEG_TO_RAD);
        spot.setDirection(cam.getDirection());
        spot.setPosition(cam.getLocation());
        rootNode.addLight(spot);

//        DiffuseMap  : Models/HoverTank/tank_diffuse.jpg
//        NormalMap   : Models/HoverTank/tank_normals.png
//        SpecularMap : Models/HoverTank/tank_specular.jpg
//        GlowMap     : Models/HoverTank/tank_glow_map.jpg
//        UseMaterialColors : true
//        Ambient  : 0.0 0.0 0.0 1.0
//        Diffuse  : 1.0 1.0 1.0 1.0
//        Specular : 1.0 1.0 1.0 1.0
//        Shininess : 8
        mat.getTextureParam("DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
        fpp.addFilter(bf);
        viewPort.addProcessor(fpp);

        /** A white, directional light source */
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(1.1f, -1.5f, -1.5f));
//        sun.setColor(ColorRGBA.White);
//        rootNode.addLight(sun);
//        
//        AmbientLight ambient = new AmbientLight();
//        ambient.setColor(ColorRGBA.White);
//        rootNode.addLight(ambient);
    }

    @Override
    public void simpleUpdate(float tpf) {
        spot.setDirection(cam.getDirection());
        spot.setPosition(cam.getLocation());
    }

    public static void main(String[] args) {
        HoverTank app = new HoverTank();
        app.start();

    }
}
