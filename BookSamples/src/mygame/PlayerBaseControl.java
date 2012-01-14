package mygame;

import com.jme3.export.Savable;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * Playerbase is the central game data object. It manages 
 * the level, score, player budget, player health.
 * Since the user controls the player, the update() method does nothing.
 */
public class PlayerBaseControl extends AbstractControl implements Savable, Cloneable {

  public PlayerBaseControl() {
  }

  public int getLevel() {
    return (Integer) spatial.getUserData("level");
  }

  public int getScore() {
    return (Integer) spatial.getUserData("score");
  }

  /**
   * Modifies the player score by adding to it.
   * @param mod is (typically) a positive value added to the player score.
   */
  public void addScoreMod(int mod) {
    spatial.setUserData("score", getScore() + mod);
  }

  public float getHealth() {
    float f = (Float) spatial.getUserData("health");
    return Math.round(f * 100) / 100; // drop the decimals
  }

  /**
   * Modifies the player health by adding a positive or negative number to it.
   * @param mod is a value added to the player budget.
   */
  public void addHealthMod(float mod) {
    spatial.setUserData("health", (Float) spatial.getUserData("health") + mod);
  }

  public int getBudget() {
    return (Integer) spatial.getUserData("budget");
  }

  /**
   * Modifies the player budget by adding to it.
   * @param mod is often a negative value substracted from the player budget.
   */
  public void addBudgetMod(int mod) {
    spatial.setUserData("budget", (Integer) spatial.getUserData("budget") + mod);
  }

  @Override
  protected void controlUpdate(float tpf) {
    // nothing
  }

  @Override
  protected void controlRender(RenderManager rm, ViewPort vp) {
    // nothing
  }

  public Control cloneForSpatial(Spatial spatial) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
