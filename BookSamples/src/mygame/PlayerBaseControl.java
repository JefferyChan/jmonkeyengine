package mygame;

import com.jme3.export.Savable;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * planet is the central game data object.
 */
public class PlayerBaseControl extends AbstractControl implements Savable, Cloneable {

    public PlayerBaseControl() {}

    
    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return (Integer)spatial.getUserData("level");
    }

    /**
     * @return the score
     */
    public int getScore() {
        return (Integer) spatial.getUserData("score");
    }

    /**
     * @param (typically) a positive value added to the player score.
     */
    public void addScoreMod(int mod) {
        spatial.setUserData("score", getScore()+mod);
    }
    
    public void addHealthMod(float mod) {
        spatial.setUserData("health", (Float) spatial.getUserData("health") + mod);
    }

   

    public int getBudget() {
        return (Integer) spatial.getUserData("budget");
    }

    /**
     * @param mod (typically) a negative number 
     * by how much to decrease the player's budget.
     */
    public void addBudgetMod(int mod) {
        spatial.setUserData("budget", (Integer) spatial.getUserData("budget") + mod);
    }

    public float getHealth() {
        return (Float) spatial.getUserData("health");
    }
}
