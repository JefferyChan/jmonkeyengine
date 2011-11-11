/*
 * Copyright (c) 2003-2004, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
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
package jmetest.renderer;

import java.net.URL;

import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.input.NodeHandler;
import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.light.SpotLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.TextureRenderer;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.model.Model;
import com.jme.scene.model.msascii.MilkshapeASCIIModel;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;

/**
 * <code>TestRenderToTexture</code>
 * @author Joshua Slack
 */
public class TestCameraMan extends SimpleGame {
  private Model model;
  private Node monitorNode;
  private CameraNode camNode;

  private TextureRenderer tRenderer;
  private Texture fakeTex;

  /**
   * Entry point for the test,
   * @param args
   */
  public static void main(String[] args) {
    TestCameraMan app = new TestCameraMan();
    app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
    app.start();
  }

  protected void simpleUpdate() {
    monitorNode.updateGeometricState(0.0f, true);
  }

  protected void simpleRender() {
    tRenderer.render(model, fakeTex);
    display.getRenderer().draw(monitorNode);
  }

  /**
   * builds the trimesh.
   * @see com.jme.app.SimpleGame#initGame()
   */
  protected void simpleInitGame() {
    cam.setLocation(new Vector3f(0.0f, 50.0f, 100.0f));
    cam.update();

    tRenderer = display.createTextureRenderer(256, 256, false, true, false, false,
                                              TextureRenderer.RENDER_TEXTURE_2D,
                                              0);
    camNode = new CameraNode("Camera Node", tRenderer.getCamera());
    camNode.setLocalTranslation(new Vector3f(0, 50, -50));
    camNode.updateGeometricState(0, true);

    // Setup the input controller and timer
    input = new NodeHandler(this, camNode, "LWJGL");
    input.setKeySpeed(10f);
    input.setMouseSpeed(1f);

    display.setTitle("Camera Man");

    DirectionalLight am = new DirectionalLight();
    am.setDiffuse(new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f));
    am.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
    am.setDirection(new Vector3f(1, 0, 0));

    LightState state = display.getRenderer().getLightState();
    state.setEnabled(true);
    state.attach(am);
    am.setEnabled(true);

    SpotLight sl = new SpotLight();
    sl.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
    sl.setAmbient(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
    sl.setDirection(new Vector3f(0, 0, 1));
    sl.setLocation(new Vector3f(0, 0, 0));
    sl.setAngle(25);
    sl.setEnabled(true);

    rootNode.setRenderState(state);

    model = new MilkshapeASCIIModel("Milkshape Model");
    URL modelURL = TestCameraMan.class.getClassLoader().getResource(
        "jmetest/data/model/msascii/run.txt");
    model.load(modelURL, "jmetest/data/model/msascii/");
    model.getAnimationController().setActive(false);
    rootNode.attachChild(model);

    CullState cs = display.getRenderer().getCullState();
    cs.setCullMode(CullState.CS_BACK);
    cs.setEnabled(true);
    rootNode.setRenderState(cs);
    model.setRenderState(cs);

    lightState.detachAll();
    LightNode cameraLight = new LightNode("Camera Light", lightState);
    cameraLight.setLight(sl);
    cameraLight.setTarget(model);

    Model camBox = new MilkshapeASCIIModel("Camera Box");
    URL camBoxUrl = TestCameraMan.class.getClassLoader().getResource(
        "jmetest/data/model/msascii/camera.txt");
    camBox.load(camBoxUrl, "jmetest/data/model/msascii/");
    camNode.attachChild(camBox);
    camNode.attachChild(cameraLight);
    rootNode.attachChild(camNode);

    monitorNode = new Node("Monitor Node");
    Quad quad = new Quad("Monitor");
    quad.initialize(3, 3);
    quad.setLocalTranslation(new Vector3f(3.75f, 52.5f, 90));
    monitorNode.attachChild(quad);

    Quad quad2 = new Quad("Monitor");
    quad2.initialize(3.4f, 3.4f);
    quad2.setLocalTranslation(new Vector3f(3.95f, 52.6f, 89.5f));
    monitorNode.attachChild(quad2);

    // Setup our params for the depth buffer
    ZBufferState buf = display.getRenderer().getZBufferState();
    buf.setEnabled(true);
    buf.setFunction(ZBufferState.CF_LEQUAL);

    monitorNode.setRenderState(buf);

    // Ok, now lets create the Texture object that our monkey cube will be rendered to.

//        tRenderer.setBackgroundColor(new ColorRGBA(.667f, .667f, .851f, 1f));
    tRenderer.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 1f));
    fakeTex = tRenderer.setupTexture();
    TextureState screen = display.getRenderer().getTextureState();
    screen.setTexture(fakeTex);
    screen.setEnabled(true);
    quad.setRenderState(screen);

    monitorNode.updateGeometricState(0.0f, true);
    monitorNode.updateRenderState();
  }

}