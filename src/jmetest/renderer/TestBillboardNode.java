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

import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.BillboardNode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;

/**
 * <code>TestLightState</code>
 * @author Mark Powell
 * @version $Id: TestBillboardNode.java,v 1.10 2004-04-24 23:06:48 renanse Exp $
 */
public class TestBillboardNode extends SimpleGame {

  /**
   * Entry point for the test,
   * @param args
   */
  public static void main(String[] args) {
    TestBillboardNode app = new TestBillboardNode();
    app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
    app.start();
  }

  /**
   * builds the trimesh.
   * @see com.jme.app.SimpleGame#initGame()
   */
  protected void simpleInitGame() {
    display.setTitle("Billboard test");
    cam.setLocation(new Vector3f(0.0f, 0.0f, 75.0f));
    cam.update();
    lightState.setEnabled(false);

    BillboardNode billboard = new BillboardNode("Billboard");
    billboard.setType(BillboardNode.AXIAL);

    Quad q = new Quad("Quad");
    q.initialize(1, 1);
    q.setLocalScale(3.0f);

    billboard.attachChild(q);
    rootNode.attachChild(billboard);

    TextureState ts = display.getRenderer().getTextureState();
    ts.setEnabled(true);
    Texture t1 = TextureManager.loadTexture(
        TestBoxColor.class.getClassLoader().getResource(
        "jmetest/data/images/Monkey.jpg"),
        Texture.MM_LINEAR,
        Texture.FM_LINEAR,
        true);
    ts.setTexture(t1);
    rootNode.setRenderState(ts);

  }
}
