package chapter4;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

/** A template how to create an Application State.
 * This example state simply changes the background color 
 * depending on the camera position. */
public class MyAppState1 implements AppState {

  private Application app;

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    this.app = app;
  }

  public boolean isInitialized() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setEnabled(boolean active) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isEnabled() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void stateAttached(AppStateManager stateManager) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void stateDetached(AppStateManager stateManager) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void update(float tpf) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void render(RenderManager rm) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void postRender() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void cleanup() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setActive(boolean active) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isActive() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}