package chapter4;

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
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This sample demonstrates how to detect and respond to user input.
 * Left-clicking rotates the rube. 
 * Pressing P (un)pauses the game.
 */
public class Main1UserInput extends SimpleApplication {

  private Trigger trigger_pause = new KeyTrigger(KeyInput.KEY_P);
  private Trigger trigger_pause2 = new KeyTrigger(KeyInput.KEY_ESCAPE);
  private Trigger trigger_rotate = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
  private Geometry geom;
  private static Main1UserInput app;

  @Override
  /** initialize the scene here */
  public void simpleInitApp() {
    /** unregister some of the default input mappings */
    inputManager.deleteMapping(INPUT_MAPPING_EXIT);
    inputManager.deleteMapping(INPUT_MAPPING_CAMERA_POS);
    inputManager.deleteMapping(INPUT_MAPPING_MEMORY);
    /** register input mappings to input manager */
    inputManager.addMapping("Pause Game", trigger_pause, trigger_pause2);
    inputManager.addMapping("Rotate", trigger_rotate);
    inputManager.addListener(actionListener, new String[]{"Pause Game"});
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
      if (name.equals("Pause Game") && !isPressed) {

      } // else if ...
    }
  };
  
  private AnalogListener analogListener = new AnalogListener() {

    public void onAnalog(String name, float intensity, float tpf) {
      
        if (name.equals("Rotate")) {
          geom.rotate(0, intensity * tpf * 100, 0);
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
    app = new Main1UserInput();
    app.start();

  }

}
