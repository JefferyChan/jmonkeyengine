package chapter10;


import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * Playing 3D audio. 
 */
public class BackgroundSounds extends SimpleApplication {

  private AudioNode audio_nature;

  public static void main(String[] args) {
    BackgroundSounds app = new BackgroundSounds();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(40);
    initScene();
    
    // create and configure a sound
    audio_nature = new AudioNode(assetManager, "Sounds/Environment/River.ogg");
    audio_nature.setPositional(true); // Use 3D audio
    audio_nature.setRefDistance(5);   // Half the distance where still audible
    audio_nature.setMaxDistance(100); // Distance where it stop becoming quieter
    audio_nature.setLocalTranslation(Vector3f.ZERO);
    audio_nature.setVolume(5);    
    audio_nature.setLooping(true); // activate continous play mode
    audio_nature.play();           // start playing continuously!  
  }
  
  /**
   * just a blue box floating in space
   */
  private void initScene() {
    Box box1 = new Box(Vector3f.ZERO, 1, 1, 1);
    Geometry player = new Geometry("Player", box1);
    Material mat1 = new Material(assetManager, 
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setColor("Color", ColorRGBA.Blue);
    player.setMaterial(mat1);
    rootNode.attachChild(player);
  }

  /** Move the listener with the a camera - for 3D audio. */
  @Override
  public void simpleUpdate(float tpf) {
    listener.setLocation(cam.getLocation());
    listener.setRotation(cam.getRotation());
  }
}