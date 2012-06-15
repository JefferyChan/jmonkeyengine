package chapter04;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * This control demo uses the same cube as CubeChaser2's simpleUpdate() loop 
 * to change the location of the cube the player looks at.
 */
public class CubeChaserControl extends AbstractControl {

  private Ray ray = new Ray();
  private final Camera cam;
  private final Node rootNode;

  public CubeChaserControl(Camera cam, Node rootNode) {
    this.cam = cam;
    this.rootNode = rootNode;
  }

  @Override
  public void update(float tpf) {
    // 1. Reset results list.
    CollisionResults results = new CollisionResults();
    // 2. Aim the ray from camera location in camera direction.
    ray.setOrigin(cam.getLocation());
    ray.setDirection(cam.getDirection());
    // 3. Collect intersections between ray and all nodes in results list.
    rootNode.collideWith(ray, results);
    // 4. Use the result
    if (results.size() > 0) {
      // The closest result is the target that the player picked:
      Geometry target = results.getClosestCollision().getGeometry();
      // if the target is one of the chosen ones affected by this control...
      if (target.equals(spatial)) {
        // if camera closer than 10...
        if (cam.getLocation().distance(target.getLocalTranslation()) < 10) {
          // ... move the cube in the direction that camera is facing
          spatial.setLocalTranslation(spatial.getLocalTranslation().
                  addLocal(cam.getDirection().normalizeLocal()));
        }
      }
    }
  }

  @Override
  protected void controlUpdate(float tpf) {
  }

  @Override
  protected void controlRender(RenderManager rm, ViewPort vp) {
  }

  public Control cloneForSpatial(Spatial spatial) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
