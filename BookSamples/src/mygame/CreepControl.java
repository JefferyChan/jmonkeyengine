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


  private CreepData theCreep;

  public CreepControl(CreepData cd) {
    this.theCreep = cd;
  }

  public Boolean isAlive() {
    return theCreep.getHealth() > 0;
  }

  @Override
  protected void controlUpdate(float tpf) {

    if (theCreep.isAlive()) {
      Vector3f newloc = new Vector3f(
              spatial.getLocalTranslation().getX(),
              spatial.getLocalTranslation().getY(),
              spatial.getLocalTranslation().getZ()
              - (theCreep.getSpeed() * tpf * FastMath.rand.nextFloat()));
      if (newloc.z > 0) {
        // if not there yet, creep keeps walking towards player base
        spatial.setLocalTranslation(newloc);
        theCreep.setLoc(newloc);
      } else {
        // creep has reached player base and performs kamikaze attack!
        theCreep.kamikaze();
        theCreep.getPlayer().addHealthMod(-1);
        spatial.removeFromParent();
        spatial.removeControl(this);
      }
    } else {
      // player's towers killed the creep. Reward: increase player budget.
        theCreep.getPlayer().eliminateCreep();
        theCreep.getPlayer().addBudgetMod(2);
        spatial.removeFromParent();
        spatial.removeControl(this);
    }
  }

  @Override
  protected void controlRender(RenderManager rm, ViewPort vp) {
  }

  public Control cloneForSpatial(Spatial spatial) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
