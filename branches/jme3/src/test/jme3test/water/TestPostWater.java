package jme3test.water;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.util.TangentBinormalGenerator;
import com.jme3.water.WaterFilter;

/**
 * test
 * @author normenhansen
 */
public class TestPostWater extends SimpleApplication {

    private FilterPostProcessor fpp;
    private Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
    private WaterFilter water;

    public static void main(String[] args) {
        TestPostWater app = new TestPostWater();        
        app.start();
    }

    @Override
    public void simpleInitApp() {


//        Material mat = assetManager.loadMaterial("Textures/Terrain/Rocky/Rocky.j3m");
//        Spatial scene = assetManager.loadModel("Models/Terrain/Terrain.mesh.xml");
//        TangentBinormalGenerator.generate(((Geometry)((Node)scene).getChild(0)).getMesh());
//        scene.setMaterial(mat);
//        scene.setShadowMode(ShadowMode.CastAndReceive);
//        scene.setLocalScale(400);
//        scene.setLocalTranslation(0, -10, -120);
//        Node mainScene =new Node();
//        mainScene.attachChild(scene);

        Node mainScene = (Node) assetManager.loadModel("/Scenes/Beach/Beach2.mesh.j3o");
        Geometry beach = (Geometry) mainScene.getChild("Beach2-geom-1");

        beach.setLocalScale(10);
        beach.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y));
        beach.setShadowMode(ShadowMode.CastAndReceive);

        Material mat = assetManager.loadMaterial("/Scenes/Beach/sand.j3m");
        mat.getTextureParam("DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);
        mat.getTextureParam("NormalMap").getTextureValue().setWrap(WrapMode.Repeat);

        beach.setMaterial(mat);

        beach.getMesh().scaleTextureCoordinates(new Vector2f(60f, 60f));
        TangentBinormalGenerator.generate(beach.getMesh());
        rootNode.attachChild(mainScene);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
        mainScene.addLight(sun);

        DirectionalLight l = new DirectionalLight();
        l.setDirection(Vector3f.UNIT_Y.mult(-1));
        l.setColor(ColorRGBA.White.clone().multLocal(0.3f));
        mainScene.addLight(l);

        flyCam.setMoveSpeed(50);

        cam.setLocation(new Vector3f(-3.613934f, 33.164284f, 106.1938f));
        cam.setRotation(new Quaternion(0.027341122f, 0.9686672f, -0.12411701f, 0.21338053f));


        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", false);
        sky.setLocalScale(350);
        mainScene.attachChild(sky);
        cam.setFrustumFar(4000);
        //cam.setFrustumNear(100);
        AudioNode waves = new AudioNode(assetManager, "Sound/Environment/Ocean Waves.ogg", false);
        waves.setLooping(true);
        audioRenderer.playSource(waves);


        fpp = new FilterPostProcessor(assetManager);
        water = new WaterFilter(rootNode, lightDir);
 
        water.setWaterHeight(initialWaterHeight);
        fpp.addFilter(water);
        viewPort.addProcessor(fpp);

    }
    //This part is to emulate tides, slightly varrying the height of the water plane
    private float time = 0.0f;
    private float waterHeight = 0.0f;
    private float initialWaterHeight = 0.8f;

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        time += tpf;
        waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
        water.setWaterHeight(initialWaterHeight + waterHeight);

    }
}
