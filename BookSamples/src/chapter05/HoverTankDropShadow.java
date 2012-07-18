package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.texture.Texture.WrapMode;

/**
 * Loading a multimapped model with UV textures and Phong illumination.
 */
public class HoverTankDropShadow extends SimpleApplication {

    private SpotLight spot;
    private PssmShadowRenderer pssmRenderer;

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);

        /** A HoverTank model using a .j3m material. */
        Node tank = (Node) assetManager.loadModel(
                "Models/HoverTank/Tank.mesh.xml");
        Material mat = assetManager.loadMaterial("Materials/tank.j3m");
        tank.setMaterial(mat);
        rootNode.attachChild(tank);
        tank.setShadowMode(ShadowMode.CastAndReceive);

        /** Overall brightness*/
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);

        /** A cone-shaped spotlight, see also simpleUpdate(). */
        spot = new SpotLight();
        spot = new SpotLight();
        spot.setSpotRange(100);
        spot.setSpotOuterAngle(20 * FastMath.DEG_TO_RAD);
        spot.setSpotInnerAngle(15 * FastMath.DEG_TO_RAD);
        spot.setDirection(cam.getDirection());
        spot.setPosition(cam.getLocation());
        rootNode.addLight(spot);

        /* Drop shadows */
        pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 4);
        viewPort.addProcessor(pssmRenderer);

        /* Activate the glow effect in the HoverTank's material */
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.SceneAndObjects);
        fpp.addFilter(bf);
        viewPort.addProcessor(fpp);

        /** A floor with a seemless tiled, multimapped texture */
        Box floor_mesh = new Box(new Vector3f(-20, -1, -20), new Vector3f(20, -2, 20));
        floor_mesh.scaleTextureCoordinates(new Vector2f(8, 8));
        Geometry floor_geo = new Geometry("floor", floor_mesh);
        Material floor_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        floor_mat.setTexture("DiffuseMap", assetManager.loadTexture(
                "Textures/BrickWall/BrickWall_diffuse.jpg"));
        floor_mat.getTextureParam("DiffuseMap").
                getTextureValue().setWrap(WrapMode.Repeat);
        floor_mat.setTexture("NormalMap", assetManager.loadTexture(
                "Textures/BrickWall/BrickWall_normal.jpg"));
        floor_mat.getTextureParam("NormalMap").
                getTextureValue().setWrap(WrapMode.Repeat);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setShadowMode(ShadowMode.Receive);
        rootNode.attachChild(floor_geo);
    }

    @Override
    public void simpleUpdate(float tpf) {
        /* keep the spotlight moving with the camera */
        spot.setDirection(cam.getDirection());
        spot.setPosition(cam.getLocation());
        /* keep the shadows moving with spotlight/camera */
        pssmRenderer.setDirection(cam.getDirection());
    }

    public static void main(String[] args) {
        HoverTankDropShadow app = new HoverTankDropShadow();
        app.start();

    }
}
