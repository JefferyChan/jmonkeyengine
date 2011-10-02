package mygame;

import com.jme3.math.Vector3f;
import java.util.List;

/**
 *
 */
public class PlanetData {

  private List<TowerData> towers;
  private List<CreepData> creeps;
  private PlayerData player;
  private int level;
  private int score=0;

  /** A planet objects gives access to game elements.
   * 
   * @param l The level,
   * @param c Creeps,
   * @param t Towers,
   * @param p Player.
   */
  public PlanetData(int l, List<CreepData> c, List<TowerData> t, PlayerData p) {
    this.creeps=c;
    this.player=p;
    this.towers=t;
    this.level=l;
  }

  /**
   * @return the towers
   */
  public List<TowerData> getTowers() {
    return towers;
  }

  /**
   * @return the creeps
   */
  public List<CreepData> getCreeps() {
    return creeps;
  }

  /**
   * @return the player
   */
  public PlayerData getPlayer() {
    return player;
  }

  /**
   * @return the level
   */
  public int getLevel() {
    return level;
  }

  /**
   * @return the score
   */
  public int getScore() {
    return score;
  }

  /**
   * @param (typically) a positive value added to the player score.
   */
  public void addScoreMod(int mod) {
    this.score += mod;
  }
}

