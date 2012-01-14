package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Level;

/**
 * In this tower defense game, the creeps plan a kamikaze attack 
 * against the player base. The user has to recharge defense towers 
 * with different types of ammo to stop them. <br />
 * The player base is at the origin of the scene. In front of the player base, 
 * along the positive z axis, there's a valley. Player-controlled towers 
 * stand to the left and right of the valley. 
 * A pack of creeps appears at the far end of the positive z axis and 
 * runs down through the valley towards the player base.<br />
 * <ul>
 * <li>Main: Event loop, HUD, input handling (selecting Towers, loading Charges)</li>
 * <li>Factory: Initializes the scene, generates Charges.</li>
 * <li>PlayerBaseControl: Manages score, level, player health, player budget. </li>
 * <li>CreepControl: Manages creep health and speed; 
 *     automatically moves Creeps towards playerbase; 
 *     if Creep reaches playerbase, it decreases the player health.</li>
 * <li>TowerControl: Manages loaded Charges; 
 *     if loaded, shoots Charges at closest Creeps automatically.</li>
 * <li>Charge: Contains sets of ammo that modify speed and health 
 *     of Creeps on impact.</li>
 * </ul>
 * @author zathras
 */
public class Main extends SimpleApplication {

  // track game state
  private boolean gameRunning = true;
  private boolean lastGameWon = false;
  // Factory creates playerbase, creeps, towers, and charges for a level.
  Factory factory;
  // GUI and input handling 
  private int selected = -1;   // tracks which tower the player has selected 
  private BitmapText hudText;  // HUD displays score
  private BitmapText infoText; // HUD displays instructions
  private String infostring;
  // timers reset laser beam visualizations and dispense player budget
  private float timer_beam = 0f;
  private float timer_budget = 0f;
  // player control manages player health and budget
  private PlayerBaseControl playerBase;

  /** -------------------------------------------------------------- */
  // create a new instance of this application
  public static void main(String[] args) {
    Main app = new Main();
    app.start();
  }

  // initialize the application
  @Override
  public void simpleInitApp() {
    // configure the jme app including camera and background color
    java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
    setDisplayStatView(false); // don't show debugger
    viewPort.setBackgroundColor(ColorRGBA.White);
    cam.setLocation(new Vector3f(0, 8, -6f));
    cam.lookAt(new Vector3f(0f, 0f, 8), Vector3f.UNIT_Y);
    // initialize the scene graph
    initHUD();    // attach user interface (info text)
    initInputs(); // activate input handling
    startGame(1); // start game with level 1
  }

  /** Diplay text with instructions and score*/
  private void initHUD() {
    // Info: Display static playing instructions
    infoText = new BitmapText(guiFont, false);
    int screenHeight = settings.getHeight();
    float lineHeight = infoText.getLineHeight();
    infostring = "Click tower to select."
            + " Press N(uke) / G(atling) / F(reeze) to load charges.";
    infoText.setSize(guiFont.getCharSet().getRenderedSize());
    infoText.setColor(ColorRGBA.Blue);
    infoText.setLocalTranslation(0, screenHeight, 0);
    infoText.setText(infostring);
    guiNode.attachChild(infoText);

    // Score: this will later display health and budget
    hudText = new BitmapText(guiFont, false);
    hudText.setSize(guiFont.getCharSet().getRenderedSize());
    hudText.setColor(ColorRGBA.Blue);
    hudText.setLocalTranslation(0, screenHeight - lineHeight, 0);
    guiNode.attachChild(hudText);
  }

  /** -------------------------------------------------------------- */
  /** Input mapping <br /> 
   * Declaring which keys/click we want to use for "Restart",
   * and for "Select" and "Load...Charge" actions for towers. 
   * The strings are identifiers used by the ActionListener later.
   */
  private void initInputs() {
    // deactivate default input mapping: camera is static in this game.
    inputManager.clearMappings();
    // user needs to see mouse pointer to be able to click
    inputManager.setCursorVisible(true);
    // configure input mappings
    inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_RETURN));
    inputManager.addMapping("Quit",    new KeyTrigger(KeyInput.KEY_ESCAPE));
    inputManager.addMapping("Select",  new MouseButtonTrigger(0)); // click
    inputManager.addMapping("LoadFreezeCharge", new KeyTrigger(KeyInput.KEY_F));
    inputManager.addMapping("LoadNukeCharge", new KeyTrigger(KeyInput.KEY_N));
    inputManager.addMapping("LoadGatlingCharge", new KeyTrigger(KeyInput.KEY_G));
    // register to listener
    inputManager.addListener(actionListener,
            "Restart", "Select", "Quit", 
            "LoadGatlingCharge", "LoadNukeCharge", "LoadFreezeCharge");
  }
  /**
   * Input handling<br />
   * You left-click to select one Tower and deselect the previous one.
   * You press keys (F / G / N) to assign Charges to the selected Tower.
   * You can only assign Charges if the player has budget.
   * A tower can have zero to 'level+1' Charges assigned.
   */
  private ActionListener actionListener = new ActionListener() {

    @Override
    public void onAction(String mapping, boolean keyDown, float tpf) {
      if (gameRunning) {
        // A player clicks to select a tower.
        if (mapping.equals("Select") && !keyDown) {
          // Deselect previously selected tower if applicable
          if (selected != -1) {
            Spatial prevTower = (rootNode.getChild("tower-" + selected));
            prevTower.setMaterial((Material) prevTower.getUserData("standardMaterial"));
          }
          // Determine the coordinate where user clicked
          CollisionResults results = new CollisionResults();
          Vector2f click2d = inputManager.getCursorPosition();
          Vector3f click3d = cam.getWorldCoordinates(
                  new Vector2f(click2d.x, click2d.y), 0f).clone();
          Vector3f dir = cam.getWorldCoordinates(
                  new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d);
          // Cast invisible ray from click coord foward and intersect with towers
          Ray ray = new Ray(click3d, dir);
          rootNode.getChild("TowerNode").collideWith(ray, results);
          // Determine what the user selected
          if (results.size() > 0) {
            // Ray collides with tower: Player has selected a tower
            CollisionResult closest = results.getClosestCollision();
            selected = closest.getGeometry().getControl(TowerControl.class).getIndex();
            Spatial selectedTower = rootNode.getChild("tower-" + selected);
            selectedTower.setMaterial((Material) selectedTower.getUserData("selectedMaterial"));
          } else {
            // Ray misses towers: Player has selected nothing
            selected = -1;
          }
        }
        // If a tower is selected and user presses keys, then load a Charge:
        // Add new Charge only if player has budget and if tower is not full yet 
        // (max number of Charges per tower is 'level+1')!
        if (selected != -1 && playerBase.getBudget() > 0 && !keyDown) {
          TowerControl selectedTower =
                  rootNode.getChild("tower-" + selected).getControl(TowerControl.class);
          if (selectedTower.getChargeNum() <= playerBase.getLevel()) {
            // Selected tower is not full yet: Load one more Charge
            if (mapping.equals("LoadFreezeCharge")) {
              selectedTower.addCharge(factory.getFreezeCharge());
              playerBase.addBudgetMod(-1);
            } else if (mapping.equals("LoadNukeCharge")) {
              selectedTower.addCharge(factory.getNukeCharge());
              playerBase.addBudgetMod(-1);
            } else if (mapping.equals("LoadGatlingCharge")) {
              selectedTower.addCharge(factory.getGatlingCharge());
              playerBase.addBudgetMod(-1);
            }
          }
        }
      } else {
        // Game is paused: ignore game input, only test for restarting.
        if (mapping.equals("Restart") && !keyDown) {
          if (lastGameWon) {
             // if last game won, then next level
            startGame(playerBase.getLevel() + 1);
          } else {
            // if last game lost, then restart from level 1
            startGame(1); 
          }
        }
      }
      if (mapping.equals("Quit") && !keyDown) {
        endGame();
        stop();
      }
    }
  };

  /** -------------------------------------------------------------- */
  /** 
   * The main loop increases the player budget, displays health and budget,
   * and resets the laser beams.
   * It also tests whether the player or the creeps got killed, 
   * and determines winning or losing. <br />
   * Timers depend of time-per-frame (tpf)
   */
  @Override
  public void simpleUpdate(float tpf) {
    if (gameRunning && !paused) {
      
      // Player earns x coins every 10+x seconds per level x
      timer_budget += tpf;
      if (timer_budget > playerBase.getLevel() + 10) {
        playerBase.addBudgetMod(playerBase.getLevel());
        timer_budget = 0;
      }
      
      // Reset all laserbeam visualizations and GC them every second
      timer_beam += tpf;
      if (timer_beam > 1f) {
        if (thereAreBeams()) {
          clearAllBeams();
        }
        timer_beam = 0;
      }

      // Update health/budget display:
      String score = "(" + playerBase.getLevel()
              + ") Budget: " + playerBase.getBudget()
              + ", Health: " + playerBase.getHealth();

      // Test whether player wins or loses
      if (playerBase.getHealth() <= 0) {
        hudText.setText(score + "      YOU LOSE.");
        lastGameWon = false;
        endGame();
      } else if ((getCreepNum() == 0) && playerBase.getHealth() > 0) {
        hudText.setText(score + "      YOU WIN!");
        lastGameWon = true;
        endGame();
      } else {
        // Otherwise display default text, battle is ongoing.
        hudText.setText(score + "      GO! GO! GO!");
      }
    }
  }

  /** -------------------------------------------------------------- */
  
  /** How many creeps are still in the game? */
  private int getCreepNum() {
    return (((Node) (rootNode.getChild("CreepNode"))).getChildren().size());
  }

  /** Do more than zero laser beams extend from any towers? 
  Need to test this to GC beam geometries every few seconds. */
  private Boolean thereAreBeams() {
    return ((Node) (rootNode.getChild("BeamNode"))).descendantMatches("Beam").size() > 0;
  }

  /** GC the laser beam visualizations. */
  private void clearAllBeams() {
    ((Node) (rootNode.getChild("BeamNode"))).detachAllChildren();
  }

  /** -------------------------------------------------------------- */
  
  /** Reset everything and create the next scene */
  private void startGame(int level) {
    rootNode.detachAllChildren();
    rootNode.getLocalLightList().clear();
    factory = new Factory(rootNode, assetManager, level);
    playerBase = rootNode.getChild("PlayerBaseNode").getControl(PlayerBaseControl.class);
    infoText.setText(infostring);
    selected    = -1;
    gameRunning = true;
  }

  /** Clean up: pause the game, stop all creeps, remove all beams when game ends */
  private void endGame() {
    gameRunning = false;
    // Remove the controls from creeps so they stop walking
    for (Spatial c : rootNode.descendantMatches("Creep-.*")) {
      c.getControl(CreepControl.class).remove();
    }
    clearAllBeams();
    infoText.setText("Press RETURN to continue.");
  }
}
