package mygame;

/**
 *
 */
public class PlayerData {

  private int health;
  private int budget;
  private int creepskilled;

  public PlayerData(int h, int b, int kk) {
    this.health = h;
    this.budget = b;
    this.creepskilled=kk;
  }

  /**
   * @param mod (typically) a negative number 
   * by how much to decrease the player's health.
   */
  public void addHealthMod(int mod) {
    this.health += mod;
  }

  public void eliminateCreep() {
    // player tracks how many creeps are out of the game, 
    // either killed by towers killed or reached base
    this.creepskilled++;
  }
  public int getEliminatedCreeps() {
    return this.creepskilled;
  }

  public int getBudget() {
    return budget;
  }

  /**
   * @param mod (typically) a negative number 
   * by how much to decrease the player's budget.
   */
  public void addBudgetMod(int mod) {
    this.budget += mod;
  }

  public int getHealth() {
    return health;
  }
}