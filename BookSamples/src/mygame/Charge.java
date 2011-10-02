package mygame;

import com.jme3.material.Material;

/**
 *
 */
public class Charge {

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
   * @param a Ammo is a positive number how many shots are this one charge.
   * @param r Range a distance (depending on <tt>scale</tt>) how far the tower can shoot this charge.
   * @param b The blast range (depending on <tt>scale</tt>) how far away 
   * neighbouring creeps are also damaged (not used yet, but can be used for nuke tower) 
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

}
