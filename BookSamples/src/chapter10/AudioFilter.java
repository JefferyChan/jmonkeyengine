package chapter10; 

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.LowPassFilter;

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
    src.play();
  }

  @Override
  public void simpleUpdate(float tpf) {
    if (src.getStatus() != AudioNode.Status.Playing) {
      audioRenderer.deleteAudioData(src.getAudioData());

      System.out.println("Playing with low pass filter");
      src = new AudioNode(assetManager, 
              "Sounds/Effects/Foot steps.ogg", true);
      src.setDryFilter(new LowPassFilter(1f, .1f));
      src.setVolume(3);
      src.play();
    }
  }
}
