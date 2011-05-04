package chapter4;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * A template for creating Controls for spatials. This control rotates 
 * the spatials it is added to, e.g. cube.addControl( new MyControl() );
 */
public class MyControl extends AbstractControl {

  @Override
  protected void controlUpdate(float tpf) {
    /*+ TODO -- implement the spatial's behaviour */
    spatial.rotate(tpf, tpf, tpf);
  }

  protected void controlRender(RenderManager rm, ViewPort vp) {
    /** unused */
  }

  public Control cloneForSpatial(Spatial spatial) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}