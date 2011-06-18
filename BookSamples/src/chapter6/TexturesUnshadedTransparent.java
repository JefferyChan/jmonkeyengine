package chapter6;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.renderer.queue.RenderQueue.Bucket;

/** 
 * How to give an object's surface a material and texture.
 * This class demonstrates opaque and transparent textures, 
 * and textures that let colors "bleed" through. 
 * No Phong illumination. */
public class TexturesUnshadedTransparent extends SimpleApplication {

  public static void main(String[] args) {
    TexturesUnshadedTransparent app = new TexturesUnshadedTransparent();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    /** A simple textured cube */
    Box boxshape1 = new Box(new Vector3f(-2f, 0f, 0f), 0.5f, 0.5f, 0.5f);
    Geometry cube = new Geometry("My Textured Box", boxshape1);
    Material mat_stl = new Material(assetManager, 
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat_stl.setTexture("ColorMap", assetManager.loadTexture("Interface/Monkey.png"));
    cube.setMaterial(mat_stl);
    rootNode.attachChild(cube);

    /** This material turns the box into a stained glass window. 
     * The texture has an alpha channel and is partially transparent. */
    Box boxshape3 = new Box(new Vector3f(2f, 0f, 0f), 1f, 1.4f, 0.01f);
    Geometry window_frame = new Geometry("window frame", boxshape3);
    Material mat_tt = new Material(assetManager, 
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat_tt.setTexture("ColorMap",
            assetManager.loadTexture("Textures/mucha-window.png"));
    window_frame.setMaterial(mat_tt);
    mat_tt.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    window_frame.setQueueBucket(Bucket.Transparent);
    rootNode.attachChild(window_frame);

    /** A box with its material color "bleeding" through. The texture has 
     * an alpha channel and is partially transparent. */
    Box boxshape4 = new Box(new Vector3f(0f, 0f, -2f), 3f, 1f, 1f);
    //boxshape4.scaleTextureCoordinates(new Vector2f(5,1));
    Geometry cube_bleed = new Geometry("Bleed-through color", boxshape4);
    Material mat_tl = new Material(assetManager, 
            "Common/MatDefs/Misc/Unshaded.j3md");
    Texture tex_ml = assetManager.loadTexture("Textures/bark.png");
    //tex_ml.setWrap(Texture.WrapAxis.S,Texture.WrapMode.MirroredRepeat);
    mat_tl.setTexture("ColorMap", tex_ml);
    mat_tl.setColor("Color", new ColorRGBA(1f, 0.8f, 0.7f, 1f)); // e.g. brown=(1.0,0.9,0.8)
    cube_bleed.setMaterial(mat_tl);
    rootNode.attachChild(cube_bleed);
  }
}