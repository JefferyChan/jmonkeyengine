/*
 * Created on Jan 20, 2004
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package jmetest.effects;

import com.jme.app.SimpleGame;
import com.jme.effects.ParticleController;
import com.jme.effects.ParticleSystem;
import com.jme.image.Texture;
import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.InputSystem;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.LoggingSystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;

/**
 * @author Ahmed
 */
public class TestParticleSystem
    extends SimpleGame {

  private ParticleSystem ps;
  private ParticleController pc;

  private Node root;
  private Node main;

  private Camera cam;

  private Timer timer;
  private InputHandler input;
  private KeyInput key;

  private Text fps;

  private Vector3f currentPos = new Vector3f();
  private Vector3f newPos = new Vector3f();
  private Node fpsNode;

  public static void main(String[] args) {
    TestParticleSystem app = new TestParticleSystem();
    app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
    app.start();
  }

  protected void update(float interpolation) {
    timer.update();
    input.update(timer.getTimePerFrame() * 10);

    if ( (int) currentPos.x == (int) newPos.x
        && (int) currentPos.y == (int) newPos.y
        && (int) currentPos.z == (int) newPos.z) {
      newPos.x = (float) Math.random() * 50 - 25;
      newPos.y = (float) Math.random() * 50 - 25;
      newPos.z = (float) Math.random() * 50 - 25;
    }

    currentPos.x -= (currentPos.x - newPos.x)
        / (timer.getFrameRate() / 2);
    currentPos.y -= (currentPos.y - newPos.y)
        / (timer.getFrameRate() / 2);
    currentPos.z -= (currentPos.z - newPos.z)
        / (timer.getFrameRate() / 2);

    ps.setLocalTranslation(currentPos);

    fps.print("FPS: " + (int) timer.getFrameRate() + " - " +
              display.getRenderer().getStatistics());
    main.updateGeometricState(timer.getTimePerFrame(), true);
    if (KeyBindingManager
        .getKeyBindingManager()
        .isValidCommand("PrintScrn")) {
      display.getRenderer().takeScreenShot("data/screeny");
    }
  }

  protected void render(float interpolation) {
    display.getRenderer().clearStatistics();
    display.getRenderer().clearBuffers();
    display.getRenderer().draw(main);
    display.getRenderer().draw(fpsNode);
  }

  protected void initSystem() {
    LoggingSystem.getLogger().setLevel(java.util.logging.Level.WARNING);
    try {
      display = DisplaySystem.getDisplaySystem(properties.getRenderer());
      display.createWindow(
          properties.getWidth(),
          properties.getHeight(),
          properties.getDepth(),
          properties.getFreq(),
          properties.getFullscreen());

      cam =
          display.getRenderer().getCamera(
          properties.getWidth(),
          properties.getHeight());
    }
    catch (JmeException e) {
      e.printStackTrace();
      System.exit(1);
    }

    display.getRenderer().setBackgroundColor(new ColorRGBA(0, 0, 0, 1));

    cam.setFrustum(1f, 1000f, -0.55f, 0.55f, 0.4125f, -0.4125f);

    Vector3f loc = new Vector3f(10, 0, 0);
    Vector3f left = new Vector3f(0, -1, 0);
    Vector3f up = new Vector3f(0, 0, 1f);
    Vector3f dir = new Vector3f( -1, 0, 0);
    cam.setFrame(loc, left, up, dir);

    display.getRenderer().setCamera(cam);

    timer = Timer.getTimer(properties.getRenderer());
    input = new FirstPersonHandler(this, cam, properties.getRenderer());
    input.setMouseSpeed(0.2f);
    input.setKeySpeed(1f);

    InputSystem.createInputSystem(properties.getRenderer());
    key = InputSystem.getKeyInput();
    KeyBindingManager.getKeyBindingManager().setKeyInput(key);
    KeyBindingManager.getKeyBindingManager().set(
        "PrintScrn",
        KeyInput.KEY_F1);
    display.setTitle("Particle System");
    display.getRenderer().enableStatistics(true);
  }

  protected void initGame() {
    root = new Node("Scene graph root");
    root.setForceView(true);
    main = new Node("Main node");
    main.setForceView(true);

    AlphaState as1 = display.getRenderer().getAlphaState();
    as1.setBlendEnabled(true);
    as1.setSrcFunction(AlphaState.SB_SRC_ALPHA);
    as1.setDstFunction(AlphaState.DB_ONE);
    as1.setTestEnabled(true);
    as1.setTestFunction(AlphaState.TF_GREATER);
    as1.setEnabled(true);

    TextureState ts = display.getRenderer().getTextureState();
    ts.setTexture(
        TextureManager.loadTexture(
        TestParticleSystem.class.getClassLoader().getResource(
        "jmetest/data/texture/flaresmall.jpg"),
        Texture.MM_LINEAR,
        Texture.FM_LINEAR,
        true));
    ts.setEnabled(true);

    TextureState font = display.getRenderer().getTextureState();
    font.setTexture(
        TextureManager.loadTexture(
        TestParticleSystem.class.getClassLoader().getResource(
        "jmetest/data/font/font.png"),
        Texture.MM_LINEAR,
        Texture.FM_LINEAR,
        true));
    font.setEnabled(true);


    fps = new Text("FPS label", "");
    fps.setRenderState(font);
    fps.setRenderState(as1);
    fps.setForceView(true);

    fpsNode = new Node("FPS node");
    fpsNode.attachChild(fps);
    fpsNode.setForceView(true);

    Node worldToParticle = new Node("Particle World");
    worldToParticle.setRenderState(ts);
    worldToParticle.setRenderState(as1);

    ps = new ParticleSystem("Particle System", worldToParticle, 300);
    ps.setStartColor(
        new ColorRGBA(1f, 0f, 0f, 1f));
    ps.setEndColor(new ColorRGBA(0f, 1f, 0f, 0f));
    ps.setSize(5, 1);
    ps.setGravity(new Vector3f(0, 0, 0));
    ps.setSpeed(1f);
    ps.setFriction(1f);
    ps.setFade(0.05f);
    ps.setStartPosition(new Vector3f( -50, 0, 0));
    ps.setSpeed(10);

    pc = new ParticleController(ps);
    pc.setRepeatType(Controller.RT_WRAP);
    ps.addController(pc);

    root.attachChild(ps);
    root.attachChild(worldToParticle);
    main.attachChild(root);
    root.updateGeometricState(0.0f, true);
    fpsNode.updateGeometricState(0.0f, true);

  }

  protected void reinit() {
  }

  protected void cleanup() {
  }
}
