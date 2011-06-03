package solutions.chapter4;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This sample demonstrates how to detect and respond to user input.
 * Left-clicking rotates the rube. 
 * Pressing spacebar/C toggles the cube's color.
 */
public class UserInput extends SimpleApplication {

  private Geometry geom;
  private Trigger trigger_color = new KeyTrigger(KeyInput.KEY_SPACE);
  private Trigger trigger_color2 = new KeyTrigger(KeyInput.KEY_C);
  private Trigger trigger_rotate = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);

  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    /** unregister some a default input mapping */
    //inputManager.deleteMapping(INPUT_MAPPING_CAMERA_POS); // Key_C
    /** register input mappings to input manager */
    inputManager.addMapping("Toggle Color", trigger_color, trigger_color2);
    inputManager.addMapping("Rotate", trigger_rotate);
    inputManager.addListener(actionListener, new String[]{"Toggle Color"});
    inputManager.addListener(analogListener, new String[]{"Rotate"});

    /** Create a blue cube */
    Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);
    geom = new Geometry("Box", mesh);
    Material mat = new Material(assetManager,
            "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("m_Color", ColorRGBA.Blue);
    geom.setMaterial(mat);
    rootNode.attachChild(geom);
  }
  
  private ActionListener actionListener = new ActionListener() {

    public void onAction(String name, boolean isPressed, float tpf) {
      if (name.equals("Toggle Color") && !isPressed) {
        geom.getMaterial().setColor("Color", ColorRGBA.randomColor());
      } // else if ...
    }
  };
  
  private AnalogListener analogListener = new AnalogListener() {

    public void onAnalog(String name, float intensity, float tpf) {

      if (name.equals("Rotate")) {
        geom.rotate(0, intensity, 0);
      } // else if ...
    }
  };

  @Override
  /** (optional) Interact with update loop here */
  public void simpleUpdate(float tpf) {
  }

  @Override
  /** (optional) Advanced renderer/frameBuffer modifications */
  public void simpleRender(RenderManager rm) {
  }

  /** Start the jMonkeyEngine application */
  public static void main(String[] args) {
    UserInput app = new UserInput();
    app.start();

  }
}
