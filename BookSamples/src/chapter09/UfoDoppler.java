package chapter09;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * This example demonstrates the Doppler effect in an orbiting "UFO".
 */
public class UfoDoppler extends SimpleApplication {

  UfoDoppler app;
  private AudioNode ufo_audio;
  private Geometry ufo_geo;
  private Node ufo_node = new Node("UFO");
  float x = 1, z = 1;
  private float rate = -0.25f;
  private float xDist = 2.5f;
  private float zDist = 2.5f;
  private float angle = FastMath.TWO_PI;

  public static void main(String[] args) {
    UfoDoppler app = new UfoDoppler();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    // position the camera
    cam.setLocation(new Vector3f(-5, 0, 5));
    // configure UFO sound
    ufo_audio = new AudioNode(assetManager, "Sounds/Effects/Beep.ogg");
    ufo_audio.setPositional(true);    // moving 3D sound
    ufo_audio.setLooping(true);       // keep playing
    ufo_audio.setReverbEnabled(true); // Doppler effect
    ufo_audio.setRefDistance(6);      // 50% volume fall-off at 6 wu distance
    ufo_audio.setMaxDistance(100);    // The UFO will be the most quiet at 100 wu away
    ufo_audio.play();
    ufo_node.attachChild(ufo_audio);
    // a simple UFO geometry marking the position of the sound
    Sphere sphere = new Sphere(32, 32, .5f);
    ufo_geo = new Geometry("ufo", sphere);
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.LightGray);
    ufo_geo.setMaterial(mat);
    ufo_node.attachChild(ufo_geo);
    // both sound and geometry are attached to one ufo_node
    rootNode.attachChild(ufo_node);
  }

  @Override
  public void simpleUpdate(float tpf) {
    // this formula calculates coordinates of the UFOs orbit
    float dx = (float) (Math.sin(angle) * xDist);
    float dz = (float) (-Math.cos(angle) * zDist);
    x += dx * tpf;
    z += dz * tpf;
    angle += tpf * rate;
    if (angle > FastMath.TWO_PI) {
      angle = FastMath.TWO_PI;
      rate = -rate;
    } else if (angle < -0) {
      angle = -0;
      rate = -rate;
    }
    // The UFO sound flies in circles and pulls the attached geometry with it
    ufo_audio.setVelocity(new Vector3f(dx, 0, dz));
    ufo_node.setLocalTranslation(x, 0, z);
    // print current location and speed
    //System.out.println("LOC: " + (int) x + ", " + (int) z + ", VEL: " + (int) dx + ", " + (int) dz);
    // keep the listener's ear at the camera position!
    listener.setLocation(cam.getLocation());
    listener.setRotation(cam.getRotation());
  }
}
