package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;


/** Materials with various texture maps and Phong illumination properties. */
public class GlassMaterial extends SimpleApplication {

  public static void main(String[] args) {
    GlassMaterial app = new GlassMaterial();
    app.start();
  }

  @Override
  public void simpleInitApp() {
   
    /** A wall with a rough bricky surface */
    Box box = new Box(2,2,2);
    Geometry wall = new Geometry("Shiny rock", box);
    Material mat_wall = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    mat_wall.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_diffuse.jpg"));
    mat_wall.setTexture("NormalMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_normal.jpg"));
    mat_wall.setFloat("Shininess", 0f); // [0,128]
    wall.setMaterial(mat_wall);
    wall.setLocalTranslation(0,-3,0);   // Move it a bit
    rootNode.attachChild(wall);
 
    Sphere sphere = new Sphere(32,32, 2f);
    Geometry glasball = new Geometry("normal sphere", sphere);
    Material mat_glass = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    mat_glass.setBoolean("UseMaterialColors",true);
    mat_glass.setBoolean("UseAlpha",true);
    mat_glass.setFloat("AlphaDiscardThreshold",0.0f);
    mat_glass.setTexture("AlphaMap", assetManager.loadTexture("Textures/bark.png"));
    mat_glass.setColor("Ambient", ColorRGBA.Cyan );
    mat_glass.setColor("Diffuse", ColorRGBA.Cyan );
    mat_glass.setColor("Specular", ColorRGBA.White );
    mat_glass.setFloat("Shininess", 16f);    // [0,128]
    mat_glass.getAdditionalRenderState().setBlendMode(BlendMode.Color);
    mat_glass.getAdditionalRenderState().setDepthWrite(false);
    glasball.setQueueBucket(Bucket.Transparent);
    glasball.setMaterial(mat_glass);
    glasball.move(0f, 1, 0);
    rootNode.attachChild(glasball); 

    
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