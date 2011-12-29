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
 * Tower has charges and fires at creeps nearby until out of ammo.
 * Charge status is shown with colored spheres. in each level it can have more charges.
 */
public class TowerControl extends AbstractControl implements Savable, Cloneable {

    private List<Charge> charges = new ArrayList<Charge>();

    public TowerControl(){}
    
   

    @Override
    protected void controlUpdate(float tpf) {
        /* if this tower is loaded with charges, then plan attack */
        if (getChargeNum() > 0) {
            /* visualize current charges */
            int ix = 0;
            for (Charge c : getCharges()) {
                System.out.println(spatial.getName()+" has one charge with ammo "+c.getAmmo());
                getDotNode().attachChild(makeDot(c, ix, getLoc()));
                ix++;
            }
            /* pick the top charge from the list */
            Charge charge = getNextCharge();
            /* identify reachable creeps - depends on creep's distance to tower */
            List<CreepControl> reachable = new ArrayList<CreepControl>();
            List<Spatial> creeps = (List<Spatial>)(getCreeps().getChildren());
            for (Spatial creep_geo : creeps) {
                CreepControl creep = creep_geo.getControl(CreepControl.class);
                if ( creep.isAlive() 
                        && 
                        getTowerTop().distance(creep.getLoc()) < charge.getRange()) {
                    reachable.add(creep);
                    // TODO this should be empty if all creeps dead?
                }
            }
            /* if this tower has ammo, and can reach a creep, then... */
            if (reachable.size() > 0) {
                System.out.println(spatial.getName()+" has ammo? "+charge.getAmmo());
                /* ... shoot at each reachable creep once, until out of creeps or out of ammo */
                for (int i = charge.getAmmo(); i > 0; i--) {
                    System.out.println(spatial.getName()+" ammo left: "+i);
                    for (CreepControl creep : reachable) {
                        /* show a laser beam from tower to the creep that got hit. the laser visuals are slightly random. */
                        Vector3f hit = creep.getLoc();
                        Line beam = new Line(
                                getTowerTop(),
                                new Vector3f(
                                hit.x + FastMath.rand.nextFloat() / 4,
                                hit.y + FastMath.rand.nextFloat() / 4,
                                hit.z + FastMath.rand.nextFloat() / 4));
                        Geometry beam_geo = new Geometry("Beam", beam);
                        beam_geo.setMaterial(charge.getBeamMaterial());
                        getBeamNode().attachChild(beam_geo);
                        /* the laser beam has an effect */
                        creep.addHealth(charge.getHealthImpact()); // all towers do damage to target
                        creep.addSpeed(charge.getSpeedImpact());   // freeze towers also slow target down
                        // TODO: consider blastrange factor, e.g. for nukes
                        /** shooting uses up ammo in this charge */
                        charge.addAmmo(-1);
                        if (charge.getAmmo() <= 0) {
                            /** this charge is out of ammo, stop shooting until next turn. */
                            removeCharge(charge);
                            // update visuals to reflect current charge status
                            getDotNode().detachAllChildren();
                            for (Charge c : getCharges()) {
                                getDotNode().attachChild(makeDot(c, getCharges().indexOf(c), getLoc()));
                            }
                            break;
                            // TODO: this shoots very fast, add a sleep timer?
                        }
                    }
                }
            }
        } else {
            // else this tower has no ammo
            getDotNode().detachAllChildren();

        }
    }

    private Geometry makeDot(Charge a, int i, Vector3f loc) {
        Sphere dot = new Sphere(10, 10, .1f);
        Geometry dot_geo = new Geometry("ChargeMarkerDot", dot);
        dot_geo.setMaterial(a.getBeamMaterial());
        int offset_x = (getIndex() % 2 == 0 ? 1 : -1);
        dot_geo.setLocalTranslation(loc.x - (offset_x * 0.33f), loc.y - (i * .25f) + 1, loc.z);
        return dot_geo;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addCharge(Charge a) {
        charges.add(a);
    }

    public int getChargeNum() {
        return charges.size();
    }

    public void removeCharge(Charge a) {
        charges.remove(a);
    }

    
    public Node getCreeps() {
        return (Node)spatial.getUserData("creepNode");
    }

    /**
     * The location and height of the tower is needed to calculate range.
     * @return the location of the tip of this tower
     */
    public Vector3f getTowerTop() {
        Vector3f loc = getLoc();
        return new Vector3f(loc.x, loc.y + getHeight() / 2 , loc.z);
    }

    public Vector3f getLoc() {
        return spatial.getLocalTranslation();
    }
    
   public Node getBeamNode() {
        return (Node)spatial.getUserData("beamNode");
    }

    public float getHeight() {
        return (Float)spatial.getUserData("towerHeight");
    }

    public Charge getAttack(int i) {
        return charges.get(i);
    }

    public List<Charge> getCharges() {
        return charges;
    }

    public Charge getNextCharge() {
        return charges.get(charges.size() - 1);
    }

    public int getIndex() {
        return (Integer)spatial.getUserData("index");
    }

    private Node getDotNode() {
       return spatial.getUserData("dotNode");
    }

}
