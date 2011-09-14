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
 * This exmaple shows how to play positional sounds. The positional sound is
 * attached to a blue box (which is attached to rootnode). You hear the sound
 * coming from the box.
 */
public class Positional extends SimpleApplication {

  private Node landing_node;

  public static void main(String[] args) {
    Positional app = new Positional();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(40);

    // create and configure a sound
    AudioNode water_audio = new AudioNode(assetManager,
            "Sounds/Environment/River.ogg");
    water_audio.setPositional(true); // Use 3D audio
    water_audio.setRefDistance(5);   // Half the distance where still audible
    water_audio.setMaxDistance(100); // Distance where it stop becoming quieter
    water_audio.setVolume(5);
    water_audio.setLooping(true); // activate continous play mode
    water_audio.play();           // start playing continuously!  

    // Create a node for the object the sound belongs to
    landing_node = new Node("Harbor");
    landing_node.setLocalTranslation(Vector3f.ZERO);
    rootNode.attachChild(landing_node);

    // attach this positional sound to a scene node
    landing_node.attachChild(water_audio);

    // attach a geometry to the scene node -- here just a blue cube
    Box box1 = new Box(Vector3f.ZERO, 1, 1, 1);
    Geometry landing_geo = new Geometry("boat landing", box1);
    Material mat1 = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setColor("Color", ColorRGBA.Blue);
    landing_geo.setMaterial(mat1);
    landing_node.attachChild(landing_geo);
  }

  @Override
  public void simpleUpdate(float tpf) {
    // keep the audio listener moving with the camera
    listener.setLocation(cam.getLocation());
    listener.setRotation(cam.getRotation());
  }
}
