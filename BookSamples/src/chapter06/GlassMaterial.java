package chapter06;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
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
    Geometry wall = new Geometry("Wall", box);
    
    Material wall_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    wall_mat.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_diffuse.jpg"));
    wall_mat.setTexture("NormalMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_normal.jpg"));
    wall.setMaterial(wall_mat);
    wall.setLocalTranslation(0,-3,0);   // Move it a bit
    rootNode.attachChild(wall);
 
    Sphere sphere = new Sphere(32,32, 2f);
    Geometry glass_geo = new Geometry("normal sphere", sphere);
    Material glass_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    glass_mat.setBoolean("UseMaterialColors",true);
    glass_mat.setBoolean("UseAlpha",true);
    glass_mat.setFloat("AlphaDiscardThreshold",0.1f);
    glass_mat.setTexture("AlphaMap", assetManager.loadTexture("Textures/rock.png"));
    glass_mat.setColor("Ambient", ColorRGBA.Cyan );
    glass_mat.setColor("Diffuse", ColorRGBA.Cyan );
    glass_mat.setColor("Specular", ColorRGBA.White );
    glass_mat.setFloat("Shininess", 16f);    // [0,128]
    glass_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    glass_mat.getAdditionalRenderState().setDepthWrite(false);
    glass_geo.setQueueBucket(Bucket.Translucent);
    glass_geo.setMaterial(glass_mat);
    glass_geo.move(0f, 1f, 0);
    glass_geo.rotate(FastMath.DEG_TO_RAD*90, 0, 0);
    rootNode.attachChild(glass_geo); 

    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(.3f,-0.5f,-0.5f));
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

  }
}