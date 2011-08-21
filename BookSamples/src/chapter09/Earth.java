package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import jme3tools.converters.ImageToAwt;

/**
 * test
 *
 * @author normenhansen
 */
public class Earth extends SimpleApplication {

  TerrainQuad terrain;
  Material terrain_mat;

  public static void main(String[] args) {
    Earth app = new Earth();
    app.start();
  }

  @Override
  public void simpleInitApp() {

    setDisplayFps(false);
    setDisplayStatView(false);

    createTerrain();

    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White);
    rootNode.addLight(ambient); 
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(-2.9236743f, -1.27054665f, 5.896916f));
    sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
    rootNode.addLight(sun);

    flyCam.setMoveSpeed(50);
 
    Spatial sky = SkyFactory.createSky(assetManager, 
            "Textures/Sky/Bright/BrightSky.dds", false);
    rootNode.attachChild(sky);

    cam.setFrustumFar(4000);

//    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
//    viewPort.addProcessor(fpp);
  }

  private void createTerrain() {
    Texture heightMapImage = assetManager.loadTexture(
            "Textures/Terrain/splat/mountains512.png");
    terrain_mat = new Material(assetManager, 
            "Common/MatDefs/Terrain/TerrainLighting.j3md");
    terrain_mat.setBoolean("useTriPlanarMapping", false);
    terrain_mat.setBoolean("WardIso", true);
    terrain_mat.setTexture("AlphaMap", assetManager.loadTexture(
            "Textures/Terrain/splat/alphamap.png"));
    
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap", grass);
    terrain_mat.setFloat("DiffuseMap_0_scale", 64);
    Texture normalMap0 = assetManager.loadTexture(
            "Textures/Terrain/splat/grass_normal.jpg");
    normalMap0.setWrap(WrapMode.Repeat);
    
    Texture rock = assetManager.loadTexture(
            "Textures/Terrain/splat/rock.PNG");
    rock.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap_1", rock);
    terrain_mat.setFloat("DiffuseMap_1_scale", 64);
    Texture normalMap1 = assetManager.loadTexture(
            "Textures/Terrain/splat/road_normal.png");
    normalMap1.setWrap(WrapMode.Repeat);
    
    Texture road = assetManager.loadTexture(
            "Textures/Terrain/splat/road.png");
    road.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap_2", road);
    terrain_mat.setFloat("DiffuseMap_2_scale", 128);
    Texture normalMap2 = assetManager.loadTexture(
            "Textures/Terrain/splat/rock_normal.png");
    normalMap2.setWrap(WrapMode.Repeat);
    
    terrain_mat.setTexture("NormalMap", normalMap0);
    terrain_mat.setTexture("NormalMap_1", normalMap2);
    terrain_mat.setTexture("NormalMap_2", normalMap2);

    AbstractHeightMap heightmap = null;
    try {
      heightmap = new ImageBasedHeightMap(
              ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }
      
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    TerrainLodControl lodControl = new TerrainLodControl(terrain, getCamera());
    terrain.addControl(lodControl);
    terrain.setMaterial(terrain_mat);
    terrain.setLocalTranslation(0, -100, 0);
    //terrain.setLocalScale(2f, 2f, 2f);
    terrain.setShadowMode(ShadowMode.Receive);
    rootNode.attachChild(terrain);
  }

  @Override
  public void simpleUpdate(float tpf) { /** unused */ }
  
}
