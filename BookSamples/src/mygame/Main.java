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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <ul>
 * <li>Main: Event loop, HUD, input handling (loading Charges into selected Towers)</li>
 * <li>Factory: Initializes the scene, generates Charges.</li>
 * <li>PlayerBaseControl: Manages score, level, player health, player budget. </li>
 * <li>CreepControl: Manages creep health and speed; automatically moves Creeps towards playerbase, 
 *   substracts player‘s health if Creep reaches playerbase.</li>
 * <li>TowerControl: Manages Charges; automatically shoots Charges at Creeps.</li>
 * <li>Charge: Contains sets of ammo that modify speed and health of a Creep.</li>
 * </ul>
 * @author zathras
 */
public class Main extends SimpleApplication {

    private boolean gamerunning = true;
    // TODO more values should depend on level
    private int level = 2;
    // Factory creates playerbase, creeps, towers, and charges
    Factory factory;
    // GUI
    private int selected = -1;   // which tower the player has selected 
    private BitmapText hudText;  // HUD displays score
    private BitmapText infoText; // HUD displays instructions
    // timers reset beam visualizations and dispense budget
    private float timer_beam;
    private float timer_budget;
    // main control manages score etc
    private PlayerBaseControl playerBase;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
        viewPort.setBackgroundColor(ColorRGBA.White);
        cam.setLocation(new Vector3f(0, 8, -6f));
        cam.lookAt(new Vector3f(0f, 0f, 8), Vector3f.UNIT_Y);
        initHUD();
        initInputs();
        factory = new Factory(rootNode, assetManager, level);
        playerBase = rootNode.getChild("PlayerBaseNode").getControl(PlayerBaseControl.class);
    }

    private void initHUD() {
        hudText = new BitmapText(guiFont, false);
        infoText = new BitmapText(guiFont, false);
        int screenHeight = settings.getHeight();
        float lineHeight = hudText.getLineHeight();
        // info text: display instructions
        infoText.setSize(guiFont.getCharSet().getRenderedSize());
        infoText.setColor(ColorRGBA.Blue);
        infoText.setLocalTranslation(300, screenHeight - lineHeight, 0);
        infoText.setText("Click tower to select. Press N(uke) / G(atling) / F(reeze) to load charges.");
        guiNode.attachChild(infoText);
        // score: display health and budget
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.Blue);
        hudText.setLocalTranslation(300, screenHeight - lineHeight * 2, 0);
        guiNode.attachChild(hudText);
    }

    /**
     * Input handling :Defining the "Select" action for towers: 
     * Click to select one tower and deselect previous one.
     * Press keys (F / G / N) to assign Charges to the selected tower.
     * You can only assign Charges if the player has budget.
     * A tower can have zero to 'level+1' Charges assigned.
     */
    private ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String mapping, boolean keyPressed, float tpf) {
            // A player clicks to select a tower.
            if (mapping.equals("Select") && !keyPressed) {
                if (selected != -1) {
                    // Deselect previously selected tower if applicable
                    Spatial prevTower = (rootNode.getChild("tower-" + selected));
                    prevTower.setMaterial((Material) prevTower.getUserData("standardMaterial"));
                }
                // determine which new tower was selected
                CollisionResults results = new CollisionResults();
                Vector2f click2d = inputManager.getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(
                        new Vector2f(click2d.x, click2d.y), 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(
                        new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d);
                Ray ray = new Ray(click3d, dir);
                rootNode.getChild("TowerNode").collideWith(ray, results);
                if (results.size() > 0) {
                    // player has selected a tower
                    CollisionResult closest = results.getClosestCollision();
                    selected = closest.getGeometry().getControl(TowerControl.class).getIndex();
                    Spatial selectedTower = rootNode.getChild("tower-" + selected);
                    selectedTower.setMaterial((Material) selectedTower.getUserData("selectedMaterial"));
                } else {
                    // player has clicked nothing
                    selected = -1;
                }
            }
            // A player presses keys to load the selected tower with a charge:
            // Add new Charge if tower not full yet and if player has budget.
            if (!keyPressed && selected != -1 && playerBase.getBudget() > 0) {
                TowerControl selectedTower = rootNode.getChild("tower-" + selected).getControl(TowerControl.class);
                if (mapping.equals("Make FreezeTower")) {
                    if (selectedTower.getChargeNum() <= level) {
                        selectedTower.addCharge(factory.getFreezeCharge());
                        playerBase.addBudgetMod(-1);
                    }
                } else if (mapping.equals("Make NukeTower")) {
                    if (selectedTower.getChargeNum() <= level) {
                        selectedTower.addCharge(factory.getNukeCharge());
                        playerBase.addBudgetMod(-1);
                    }
                } else if (mapping.equals("Make GatlingTower")) {
                    if (selectedTower.getChargeNum() <= level) {
                        selectedTower.addCharge(factory.getGatlingCharge());
                        playerBase.addBudgetMod(-1);
                    }
                }
            }
        }
    };

    private void initInputs() {
       inputManager.clearMappings();  // reset default WASD inputs, camera is static
        inputManager.setCursorVisible(true); // user needs to see mouse pointer to click
        inputManager.addMapping("Select", new MouseButtonTrigger(0));
        inputManager.addMapping("Make FreezeTower",  new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("Make NukeTower",    new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("Make GatlingTower", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addListener(actionListener,
                "Select", "Make GatlingTower", "Make NukeTower", "Make FreezeTower");
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (gamerunning) {
            // Player earns money roughly every 10 secs
            timer_budget += tpf;
            if (timer_budget > playerBase.getLevel() + 10) {
                playerBase.addBudgetMod(playerBase.getLevel());
                timer_budget = 0;
            }
            // Reset all laserbeam visualizations and GC them every second
            timer_beam += tpf;
            if (timer_beam > 1f) {
                if (thereAreBeams()) clearAllBeams();
                timer_beam = 0;
            }
            // Update score display
            hudText.setText(
                    "Budget: " + playerBase.getBudget()
                    + ", Health: " + playerBase.getHealth()
                    + "      GO! GO! GO!");
            // Test whether player loses and game ends
            if (playerBase.getHealth() <= 0) {
                hudText.setText(
                        "Budget: " + playerBase.getBudget()
                        + ", Health: " + playerBase.getHealth()
                        + "      YOU LOSE.");
                endGame();
            }
            // Test whether player wins and game ends
            if ((getCreepNum() == 0) && playerBase.getHealth() > 0) {
                hudText.setText(
                        "Budget: " + playerBase.getBudget()
                        + ", Health: " + playerBase.getHealth()
                        + "      YOU WIN!");
                endGame();
            }
        }
    }
    
    /** -------------------------------- */

    /** Remove the laser beam visualizations. */
    private void clearAllBeams() {
        ((Node) (rootNode.getChild("BeamNode"))).detachAllChildren();
    }

    /** How many creeps are still in the game? */
    private int getCreepNum() {
        return (((Node) (rootNode.getChild("CreepNode"))).getChildren().size());
    }

    /** How many laser beams extend from all towers, more than zero? 
     Need to test this to GC them after a few seconds. */
    private Boolean thereAreBeams() {
        return ((Node) (rootNode.getChild("BeamNode"))).descendantMatches("Beam").size() > 0;
    }

    /** -------------------------------- */

    /** Remove the controls from all remaining creeps when game ends so they stop walking */
    private void stopCreeps() {
        for (Spatial c : rootNode.descendantMatches("Creep-.*")) {
            c.getControl(CreepControl.class).remove();
        }
    }

    /** Clean up: pause the game, stop all creeps, remove all beams when game ends */
    private void endGame() {
        gamerunning = false;
        stopCreeps();
        clearAllBeams();
        // TODO start next level
    }
}
