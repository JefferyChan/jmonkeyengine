package mygame;

import com.jme3.math.Vector3f;

/**
 * 
 * Creep (counts its health, moves towards base, substracts player‘s health)
 * @author zathras
 */
public class CreepData {

  private int index;
  private float health;
  private float speed;
  private final float speed_min=0.5f;
  private Vector3f loc;
  private PlayerData playerData;

  public CreepData(int index, int health, float speed, Vector3f startloc, PlayerData pd) {
    this.index = index;
    this.health = health;
    this.speed = speed;
    this.playerData=pd;
    if(this.speed<speed_min) this.speed=speed_min; 
    this.loc = startloc;
  }

  public Boolean isAlive() {
    return health > 0;
  }

  /**
   * @param mod (typically) a negative number 
   * by how much to decrease the creep's health.
   */
  public void addHealth(float mod) {
    this.health += mod;
  }
  /** Creep commits kamikaze when attacking base 
   * and is no longer an active creep.
   */
  public void kamikaze() {
    this.health = 0;
    playerData.eliminateCreep();
  }
  /**
   * @param mod (typically) a negative number 
   * by how much to decrease the creep's speed.
   */
  public void addSpeed(float mod) {
    this.speed += mod;
    if(this.speed<speed_min) this.speed=speed_min; 
  }

 public void setLoc(Vector3f l) {
    this.loc=l;
  }


  public Vector3f getLoc() {
    return loc;
  }

  public int getIndex() {
    return index;
  }

  public float getSpeed() {
    return speed;
  }

  public float getHealth() {
    return health;
  }
  public PlayerData getPlayer() {
    return playerData;
  }
}
