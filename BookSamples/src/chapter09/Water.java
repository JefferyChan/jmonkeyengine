package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import java.util.ArrayList;
import java.util.List;
import jme3tools.converters.ImageToAwt;

/**
 * test
 *
 * @author normenhansen
 */
public class Water extends SimpleApplication {

  private Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
  private WaterFilter water;
  TerrainQuad terrain;
  Material matRock;

  public static void main(String[] args) {
    Water app = new Water();
    app.start();
  }

  @Override
  public void simpleInitApp() {

    setDisplayFps(false);
    setDisplayStatView(false);

    Node mainScene = new Node("Main Scene");
    rootNode.attachChild(mainScene);

    createTerrain(mainScene);

    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(lightDir);
    sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
    mainScene.addLight(sun);

    flyCam.setMoveSpeed(50);
    cam.setLocation(new Vector3f(-327.21957f, 61.6459f, 126.884346f));
    cam.setRotation(new Quaternion(0.052168474f, 0.9443102f, -0.18395276f, 0.2678024f));
    cam.setRotation(new Quaternion().fromAngles(new float[]{FastMath.PI * 0.06f, FastMath.PI * 0.65f, 0}));

    Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", false);
    sky.setLocalScale(350);
    mainScene.attachChild(sky);

    cam.setFrustumFar(4000);

    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
    viewPort.addProcessor(fpp);

    water = new WaterFilter(rootNode, lightDir);
    water.setWaveScale(0.003f);
    water.setMaxAmplitude(2f);
    water.setFoamExistence(new Vector3f(1f, 4, 0.5f));
    water.setFoamTexture((Texture2D) 
            assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
    water.setRefractionStrength(0.2f);
    water.setWaterHeight(initialWaterHeight);
    fpp.addFilter(water);
    
    BloomFilter bloom = new BloomFilter();
    bloom.setExposurePower(55);
    bloom.setBloomIntensity(1.0f);
    fpp.addFilter(bloom);
    
    LightScatteringFilter lsf = new LightScatteringFilter(lightDir.mult(-300));
    lsf.setLightDensity(1.0f);
    fpp.addFilter(lsf);
    
    DepthOfFieldFilter dof = new DepthOfFieldFilter();
    dof.setFocusDistance(0);
    dof.setFocusRange(100);
    fpp.addFilter(dof);
  }

  private void createTerrain(Node rootNode) {
    matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
    matRock.setBoolean("useTriPlanarMapping", false);
    matRock.setBoolean("WardIso", true);
    matRock.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
    Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
    Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    matRock.setTexture("DiffuseMap", grass);
    matRock.setFloat("DiffuseMap_0_scale", 64);
    Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
    dirt.setWrap(WrapMode.Repeat);
    matRock.setTexture("DiffuseMap_1", dirt);
    matRock.setFloat("DiffuseMap_1_scale", 16);
    Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
    rock.setWrap(WrapMode.Repeat);
    matRock.setTexture("DiffuseMap_2", rock);
    matRock.setFloat("DiffuseMap_2_scale", 128);
    Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
    normalMap0.setWrap(WrapMode.Repeat);
    Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
    normalMap1.setWrap(WrapMode.Repeat);
    Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
    normalMap2.setWrap(WrapMode.Repeat);
    matRock.setTexture("NormalMap", normalMap0);
    matRock.setTexture("NormalMap_1", normalMap2);
    matRock.setTexture("NormalMap_2", normalMap2);

    AbstractHeightMap heightmap = null;
    try {
      heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    List<Camera> cameras = new ArrayList<Camera>();
    cameras.add(getCamera());
    terrain.setMaterial(matRock);
    terrain.setLocalScale(new Vector3f(5, 5, 5));
    terrain.setLocalTranslation(new Vector3f(0, -30, 0));
    terrain.setLocked(false); // unlock it so we can edit the height

    terrain.setShadowMode(ShadowMode.Receive);
    rootNode.attachChild(terrain);

  }
  //This part is to emulate tides, slightly varrying the height of the water plane
  private float time = 0.0f;
  private float waterHeight = 0.0f;
  private float initialWaterHeight = 0.8f;

  @Override
  public void simpleUpdate(float tpf) {
    time += tpf;
    waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
    water.setWaterHeight(initialWaterHeight + waterHeight);

  }
}
