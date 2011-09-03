package chapter10;  //works, node moves, ear stays

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
//import com.jme3.audio.PointAudioSource;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class Positional extends SimpleApplication {

  private AudioNode soundsource;
  private Environment e;

  public static void main(String[] args) {
    Positional test = new Positional();
    test.start();
  }

  @Override
  public void simpleInitApp() {
    System.out.println("Playing in environment Acoustic Lab");
    audioRenderer.setEnvironment(Environment.AcousticLab);
    soundsource = new AudioNode(assetManager, "Sounds/Effects/Bang.wav");
  }

  @Override
  public void simpleUpdate(float tpf) {
    // every time the sound source is done playing, move it to a random spot.
    if (soundsource.getStatus() != AudioNode.Status.Playing) {
      Vector3f v = new Vector3f();
      v.setX(FastMath.nextRandomFloat());
      v.setY(FastMath.nextRandomFloat());
      v.setZ(FastMath.nextRandomFloat());
      v.multLocal(40, 2, 40);
      v.subtractLocal(20, 1, 20);
      soundsource.setLocalTranslation(v);
      soundsource.playInstance();
    }
    // change the environment depending on the quadrant it ended up in
    float x = soundsource.getLocalTranslation().getX();
    float z = soundsource.getLocalTranslation().getZ();
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
      e = Environment.Garage;
      // You can configure custom environment too
      Environment myGarage = new Environment(1, 1, 1, 1, .9f, .5f, .751f, .0039f, .661f, .0137f);
      audioRenderer.setEnvironment(myGarage);
    }
//    }
  }
}
