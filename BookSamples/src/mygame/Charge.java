package mygame;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import java.io.IOException;

/** 
 * This is one charge. One charge contains one or more shots of the same type.
 * Each Charge type does a certain combination of speed damage, 
 * health damage, and blast damage. Charges are created by the factory class
 * every time the user selects a tower and presses an action key.
 * A tower can hold several charges.
 * @author zathras
 */
public class Charge implements Savable {

    private float speedDamage;
    private float healthDamage;
    private int ammo;
    private float blast;
    private float range;
    private Material beam_mat;

    /** 
     * This creates one charge type.
     * @param s SpeedDamage is a negative number if this is a freezetower 
     * @param h Healthdamage is a negative number how much damage it deals per shot
     * @param a Ammo is a positive number how many shots are in one charge.
     * @param r Range a distance how far the tower can shoot this charge.
     * @param b The blast range how far away neighbouring creeps are also damaged
     *  (can be used for nuke tower) 
     * @param m A colored material that is used for the laser beam.
     */
    public Charge(float s, float h, int a, float r, float b, Material m) {
        this.healthDamage = h;
        this.speedDamage = s;
        this.ammo = a;
        this.blast = b;
        this.range = r;
        this.beam_mat = m;
    }

    /** How much Ammo this charge has*/
    public int getAmmoNum() {
        return ammo;
    }

    /** deplete ammo by subtracting (adding negative numbers) */
    public void addAmmo(int mod) {
        ammo += mod;
    }
    
    /** how much impact on speed one shot of this ammo has. 
     * typically a negative number, used to slow down creeps. */
    public float getSpeedImpact() {
        return speedDamage;
    }

    /** how much impact on health one shot of this ammo has.
    Typically a negative number to weaken creeps. */
    public float getHealthImpact() {
        return healthDamage;
    }

    public float getBlast() {
        return blast;
    }

    public float getRange() {
        return range;
    }

    public Material getBeamMaterial() {
        return beam_mat;
    }

    /** ----------------------------------------------------------- */

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(ammo, "ammo", 1);
        capsule.write(healthDamage, "healthDamage", 1f);
        capsule.write(speedDamage, "speedDamage", 1f);
        capsule.write(blast, "blast", 1f);
        capsule.write(range, "range", 1f);
        capsule.write(beam_mat, "beam_mat", new Material());
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        ammo = capsule.readInt("ammo", 1);
        healthDamage = capsule.readFloat("healthDamage", 1);
        speedDamage = capsule.readFloat("speedDamage", 1);
        blast = capsule.readFloat("blast", 1);
        range = capsule.readFloat("range", 1);
        beam_mat = (Material) capsule.readSavable("beam_mat", new Material());
    }
}
