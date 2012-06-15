package chapter04;

import com.jme3.app.state.AbstractAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

/** A template how to create an Application state to control interactions */
public class CubeChaserState extends AbstractAppState {

  private Ray ray = new Ray();
  private final Camera cam;
  private final Node rootNode;

  public CubeChaserState(Camera cam, Node rootNode) {
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
      // if camera closer than 10...
      if (cam.getLocation().distance(target.getLocalTranslation()) < 10) {
        // ... move the cube in the direction that camera is facing
        target.setLocalTranslation(target.getLocalTranslation().addLocal(cam.getDirection()));

      }
    }

  }
}