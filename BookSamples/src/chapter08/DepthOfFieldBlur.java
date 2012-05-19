package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.scene.Spatial;

/**
 * This demo shows depth-of-field blur in a scene. Move the mouse to look around.
 * Press SPACE, U/J, I/K, O/L to try different focus settings.
 * @author Nehon, edits by zathras
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
    DirectionalLight dl = new DirectionalLight();
    dl.setDirection(new Vector3f(1f, -1f, -1f));
    rootNode.addLight(dl);

    viewPort.setBackgroundColor(ColorRGBA.Cyan);
    flyCam.setMoveSpeed(50);
    cam.setFrustumFar(3000);

    // activate depth of field blur
    fpp = new FilterPostProcessor(assetManager);
    dofFilter = new DepthOfFieldFilter();
    dofFilter.setFocusDistance(0);
    dofFilter.setFocusRange(20);
    dofFilter.setBlurScale(2f);
    fpp.addFilter(dofFilter);
    viewPort.addProcessor(fpp);

    // configure keys to chnage blur settings
    inputManager.addMapping("toggle", new KeyTrigger(keyInput.KEY_SPACE));
    inputManager.addMapping("blurScaleUp", new KeyTrigger(keyInput.KEY_U));
    inputManager.addMapping("blurScaleDown", new KeyTrigger(keyInput.KEY_J));
    inputManager.addMapping("focusRangeUp", new KeyTrigger(keyInput.KEY_I));
    inputManager.addMapping("focusRangeDown", new KeyTrigger(keyInput.KEY_K));
    inputManager.addMapping("focusDistanceUp", new KeyTrigger(keyInput.KEY_O));
    inputManager.addMapping("focusDistanceDown", new KeyTrigger(keyInput.KEY_L));
    inputManager.addListener(analogListener, "blurScaleUp", "blurScaleDown",
            "focusRangeUp", "focusRangeDown", "focusDistanceUp", "focusDistanceDown");
    inputManager.addListener(actionListener, "toggle");

    initScene();
  }

  private void initScene() {
    // load a town model
    assetManager.registerLocator("town.zip", ZipLocator.class);
    scene_geo = assetManager.loadModel("main.scene");
    scene_geo.setLocalScale(2f);
    scene_geo.setLocalTranslation(0, -10f, 0);
    rootNode.attachChild(scene_geo);
  }
  
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean isPressed, float tpf) {
      if (isPressed) {
        if (name.equals("toggle")) {
          dofFilter.setEnabled(!dofFilter.isEnabled());
        }
      }
    }
  };
  
  private AnalogListener analogListener = new AnalogListener() {
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
  };

  @Override
  public void simpleUpdate(float tpf) {
    // change the focus depending on where the player looks
    Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f);
    Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f);
    direction.subtractLocal(origin).normalizeLocal();
    Ray ray = new Ray(origin, direction);
    CollisionResults results = new CollisionResults();
    int numCollisions = scene_geo.collideWith(ray, results);
    if (numCollisions > 0) {
      CollisionResult hit = results.getClosestCollision();
      dofFilter.setFocusDistance(hit.getDistance() / 10.0f);
      fpsText.setText("Press SPACE, U/J, I/K, O/L. Distance: " + hit.getDistance());
    } else {
      fpsText.setText("Press SPACE, U/J, I/K, O/L. Distance: INF");
    }
  }
}
