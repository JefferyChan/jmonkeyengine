//package chapter06;
//
//import com.jme3.app.SimpleApplication;
//import com.jme3.light.DirectionalLight;
//import com.jme3.material.Material;
//import com.jme3.material.plugins.NeoTextureMaterialKey;
//import com.jme3.math.ColorRGBA;
//import com.jme3.math.Vector2f;
//import com.jme3.math.Vector3f;
//import com.jme3.scene.Geometry;
//import com.jme3.scene.shape.Sphere;
//import com.jme3.util.TangentBinormalGenerator;
//
///** 
// * To run this example in the jMonkeyPlatform:
// * 1. Go to Plugins > Available Pluginss, 
// * 2. Choose NeoTextureEditor and click Install
// */
//public class TexturesProcedural extends SimpleApplication {
//
//  public static void main(String[] args) {
//    TexturesProcedural app = new TexturesProcedural();
//    app.start();
//  }
//
//  @Override
//  public void simpleInitApp() {
//
//    Sphere sphere = new Sphere(32, 32, 2f);
//    sphere.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
//    TangentBinormalGenerator.generate(sphere);           // Generate Normals for bump maps!
//    sphere.scaleTextureCoordinates(new Vector2f(5f, 5f));
//    Geometry brain = new Geometry("braaains", sphere);
//
//    assetManager.registerLoader("com.jme3.material.plugins.NeoTextureMaterialLoader", "tgr");
//    NeoTextureMaterialKey key = new NeoTextureMaterialKey("Textures/brain.tgr");
//    Material mat_p = assetManager.loadAsset(key);
//    key.setMaterialDef("Commons/MatDefs/Light/Lighting.j3md");
//    mat_p.setFloat("Shininess", 8);
//
//    brain.setMaterial(mat_p);
//    rootNode.attachChild(brain);
//    brain.rotate(1, 1, 1);
//
//    DirectionalLight sun = new DirectionalLight();
//    sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
//    sun.setColor(ColorRGBA.White);
//    rootNode.addLight(sun);
//
//  }
//}