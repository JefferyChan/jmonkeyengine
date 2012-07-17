package chapter05;

import com.jme3.app.SimpleApplication;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.TangentBinormalGenerator;


/** Materials with various texture maps and Phong illumination properties. */
public class GlassMaterial extends SimpleApplication {

  public static void main(String[] args) {
    GlassMaterial app = new GlassMaterial();
    app.start();
  }

  @Override
  public void simpleInitApp() {
   flyCam.setMoveSpeed(50f);
    /** A wall with a rough bricky surface */
    Box box = new Box(10,1,10);
    TangentBinormalGenerator.generate(box);
    //box.scaleTextureCoordinates(new Vector2f(5f,5f));
    
    
    Geometry floor_geo = new Geometry("floor", box);
    
    Material floor_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    floor_mat.setTexture("DiffuseMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_diffuse.jpg"));
    floor_mat.setTexture("NormalMap",
            assetManager.loadTexture("Textures/BrickWall/BrickWall_normal.jpg"));
    floor_geo.setMaterial(floor_mat);
    floor_mat.getTextureParam("DiffuseMap").getTextureValue().setWrap(WrapMode.Repeat);
    floor_geo.setLocalTranslation(0,-4f,0);   // Move it a bit
    rootNode.attachChild(floor_geo);
 
    Sphere sphere = new Sphere(32,32, 2.5f);
    
    TangentBinormalGenerator.generate(sphere);
    
    Geometry glass_geo = new Geometry("normal sphere", sphere);
    Material glass_mat = new Material(assetManager, 
            "Common/MatDefs/Light/Lighting.j3md");
    glass_mat.setBoolean("UseMaterialColors",true);
    //glass_mat.setBoolean("UseAlpha",true);
//    glass_mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/mucha-window.png"));
    glass_mat.setColor("Ambient", new ColorRGBA(0,1,1,.75f) );
    glass_mat.setColor("Diffuse", new ColorRGBA(0,1,1,.75f) );
    glass_mat.setColor("Specular", ColorRGBA.White );
    glass_mat.setFloat("Shininess", 128f);    // [0,128]
    glass_mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off); 
//    glass_mat.getAdditionalRenderState().setAlphaFallOff(0.1f);
//    glass_mat.getAdditionalRenderState().setAlphaTest(true);
    glass_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    //glass_mat.getAdditionalRenderState().setDepthWrite(false);
    glass_geo.setQueueBucket(Bucket.Translucent);
    glass_geo.setMaterial(glass_mat);
    glass_geo.rotate(FastMath.DEG_TO_RAD*90, 0, 0);
    rootNode.attachChild(glass_geo); 

    
    
    /** Must add a light to make the lit object visible! */
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(.3f,-0.5f,-0.5f));
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

  }
}