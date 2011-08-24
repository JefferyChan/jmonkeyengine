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
    flyCam.setMoveSpeed(100f);
    cam.setLocation(new Vector3f(0, 10, -10));
    cam.lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);

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
              heightMapImage.getImage(), false, true, 0), 1f);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Create the terrain and apply the material
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
    terrain.addControl(control);
    terrain.setMaterial(terrain_mat);
    terrain.setLocalTranslation(0, -100, 0);
    terrain.setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(terrain);

    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

  }
}
