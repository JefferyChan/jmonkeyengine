package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.util.SkyFactory;

/**
 * terrain
 *
 * @author normenhansen
 */
public class EarthScene extends SimpleApplication {

  TerrainQuad terrain;
  Material terrain_mat;

  public static void main(String[] args) {
    EarthScene app = new EarthScene();
    app.start();
  }

  @Override
  public void simpleInitApp() {

    setDisplayFps(true);
    setDisplayStatView(false);

    Spatial myTerrain = assetManager.loadModel("Scenes/newScene765.j3o");
    rootNode.attachChild(myTerrain);

    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White);
    rootNode.addLight(ambient);
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(-2.9236743f, -1.27054665f, 5.896916f));
    sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
    rootNode.addLight(sun);

    flyCam.setMoveSpeed(100);

    Spatial sky = SkyFactory.createSky(assetManager,
            "Textures/Sky/Bright/BrightSky.dds", false);
    rootNode.attachChild(sky);

    cam.setFrustumFar(4000);
  }

  @Override
  public void simpleUpdate(float tpf) {  }
}
