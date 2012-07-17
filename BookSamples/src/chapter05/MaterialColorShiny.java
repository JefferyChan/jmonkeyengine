package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

/** Three colored spheres with increasing roughness (shininess).
 * A non-shiny surface appears rough, a very shiny surface appears smooth.
 */
public class MaterialColorShiny extends SimpleApplication {

  public static void main(String[] args) {
    MaterialColorShiny app = new MaterialColorShiny();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    
    Sphere sphere_mesh = new Sphere(32,32, 1f);
    
    Geometry sphere1_geo = new Geometry("rough sphere", sphere_mesh);
    Material sphere1_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    sphere1_mat.setBoolean("UseMaterialColors",true);
    sphere1_mat.setColor("Ambient", ColorRGBA.Black );
    sphere1_mat.setColor("Diffuse", ColorRGBA.Cyan );
    sphere1_mat.setColor("Specular", ColorRGBA.White );
    sphere1_mat.setFloat("Shininess", 0f); // [1,128]
    sphere1_geo.setMaterial(sphere1_mat);
    sphere1_geo.move(-2.5f, 0, 0);
    rootNode.attachChild(sphere1_geo); 
    
    Geometry sphere2_geo = new Geometry("normal sphere", sphere_mesh);
    Material sphere2_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    sphere2_mat.setBoolean("UseMaterialColors",true);
    sphere2_mat.setColor("Ambient", ColorRGBA.Black );
    sphere2_mat.setColor("Diffuse", ColorRGBA.Cyan );
    sphere2_mat.setColor("Specular", ColorRGBA.White );
    sphere2_mat.setFloat("Shininess", 4f); // [1,128]
    sphere2_geo.setMaterial(sphere2_mat);
    rootNode.attachChild(sphere2_geo); 
    
    Geometry sphere3_geo = new Geometry("Smooth sphere", sphere_mesh);
    Material sphere3_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    sphere3_mat.setBoolean("UseMaterialColors",true);
    sphere3_mat.setColor("Ambient", ColorRGBA.Black );
    sphere3_mat.setColor("Diffuse", ColorRGBA.Cyan );
    sphere3_mat.setColor("Specular", ColorRGBA.White );
    sphere3_mat.setFloat("Shininess", 128f); // [1,128]
    sphere3_geo.setMaterial(sphere3_mat);
    sphere3_geo.move(2.5f, 0, 0);
    rootNode.attachChild(sphere3_geo); 
    
    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1, 0, -2));
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
   
  }
}