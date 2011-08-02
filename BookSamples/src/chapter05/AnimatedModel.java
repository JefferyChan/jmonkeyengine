package chapter05;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;

/** Sample 7 - how to load an OgreXML model and play an animation, 
 * using channels, a controller, and an AnimEventListener. */
public class AnimatedModel extends SimpleApplication
        implements AnimEventListener {

  private AnimChannel channel;
  private AnimControl control;
  private Node player;

  public static void main(String[] args) {
    AnimatedModel app = new AnimatedModel();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    viewPort.setBackgroundColor(ColorRGBA.LightGray);
    inputManager.addMapping("Walking", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(analogListener, "Walking");
    
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

    player = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.j3o");
    player.setLocalScale(0.5f);
    rootNode.attachChild(player);

    control = player.getControl(AnimControl.class);
    control.addListener(this);
    for (String anim : control.getAnimationNames()) System.out.println(anim); 
    Geometry submesh = (Geometry) player.getChild("door 12");

    channel = control.createChannel();
    channel.setAnim("stand");

//    SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
//    Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
//    mat.setColor("Color", ColorRGBA.Green);
//    mat.getAdditionalRenderState().setDepthTest(false);
//    skeletonDebug.setMaterial(mat);
//    player.attachChild(skeletonDebug);
  }

  public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
    if (animName.equals("Walk")) {
      channel.setAnim("stand", 0.50f);
      channel.setLoopMode(LoopMode.DontLoop);
      channel.setSpeed(1f);
    }
  }

  public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    // unused
  }

  private AnalogListener analogListener = new AnalogListener() {
    public void onAnalog(String name, float value, float tpf) {
      if (name.equals("Walking") ) {
        if (!channel.getAnimationName().equals("Walk")) {
          channel.setAnim("Walk",0.5f);
          channel.setLoopMode(LoopMode.Loop);
        }
      }
    }
  };

}