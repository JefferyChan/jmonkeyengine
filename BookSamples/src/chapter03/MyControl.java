package chapter03;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * A template for creating Controls for spatials. This control rotates 
 * the spatial it is added to. Usage: cube.addControl( new MyControl() );
 */
public class MyControl extends AbstractControl {

    @Override
    protected void controlUpdate(float tpf) {
        /** implement the spatial's behaviour */
        spatial.rotate(tpf, tpf, tpf);
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}