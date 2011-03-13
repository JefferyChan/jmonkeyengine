package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * test
 * @author nehon
 */
public class TestReflection extends SimpleApplication {

    public static void main(String[] args) {
        TestReflection app = new TestReflection();
        app.start();
    }
    private boolean useReflection = true;
    private boolean useRefraction = true;

    @Override
    public void simpleInitApp() {
        final Node buggy = (Node) assetManager.loadModel("Models/Buggy/Buggy.j3o");


        TextureKey key = new TextureKey("Textures/Sky/Bright/BrightSky.dds", true);
        key.setGenerateMips(true);
        key.setAsCube(true);
        final Texture tex = assetManager.loadTexture(key);

        for (Spatial geom : buggy.getChildren()) {
            if (geom instanceof Geometry) {
                Material m=((Geometry) geom).getMaterial();
                m.setTexture("EnvMap", tex);                
                m.setBoolean("UseReflection", useReflection);
                m.setBoolean("UseRefraction", useRefraction);
                m.setFloat("RefractionIntensity", 1.0f);   
                
                m.setFloat("ReflectionPower", 4.0f);
                m.setFloat("ReflectionIntensity", 1.3f);         
                //  ((Geometry) geom).getMaterial().setTexture("GlowMap", assetManager.loadTexture("Models/Buggy/buggy_glow.jpg"));
            }

        }

        ChaseCamera chaseCam = new ChaseCamera(cam, inputManager);
        chaseCam.setLookAtOffset(new Vector3f(0,0.5f,-1.0f));
        flyCam.setEnabled(false);;
        buggy.addControl(chaseCam);
        rootNode.attachChild(buggy);
        rootNode.attachChild(SkyFactory.createSky(assetManager, tex, false));

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
        bf.setBloomIntensity(2.3f);
        bf.setExposurePower(0.6f);
        //    bf.setDownSamplingFactor(2);
        
     
        fpp.addFilter(bf);
        
        viewPort.addProcessor(fpp);

        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("toggleReflection") && isPressed) {
                    useReflection = !useReflection;
                    for (Spatial geom : buggy.getChildren()) {
                        if (geom instanceof Geometry) {
                             ((Geometry) geom).getMaterial().setBoolean("UseReflection", useReflection);
                        }
                    }
                   
                }
                if (name.equals("toggleRefraction") && isPressed) {
                    useRefraction = !useRefraction;
                    for (Spatial geom : buggy.getChildren()) {
                        if (geom instanceof Geometry) {
                             ((Geometry) geom).getMaterial().setBoolean("UseRefraction", useRefraction);
                        }
                    }
                   
                }
            }
           
        }, "toggleReflection","toggleRefraction");

        inputManager.addMapping("toggleReflection", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("toggleRefraction", new KeyTrigger(KeyInput.KEY_2));
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
