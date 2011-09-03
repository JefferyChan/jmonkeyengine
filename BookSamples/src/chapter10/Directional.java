package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
//import com.jme3.audio.PointAudioSource;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Directional extends SimpleApplication {

  private AudioNode nature, waves;

  public static void main(String[] args) {
    Directional test = new Directional();
    test.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50f);
    //just a blue box floating in space
    Box box1 = new Box(Vector3f.UNIT_XYZ, 1, 1, 1);
    Geometry player = new Geometry("Player", box1);
    Material mat1 = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setColor("Color", ColorRGBA.Blue);
    player.setMaterial(mat1);
    rootNode.attachChild(player);

    // load as buffered instance (false)
    waves = new AudioNode(assetManager, "Sounds/Environment/Ocean Waves.ogg", false);
    waves.setLooping(true);
    waves.setPositional(true);
    waves.setDirectional(true);
    waves.setInnerAngle(45);
    waves.setOuterAngle(135);
    //waves.setMaxDistance(100);
    //waves.setRefDistance(10);
    waves.setDirection(new Vector3f(0,0,1));
    waves.play(); // play as instance
    waves.setLocalTranslation(1, 1, 1);
    
//    // load as stream (true)
//    nature = new AudioNode(assetManager, "Sounds/Environment/Nature.ogg", true); 
//    nature.setVolume(3);
//    nature.play(); // play as stream
  }

  @Override
  public void simpleUpdate(float tpf) {
    listener.setLocation(cam.getLocation());
    listener.setRotation(cam.getRotation());
  }
}
