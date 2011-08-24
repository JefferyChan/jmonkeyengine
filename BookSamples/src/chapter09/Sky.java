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

  }
}
