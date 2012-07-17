package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;


/** Materials with bump maps and Phong illumination. */
public class MaterialLighting extends SimpleApplication {

  public static void main(String[] args) {
    MaterialLighting app = new MaterialLighting();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50);
    
    /** A ball with a smooth shiny pebbly surface */
    Sphere rock_mesh = new Sphere(16,16,1);
    TangentBinormalGenerator.generate(rock_mesh);           // Generate Normals for bump maps!
    Geometry rock_geo = new Geometry("Shiny rock", rock_mesh);
    rock_mesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
    Material rock_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    rock_mat.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/Pebbles/Pebbles_diffuse.png"));
//    rock_mat.setTexture("NormalMap",
//            assetManager.loadTexture("Textures/Pebbles/Pebbles_normal.png"));
    rock_mat.setFloat("Shininess", 10);    // [0,128]
    rock_mat.setBoolean("UseMaterialColors",true);
    rock_mat.setColor("Ambient",ColorRGBA.White);
    rock_mat.setColor("Specular",ColorRGBA.White);
    rock_mat.setColor("Diffuse",ColorRGBA.White);
    rock_geo.setMaterial(rock_mat);
    rock_geo.move(0,0,0); 
    rock_geo.rotate(FastMath.DEG_TO_RAD*90, 0, 0);
    rootNode.attachChild(rock_geo);
   
    /** A wall with a rough bricky surface */
    Box wall_mesh = new Box(2,2,2);
    TangentBinormalGenerator.generate(wall_mesh);           // Generate Normals for bump maps!
    Geometry wall_geo = new Geometry("bumpy brick wall", wall_mesh);
    Material wall_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    wall_mat.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_diffuse.jpg"));
//    wall_mat.setTexture("NormalMap",
//            assetManager.loadTexture("Textures/BrickWall/BrickWall_normal.jpg"));
    wall_mat.setFloat("Shininess", 10);    // [0,128]
    wall_mat.setBoolean("UseMaterialColors",true);
    wall_mat.setColor("Ambient",ColorRGBA.White);
    wall_mat.setColor("Specular",ColorRGBA.White);
    wall_mat.setColor("Diffuse",ColorRGBA.White);
    wall_geo.setMaterial(wall_mat);
    wall_geo.setLocalTranslation(0,-3,0);   // Move it a bit
    rootNode.attachChild(wall_geo);

    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1,-1.1f,-1.2f));
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
    
    /** A white, spot light source. */ 
//    PointLight lamp = new PointLight();
//    lamp.setPosition(new Vector3f(-3,3,5));
//    lamp.setColor(ColorRGBA.White);
//    rootNode.addLight(lamp); 
    
        /** A white ambient light source. */ 
//    AmbientLight ambient = new AmbientLight();
//    ambient.setColor(ColorRGBA.White.mult(5f));
//    rootNode.addLight(ambient); 
  }
}