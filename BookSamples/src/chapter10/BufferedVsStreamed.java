package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
//import com.jme3.audio.PointAudioSource;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class BufferedVsStreamed extends SimpleApplication {

  private AudioNode nature, waves;

  public static void main(String[] args) {
    BufferedVsStreamed test = new BufferedVsStreamed();
    test.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50f);
    /**
     * just a blue box floating in space
     */
    Box box1 = new Box(Vector3f.ZERO, 1, 1, 1);
    Geometry player = new Geometry("Player", box1);
    Material mat1 = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat1.setColor("Color", ColorRGBA.Blue);
    player.setMaterial(mat1);
    rootNode.attachChild(player);

    // load as buffered (false)
    waves = new AudioNode(assetManager, "Sounds/Environment/Ocean Waves.ogg", false);
    waves.setLocalTranslation(new Vector3f(0, 0, 0));
    waves.setLooping(true);
    waves.setPositional(true);
    waves.setMaxDistance(100);
    waves.setRefDistance(5);
    waves.playInstance(); // play as instance

    // load as stream (true)
    nature = new AudioNode(assetManager, "Sounds/Environment/Nature.ogg", true); 
    nature.setVolume(1);

//        float[] eax = new float[]
//            {15,	38.0f,	0.300f,	-1000,	-3300,	0,	
//        1.49f,	0.54f,	1.00f,  -2560,	0.162f, 0.00f,0.00f,0.00f,	
//        -229,	0.088f,		0.00f,0.00f,0.00f,	
//        0.125f, 1.000f, 0.250f, 0.000f, -5.0f,  5000.0f,	
//        250.0f, 0.00f,	0x3f } ;
//
//        Environment env = new Environment(eax);
//        ar.setEnvironment(env);

    nature.play(); // play as stream
  }

  @Override
  public void simpleUpdate(float tpf) {
    listener.setLocation(cam.getLocation());
    listener.setRotation(cam.getRotation());
  }
}
