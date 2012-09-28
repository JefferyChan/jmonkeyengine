package chapter08;

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
    
    // add focal blur
    DepthOfFieldFilter dof = new DepthOfFieldFilter();
    dof.setFocusDistance(0);
    dof.setFocusRange(100);
    fpp.addFilter(dof);
  }

  private Spatial initScene() {
    // mainscene has everything that reflects in water, 
    // including sky and light, but not the water itself.
    rootNode.attachChild(reflectedScene);

    reflectedScene.attachChild(createTerrain()); // terrain reflects in water

    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(lightDir);
    sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
    reflectedScene.addLight(sun);

    AmbientLight ambientLight = new AmbientLight();
    reflectedScene.addLight(ambientLight);

    Spatial sky = SkyFactory.createSky(assetManager, 
            "Textures/Sky/Bright/BrightSky.dds", false);
    reflectedScene.attachChild(sky);
    return reflectedScene;
  }

  private TerrainQuad createTerrain() {
    Texture heightMapImage = assetManager.loadTexture(
            "Textures/Terrain/heightmap.png");
    
    Material terrainMat = new Material(assetManager, 
            "Common/MatDefs/Terrain/TerrainLighting.j3md");
    terrainMat.setBoolean("useTriPlanarMapping", false);
    terrainMat.setBoolean("WardIso", true);    
    terrainMat.setTexture("AlphaMap", assetManager.loadTexture(
            "Textures/Terrain/alphamap.png"));
    
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    terrainMat.setTexture("DiffuseMap_1", grass);
    terrainMat.setFloat("DiffuseMap_1_scale", 64);
    Texture normalMap1 = assetManager.loadTexture(
            "Textures/Terrain/grass_normal.jpg");
    normalMap1.setWrap(WrapMode.Repeat);
    terrainMat.setTexture("NormalMap_1", normalMap1);
    
    Texture rock = assetManager.loadTexture(
            "Textures/Terrain/rock.png");
    rock.setWrap(WrapMode.Repeat);
    terrainMat.setTexture("DiffuseMap", rock);
    terrainMat.setFloat("DiffuseMap_0_scale", 64);
    Texture normalMap0 = assetManager.loadTexture(
            "Textures/Terrain/rock_normal.png");
    normalMap0.setWrap(WrapMode.Repeat);
    terrainMat.setTexture("NormalMap", normalMap0);
    
    Texture road = assetManager.loadTexture(
            "Textures/Terrain/road.png");
    road.setWrap(WrapMode.Repeat);
    terrainMat.setTexture("DiffuseMap_2", road);
    terrainMat.setFloat("DiffuseMap_2_scale", 64);
    Texture normalMap2 = assetManager.loadTexture(
            "Textures/Terrain/road_normal.png");
    normalMap2.setWrap(WrapMode.Repeat);
    terrainMat.setTexture("NormalMap_2", normalMap2);

    AbstractHeightMap heightmap = null;
    try {

      heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), .5f);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }
    heightmap.smooth(0.9f,3);
    
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    terrain.setMaterial(terrainMat);
    terrain.scale(4, 4, 4);
    terrain.move(0,-60,0);
    
    return terrain;
  }

  @Override
  public void simpleUpdate(float tpf) {
    // simulate tides by varying the height of the water plane
    time += tpf;
    waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
    water.setWaterHeight(initialWaterHeight + waterHeight);
  }
}
