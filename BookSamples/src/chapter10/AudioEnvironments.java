package chapter10; // works

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;

/**
 * In this audio demo, use the WASD keys and mouse to move around the origin.
 * You hear what the same foot steps sound like in four different environments.
 *  *
 * @author zathras
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
    // Play sound once with default settings.
    System.out.println("Playing in environment Acoustic Lab");
    audioRenderer.setEnvironment(Environment.AcousticLab);
    steps_audio = new AudioNode(assetManager, "Sounds/Effects/Foot steps.ogg", true);
    steps_audio.setVolume(3);
    steps_audio.play();
  }

  @Override
  public void simpleUpdate(float tpf) {
    // repeat the audio with possibly changed environment
    if (steps_audio.getStatus() != AudioNode.Status.Playing) {
      //audioRenderer.deleteAudioData(src.getAudioData());
      steps_audio.play();
    }
    float x = cam.getLocation().getX();
    float z = cam.getLocation().getZ();
    if (x > 0 && z > 0 && e != Environment.Dungeon) {
      System.out.println("Playing in environment Dungeon");
      audioRenderer.setEnvironment(Environment.Dungeon);
      e = Environment.Dungeon;
    } else if (x > 0 && z > 0 && e != Environment.Cavern) {
      System.out.println("Playing in environment Cavern");
      audioRenderer.setEnvironment(Environment.Cavern);
      e = Environment.Cavern;
    } else if (x > 0 && z > 0 && e != Environment.Closet) {
      System.out.println("Playing in  environment Closet");
      audioRenderer.setEnvironment(Environment.Closet);
      e = Environment.Closet;
    } else {
      System.out.println("Playing in environment Garage");
      audioRenderer.setEnvironment(Environment.Garage);
      e = Environment.Garage;
    }
  }
}
