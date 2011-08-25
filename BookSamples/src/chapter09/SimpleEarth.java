package chapter09;

import jme3tools.converters.ImageToAwt;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * Demonstrates a simple terrain.
 *
 * @author bowens
 */
public class SimpleEarth extends SimpleApplication {

  private TerrainQuad terrain;
  Material terrain_mat;

  public static void main(String[] args) {
    SimpleEarth app = new SimpleEarth();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    setDisplayFps(true);
    setDisplayStatView(false);
    cam.setFrustumFar(4000);
    flyCam.setMoveSpeed(100);

    // Terrain material supports texture splatting
    terrain_mat = new Material(assetManager,
            "Common/MatDefs/Terrain/Terrain.j3md");

    // Terrain AlphaMap for the splatting material
    terrain_mat.setTexture("Alpha", assetManager.loadTexture(
            "Textures/Terrain/splat/alphamap.png"));

    // Grass texture for the splatting material
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("Tex1", grass);
    terrain_mat.setFloat("Tex1Scale", 32);

    // Rock texture for the splatting material
    Texture rock = assetManager.loadTexture(
            "Textures/Terrain/splat/rock.png");
    rock.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("Tex2", rock);
    terrain_mat.setFloat("Tex2Scale", 64);

    // Road texture for the splatting material
    Texture road = assetManager.loadTexture(
            "Textures/Terrain/splat/road.png");
    road.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("Tex3", road);
    terrain_mat.setFloat("Tex3Scale", 64);

    // Heightmap image on which we base the terrain
    Texture heightMapImage = assetManager.loadTexture(
            "Textures/Terrain/splat/heightmap.png");
    // Create the heightmap object from the heightmap image 
    AbstractHeightMap heightmap = null;
    try {
      heightmap = new ImageBasedHeightMap(ImageToAwt.convert(
              heightMapImage.getImage(), false, true, 0), 0.5f);
      heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Create the terrain, apply the material, attach to rootnode
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    terrain.setMaterial(terrain_mat);
    terrain.setLocalTranslation(0, -150, 0);
    terrain.setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(terrain);


  }
}
