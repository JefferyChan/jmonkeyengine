package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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
    
    /** A ball with a smooth shiny pebbly surface */
    Sphere rock = new Sphere(16,16,2);
    Geometry shiny_rock = new Geometry("Shiny rock", rock);
    rock.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
    TangentBinormalGenerator.generate(rock);           // Generate Normals for bump maps!
    Material mat_pebb = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    mat_pebb.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/Pebbles/Pebbles_diffuse.png"));
    mat_pebb.setTexture("NormalMap",
            assetManager.loadTexture("Textures/Pebbles/Pebbles_normal.png"));
    mat_pebb.setFloat("Shininess", 4f);    // [0,128]
    shiny_rock.setMaterial(mat_pebb);
    shiny_rock.setLocalTranslation(0,1,0); // Move it a bit
    shiny_rock.rotate(1.6f, 0, 0);         // Rotate it a bit
    rootNode.attachChild(shiny_rock);
   
    /** A wall with a rough bricky surface */
    Box box = new Box(2,2,2);
    Geometry wall = new Geometry("Shiny rock", box);
    Material mat_wall = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    mat_wall.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_diffuse.jpg"));
    mat_wall.setTexture("NormalMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_normal.jpg"));
    wall.setMaterial(mat_wall);
    wall.setLocalTranslation(0,-3,0);   // Move it a bit
    rootNode.attachChild(wall);

    
    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1,-2f,-1.5f).normalizeLocal());
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