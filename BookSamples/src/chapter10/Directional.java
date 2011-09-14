package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * This example demonstrates directional sound that it only audible within a
 * "sound cone". the sound cone is defined by a direction and and inner angle
 * (loudest) and outer angle (more quite). The sound is not audible outside the
 * "sound cone".
 *
 * @author zathras
 */
public class Directional extends SimpleApplication {

  private AudioNode waves_audio;
  private Node waves_node;

  public static void main(String[] args) {
    Directional test = new Directional();
    test.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50f);
    // sound source parent node
    waves_node = new Node("sound source");
    waves_node.setLocalTranslation(1, 1, 1);
    rootNode.attachChild(waves_node);
    //just a blue box floating in space
    Box box1 = new Box(Vector3f.UNIT_XYZ, 1, 1, 1);
    Geometry waves_geo = new Geometry("Sound source model", box1);
    Material mat1 = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setColor("Color", ColorRGBA.Blue);
    waves_geo.setMaterial(mat1);
    waves_node.attachChild(waves_geo);
    // load sound as prebuffered (streaming=false)
    waves_audio = new AudioNode(assetManager,
            "Sounds/Environment/Ocean Waves.ogg", false);
    waves_audio.setLooping(true);
    waves_audio.setPositional(true);
    waves_audio.setDirectional(true);
    waves_audio.setInnerAngle(50);
    waves_audio.setOuterAngle(120);
    waves_audio.setDirection(new Vector3f(0, 0, 1));
    waves_audio.play();
    // attach the sound to its parent node
    waves_node.attachChild(waves_audio);
  }

  @Override
  public void simpleUpdate(float tpf) {
    // keep the audio listener moving with the camera
    listener.setLocation(cam.getLocation());
    listener.setRotation(cam.getRotation());
  }
}
