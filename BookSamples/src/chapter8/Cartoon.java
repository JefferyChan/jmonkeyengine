package chapter8;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.renderer.Caps;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

/**
 * The demo shows how to make a scene apear in cartoon style.
 * <p/>
 * @author zathras
 */
public class Cartoon extends SimpleApplication {

  private FilterPostProcessor fpp;

  @Override
  public void simpleInitApp() {
    
    /** are minimum requirements for cel shader met? */
    if (renderer.getCaps().contains(Caps.GLSL100)) {
      fpp = new FilterPostProcessor(assetManager);
      CartoonEdgeFilter toon = new CartoonEdgeFilter();
      toon.setEdgeColor(ColorRGBA.Black);
      fpp.addFilter(toon);
      viewPort.addProcessor(fpp);
    }

    /** Add light */
    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(1f, -1f, -1f));
    rootNode.addLight(dl);

    initScene(); // load some models, e.g. a town, tea pot
    makeToonish(rootNode);
    
    viewPort.setBackgroundColor(ColorRGBA.Cyan);
  }

  public void makeToonish(Spatial spatial) {
    if (spatial instanceof Node) {
      Node n = (Node) spatial;
      for (Spatial child : n.getChildren()) {
        makeToonish(child);
      }
    } else if (spatial instanceof Geometry) {
      Geometry g = (Geometry) spatial;
      Material m = g.getMaterial();
      if (m.getMaterialDef().getName().equals("Phong Lighting")) {
        Texture t = assetManager.loadTexture("Textures/ColorRamp/toon.png");
        m.setTexture("ColorRamp", t);
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Specular", ColorRGBA.Black);
        m.setColor("Diffuse", ColorRGBA.White);
        m.setBoolean("VertexLighting", true);
      }
    }
  }

  private void initScene() {
    /**
     * Add some objects to the scene: A town
     */
    Spatial scene_geo = assetManager.loadModel("Models/HoverTank/Tank.mesh.xml");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    rootNode.attachChild(scene_geo);
    scene_geo.setShadowMode(ShadowMode.CastAndReceive);

    /**
     * Add some objects to the scene: a tea pot
     */
    Geometry tea_geo = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.j3o");
    Material mat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Diffuse", ColorRGBA.Pink);
    tea_geo.setMaterial(mat);
    
    tea_geo.scale(3);
    tea_geo.setLocalTranslation(3, 2, 2);
    rootNode.attachChild(tea_geo);
    tea_geo.setShadowMode(ShadowMode.CastAndReceive);

    /**
     * configure some game properties depending on the scene
     */
    flyCam.setMoveSpeed(30f);
    cam.lookAt(tea_geo.getLocalTranslation(), Vector3f.UNIT_Y);
  }

  public static void main(String[] args) {
    Cartoon app = new Cartoon();
    app.start();

  }
}
