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
 * The
 * @author zathras
 */
public final class Factory {

    private AssetManager assetManager;
    private Node rootNode;
    private int level;
    // Geometry size ratios 
    private final float creepRadius = 0.3f;
    private final float towerRadius = 0.3f;
    private final float towerHeight = 2.0f;
    // nodes
    Node playerbase_node = new Node("PlayerBaseNode");
    Node creepNode       = new Node("CreepNode");
    // materials
    Material creep_mat;
    Material floor_mat;
    Material playerbase_mat;
    Material tower_sel_mat;
    Material tower_std_mat;
    Material blast_mat;
    // CONFIGURABLE INITIALIZATION FACTORS (configure in constructor)
    private int CREEP_NUM;
    private int TOWER_NUM;
    private int PLAYER_BUDGET;
    private float PLAYER_HEALTH;
    private float CREEP_HEALTH;
    private float CREEP_SPEED;
    // CONFIGURABLE TOWER CHARGES: SpeedDamage, HealthDamage, AmmoNum, Range, Blastrange
    float[] FREEZE  = {-.5f,  -2f, 3, 2.25f * towerHeight, 0f};
    float[] GATLING = {0.0f,  -2f, 8, 2.50f * towerHeight, 1f};
    float[] NUKE    = {1.0f, -10f, 1, 2.00f * towerHeight, 2f};


    public Factory(Node rootNode, AssetManager as, int level) {
        this.assetManager = as;
        this.rootNode = rootNode;
        this.level = level;
        // configurable factors depend on level
        this.CREEP_NUM     = 0 + level * 10;
        this.TOWER_NUM     = 2 + level * 2;
        this.PLAYER_BUDGET = 5 + level * 5;
        this.PLAYER_HEALTH =  2f + level;
        this.CREEP_HEALTH  = 10f + level * 10;
        this.CREEP_SPEED   = 2f*FastMath.rand.nextFloat() + (level / 5f);
        // init the scene
        initLights();
        initMaterials();
        initPlayerBase(); // first
        initCreeps(); // second
        initTowers(); // third
    }

    /** --------------------------------------------------------- */ 

    private void initMaterials() {
        // creep material
        creep_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        creep_mat.setColor("Diffuse", ColorRGBA.Black);
        creep_mat.setColor("Ambient", ColorRGBA.Black);
        creep_mat.setBoolean("UseMaterialColors",true); 
        // floor material
        floor_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        floor_mat.setColor("Diffuse", ColorRGBA.Orange);
        floor_mat.setColor("Ambient", ColorRGBA.Orange);
        floor_mat.setBoolean("UseMaterialColors",true); 
        //player material
        playerbase_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        playerbase_mat.setColor("Diffuse", ColorRGBA.Yellow);
        playerbase_mat.setColor("Ambient", ColorRGBA.Yellow);
        playerbase_mat.setBoolean("UseMaterialColors",true); 
        // tower SelectedMaterial
        tower_sel_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        tower_sel_mat.setColor("Diffuse", new ColorRGBA(0.5f, 1, 0.5f, 1f));
        tower_sel_mat.setColor("Ambient", new ColorRGBA(0.5f, 1, 0.5f, 1f));
        tower_sel_mat.setBoolean("UseMaterialColors",true); 
        //tower StandardMaterial
        tower_std_mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        tower_std_mat.setColor("Diffuse", ColorRGBA.Green);
        tower_std_mat.setColor("Ambient", ColorRGBA.Green);
        tower_std_mat.setBoolean("UseMaterialColors",true); 
        // blast material
        blast_mat = (Material) assetManager.loadMaterial("Materials/blast.j3m");
    }
    
    private void initLights(){
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0.8f, -0.7f, 1).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    /** --------------------------------------------------------- */ 
    
    public void initPlayerBase() {
        // player base geometry
        Box b2 = new Box(Vector3f.ZERO, 3f / 2f, .5f, .5f);
        Geometry playerbase_geo = new Geometry("Playerbase", b2);
        playerbase_geo.setMaterial(playerbase_mat);
        playerbase_geo.move(0, 1, 0);
        playerbase_node.attachChild(playerbase_geo);

        // floor geometry
        Node floor_node = new Node("Floor");
        Box b = new Box(Vector3f.ZERO, 33f, 1f, 33f);
        Geometry floor = new Geometry("Floor", b);
        floor.setMaterial(floor_mat);
        floor.setLocalTranslation(0, -.5f, 0);
        floor_node.attachChild(floor);

        // data
        playerbase_node.setUserData("level", level);
        playerbase_node.setUserData("score", 0);
        playerbase_node.setUserData("health", PLAYER_HEALTH);
        playerbase_node.setUserData("budget", PLAYER_BUDGET);
        playerbase_node.addControl(new PlayerBaseControl());

        // Add floor and player base nodes to rootNode
        playerbase_node.attachChild(floor_node);
        rootNode.attachChild(playerbase_node);
    }
    
    /** --------------------------------------------------------- */ 


    /** Creates one tower geometry at the origin. */
    private Geometry makeTower(float towerRadius, float towerHeight, int index) {
        Box tower_shape = new Box(
                Vector3f.ZERO,
                towerRadius,
                towerHeight * .5f,
                towerRadius);

        Geometry tower_geo = new Geometry("tower-" + index, tower_shape);
        tower_geo.setMaterial(tower_std_mat);
        return tower_geo;
    }

    /** Towers stand in two rows to the left and right of the positive z axis
     * in the "tower-protected valley". 
     * They shoot beams from their tops at creeps who are close enough. */
    public void initTowers() {

        Node towerNode = new Node("TowerNode");
        // The beam node holds the laser beams for all towers.
        Node beamNode = new Node("BeamNode");
        rootNode.attachChild(beamNode);
        // Generate a series of towers:
        for (int index = 0; index < TOWER_NUM; index++) {
            // Distribute towers to left and right of valley along positive z axis
            // Note: tower loc is in center and not at the feet, hence *1.5f for y.
            int leftOrRight = (index % 2 == 0 ? 1 : -1);
            float offset_x  = leftOrRight * 2.5f;
            float offset_y  = towerHeight * 1.5f; 
            Vector3f loc = new Vector3f( offset_x, offset_y, index);
            // tower geo
            Geometry tower_geo = makeTower(towerRadius, towerHeight, index);
            tower_geo.setLocalTranslation(loc);
            towerNode.attachChild(tower_geo);
            // the dot node holds the ChargeMarkers for one tower
            Node dotNode = new Node("dotNode-" + index);
            rootNode.attachChild(dotNode);
            // data is stored per tower geo, no data in the towernode.
            tower_geo.setUserData("index", index);
            tower_geo.setUserData("chargesNum", 0);
            tower_geo.setUserData("towerHeight", towerHeight);
            tower_geo.setUserData("dotNode", dotNode);
            tower_geo.setUserData("beamNode", beamNode);
            tower_geo.setUserData("creepNode", creepNode);
            tower_geo.setUserData("blastMaterial", blast_mat);
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
        for (int index = 0; index < CREEP_NUM; index++) {
            // distribute creeps to the left and right of the positive x axis
            int leftOrRight = (index % 2 == 0 ? 1 : -1); 
            float offset_x  = 1.5f * leftOrRight * FastMath.rand.nextFloat();
            float offset_z  = 2.5f * ((TOWER_NUM / 2f) + 3f);
            Vector3f spawnloc = new Vector3f(offset_x, 1f, offset_z);
            // creep geometry
            Geometry creep_geo = makeCreep(creepRadius, spawnloc, index);
            creepNode.attachChild(creep_geo);
            // data
            creep_geo.setUserData("index",  index);
            creep_geo.setUserData("health", CREEP_HEALTH);
            creep_geo.setUserData("speed", CREEP_SPEED);
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
     * Range: medium.
     */
    public Charge getFreezeCharge() {
        Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beam_mat.setColor("Color", ColorRGBA.Cyan);
        return new Charge(FREEZE, beam_mat);
    }

    /**
     * Gatling charges do minimal damage but they can be shot more often per round 
     * and at various targets. Range: far.
     */
    public Charge getGatlingCharge() {
        Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beam_mat.setColor("Color", ColorRGBA.Yellow);
        return new Charge(GATLING, beam_mat);
    }

    /**
     * Nuke charges do a lot of damage but they are expensive (only one shot per charge).
     * As a side effect they not only damage but also thaw/accelerate 
     * the neighbouring creeps! Range: short.
     */
    public Charge getNukeCharge() {
        Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beam_mat.setColor("Color", ColorRGBA.Red);
        return new Charge(NUKE, beam_mat);
    }
}
