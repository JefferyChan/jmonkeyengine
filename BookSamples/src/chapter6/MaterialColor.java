package chapter6;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

/** A single-colored lit sphere using material color properties.
 */
public class MaterialColor extends SimpleApplication {

  public static void main(String[] args) {
    MaterialColor app = new MaterialColor();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    
    Sphere sphere = new Sphere(32,32, 1f);
    Geometry simple_sphere = new Geometry("colored Phong-lit sphere", sphere);
    Material mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors",true);
    mat.setColor("Diffuse", ColorRGBA.Blue );
    mat.setColor("Ambient", ColorRGBA.Black );
    simple_sphere.setMaterial(mat);
    rootNode.attachChild(simple_sphere); 
    
    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
    
  }
}