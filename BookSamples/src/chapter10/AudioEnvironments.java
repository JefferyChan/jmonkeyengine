package chapter10; // works

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;

/**
 * In this demom use the WASD keys and mouse to move around the origin. You
 * hear foot steps the way they would sound in different environments.
 */
public class AudioEnvironments extends SimpleApplication {

  private AudioNode steps_audio;
  private Environment e = Environment.AcousticLab;

  public static void main(String[] args) {
    AudioEnvironments test = new AudioEnvironments();
    test.start();
  }

  @Override
  public void simpleInitApp() {
    initKeys();
    System.out.println("Playing in environment Acoustic Lab");
    //audioRenderer.setEnvironment(Environment.AcousticLab);
    steps_audio = new AudioNode(assetManager, "Sounds/Effects/Foot steps.ogg", true);
    steps_audio.setVolume(3);
    steps_audio.play();
  }

  @Override
  public void simpleUpdate(float tpf) {
    if (steps_audio.getStatus() != AudioNode.Status.Playing) {
      //audioRenderer.deleteAudioData(src.getAudioData());
      steps_audio.play();
    }
//    float x = cam.getLocation().getX();
//    float z = cam.getLocation().getZ();
//    if (x > 0 && z > 0 && e != Environment.Dungeon) {
//      System.out.println("Playing in environment Dungeon");
//      audioRenderer.setEnvironment(Environment.Dungeon);
//      e = Environment.Dungeon;
//    } else if (x > 0 && z > 0 && e != Environment.Cavern) {
//      System.out.println("Playing in environment Cavern");
//      audioRenderer.setEnvironment(Environment.Cavern);
//      e = Environment.Cavern;
//    } else if (x > 0 && z > 0 && e != Environment.Closet) {
//      System.out.println("Playing in  environment Closet");
//      audioRenderer.setEnvironment(Environment.Closet);
//      e = Environment.Closet;
//    } else {
//      System.out.println("Playing in environment Garage");
//      audioRenderer.setEnvironment(Environment.Garage);
//      e = Environment.Garage;
//    }

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
   * Depending whether you a left/right/before/behind the origin it changes the
   * environment.
   */
  private ActionListener actionListener = new ActionListener() {

    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Shoot") && !keyPressed) {
        steps_audio.play(); // play it (once)
      }
    }
  };
}
