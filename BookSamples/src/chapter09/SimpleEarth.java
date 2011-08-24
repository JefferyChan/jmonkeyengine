/*
 * Copyright (c) 2009-2010 jMonkeyEngine All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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
 */
package chapter09;

import jme3tools.converters.ImageToAwt;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * Demonstrates how to use terrain.
 *
 * @author bowens
 */
public class SimpleEarth extends SimpleApplication {

  private TerrainQuad terrain;
  Material matRock;

  public static void main(String[] args) {
    SimpleEarth app = new SimpleEarth();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(100f);
    cam.setLocation(new Vector3f(0, 10, -10));
    cam.lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);

    // TERRAIN TEXTURE material
    matRock = new Material(assetManager,
            "Common/MatDefs/Terrain/Terrain.j3md");
    matRock.setBoolean("useTriPlanarMapping", false);

    // ALPHA map (for splat textures)
    matRock.setTexture("Alpha", assetManager.loadTexture(
            "Textures/Terrain/splat/alphamap.png"));

    // HEIGHTMAP image (for the terrain heightmap)
    Texture heightMapImage =
            assetManager.loadTexture(
            "Textures/Terrain/splat/mountains512.png");

    // GRASS texture
    Texture grass = assetManager.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
    grass.setWrap(WrapMode.Repeat);
    matRock.setTexture("Tex1", grass);
    matRock.setFloat("Tex1Scale", 64);

    // ROCK texture
    Texture dirt = assetManager.loadTexture(
            "Textures/Terrain/splat/rock.png");
    dirt.setWrap(WrapMode.Repeat);
    matRock.setTexture("Tex2", dirt);
    matRock.setFloat("Tex2Scale", 16);

    // PAVEMENT texture
    Texture rock = assetManager.loadTexture(
            "Textures/Terrain/splat/road.png");
    rock.setWrap(WrapMode.Repeat);
    matRock.setTexture("Tex3", rock);
    matRock.setFloat("Tex3Scale", 128);

    // CREATE HEIGHTMAP
    AbstractHeightMap heightmap = null;
    try {
      heightmap = new ImageBasedHeightMap(ImageToAwt.convert(
              heightMapImage.getImage(), false, true, 0), 1f);
      heightmap.load();
    } catch (Exception e) {
      e.printStackTrace();
    }

    terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());//, new LodPerspectiveCalculatorFactory(getCamera(), 4)); // add this in to see it use entropy for LOD calculations
    TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
    terrain.addControl(control);
    terrain.setMaterial(matRock);
    terrain.setLocalTranslation(0, -100, 0);
    terrain.setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(terrain);

    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

  }
}
