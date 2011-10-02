package mygame;

import com.jme3.export.Savable;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TowerControl extends AbstractControl implements Savable, Cloneable {

  private TowerData td;
  private int num = 0;
  private Node dotNode;

  public TowerControl(TowerData td) {
    this.td = td;
  }

  @Override
  protected void controlUpdate(float tpf) {
    this.dotNode = ((Node) (spatial.getParent().getChild("dot node")));
    /* if this tower has ammo, then plan attack */
    num = td.getChargeNum();
    if (num > 0) {
      /* visualize charges */
      List<Charge> all = td.getCharges();
      int ix =0;
      for (Charge c : all) {
        dotNode.attachChild(makeDot(c, ix, td.getLoc()));
        ix++;
      }
      /* pick the top charge from the list */
      Charge a = td.getNextCharge();
      /* identify reachable creeps - depends on creep's distance to tower */
      List<CreepData> reachable = new ArrayList<CreepData>();
      List<CreepData> cd = td.getCreepData();
      for (CreepData c : cd) {
        if (c.isAlive() && td.getTop().distance(c.getLoc()) < a.getRange()) {
          reachable.add(c);
        }
      }
      /* if this tower has ammo, and can reach a creep */
      if (reachable.size() > 0) {
        /* shoot at each reachable creep once, until out of creeps or out of ammo */
        for (int i = a.getAmmo(); i > 0; i--) {
          for (CreepData creep : reachable) {
            /* show a laser beam from tower to the creep that got hit. the laser visuals are slightly random. */
            Vector3f hit = creep.getLoc();
            Line beam = new Line(
                    td.getTop(),
                    new Vector3f(
                    hit.x + FastMath.rand.nextFloat() / 4,
                    hit.y + FastMath.rand.nextFloat() / 4,
                    hit.z + FastMath.rand.nextFloat() / 4));
            Geometry beam_geo = new Geometry("Beam", beam);
            beam_geo.setMaterial(a.getBeamMaterial());
            Node beamNode = ((Node) (spatial.getParent().getParent().getChild("beam node")));
            beamNode.attachChild(beam_geo);
            /* the laser beam has an effect */
            creep.addHealth(a.getHealthImpact()); // all towers do damage to target
            creep.addSpeed(a.getSpeedImpact());   // freeze towers also slow target down
            // TODO: consider blastrange for nukes
            /** shooting uses up ammo in this charge */
            a.addAmmo(-1);
            if (a.getAmmo() <= 0) {
              /** this charge is out of ammo, stop shooting until next turn. */
              td.removeCharge(a);
              // update visuals to reflect current charge status
              dotNode.detachAllChildren();
              all = td.getCharges();
              for (Charge c : all) {
                dotNode.attachChild(makeDot(c, all.indexOf(c), td.getLoc()));
              }
              break;
            }
          }
        }
      }
    } else {
      // no ammo
      dotNode.detachAllChildren();

    }
  }

  private Geometry makeDot(Charge a, int i, Vector3f loc) {
    System.out.println("Charge " + a + " index " + i);
    Sphere dot = new Sphere(10, 10, .1f);
    Geometry dot_geo = new Geometry("Beam", dot);
    dot_geo.setMaterial(a.getBeamMaterial());
    int offset_x = (td.getIndex() % 2 == 0 ? 1 : -1);
    dot_geo.setLocalTranslation(loc.x - (offset_x*0.33f), loc.y - (i*.25f)+1, loc.z);
    return dot_geo;
  }

  @Override
  protected void controlRender(RenderManager rm, ViewPort vp) {
  }

  public Control cloneForSpatial(Spatial spatial) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
