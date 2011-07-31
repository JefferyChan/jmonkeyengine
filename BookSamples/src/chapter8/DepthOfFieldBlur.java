package chapter8;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * test <p/>
 * <p/>
 * @author Nehon
 */
public class DepthOfFieldBlur extends SimpleApplication {

  private FilterPostProcessor fpp;
  private DepthOfFieldFilter dofFilter;
  private Spatial scene_geo;
  
  public static void main(String[] args) {
    DepthOfFieldBlur app = new DepthOfFieldBlur();
    app.start();
  }

  @Override
  public void simpleInitApp() {

    assetManager.registerLocator("assets/Scenes/town.zip", ZipLocator.class.getName());
    scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -1, 0);
    rootNode.attachChild(scene_geo);

    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(1f, -1f, -1f));
    rootNode.addLight(dl);
        
    viewPort.setBackgroundColor(ColorRGBA.Blue);
    
    flyCam.setMoveSpeed(50);
    cam.setFrustumFar(3000);
    
    fpp = new FilterPostProcessor(assetManager);
    dofFilter = new DepthOfFieldFilter();
    dofFilter.setFocusDistance(0);
    dofFilter.setFocusRange(50);
    dofFilter.setBlurScale(1.4f);
    fpp.addFilter(dofFilter);
    viewPort.addProcessor(fpp);

    inputManager.addListener(new ActionListener() {

      public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
          if (name.equals("toggle")) {
            dofFilter.setEnabled(!dofFilter.isEnabled());
          }


        }
      }
    }, "toggle");
    inputManager.addListener(new AnalogListener() {

      public void onAnalog(String name, float value, float tpf) {
        if (name.equals("blurScaleUp")) {
          dofFilter.setBlurScale(dofFilter.getBlurScale() + 0.01f);
          System.out.println("blurScale : " + dofFilter.getBlurScale());
        }
        if (name.equals("blurScaleDown")) {
          dofFilter.setBlurScale(dofFilter.getBlurScale() - 0.01f);
          System.out.println("blurScale : " + dofFilter.getBlurScale());
        }
        if (name.equals("focusRangeUp")) {
          dofFilter.setFocusRange(dofFilter.getFocusRange() + 1f);
          System.out.println("focusRange : " + dofFilter.getFocusRange());
        }
        if (name.equals("focusRangeDown")) {
          dofFilter.setFocusRange(dofFilter.getFocusRange() - 1f);
          System.out.println("focusRange : " + dofFilter.getFocusRange());
        }
        if (name.equals("focusDistanceUp")) {
          dofFilter.setFocusDistance(dofFilter.getFocusDistance() + 1f);
          System.out.println("focusDistance : " + dofFilter.getFocusDistance());
        }
        if (name.equals("focusDistanceDown")) {
          dofFilter.setFocusDistance(dofFilter.getFocusDistance() - 1f);
          System.out.println("focusDistance : " + dofFilter.getFocusDistance());
        }

      }
    }, "blurScaleUp", "blurScaleDown", "focusRangeUp", "focusRangeDown", "focusDistanceUp", "focusDistanceDown");


    inputManager.addMapping("toggle", new KeyTrigger(keyInput.KEY_SPACE));
    inputManager.addMapping("blurScaleUp", new KeyTrigger(keyInput.KEY_U));
    inputManager.addMapping("blurScaleDown", new KeyTrigger(keyInput.KEY_J));
    inputManager.addMapping("focusRangeUp", new KeyTrigger(keyInput.KEY_I));
    inputManager.addMapping("focusRangeDown", new KeyTrigger(keyInput.KEY_K));
    inputManager.addMapping("focusDistanceUp", new KeyTrigger(keyInput.KEY_O));
    inputManager.addMapping("focusDistanceDown", new KeyTrigger(keyInput.KEY_L));

  }

  @Override
  public void simpleUpdate(float tpf) {
    Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f);
    Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f);
    direction.subtractLocal(origin).normalizeLocal();
    Ray ray = new Ray(origin, direction);
    CollisionResults results = new CollisionResults();
    int numCollisions = scene_geo.collideWith(ray, results);
    if (numCollisions > 0) {
      CollisionResult hit = results.getClosestCollision();
      fpsText.setText("" + hit.getDistance());
      dofFilter.setFocusDistance(hit.getDistance() / 10.0f);
    }
  }
}
