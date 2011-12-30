package mygame;

import com.jme3.export.Savable;
import com.jme3.material.Material;
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
 * Tower has charges. It regularly fires at all creeps nearby until out of ammo.
 * Its current Charge status is visualized by colored spheres (ChargeMarkers). 
 * In each level, a tower can have level+1 Charges.
 */
public class TowerControl extends AbstractControl implements Savable, Cloneable {

    private List<Charge> charges = new ArrayList<Charge>();
    float timer=0f;
    public TowerControl() {}

    @Override
    protected void controlUpdate(float tpf) {
        timer+=tpf;
        /* Test whether this tower is loaded with charges. */
        if (getChargeNum() > 0) {
            // Tower is loaded: Tower can attack.
            updateMyChargeMarkers();
            /* Load the cannon with the first charge */
            Charge charge = popNextCharge();
            /* Identify reachable creeps */
            List<CreepControl> reachable = new ArrayList<CreepControl>();
            List<Spatial> creeps = getCreepNode().getChildren();
            for (Spatial creep_geo : creeps) {
                CreepControl creep = creep_geo.getControl(CreepControl.class);
                // Depends on creep's distance to tower top versus range of charge:
                if (creep.isAlive()
                        && getTowerTop().distance(creep.getLoc()) < charge.getRange()) {
                    reachable.add(creep);
                }
            }
            /* If the loaded tower can reach at least one creep, then... */
            if (reachable.size() > 0 && timer > .5f) {
                /* ... shoot at each reachable creep once, 
                 until out of reachable creeps or charge out of ammo */
                for (int ammo = charge.getAmmoNum(); ammo > 0; ammo--) {
                    for (CreepControl creep : reachable) {
                        /* Show a laser beam from tower to the creep that got hit. 
                         * The laser visuals are slightly random. */
                        Vector3f hit = creep.getLoc();
                        Line beam = new Line(
                                getTowerTop(),
                                new Vector3f(
                                hit.x + FastMath.rand.nextFloat() / 10f,
                                hit.y + FastMath.rand.nextFloat() / 10f,
                                hit.z + FastMath.rand.nextFloat() / 10f));
                        Geometry beam_geo = new Geometry("Beam", beam);
                        beam_geo.setMaterial(charge.getBeamMaterial());
                        getBeamNode().attachChild(beam_geo);
                        /* The laser beam has an effect on the creep */
                        creep.addHealth(charge.getHealthImpact()); // all towers do damage to target
                        creep.addSpeed(charge.getSpeedImpact());   // freeze towers also slow target down
                         // additional blast impact on neighbours of this creep
                        blast(creep, charge.getBlast(), charge.getHealthImpact(), charge.getSpeedImpact() );
                        /** Shooting uses up ammo in this charge */
                        charge.addAmmo(-1);
                        if (charge.getAmmoNum() <= 0) {
                            // this charge is out of ammo, discard the charge.
                            ammo=0;
                            removeCharge(charge);
                            // update visuals 
                            updateMyChargeMarkers();
                            // pause to reload, that is, stop shooting until next turn.
                            break;
                            // TODO: this still shoots very fast, break earlier? add a sleep timer?
                        }
                    }
                }
                timer=0f;
            }
        } else {
            // this tower has no ammo and displays no ChargeMarkers
            getChargeMarkerNode().detachAllChildren();

        }
    }

    /** --------------------------------------- */

    private Node getChargeMarkerNode() {
        return (Node) spatial.getUserData("dotNode");
    }

    /** Resets outdated chargeMarkers of this tower and displays the current ones. */
    private void updateMyChargeMarkers() {
        getChargeMarkerNode().detachAllChildren();
        for (int index = 0; index < getChargeNum(); index++ ) {
            getChargeMarkerNode().attachChild(makeChargeMarker(index));
        }
    }

    /** Creates a new ChargeMarker in the color of the beam of the Charge
     * and attaches it to the top of the tower, under the previous one 
     * (offset depends on charge index and by tower index/position). */
    private Geometry makeChargeMarker(int chargeIndex) {
        Vector3f loc = spatial.getLocalTranslation();
        Charge charge = getCharge(chargeIndex);
        Sphere dot = new Sphere(10, 10, .1f);
        Geometry chargeMarker_geo = new Geometry("ChargeMarker", dot);
        chargeMarker_geo.setMaterial(charge.getBeamMaterial());
        int offset_x = (getIndex() % 2 == 0 ? 1 : -1);
        chargeMarker_geo.setLocalTranslation(
                loc.x - (offset_x * 0.33f), 
                loc.y - (chargeIndex * .25f) + 1, 
                loc.z);
        return chargeMarker_geo;
    }

    public void addCharge(Charge a) {
        charges.add(a);
    }

    public int getChargeNum() {
        return charges.size();
    }

    /** Returns a copy of a certain Charge without popping it from the queue */
    public Charge getCharge(int i) {
        return ((ArrayList<Charge>)((ArrayList)charges).clone()).get(i); // TODO  get, not pop, from arraylist?
    }

    /** returns the first Charge (FIFO?) and pops it from the queue */
    public Charge popNextCharge() {
        return charges.get(charges.size() - 1);
    }

    public void removeCharge(Charge a) {
        charges.remove(a);
    }

    /** --------------------------------------- */
    
    public float getHeight() {
        return (Float) spatial.getUserData("towerHeight");
    }


    public int getIndex() {
        return (Integer) spatial.getUserData("index");
    }

    public Node getBeamNode() {
        return (Node) spatial.getUserData("beamNode");
    }

    public Node getCreepNode() {
        return (Node) spatial.getUserData("creepNode");
    }

    /**
     * The location and height of the tower is needed to calculate range.
     * @return loc the coordinates of the top of this tower.
     */
    public Vector3f getTowerTop() {
        Vector3f loc = getLoc();
        return new Vector3f(loc.x, loc.y + getHeight() / 2, loc.z);
    }

    public Vector3f getLoc() {
        return spatial.getLocalTranslation();
    }
    
    private Material getBlastMaterial(){
    return (Material)spatial.getUserData("blastMaterial");
    }

    /** --------------------------------------- */

    /** A blast charge deals extra damage to neighbours of target, but it also
     * recharges their speed (thaws them). */
    private void blast(CreepControl creep, float blastRange, float health_damage, float speed_damage){
        Sphere s = new Sphere(16, 16, blastRange);
        Geometry blast_geo = new Geometry("Blast Range", s);
        blast_geo.setMaterial(getBlastMaterial());

        spatial.getParent().attachChild(blast_geo);
        blast_geo.setLocalTranslation(creep.getLoc());
        
        List<Spatial> creeps = (getCreepNode().getChildren());
        for (Spatial neighbour : creeps) {
            float dist = neighbour.getLocalTranslation().distance(creep.getLoc());
            if ( dist < blastRange && dist > 0f) {
                neighbour.getControl(CreepControl.class).addHealth(health_damage / 2f);
                neighbour.getControl(CreepControl.class).addSpeed(speed_damage / 2);
            }
        }
     //   spatial.getParent().detachChild(blast_geo);
    }
    
    /** --------------------------------------- */
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

 }
