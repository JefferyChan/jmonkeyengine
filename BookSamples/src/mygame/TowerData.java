package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * A tower contains zero or more charges, and has a location.
 * offset_x : Odd indexes are left (-x), even right (+x).
 * index : Tower index from 0 to n.
 * scale : Scene distances and sizes can be scaled to fit models.
 * towerHeight : Geo is shifted up to not sink into ground. <br />

 */
public class TowerData {

  private List<Charge> charges;
  private int index;
  private float towerHeight;
  private float scale;
  private int max;
  private Vector3f loc;
  private List<CreepData> creepData;

  public TowerData(int index, float towerHeight, float scale, int max, List<CreepData> cd) {
    this.index = index;
    this.max = max;
    this.scale = scale;
    this.creepData = cd;
    this.towerHeight = towerHeight;
    charges = new ArrayList<Charge>();

    int offset_x = (index % 2 == 0 ? 1 : -1);
    // loc is centered, it's not at the feet of the object.
    this.loc = new Vector3f(
            scale * 2.5f * offset_x,
            scale * towerHeight / 2,
            scale * ((index / 2f) + 1f) * 2);
  }

  public void addCharge(Charge a) {
    charges.add(a);
  }

  public int getChargeNum() {
    return charges.size();
  }

  public List<CreepData> getCreepData() {
    return creepData;
  }

  public void removeCharge(Charge a) {
    charges.remove(a);
  }

  public int getMax() {
    return this.max;
  }

  public void setMax(int m) {
    this.max = m;
  }

  /**
   * The location and height of the tower is needed to calculate range.
   * @return the location of the tip of this tower
   */
  public Vector3f getTop() {
    return new Vector3f(loc.x, loc.y + towerHeight / 2 * scale, loc.z);
  }

  public Vector3f getLoc() {
    return loc;
  }

  public float getHeight() {
    return towerHeight;
  }

  public float getScale() {
    return scale;
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
    return index;
  }

}
