package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * How to make user input trigger a (non-looping) sound.
 */
public class AudioTrigger extends SimpleApplication {

  private AudioNode audio_gun;

  public static void main(String[] args) {
    AudioTrigger app = new AudioTrigger();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(40);
    initScene();
    initAudio();
    initKeys();
  }

  /**
   * This simple scene is just a blue box floating in space
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

  /**
   * Create the audio object (a gun sound) and configure it. 
   */
  private void initAudio() {
    audio_gun = new AudioNode(assetManager, "Sounds/Effects/Gun.wav");
    audio_gun.setLooping(false); // tell it to play only once.
    audio_gun.setVolume(2);
  }

  /** 
   * Declare a "Shoot" action and map it to a trigger: Left mouse click "shoots".
   */
  private void initKeys() {
    inputManager.addMapping("Shoot", new MouseButtonTrigger(0));
    inputManager.addListener(actionListener, "Shoot");
  }
  
  /**
   * Define what the "Shoot" action does: It plays the gun sound once.
   */
  private ActionListener actionListener = new ActionListener() {
    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Shoot") && !keyPressed) {
        audio_gun.play(); // play it (once)
      }
    }
  };
}