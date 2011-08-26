package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * Demonstrates how to use sky.
 */
public class Sky extends SimpleApplication {

  private TerrainQuad terrain;
  Material matRock;

  public static void main(String[] args) {
    Sky app = new Sky();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(100f);
    cam.setLocation(new Vector3f(0, 10, -10));
    cam.lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);

    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

//    Spatial mySky = assetManager.loadModel("Scenes/mySky.j3o");
//    rootNode.attachChild(mySky);

    Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
    Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
    Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
    Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
    Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
    Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");
    Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
    rootNode.attachChild(sky);

  }
}
