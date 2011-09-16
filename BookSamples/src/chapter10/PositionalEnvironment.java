package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 * This example demonstrates positional sound. Your hear the sound of a gun shot
 * from various directions. Each quadrant represents a different environment.
 * The ear (listener) moves with the camera.
 *
 * @author zathras
 */
public class PositionalEnvironment extends SimpleApplication {

    private AudioNode shot_audio;
    private Environment e = Environment.AcousticLab;

    public static void main(String[] args) {
        PositionalEnvironment test = new PositionalEnvironment();
        test.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(Vector3f.ZERO);
        // initialize the sound node
        audioRenderer.setEnvironment(e);
        shot_audio = new AudioNode(assetManager, "Sounds/Effects/Bang.wav");
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Move the listener with the a camera - for 3D audio.
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
        // every time the sound source is done playing, move it to a random spot.
        Vector3f randomLoc = new Vector3f();
        if (shot_audio.getStatus() != AudioNode.Status.Playing) {
            randomLoc.setX(FastMath.nextRandomFloat());
            randomLoc.setY(FastMath.nextRandomFloat());
            randomLoc.setZ(FastMath.nextRandomFloat());
            randomLoc.multLocal(40, 2, 40);
            randomLoc.subtractLocal(20, 1, 20);
            shot_audio.setLocalTranslation(randomLoc);
            shot_audio.play();
        }
        float x = randomLoc.getX();
        float z = randomLoc.getZ();
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
            // You can configure custom environments too
            // e = new Environment(1, 1, 1, 1, 
            //        .9f, .5f, .751f, .0039f, .661f, .0137f);
        }
        audioRenderer.setEnvironment(e);
    }
}
