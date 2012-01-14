package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;

/**
 * The factory class initializes the scene. <br/>
 * The number of elements generated depends on the level.
 * Init order: 1. playerbase, 2. creeps, 3. towers.
 * (Because towers depend on creeps (towers want to shoot at creeps);
 * and creeps depend on the playerbase (they want to attack the player)).<br/>
 * Here you also configure the game mechanic factors to make the game more 
 * balanced: initial numbers of towers and creeps, initial speed of creeps, 
 * initial health of creeps and player, initial budget of player, as well as 
 * effects of tower charges (effects are "added" to creep health/speed 
 * when the charge hits).
 * @author zathras
 */
public final class Factory {

  private AssetManager assetManager;
  private Node rootNode;
  private final int level;
  // nodes
  private final Node playerbase_node = new Node("PlayerBaseNode");
  private final Node creepNode = new Node("CreepNode");
  // materials
  private Material creep_mat;
  private Material floor_mat;
  private Material playerbase_mat;
  private Material tower_sel_mat;
  private Material tower_std_mat;
  // Geometry size ratios 
  private final float CREEP_RADIUS = 0.3f;
  private final float TOWER_RADIUS = 0.3f;
  private final float TOWER_HEIGHT = 2.0f;
  // CONFIGURABLE INITIALIZATION FACTORS (configure in constructor)
  private final int CREEP_INIT_NUM;
  private final int TOWER_INIT_NUM;
  private final int PLAYER_INIT_BUDGET;
  private final float PLAYER_INIT_HEALTH;
  private final float CREEP_INIT_HEALTH;
  private final float CREEP_INIT_SPEED;
  // CONFIGURABLE TOWER CHARGES: SpeedDamage, HealthDamage, AmmoNum, BlastRange
  private final float[] GATLING = {0.0f, -1f, 5, 0f};
  private final float[] FREEZE  = {-1f,  -2f, 3, 0f};
  private final float[] NUKE    = {+.5f, -10f, 1, 2f};

  public Factory(Node rootNode, AssetManager as, int level) {
    this.assetManager = as;
    this.rootNode     = rootNode;
    this.level        = level;
    // configurable factors depend on level
    this.CREEP_INIT_NUM      = 2 + level * 2;
    this.TOWER_INIT_NUM      = 4 + level / 2;
    this.PLAYER_INIT_BUDGET  = 5 + level * 2;
    this.PLAYER_INIT_HEALTH  = 2f + level;
    this.CREEP_INIT_HEALTH   = 20f + level * 2;
    this.CREEP_INIT_SPEED    = 0.5f + level/10;
    // init the scene
    initLights();
    initMaterials();
    initPlayerBase(); // first
    initCreeps(); // second
    initTowers(); // third
  }

  /** --------------------------------------------------------- */
  
  public void initPlayerBase() {
    // player base geometry
    Box b2 = new Box(Vector3f.ZERO, 1.5f, .8f, 1f);
    Geometry playerbase_geo = new Geometry("Playerbase", b2);
    playerbase_geo.setMaterial(playerbase_mat);
    playerbase_geo.move(0, .8f, -1f);
    playerbase_node.attachChild(playerbase_geo);

    // floor geometry
    Node floor_node = new Node("Floor");
    Box b = new Box(Vector3f.ZERO, 33f, 0.1f, 33f);
    Geometry floor = new Geometry("Floor", b);
    floor.setMaterial(floor_mat);
    //floor.setLocalTranslation(0, 0f, 0);
    floor_node.attachChild(floor);

    // data
    playerbase_node.setUserData("level", level);
    playerbase_node.setUserData("score", 0);
    playerbase_node.setUserData("health", PLAYER_INIT_HEALTH);
    playerbase_node.setUserData("budget", PLAYER_INIT_BUDGET);
    playerbase_node.addControl(new PlayerBaseControl());

    // Add floor and player base nodes to rootNode
    playerbase_node.attachChild(floor_node);
    rootNode.attachChild(playerbase_node);
  }

  /** --------------------------------------------------------- */
  
  /** Creates one tower geometry at the origin. */
  private Geometry makeTower(int index) {
    Box tower_shape = new Box(
            Vector3f.ZERO,
            TOWER_RADIUS,
            TOWER_HEIGHT*.5f,
            TOWER_RADIUS);
    Geometry tower_geo = new Geometry("tower-" + index, tower_shape);
    tower_geo.setMaterial(tower_std_mat);
    return tower_geo;
  }

  /** 
   * Towers stand in two rows to the left and right of the positive z axis
   * along the "tower-protected valley". They shoot beams at creeps. */
  public void initTowers() {
    Node towerNode = new Node("TowerNode");
    // The beam node holds the laser beams for all towers.
    Node beamNode = new Node("BeamNode");
    rootNode.attachChild(beamNode);
    // Generate a series of towers along the sides of the valley
    for (int index = 0; index < TOWER_INIT_NUM; index++) {
      // Distribute towers to left and right of valley along positive z axis
      int leftOrRight = (index % 2 == 0 ? 1 : -1); // -1 or +1
      float offset_x = leftOrRight * 2.5f;
      float offset_y = TOWER_HEIGHT*.5f;
      float offset_z = index + 2;
      Vector3f loc = new Vector3f(offset_x, offset_y, offset_z);
      // tower geo
      Geometry tower_geo = makeTower(index);
      tower_geo.setLocalTranslation(loc);
      towerNode.attachChild(tower_geo);
      // the chargeMarkerNode holds the ChargeMarkers for one tower
      Node chargeMarkerNode = new Node("chargeMarkerNode-" + index);
      rootNode.attachChild(chargeMarkerNode);
      // data is stored per tower geo, no data in the towernode.
      tower_geo.setUserData("index", index);
      tower_geo.setUserData("chargesNum", 0);
      tower_geo.setUserData("towerHeight", TOWER_HEIGHT);
      tower_geo.setUserData("chargeMarkerNode", chargeMarkerNode);
      tower_geo.setUserData("beamNode", beamNode);
      tower_geo.setUserData("creepNode", creepNode);
      tower_geo.setUserData("selectedMaterial", tower_sel_mat);
      tower_geo.setUserData("standardMaterial", tower_std_mat);
      tower_geo.addControl(new TowerControl());
    }
    // attach to rootNode
    rootNode.attachChild(towerNode);
  }

  /** --------------------------------------------------------- */
  
  /** Creates one creep geometry at the origin */
  private Geometry makeCreep(float creepRadius, Vector3f loc, int index) {
    Dome creep_shape = new Dome(Vector3f.ZERO,
            10, 10, creepRadius, false);
    Geometry creep_geo = new Geometry("Creep-" + index, creep_shape);
    creep_geo.setMaterial(creep_mat);
    creep_geo.setLocalTranslation(loc);
    return creep_geo;
  }

  /** Creeps start at a certain distance from the towers at a spawnloc 
   * around the coordinate (+/- offset_x , + 1 , + offset_z). <ul>
   * <li>offset_x is random within the interval of the valley width. 
   * Can be positive or negative to distribute creeps to the left and right 
   * of the positive z axis. </li>
   * <li>offset_z is the same for all creeps, they start in a row ortogonal to 
   * the valley. distance increases each level, depending on the number of towers,
   * so that the creeps always start outside of the tower-protected valley.</li>
   * </ul>
   */
  public void initCreeps() {
    // generate a pack of creesp
    for (int index = 0; index < CREEP_INIT_NUM; index++) {
      // distribute creeps to the left and right of the positive x axis
      int leftOrRight = (index % 2 == 0 ? 1 : -1); // +1 or -1
      float offset_x = 1.75f * leftOrRight * FastMath.rand.nextFloat();
      float offset_y = 0;
      float offset_z = 2.5f * ((TOWER_INIT_NUM / 2f) + 6f);
      Vector3f spawnloc = new Vector3f(offset_x, offset_y, offset_z);
      // creep geometry
      Geometry creep_geo = makeCreep(CREEP_RADIUS, spawnloc, index);
      creepNode.attachChild(creep_geo);
      // data
      creep_geo.setUserData("index",  index);
      creep_geo.setUserData("health", CREEP_INIT_HEALTH);
      creep_geo.setUserData("speed",  CREEP_INIT_SPEED);
      creep_geo.setUserData("playerdata",
              playerbase_node.getControl(PlayerBaseControl.class));
      creep_geo.addControl(new CreepControl());
    }
    // add nodes to rootNode
    rootNode.attachChild(creepNode);
  }

  /** --------------------------------------------------------------------*/
  
  /**
   * Freeze charges slow down the target and do a bit of damage.
   */
  public Charge getFreezeCharge() {
    Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    beam_mat.setColor("Color", ColorRGBA.Cyan);
    return new Charge(FREEZE, beam_mat);
  }

  /**
   * Gatling charges do minimal damage but they can be shot more often per round 
   * and at various targets. 
   */
  public Charge getGatlingCharge() {
    Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    beam_mat.setColor("Color", ColorRGBA.Yellow);
    return new Charge(GATLING, beam_mat);
  }

  /**
   * Nuke charges do a lot of damage but they are expensive (only one shot per charge).
   * As a side effect they not only damage but also thaw/accelerate 
   * the neighbouring creeps! 
   */
  public Charge getNukeCharge() {
    Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    beam_mat.setColor("Color", ColorRGBA.Red);
    return new Charge(NUKE, beam_mat);
  }

  /** --------------------------------------------------------- */
  
  private void initMaterials() {
    // creep material
    creep_mat = new Material(assetManager,
            "Common/MatDefs/Light/Lighting.j3md");
    creep_mat.setColor("Diffuse", ColorRGBA.Black);
    creep_mat.setColor("Ambient", ColorRGBA.Black);
    creep_mat.setBoolean("UseMaterialColors", true);
    // floor material
    floor_mat = new Material(assetManager,
            "Common/MatDefs/Light/Lighting.j3md");
    floor_mat.setColor("Diffuse", ColorRGBA.Orange);
    floor_mat.setColor("Ambient", ColorRGBA.Orange);
    floor_mat.setBoolean("UseMaterialColors", true);
    // player material
    playerbase_mat = new Material(assetManager,
            "Common/MatDefs/Light/Lighting.j3md");
    playerbase_mat.setColor("Diffuse", ColorRGBA.Yellow);
    playerbase_mat.setColor("Ambient", ColorRGBA.Yellow);
    playerbase_mat.setBoolean("UseMaterialColors", true);
    // tower SelectedMaterial
    tower_sel_mat = new Material(assetManager,
            "Common/MatDefs/Light/Lighting.j3md");
    tower_sel_mat.setColor("Diffuse", new ColorRGBA(0.5f, 1, 0.5f, 1f));
    tower_sel_mat.setColor("Ambient", new ColorRGBA(0.5f, 1, 0.5f, 1f));
    tower_sel_mat.setBoolean("UseMaterialColors", true);
    //tower StandardMaterial
    tower_std_mat = new Material(assetManager,
            "Common/MatDefs/Light/Lighting.j3md");
    tower_std_mat.setColor("Diffuse", ColorRGBA.Green);
    tower_std_mat.setColor("Ambient", ColorRGBA.Green);
    tower_std_mat.setBoolean("UseMaterialColors", true);
  }

  private void initLights() {
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(0.8f, -0.7f, -1).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);
  }

}
