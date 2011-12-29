package mygame;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import java.io.IOException;

/**
 *
 */
public class Charge implements Savable {

  private float speedDamage;
  private float healthDamage;
  private int ammo;
  private float blast;
  private float range;
  private Material beam_mat;

  /** 
   * This is one charge. A tower can hold several charges.
   * @param s SpeedDamage is a negative number if this is a freezetower 
   * @param h Healthdamage is a negative number how much damage it deals per shot
   * @param a Ammo is a positive number how many shots are in one charge.
   * @param r Range a distance how far the tower can shoot this charge.
   * @param b The blast range how far away neighbouring creeps are also damaged
   *  (TODO not used yet, but can be used for nuke tower) 
   * @param m A colored material that is used for the laser beam. */
  public Charge(float s, float h, int a, float r, float b, Material m) {
    this.healthDamage = h;
    this.speedDamage = s;
    this.ammo = a;
    this.blast = b;
    this.range = r;
    this.beam_mat = m;
  }

  public int getAmmo() {
    return ammo;
  }

  public float getSpeedImpact() {
    return speedDamage;
  }

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

  /** deplete ammo by subtracting  */
  public void addAmmo(int mod) {
    ammo += mod;
  }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(healthDamage, "healthDamage", 1);
        capsule.write(speedDamage, "speedDamage", 1);
        capsule.write(ammo, "ammo", 1);
        capsule.write(blast, "blast", 1);
        capsule.write(range, "range", 1);
        capsule.write(beam_mat, "beam_mat", new Material());
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        healthDamage = capsule.readFloat("healthDamage", 1);
        speedDamage = capsule.readFloat("speedDamage", 1);
        ammo = capsule.readInt("ammo", 1);
        blast = capsule.readFloat("blast", 1);
        range = capsule.readFloat("range", 1);
        beam_mat = (Material)capsule.readSavable("beam_mat", new Material());
    }

}
