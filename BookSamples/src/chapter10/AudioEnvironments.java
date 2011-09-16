package chapter10; // works

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

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
        initScene();
        flyCam.setMoveSpeed(50f);
        // initialize the sound node
        audioRenderer.setEnvironment(e);
        steps_audio = new AudioNode(assetManager, "Sounds/Effects/Foot steps.ogg");
        steps_audio.setVolume(3);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Move the listener with the a camera - for 3D audio.
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
        steps_audio.setLocalTranslation(cam.getLocation());
        // repeat the audio with possibly changed environment
        if (steps_audio.getStatus() != AudioNode.Status.Playing) {
            steps_audio.play();
        }
        float x = cam.getLocation().getX();
        float z = cam.getLocation().getZ();
        if (x > 0 && z > 0 && e != Environment.Dungeon) {
            System.out.println("Playing in environment Dungeon");
            e = Environment.Dungeon;
        } else if (x > 0 && z < 0 && e != Environment.Cavern) {
            System.out.println("Playing in environment Cavern");
            e = Environment.Cavern;
        } else if (x < 0 && z < 0 && e != Environment.Closet) {
            System.out.println("Playing in environment Closet");
            e = Environment.Closet;
        } else if (x < 0 && z > 0 && e != Environment.Garage) {
            System.out.println("Playing in environment Garage");
            e = Environment.Garage;
        }
        audioRenderer.setEnvironment(e);
        
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
}
