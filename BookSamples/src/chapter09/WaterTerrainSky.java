package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;

/**
 * Demo of advanced water effect with sky, terrain, waves, foam, tides,
 * underwater caustics, god beams, depth of field blur, and bloom.
 *
 * @author normenhansen
 */
public class WaterTerrainSky extends SimpleApplication {

  private Vector3f lightDir = new Vector3f(-5f, -1f, 6f).normalize();//new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
  private WaterFilter water;
  TerrainQuad terrain;
  Material terrain_mat;
  private float time = 0.0f;
  private float waterHeight = 0.0f;
  private float initialWaterHeight = 0.8f;
  Node reflectedScene=new Node("Reflected Scene");

  public static void main(String[] args) {
    WaterTerrainSky app = new WaterTerrainSky();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    setDisplayFps(false);
    setDisplayStatView(false);

    flyCam.setMoveSpeed(400);
    cam.setFrustumFar(40000);

    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
    viewPort.addProcessor(fpp);
    // add water
    water = new WaterFilter(reflectedScene, lightDir);
    water.setWaveScale(0.003f);
    water.setMaxAmplitude(2f);
    water.setFoamExistence(new Vector3f(1f, 4, 0.5f));
    water.setFoamTexture((Texture2D) assetManager.loadTexture(
            "Common/MatDefs/Water/Textures/foam2.jpg"));
    water.setRefractionStrength(0.2f);
    water.setWaterHeight(initialWaterHeight);
    fpp.addFilter(water);

    reflectedScene.attachChild(initScene());

    // add a scene wide glow
    BloomFilter bloom = new BloomFilter();
    bloom.setExposurePower(55);
    bloom.setBloomIntensity(1.0f);
    fpp.addFilter(bloom);
    // add "god beams"
    LightScatteringFilter lsf = new LightScatteringFilter(lightDir.mult(-300));
    lsf.setLightDensity(1.0f);
    fpp.addFilter(lsf);
    // add focal blur
    DepthOfFieldFilter dof = new DepthOfFieldFilter();
    dof.setFocusDistance(0);
    dof.setFocusRange(100);
    fpp.addFilter(dof);
  }

  private Spatial initScene() {
    // mainscene has everything that reflects in water, 
    // including sky and light, but not the water itself.
    reflectedScene = new Node("Main Scene");
    rootNode.attachChild(reflectedScene);

    reflectedScene.attachChild(createTerrain()); // terrain reflects in water

    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(lightDir);
    sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
    reflectedScene.addLight(sun);

    AmbientLight ambientLight = new AmbientLight();
    reflectedScene.addLight(ambientLight);

    Spatial sky = SkyFactory.createSky(assetManager, 
            "Textures/Sky/Bright/FullskiesBlueClear03.dds", false);
    sky.setLocalScale(350);
    return sky;
  }

  private TerrainQuad createTerrain() {
    // create material with RGBA AlphaMap
    terrain_mat = new Material(assetManager,
            "Common/MatDefs/Terrain/TerrainLighting.j3md");
    terrain_mat.setTexture("AlphaMap", assetManager.loadTexture(
            "Textures/Terrain/alphamap.png"));
    // grass (red channel)
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap", grass);
    terrain_mat.setFloat("DiffuseMap_0_scale", 64);
    Texture normalMap0 = assetManager.loadTexture(
            "Textures/Terrain/grass_normal.jpg");
    normalMap0.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("NormalMap", normalMap0);
    // rock (green channel)
    Texture rock = assetManager.loadTexture(
            "Textures/Terrain/rock.png");
    rock.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap_1", rock);
    terrain_mat.setFloat("DiffuseMap_1_scale", 64);
    Texture normalMap1 = assetManager.loadTexture(
            "Textures/Terrain/rock_normal.png");
    normalMap1.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("NormalMap_1", normalMap1);
    // road (blue channel)
    Texture road = assetManager.loadTexture(
            "Textures/Terrain/road.png");
    road.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap_2", road);
    terrain_mat.setFloat("DiffuseMap_2_scale", 64);
    Texture normalMap2 = assetManager.loadTexture(
            "Textures/Terrain/road_normal.png");
    normalMap2.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("NormalMap_2", normalMap2);
    // create heightmap from image
    Texture heightMapImage = assetManager.loadTexture(
            "Textures/Terrain/heightmap.png");
    AbstractHeightMap heightmap = null;
    try {
      heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }
    heightmap.smooth(0.9f, 3);
    // create terrain from heightmap
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    TerrainLodControl lodControl = new TerrainLodControl(terrain, getCamera());
    terrain.addControl(lodControl);
    terrain.setMaterial(terrain_mat);
    terrain.setLocalTranslation(0, -10, 0);
    terrain.setLocalScale(new Vector3f(4, 4, 4));
    return terrain;
  }

  @Override
  public void simpleUpdate(float tpf) {
    // simulate tides by varrying the height of the water plane
    time += tpf;
    waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
    water.setWaterHeight(initialWaterHeight + waterHeight);
  }
}
