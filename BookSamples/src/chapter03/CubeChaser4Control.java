package chapter03;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * This control works together with the CubeChaserState and CubeChaser4. 
 * This control is only used as a marker for certain spatials (it makes them spin). 
 * The AppState defines the actual behavior.
 */
public class CubeChaser4Control extends AbstractControl {


    public CubeChaser4Control() {
    }

    @Override
    protected void controlUpdate(float tpf) {
         spatial.rotate(tpf, tpf, tpf);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String hello(){
        return "Hello, my name is "+spatial.getName();
    }
}
