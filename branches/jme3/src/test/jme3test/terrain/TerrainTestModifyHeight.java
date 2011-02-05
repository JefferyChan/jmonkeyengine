/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jme3test.terrain;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;
import jme3tools.converters.ImageToAwt;

/**
 *
 * @author Brent Owens
 */
public class TerrainTestModifyHeight extends SimpleApplication {

    private TerrainQuad terrain;
	Material matTerrain;
	Material matWire;
	boolean wireframe = false;
    boolean triPlanar = false;
    boolean wardiso = false;
    boolean minnaert = false;
	protected BitmapText hintText;
    private float grassScale = 64;
    private float dirtScale = 16;
    private float rockScale = 128;

    public static void main(String[] args) {
        TerrainTestModifyHeight app = new TerrainTestModifyHeight();
        app.start();
    }


    @Override
	public void initialize() {
		super.initialize();

		loadHintText();
        initCrossHairs();
	}

    @Override
    public void update() {
        super.update();
        
        updateHintText();
    }

	@Override
	public void simpleInitApp() {
		setupKeys();

        // First, we load up our textures and the heightmap texture for the terrain

		// TERRAIN TEXTURE material
		matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matTerrain.setBoolean("useTriPlanarMapping", false);
        matTerrain.setBoolean("WardIso", true);

		// ALPHA map (for splat textures)
		matTerrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

		// HEIGHTMAP image (for the terrain heightmap)
		Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/topleft-height.png");

		// GRASS texture
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		matTerrain.setTexture("DiffuseMap", grass);
		matTerrain.setFloat("DiffuseMap_0_scale", grassScale);


		// DIRT texture
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		matTerrain.setTexture("DiffuseMap_1", dirt);
		matTerrain.setFloat("DiffuseMap_1_scale", dirtScale);

		// ROCK texture
		Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		matTerrain.setTexture("DiffuseMap_2", rock);
		matTerrain.setFloat("DiffuseMap_2_scale", rockScale);


        /*Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.png");
        normalMap0.setWrap(WrapMode.Repeat);
        Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
        normalMap1.setWrap(WrapMode.Repeat);
        Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
        normalMap2.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("NormalMap", normalMap0);
        matTerrain.setTexture("NormalMap_1", normalMap2);
        matTerrain.setTexture("NormalMap_2", normalMap2);
        */

		// WIREFRAME material
		matWire = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        matWire.setColor("Color", ColorRGBA.Green);


		// CREATE HEIGHTMAP
		AbstractHeightMap heightmap = null;
		try {
			//heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

			heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 1f);
			heightmap.load();

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
		 * terrain will be 513x513. It uses the heightmap we created to generate the height values.
		 */
		/**
		 * Optimal terrain patch size is 65 (64x64).
		 * The total size is up to you. At 1025 it ran fine for me (200+FPS), however at
		 * size=2049, it got really slow. But that is a jump from 2 million to 8 million triangles...
		 */
		terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());//, new LodPerspectiveCalculatorFactory(getCamera(), 4)); // add this in to see it use entropy for LOD calculations
		List<Camera> cameras = new ArrayList<Camera>();
		cameras.add(getCamera());
		TerrainLodControl control = new TerrainLodControl(terrain, cameras);
		terrain.addControl(control);
		terrain.setMaterial(matTerrain);
		terrain.setModelBound(new BoundingBox());
		terrain.updateModelBound();
		terrain.setLocalTranslation(0, -100, 0);
		terrain.setLocalScale(2f, 1f, 2f);
		rootNode.attachChild(terrain);

        DirectionalLight light = new DirectionalLight();
        light.setDirection((new Vector3f(-0.5f,-1f, -0.5f)).normalize());
        rootNode.addLight(light);

        AmbientLight ambLight = new AmbientLight();
        ambLight.setColor(new ColorRGBA(1f, 1f, 0.8f, 0.2f));
        rootNode.addLight(ambLight);

		getCamera().getLocation().y = 10;
		getCamera().setDirection(new Vector3f(0, -1.5f, -1));
	}

	public void loadHintText() {
		hintText = new BitmapText(guiFont, false);
		hintText.setSize(guiFont.getCharSet().getRenderedSize());
		hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
		hintText.setText("Hit 1 to raise terrain, hit 2 to lower terrain");
		guiNode.attachChild(hintText);
	}

    public void updateHintText() {
        int x = (int) getCamera().getLocation().x;
        int y = (int) getCamera().getLocation().y;
        int z = (int) getCamera().getLocation().z;
        hintText.setText("Hit 1 to raise terrain, hit 2 to lower terrain.  "+x+","+y+","+z);
    }

    protected void initCrossHairs() {
        //guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

	private void setupKeys() {
		flyCam.setMoveSpeed(50);
		inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addListener(actionListener, "wireframe");
        inputManager.addMapping("triPlanar", new KeyTrigger(KeyInput.KEY_P));
		inputManager.addListener(actionListener, "triPlanar");
        inputManager.addMapping("WardIso", new KeyTrigger(KeyInput.KEY_9));
		inputManager.addListener(actionListener, "WardIso");
        inputManager.addMapping("Minnaert", new KeyTrigger(KeyInput.KEY_0));
		inputManager.addListener(actionListener, "Minnaert");
        inputManager.addMapping("Raise", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addListener(actionListener, "Raise");
        inputManager.addMapping("Lower", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addListener(actionListener, "Lower");
	}

	private ActionListener actionListener = new ActionListener() {

		public void onAction(String name, boolean pressed, float tpf) {
			if (name.equals("wireframe") && !pressed) {
				wireframe = !wireframe;
				if (!wireframe) {
					terrain.setMaterial(matWire);
				} else {
					terrain.setMaterial(matTerrain);
				}
			} else if (name.equals("triPlanar") && !pressed) {
                triPlanar = !triPlanar;
                if (triPlanar) {
                    matTerrain.setBoolean("useTriPlanarMapping", true);
                    // planar textures don't use the mesh's texture coordinates but real world coordinates,
                    // so we need to convert these texture coordinate scales into real world scales so it looks
                    // the same when we switch to/from tr-planar mode
                    matTerrain.setFloat("DiffuseMap_0_scale", 1f/(float)(512f/grassScale));
                    matTerrain.setFloat("DiffuseMap_1_scale", 1f/(float)(512f/dirtScale));
                    matTerrain.setFloat("DiffuseMap_2_scale", 1f/(float)(512f/rockScale));
                }  else {
                    matTerrain.setBoolean("useTriPlanarMapping", false);
                    matTerrain.setFloat("DiffuseMap_0_scale", grassScale);
                    matTerrain.setFloat("DiffuseMap_1_scale", dirtScale);
                    matTerrain.setFloat("DiffuseMap_2_scale", rockScale);
                }
            } else if (name.equals("Raise")) {
                if (pressed) {
                    Vector3f intersection = getWorldIntersection();
                    if (intersection != null) {
                        adjustHeight(intersection, 8, 1);
                    }
                }
            } else if (name.equals("Lower")) {
                if (pressed) {
                    Vector3f intersection = getWorldIntersection();
                    if (intersection != null) {
                        adjustHeight(intersection, 8, -1);
                    }
                }
            }
            
		}
	};

    private void adjustHeight(Vector3f loc, float radius, float height) {
    
        // offset it by radius because in the loop we iterate through 2 radii
        int radiusStepsX = (int) (radius / terrain.getLocalScale().x);
        int radiusStepsZ = (int) (radius / terrain.getLocalScale().z);

        float xStepAmount = terrain.getLocalScale().x;
        float zStepAmount = terrain.getLocalScale().z;

        for (int z=-radiusStepsZ; z<radiusStepsZ; z++) {
			for (int x=-radiusStepsZ; x<radiusStepsX; x++) {

                float locX = loc.x + (x*xStepAmount);
                float locZ = loc.z + (z*zStepAmount);
                
				if (isInRadius(locX-loc.x,locZ-loc.z,radius)) {
                    // see if it is in the radius of the tool
					float h = calculateHeight(radius, height, locX-loc.x, locZ-loc.z);
                    
					// increase the height
					terrain.adjustHeight(new Vector2f(locX, locZ), h);
				}
			}
		}
        terrain.updateModelBound();
    }

    private boolean isInRadius(float x, float y, float radius) {
		Vector2f point = new Vector2f(x,y);
		// return true if the distance is less than equal to the radius
		return point.length() <= radius;
	}

    private float calculateHeight(float radius, float heightFactor, float x, float z) {
        // find percentage for each 'unit' in radius
        Vector2f point = new Vector2f(x,z);
        float val = point.length() / radius;
        val = 1 - val;
        if (val <= 0)
            val = 0;
        return heightFactor * val;
	}

    private Vector3f getWorldIntersection() {
        Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        int numCollisions = terrain.collideWith(ray, results);
        if (numCollisions > 0) {
            CollisionResult hit = results.getClosestCollision();
            return hit.getContactPoint();
        }
        return null;
    }
}
