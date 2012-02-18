package chapter06;

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
    
    Sphere sphere = new Sphere(32,32, 1f);
    
    Geometry shiny_sphere1 = new Geometry("rought sphere", sphere);
    Material mat_lit1 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat_lit1.setBoolean("UseMaterialColors",true);
    mat_lit1.setColor("Ambient", ColorRGBA.Black );
    mat_lit1.setColor("Diffuse", ColorRGBA.Cyan );
    mat_lit1.setFloat("Shininess", 0f); // [0,128]
    mat_lit1.setColor("Specular", ColorRGBA.White );
    shiny_sphere1.setMaterial(mat_lit1);
    shiny_sphere1.move(-2.5f, 0, 0);
    rootNode.attachChild(shiny_sphere1); 
    
    Geometry shiny_sphere2 = new Geometry("normal sphere", sphere);
    Material mat_lit2 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat_lit2.setBoolean("UseMaterialColors",true);
    mat_lit2.setColor("Ambient", ColorRGBA.Black );
    mat_lit2.setColor("Diffuse", ColorRGBA.Cyan );
    mat_lit2.setFloat("Shininess", 8f); // [0,128]
    mat_lit2.setColor("Specular", ColorRGBA.White );
    shiny_sphere2.setMaterial(mat_lit2);
    rootNode.attachChild(shiny_sphere2); 
    
    Geometry shiny_sphere3 = new Geometry("Smooth sphere", sphere);
    Material mat_lit3 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat_lit3.setBoolean("UseMaterialColors",true);
    mat_lit3.setColor("Ambient", ColorRGBA.Black );
    mat_lit3.setColor("Diffuse", ColorRGBA.Cyan );
    mat_lit3.setFloat("Shininess", 128f); // [0,128]
    mat_lit3.setColor("Specular", ColorRGBA.White );
    shiny_sphere3.setMaterial(mat_lit3);
    shiny_sphere3.move(2.5f, 0, 0);
    rootNode.attachChild(shiny_sphere3); 
    
    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
   
  }
}