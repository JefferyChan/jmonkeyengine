package chapter04;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/** A template how to create an Application State.
 * This example state simply changes the background color 
 * depending on the camera position. */
public class MyAppState extends AbstractAppState {

    private Application app;

    @Override
    public void update(float tpf) {
        Vector3f v = app.getViewPort().getCamera().getLocation();
        ColorRGBA color = new ColorRGBA(
                v.getX() / 10,
                v.getY() / 10,
                v.getZ() / 10, 1);
        app.getViewPort().setBackgroundColor(color);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
    }

    public void setApp(Application app) {
        this.app = app;
    }
}