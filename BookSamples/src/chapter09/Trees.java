package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

/**
 * A simple unshaded terrain.
 * @author Brent Owens, zathras
 */
public class Trees extends SimpleApplication {

  private TerrainQuad terrain;
  Material terrain_mat;

  public static void main(String[] args) {
    Trees app = new Trees();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    setDisplayFps(true);
    setDisplayStatView(false);
    viewPort.setBackgroundColor(ColorRGBA.Blue);
    cam.setFrustumFar(4000);
    cam.setLocation(new Vector3f(0, 20, 30));
    flyCam.setMoveSpeed(50);

    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
    dl.setColor(ColorRGBA.White);
    rootNode.addLight(dl);

    initTerrain();

    // Load an simple box
    float boxExtend=1;
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setTexture("DiffuseMap", assetManager.loadTexture("Interface/Monkey.png"));
    Box b = new Box(boxExtend,boxExtend,boxExtend);
    Geometry box_geo = new Geometry("Box", b);
    box_geo.setMaterial(mat);
    rootNode.attachChild(box_geo);
    // Place the box at (x,?,z) on the terrain. What is the y value?
    Vector3f boxloc = new Vector3f(-30,0,-30);
    boxloc.setY( terrain.getLocalTranslation().getY() + boxExtend ); 
    box_geo.setLocalTranslation(boxloc);

    // Load a tree model
    Spatial tree = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
    tree.scale(5);
    tree.setQueueBucket(Bucket.Transparent); // leaves are transparent
    rootNode.attachChild(tree);
    // Place the tree at (x,?,z) on the terrain. What is the y value?
    //Vector3f treeloc = new Vector3f(-20,0,-90);
    Vector3f treeloc = new Vector3f(-40,0,-50);
    treeloc.setY(  terrain.getHeight(new Vector2f(treeloc.x, treeloc.z)) );
    tree.setLocalTranslation(treeloc);
    
    
  }

  private void initTerrain() {
    // load sky
    rootNode.attachChild(SkyFactory.createSky(assetManager,
            "Textures/Sky/Bright/BrightSky.dds", false));
    // load terrain
    terrain_mat = new Material(assetManager,
            "Common/MatDefs/Terrain/TerrainLighting.j3md");
    terrain_mat.setBoolean("useTriPlanarMapping", false);
    terrain_mat.setBoolean("WardIso", true);
    terrain_mat.setTexture("AlphaMap", assetManager.loadTexture(
            "Textures/Terrain/alphamap.png"));

    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("DiffuseMap", grass);
    terrain_mat.setFloat("DiffuseMap_0_scale", 64);
    Texture normalMap = assetManager.loadTexture(
            "Textures/Terrain/grass_normal.jpg");
    normalMap.setWrap(WrapMode.Repeat);
    terrain_mat.setTexture("NormalMap", normalMap);
    terrain_mat.setTexture("DiffuseMap_1", grass);
    terrain_mat.setFloat("DiffuseMap_1_scale", 64);
    terrain_mat.setTexture("NormalMap_1", normalMap);
    terrain_mat.setTexture("DiffuseMap_2", grass);
    terrain_mat.setFloat("DiffuseMap_2_scale", 64);
    terrain_mat.setTexture("NormalMap_2", normalMap);
    // Generate a random height map
    AbstractHeightMap heightmap = null;
    try {
      heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Create the terrain, apply the material, attach to rootnode
    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
    terrain.setMaterial(terrain_mat);
    rootNode.attachChild(terrain);
  }
}
