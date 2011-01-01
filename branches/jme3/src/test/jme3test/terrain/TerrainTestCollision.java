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

import jme3tools.converters.ImageToAwt;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.nodes.PhysicsVehicleNode;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a terrain object and a collision node to go with it. Then
 * drops several balls from the sky that collide with the terrain
 * and roll around.
 * Left click to place a sphere on the ground where the crosshairs intersect the terrain.
 *
 * @author Brent Owens
 */
public class TerrainTestCollision extends SimpleBulletApplication {

    TerrainQuad terrain;
    Node terrainPhysicsNode;
    Material matRock;
    Material matWire;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;

    PhysicsVehicleNode vehicle;
    final float accelerationForce = 1000.0f;
    final float brakeForce = 100.0f;
    float steeringValue = 0;
    float accelerationValue = 0;
    Vector3f jumpForce = new Vector3f(0, 3000, 0);


    public static void main(String[] args) {
        TerrainTestCollision app = new TerrainTestCollision();
        app.start();
    }


    @Override
    public void initialize() {
        super.initialize();
        createVehicle();
        loadHintText();
        initCrossHairs();
    }

    @Override
    public void simpleInitApp() {
        setupKeys();

        // First, we load up our textures and the heightmap texture for the terrain

        // TERRAIN TEXTURE material
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

        // ALPHA map (for splat textures)
        matRock.setTexture("m_Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        // HEIGHTMAP image (for the terrain heightmap)
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex1", grass);
        matRock.setFloat("m_Tex1Scale", 64f);

        // DIRT texture
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex2", dirt);
        matRock.setFloat("m_Tex2Scale", 32f);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex3", rock);
        matRock.setFloat("m_Tex3Scale", 128f);

        // WIREFRAME material
        matWire = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        matWire.setColor("m_Color", ColorRGBA.Green);


        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

            heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(2, 2, 2));
        terrain.setModelBound(new BoundingBox());
        terrain.updateModelBound();
        terrain.setLocked(false); // unlock it so we can edit the height
        rootNode.attachChild(terrain);
        

        /**
         * Now we use the TerrainPhysicsShapeFactory to generate a heightfield
         * collision shape for us, and then add it to the physics node.
         */
        terrainPhysicsNode = new PhysicsNode(CollisionShapeFactory.createMeshShape(terrain),0);
        rootNode.attachChild(terrainPhysicsNode);
        getPhysicsSpace().addAll(terrainPhysicsNode);

        // Add 5 physics spheres to the world, with random sizes and positions
        // let them drop from the sky
        for (int i = 0; i < 5; i++) {
            float r = (float) (5 * Math.random());
            PhysicsNode physicsSphere = new PhysicsNode(new SphereCollisionShape(1 + r), 1);
            float x = (float) (20 * Math.random()) - 20;
            float y = (float) (20 * Math.random()) - 10;
            float z = (float) (20 * Math.random()) - 20;
            physicsSphere.setLocalTranslation(new Vector3f(x, 100 + y, z));
            physicsSphere.attachDebugShape(getAssetManager());
            rootNode.attachChild(physicsSphere);
            getPhysicsSpace().add(physicsSphere);
        }

        PhysicsCharacterNode character = new PhysicsCharacterNode(new SphereCollisionShape(1), 0.1f);
        character.setLocalTranslation(new Vector3f(0, 100, 0));
        character.attachDebugShape(assetManager);
        rootNode.attachChild(character);
        getPhysicsSpace().add(character);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, -0.5f, -0.1f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.50f, 0.40f, 0.50f, 1.0f));
        rootNode.addLight(dl);


        getCamera().getLocation().y = 25;
        getCamera().setDirection(new Vector3f(-1, 0, -1));
    }

    public void loadHintText() {
        hintText = new BitmapText(guiFont, false);
        hintText.setSize(guiFont.getCharSet().getRenderedSize());
        hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
        //hintText.setText("Hit T to switch to wireframe");
        hintText.setText("");
        guiNode.attachChild(hintText);
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
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Raise", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("Lower", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addListener(actionListener, "Lefts");
        inputManager.addListener(actionListener, "Rights");
        inputManager.addListener(actionListener, "Ups");
        inputManager.addListener(actionListener, "Downs");
        inputManager.addListener(actionListener, "Space");
        inputManager.addListener(actionListener, "Reset");
        inputManager.addListener(actionListener, "Raise");
        inputManager.addListener(actionListener, "Lower");
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "shoot");
    }

    private void createVehicle() {
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Red);

        //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.2f, 0.5f, 2.4f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));

        //create vehicle node
        vehicle = new PhysicsVehicleNode(new Node(), compoundShape, 800);

        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 60.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMaxSuspensionForce(10000.0f);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 0.5f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1f;
        float zOff = 2f;

        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.6f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        vehicle.addWheel(wheels1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        vehicle.addWheel(wheels2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        vehicle.addWheel(wheels3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        vehicle.addWheel(wheels4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        vehicle.attachDebugShape(assetManager);
        vehicle.setLocalTranslation(new Vector3f(-140,7,-23));
        rootNode.attachChild(vehicle);

        getPhysicsSpace().add(vehicle);
    }

    @Override
    public void update() {
        super.update();
        hintText.setText("vehicle location: " + vehicle.getLocalTranslation());
    }

    private void createCollisionMarker() {
        Sphere s = new Sphere(6, 6, 1);
        collisionMarker = new Geometry("collisionMarker");
        collisionMarker.setMesh(s);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Orange);
        collisionMarker.setMaterial(mat);
        rootNode.attachChild(collisionMarker);
    }
    
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String binding, boolean keyPressed, float tpf) {
            if (binding.equals("wireframe") && !keyPressed) {
                wireframe = !wireframe;
                if (!wireframe) {
                    terrain.setMaterial(matWire);
                } else {
                    terrain.setMaterial(matRock);
                }
            }

            if (binding.equals("shoot") && !keyPressed) {

                Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f );
                Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f );
                direction.subtractLocal(origin).normalizeLocal();
                
                
                Ray ray = new Ray(origin, direction);
                CollisionResults results = new CollisionResults();
                int numCollisions = terrain.collideWith(ray, results);
                if (numCollisions >0) {
                    CollisionResult hit = results.getClosestCollision();
                    if (collisionMarker == null) {
                        createCollisionMarker();
                    }
                    Vector2f loc = new Vector2f(hit.getContactPoint().x, hit.getContactPoint().y);
                    System.out.println("collide "+hit.getContactPoint()+", height: "+terrain.getHeight(loc));
                    collisionMarker.setLocalTranslation(hit.getContactPoint());
                }
            }

            if (binding.equals("Lefts")) {
                if (keyPressed) {
                    steeringValue += .5f;
                } else {
                    steeringValue += -.5f;
                }
                vehicle.steer(steeringValue);
            } else if (binding.equals("Rights")) {
                if (keyPressed) {
                    steeringValue += -.5f;
                } else {
                    steeringValue += .5f;
                }
                vehicle.steer(steeringValue);
            } else if (binding.equals("Ups")) {
                if (keyPressed) {
                    accelerationValue += accelerationForce;
                } else {
                    accelerationValue -= accelerationForce;
                }
                vehicle.accelerate(accelerationValue);
            } else if (binding.equals("Downs")) {
                if (keyPressed) {
                    vehicle.brake(brakeForce);
                } else {
                    vehicle.brake(0f);
                }
            } else if (binding.equals("Space")) {
                if (keyPressed) {
                    vehicle.applyImpulse(jumpForce, Vector3f.ZERO);
                }
            } else if (binding.equals("Reset")) {
                if (keyPressed) {
                    System.out.println("Reset");
                    vehicle.setLocalTranslation(-140,7,-23);
                    vehicle.setLocalRotation(new Quaternion());
                    vehicle.setLinearVelocity(Vector3f.ZERO);
                    vehicle.setAngularVelocity(Vector3f.ZERO);
                    vehicle.resetSuspension();
                } else {
                }
            } else if (binding.equals("Raise")) {
                if (keyPressed) {
                    Vector2f loc = new Vector2f(collisionMarker.getWorldTranslation().x, collisionMarker.getWorldTranslation().z);
                    float h = terrain.getHeight(loc);
                    terrain.setHeight(loc, h+1);
                }
            } else if (binding.equals("Lower")) {
                if (keyPressed) {
                    Vector2f loc = new Vector2f(collisionMarker.getWorldTranslation().x, collisionMarker.getWorldTranslation().z);
                    float h = terrain.getHeight(loc);
                    terrain.setHeight(loc, h-1);
                }
            }
        }

    };
}
