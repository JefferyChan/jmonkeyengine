package mygame;

import com.jme3.export.Savable;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * Creeps count their own health, move towards base, only forwards in straight line; 
 * they can walk through each other. Creeps substract player‘s health at arrival.
 * @author zathras
 */
public class CreepControl extends AbstractControl implements Savable, Cloneable {

    private final float speed_min = 0.5f;

    public CreepControl() {}
    
    @Override
    protected void controlUpdate(float tpf) {

        if (isAlive()) {
            Vector3f newloc = new Vector3f(
                    spatial.getLocalTranslation().getX(),
                    spatial.getLocalTranslation().getY(),
                    spatial.getLocalTranslation().getZ()
                    - (getSpeed() * tpf * FastMath.rand.nextFloat()));
            if (newloc.z > 0) {
                /* if creep has not yet reached playerbase at z=0,
                 * thaw a bit (or regenerate speed, in any case), 
                 * and keep walking towards playerbase */
                addSpeed(getPlayer().getLevel() / 10);
                setLoc(newloc);
            } else {
                // creep has reached player base and performs kamikaze attack!
                kamikaze();
            }
        } else {
            // tower kills creep. Reward: increase player budget.
            getPlayer().addBudgetMod(getPlayer().getLevel()/2);
            remove();
        }
    }

    /** ---------------------------------------------- */

    public void setHealth(float h) {
        spatial.setUserData("health", h);
    }

    public float getHealth() {
        return (Float) spatial.getUserData("health");
    }

    public Boolean isAlive() {
        return getHealth() > 0f;
    }

    /**
     * @param mod (typically) a negative number 
     * by how much to decrease the creep's health.
     */
    public void addHealth(float mod) {
        spatial.setUserData("health", getHealth() + mod);
    }

    /** Creep commits kamikaze when attacking the base. 
     * impact depends on creeps remaining health.
     */
    public void kamikaze() {
        getPlayer().addHealthMod(getHealth() / -10);
        setHealth(0f);
        remove();
    }

    /** ---------------------------------------------- */
    
    /**
     * @param mod (typically) a negative number 
     * by how much to decrease the creep's speed.
     */
    public void addSpeed(float mod) {
        spatial.setUserData("speed", getSpeed() + mod);
        if (getSpeed() < speed_min) {
            spatial.setUserData("speed", speed_min);
        }
    }
    
    public float getSpeed() {
        return (Float) spatial.getUserData("speed");
    }
    
    /** ---------------------------------------------- */

    public void setLoc(Vector3f loc) {
        spatial.setLocalTranslation(loc);
    }

    public Vector3f getLoc() {
        return spatial.getLocalTranslation();
    }

    /** ---------------------------------------------- */

    public int getIndex() {
        return (Integer) spatial.getUserData("index");
    }

    public PlayerBaseControl getPlayer() {
        return ((PlayerBaseControl) spatial.getUserData("playerdata"));
    }

    public void remove() {
        spatial.removeFromParent();
        spatial.removeControl(this);

    }

    /** ---------------------------------------------- */

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
