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
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Main=PlanetControl
 * Planet (contains towers, creeps, 1 playerbase), 
 * Tower (contains Charges),
 * Charge (substracts creep‘s speed or health), 
 * Player (counts player‘s health and budget), 
 * Creep (counts its health, moves towards base, substracts player‘s health)
 *
 * @author zathras
 */
public class Main extends SimpleApplication {

  private PlanetData planet;
  private PlayerData player;
  private List<TowerData> towers;
  private List<CreepData> creeps;
  private int level = 2;
  private int creepNum = level * 6;
  private int towerNum = 4 * level;
  private int selected = -1;     // which tower the player has selected
  private boolean gamerunning=true;
  // scales and ratios
  private float scale = 1.0f;
  private float creepRadius = 0.3f;
  private float towerRadius = 0.3f;
  private float towerHeight = 2.0f;
  //
  private BitmapText hudText;
  private BitmapText infoText;
  private float timer_beam;
  private float timer_budget;
  //
  private Node towerNode = new Node("tower node");
  private Node creepNode = new Node("creep node");
  private Node beamNode = new Node("beam node");

  public static void main(String[] args) {
    Main app = new Main();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    viewPort.setBackgroundColor(ColorRGBA.White);
    cam.setLocation(new Vector3f(0, 8 * scale, -5f * scale));
    cam.lookAt(new Vector3f(0f, 0f, 10 * scale), Vector3f.UNIT_Y);
    initHUD();
    initInputs();

    player = initPlayer();
    creeps = initCreeps();
    towers = initTowers();
    planet = initPlanet();
  }

  private void initHUD() {
    hudText = new BitmapText(guiFont, false);
    hudText.setSize(guiFont.getCharSet().getRenderedSize());
    hudText.setColor(ColorRGBA.Blue);
    hudText.setLocalTranslation(300, settings.getHeight() - hudText.getLineHeight() * 2, 0);
    guiNode.attachChild(hudText);
    infoText = new BitmapText(guiFont, false);
    infoText.setSize(guiFont.getCharSet().getRenderedSize());
    infoText.setColor(ColorRGBA.Blue);
    infoText.setLocalTranslation(300, settings.getHeight() - hudText.getLineHeight(), 0);
    infoText.setText("Click to select tower. Then press N / G / F to assign attacks.");
    guiNode.attachChild(infoText);
  }

  @Override
  public void simpleUpdate(float tpf) {
    if(gamerunning){
    timer_budget += tpf;
    if (timer_budget > 20) {
      player.addBudgetMod(1);
      timer_budget = 0;
    }
    timer_beam += tpf;
    if (timer_beam > 1f) {
      if (beamNode.descendantMatches("Beam").size() > 0) {
        beamNode.detachAllChildren();
      }
      timer_beam = 0;
    }
    hudText.setText(
            "Budget: " + player.getBudget() +
            ", Health: " + player.getHealth() + "      GO! GO! GO!");
    if (player.getHealth() <= 0) {
      hudText.setText(
              "Budget: " + player.getBudget() + 
              ", Health: " + player.getHealth() + "      YOU LOSE.");
      gamerunning= false;
    } 
      System.out.println("score: "+creepNum +" vs "+ player.getEliminatedCreeps());
    if (creepNum == player.getEliminatedCreeps() ) {
      hudText.setText(
              "Budget: " + player.getBudget() + 
              ", Health: " + player.getHealth() + "      YOU WIN!");
    }
    }
  }
  /**
   * Defining the "Select" action: Determine what was hit and how to respond.
   */
  private ActionListener actionListener = new ActionListener() {

    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Select") && !keyPressed) {
        // player clicks to select a tower
        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d);
        Ray ray = new Ray(click3d, dir);
        towerNode.collideWith(ray, results);
        if (results.size() > 0) {
          // player has selected a tower
          if (selected != -1) {
            // if a tower was selected, deselect it
            TowerNode t = (TowerNode) (towerNode.getChild(selected));
            t.setMaterial(t.getStandardMaterial());
          }
          CollisionResult closest = results.getClosestCollision();
          selected = (int) Integer.parseInt(closest.getGeometry().getName());
          TowerNode t = (TowerNode) (towerNode.getChild(selected));
          t.setMaterial(t.getSelectedMaterial());
          System.out.println("Selected: " + selected + " -- Now press F/N/G.");
        } else {
          // player has clicked nothing, towers are deselected.
          System.out.println("Selected: NOTHING");
          selected = -1;
        }
      }
      if (!keyPressed && selected != -1 && player.getBudget() > 0) {
        if (name.equals("Make FreezeTower")) {
          if (planet.getTowers().get(selected).getChargeNum() <= level) {
            planet.getTowers().get(selected).addCharge(getFreezeCharge());
            player.addBudgetMod(-1);
          }
        } else if (name.equals("Make NukeTower")) {
          if (planet.getTowers().get(selected).getChargeNum() <= level) {
            planet.getTowers().get(selected).addCharge(getNukeCharge());
            player.addBudgetMod(-1);
          }
        } else if (name.equals("Make GatlingTower")) {
          if (planet.getTowers().get(selected).getChargeNum() <= level) {
            planet.getTowers().get(selected).addCharge(getGatlingCharge());
            player.addBudgetMod(-1);
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

  private List<TowerData> initTowers() {
    List<TowerData> towerData = new ArrayList<TowerData>();
    for (int index = 0; index < towerNum; index++) {
      TowerData myTower = new TowerData(index, towerHeight, scale, level, creeps);
      towerData.add(myTower);
      Node tower = new TowerNode(assetManager, myTower, towerRadius);
      towerNode.attachChild(tower);
    }
    rootNode.attachChild(towerNode);
    towerNode.attachChild(beamNode);
    return towerData;
  }

  private List<CreepData> initCreeps() {

    List<CreepData> creepData = new ArrayList<CreepData>();
    for (int index = 0; index < creepNum; index++) {
      int offset_x = (index % 2 == 0 ? 1 : -1);
      float random = FastMath.rand.nextFloat() * 1.5f * scale * offset_x;
      Vector3f spawnloc = new Vector3f(random, scale, 2.5f * scale * ((towerNum / 2f) + 3f));
      CreepData myCreep = new CreepData(index, level * 15, scale * FastMath.rand.nextFloat(), spawnloc, player);
      creepData.add(myCreep);
      creepNode.attachChild(new CreepNode(assetManager, myCreep, creepRadius, scale));
    }
    rootNode.attachChild(creepNode);
    return creepData;
  }

  private PlayerData initPlayer() {
    rootNode.attachChild(new PlayerNode(assetManager, Vector3f.ZERO, scale));
    return new PlayerData(level*3, 10 * level,0);
  }

  private PlanetData initPlanet() {
    rootNode.attachChild(new PlanetNode(assetManager));
    return new PlanetData(level, creeps, towers, player);
  }

  /**
   * freeze charges slow down the target and do a bit of damage.
   */
  private Charge getFreezeCharge() {
    Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    beam_mat.setColor("Color", ColorRGBA.Cyan);
    return new Charge(-.5f, -2, 2, 1.5f * towerHeight * scale, scale, beam_mat);
  }

  /**
   * gatling charges do minimal damage but they can shoot ten times more often.
   */
  private Charge getGatlingCharge() {
    Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    beam_mat.setColor("Color", ColorRGBA.Yellow);
    return new Charge(0, -1, 8, 1.75f * towerHeight * scale, 1.5f * scale, beam_mat);
  }

  /**
   * nuke charges do a lot of damage but they are expensive (only one shot per charge).
   */
  private Charge getNukeCharge() {
    Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    beam_mat.setColor("Color", ColorRGBA.Red);
    return new Charge(0, -8, 1, 2f * towerHeight * scale, 2 * scale, beam_mat);
  }
}
