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

/**
 * Main = PlanetControl 
 * Planet (contains towers, creeps, 1 playerbase), not very useful yet
 * Tower (contains Charges, shoots at creeps),
 * Charge (substracts creep‘s speed or health), 
 * Player (counts player‘s health and budget), 
 * Creep (counts its health, moves towards base, substracts player‘s health)
 * @author zathras
 */
public class Main extends SimpleApplication {

    private int selected = -1;     // which tower the player has selected
    private int level = 2;         // TODO more values should depend on level
    private boolean gamerunning = true;
    Factory factory;
    // GUI
    private BitmapText hudText;
    private BitmapText infoText;
    // timers
    private float timer_beam;
    private float timer_budget;
    private PlayerBaseControl playerc;
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.White);
        cam.setLocation(new Vector3f(0, 8, -6f));
        cam.lookAt(new Vector3f(0f, 0f, 8), Vector3f.UNIT_Y);
        initHUD();
        initInputs();
        initScene();
        
    }

    private void initScene() {
        factory = new Factory(rootNode,assetManager,level);
        
        playerc=rootNode.getChild("PlayerBaseNode").getControl(PlayerBaseControl.class);
    }

    private void initHUD() {
        // score: health and budget
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.Blue);
        hudText.setLocalTranslation(300, settings.getHeight() - hudText.getLineHeight() * 2, 0);
        guiNode.attachChild(hudText);
        // info text
        infoText = new BitmapText(guiFont, false);
        infoText.setSize(guiFont.getCharSet().getRenderedSize());
        infoText.setColor(ColorRGBA.Blue);
        infoText.setLocalTranslation(300, settings.getHeight() - hudText.getLineHeight(), 0);
        infoText.setText("Click tower to select. Press N(uke) / G(atling) / F(reeze) to load charges.");
        guiNode.attachChild(infoText);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (gamerunning) {
            // player earns money roughly every 10 secs
            timer_budget += tpf;
            if (timer_budget > playerc.getLevel()+10) {
                playerc.addBudgetMod(1);
                timer_budget = 0;
            }
            // reset all laserbeams and GC them every second
            timer_beam += tpf;
            if (timer_beam > 1f) {
                if (getBeamNum() > 0) {
                    clearAllBeams();
                }
                timer_beam = 0;
            }
            // update score 
            hudText.setText(
                    "Budget: " + playerc.getBudget() + 
                    ", Health: " + playerc.getHealth() + 
                    "      GO! GO! GO!");
            // test whether player loses
            if (playerc.getHealth() <= 0) {
                hudText.setText(
                        "Budget: " + playerc.getBudget() + 
                        ", Health: " + playerc.getHealth() + 
                        "      YOU LOSE.");
                gamerunning = false;
                stopCreeps();
                clearAllBeams();
            }
            // test whether player wins
            if ( (getCreepNum()==0)  ) {
                // make sure no controls are left running after game ends
                // TODO improve this stupid way to check whether the game is over
                hudText.setText(
                        "Budget: " + playerc.getBudget() +
                        ", Health: " + playerc.getHealth() +
                        "      YOU WIN!");
                gamerunning = false;
                stopCreeps();
                clearAllBeams();
                // TODO start next level
            } 
        }
    }
    
    private void clearAllBeams(){
    ((Node)(rootNode.getChild("BeamNode"))).detachAllChildren();
    }
    private int getCreepNum(){return (((Node)(rootNode.getChild("CreepNode"))).getChildren().size());}
    private int getBeamNum(){return ((Node)(rootNode.getChild("BeamNode"))).descendantMatches("Beam").size();}
    private void stopCreeps(){
        for ( Spatial c : rootNode.descendantMatches("Creep-*") ) {
                    c.getControl(CreepControl.class).remove();
                }
    }
    
    /**
     * Defining the "Select" action for towers: 
     */
    private ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String mapping, boolean keyPressed, float tpf) {
            if (mapping.equals("Select") && !keyPressed) {
                // player clicks to select a tower
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
                    if (selected != -1) {
                        // if a tower was selected, deselect previous one
                        Spatial t =  (rootNode.getChild("tower-"+selected));
                        t.setMaterial((Material)t.getUserData("standardMaterial"));
                    }
                    CollisionResult closest = results.getClosestCollision();
                    selected= closest.getGeometry().getControl(TowerControl.class).getIndex();
                    Spatial t = rootNode.getChild("tower-"+selected);
                    t.setMaterial((Material)t.getUserData("selectedMaterial"));
                    System.out.println("Selected: " + selected + " -- Now press F/N/G.");
                } else {
                    // player has clicked nothing, all towers are deselected.
                    System.out.println("Selected: NOTHING");
                    selected = -1;
                    // TODO how to delesect all (geometry materials)?
                }
            }
            // player with budget presses keys to load towers with charges:
            if (!keyPressed && selected != -1 && playerc.getBudget() > 0) {
                TowerControl towerc = rootNode.getChild("tower-"+selected).getControl(TowerControl.class);
                if (mapping.equals("Make FreezeTower")) {
                    if ( towerc.getChargeNum() <= level) {
                        towerc.addCharge(factory.getFreezeCharge());
                       playerc.addBudgetMod(-1);
                    }
                } else if (mapping.equals("Make NukeTower")) {
                    if ( towerc.getChargeNum() <= level) {
                        towerc.addCharge(factory.getNukeCharge());
                        playerc.addBudgetMod(-1);
                    }
                } else if (mapping.equals("Make GatlingTower")) {
                    if ( towerc.getChargeNum() <= level) {
                        towerc.addCharge(factory.getGatlingCharge());
                        playerc.addBudgetMod(-1);
                    }
                }
            }
        }
    };

    private void initInputs() {
        inputManager.clearMappings();
        inputManager.setCursorVisible(true);
        inputManager.addMapping("Select", new MouseButtonTrigger(0));
        inputManager.addMapping("Make FreezeTower", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("Make NukeTower", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("Make GatlingTower", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addListener(actionListener, "Select");
        inputManager.addListener(actionListener,
                "Make GatlingTower", "Make NukeTower", "Make FreezeTower");
    }

   

}
