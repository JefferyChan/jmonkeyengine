package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.LowPassFilter;

/**
 * The examples demonstrates a Low Pass filter that lets low frequencies pass
 * but cuts off all frequences higher than a cut-off value (here 0.1f). The
 * filter also reduces the volume by half. This creates a muffled sound.
 *
 * @author zathras
 */
public class AudioFilter extends SimpleApplication {

  private AudioNode src;

  public static void main(String[] args) {
    AudioFilter test = new AudioFilter();
    test.start();
  }

  @Override
  public void simpleInitApp() {
    System.out.println("Playing without filter");
    src = new AudioNode(assetManager,
            "Sounds/Effects/Foot steps.ogg", true);
    src.setVolume(10);
    src.play();
  }

  @Override
  public void simpleUpdate(float tpf) {
    if (src.getStatus() != AudioNode.Status.Playing) {
      System.out.println("Playing with low pass filter");
      src = new AudioNode(assetManager,
              "Sounds/Effects/Foot steps.ogg", true);
      src.setDryFilter(new LowPassFilter(.5f, .1f));
      src.setVolume(10);
      src.play();
    }
  }
}
