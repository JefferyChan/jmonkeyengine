package chapter05;

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
    Sphere sphere_mesh = new Sphere(32,32, 1f);
    Geometry sphere_geo = new Geometry("Colored lit sphere", sphere_mesh);
    Material sphere_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    sphere_mat.setBoolean("UseMaterialColors", true);
    sphere_mat.setColor("Diffuse", ColorRGBA.Blue );
    //sphere_mat.setColor("Ambient", ColorRGBA.Black );
    sphere_geo.setMaterial(sphere_mat);
    rootNode.attachChild(sphere_geo); 
    
    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1, 0, -2));
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
    
  }
}