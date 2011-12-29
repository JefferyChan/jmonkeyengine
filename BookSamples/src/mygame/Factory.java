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
 * The factory class initializes the scene. 
 * The number of elements generated depends on the level.
 * Init order: 1. playerbase, 2. creeps, 3. towers.
 * (Because towers depend on creeps (towers want to shoot at creeps);
 * and creeps depend on the playerbase (they want to attack the player)).
 * @author ruth
 */
public final class Factory {

    private AssetManager assetManager;
    private Node rootNode;
    private int level;
    // Geometry size ratios 
    private final float creepRadius = 0.3f; 
    private final float towerRadius = 0.3f; 
    private final float towerHeight = 2.0f; 
    private int creepNum;
    private int towerNum;
    
    Node playerbase_node = new Node("PlayerBaseNode");;
    Node creepNode = new Node("CreepNode");
        
    Material creep_mat;
    Material floor_mat;
    Material playerbase_mat;
    Material tower_sel_mat;
    Material tower_std_mat;
    Material blast_mat;

    public Factory(Node rootNode, AssetManager as, int level) {
        this.assetManager = as;
        this.rootNode=rootNode;
        this.level = level;
        this.creepNum = level * 6;
        this.towerNum = level * 4;
        initMaterials();
        initPlayerBase(); // first
        initCreeps(); // second
        initTowers(); // third
    }

    private void initMaterials() {
        // creep material
        creep_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        creep_mat.setColor("Color", ColorRGBA.Black);
        // floor material
        floor_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.setColor("Color", ColorRGBA.Orange);
        //player material
        playerbase_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        playerbase_mat.setColor("Color", ColorRGBA.Yellow);
        // tower  SelectedMaterial
        tower_sel_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        tower_sel_mat.setColor("Color", new ColorRGBA(0.5f, 1, 0.5f, 1f));
        //tower StandardMaterial
        tower_std_mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        tower_std_mat.setColor("Color", ColorRGBA.Green);
        blast_mat =(Material)assetManager.loadMaterial( "Materials/blast.j3m");
    }

    public void initPlayerBase() {
        // player base geometry
        Box b2 = new Box(Vector3f.ZERO,  3f / 2f,  .5f, .5f);
        Geometry playerbase_geo = new Geometry("Playerbase", b2);
        playerbase_geo.setMaterial(playerbase_mat);
        playerbase_geo.move(0, 1, 0);
        // node+geo
        playerbase_node.attachChild(playerbase_geo);
        
        Node planet_node = new Node("Floor");
        // floor geometry
        Box b = new Box(Vector3f.ZERO, 33f, 1f, 33f);
        Geometry floor = new Geometry("Floor", b);
        floor.setMaterial(floor_mat);
        floor.setLocalTranslation(0, -.5f, 0);
        //node+geo
        planet_node.attachChild(floor);
        //data
        playerbase_node.setUserData("level", level);
        playerbase_node.setUserData("score", 0);
        playerbase_node.setUserData("kills", 0);
        playerbase_node.setUserData("loc", playerbase_geo.getLocalTranslation());
        playerbase_node.setUserData("health", level * 3f);
        playerbase_node.setUserData("budget", level * 10);
        playerbase_node.addControl(new PlayerBaseControl());
        // add nodes to rootNode
        playerbase_node.attachChild(planet_node);
        rootNode.attachChild(playerbase_node);
     
            /** A white, directional light source */ 
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1.3f,0,-1.3f).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun); 
    }


    /** Creates one tower geometry */
    private Geometry makeTower(float towerRadius, float towerHeight, int index) {
        Box tower_shape = new Box(
                Vector3f.ZERO,
                towerRadius ,
                towerHeight *.5f,
                towerRadius );
        
        Geometry tower_geo = new Geometry("tower-" + index, tower_shape);
        tower_geo.setMaterial(tower_std_mat);
        return tower_geo;
    }

    /** Towers stand in two rows to the left and right. 
     * They shoot beams from their tops at creeps who are close. */
    public void initTowers() {
        
        Node towerNode = new Node("TowerNode");
        Node beamNode = new Node("BeamNode");

        for (int index = 0; index < towerNum; index++) {
            // Note: tower loc is not at the feet of the object, hence *1.5f for y.
            int offset_x = (index % 2 == 0 ? 1 : -1);
            Vector3f loc = new Vector3f(
                    offset_x * 2.5f ,
                    towerHeight * 1.5f,
                    index ); 
            Geometry tower_geo = makeTower(towerRadius, towerHeight, index);
            tower_geo.setLocalTranslation(loc);
            // attach
            towerNode.attachChild(tower_geo);
            // data
            Node dotNode = new Node("dotNode-"+index);
            towerNode.attachChild(dotNode);
            tower_geo.setUserData("index", index);
            tower_geo.setUserData("chargesNum", 0);
            tower_geo.setUserData("towerHeight", towerHeight);
            tower_geo.setUserData("dotNode",  dotNode);
            tower_geo.setUserData("beamNode", beamNode);
            tower_geo.setUserData("creepNode", creepNode);
            tower_geo.setUserData("blastMaterial",blast_mat);
            tower_geo.setUserData("selectedMaterial", tower_sel_mat);
            tower_geo.setUserData("standardMaterial", tower_std_mat);
            tower_geo.addControl(new TowerControl());  
        }
        towerNode.attachChild(beamNode);
                // add nodes to rootNode
        rootNode.attachChild(towerNode);        

    }

        /** Creates one creep geometry */
    private Geometry makeCreep(float creepRadius, Vector3f loc, int index) {
        Dome creep_shape = new Dome(Vector3f.ZERO, 
                10, 10, creepRadius, false);
        Geometry creep_geo = new Geometry("Creep-"+index, creep_shape);
        creep_geo.setMaterial(creep_mat);
        creep_geo.setLocalTranslation(loc);
        return creep_geo;
    }

    /** Creeps start at a certain distance from the towers at a spawnloc 
     * with random X coordinates within an interval. */
    public void initCreeps() {
        for (int index = 0; index < creepNum; index++) {
            int offset_x = (index % 2 == 0 ? 1 : -1);
            float random = FastMath.rand.nextFloat() * 1.5f * offset_x;
            Vector3f spawnloc = new Vector3f(random, 1f, 2.5f * ((towerNum / 2f) + 3f));
            Geometry creep_geo = makeCreep(creepRadius, spawnloc, index);
            // attach
            creepNode.attachChild(creep_geo);
            // data
            creep_geo.setUserData("index", index);
            creep_geo.setUserData("health", level * 15f);
            creep_geo.setUserData("speed", FastMath.rand.nextFloat()+(level/20f));
            creep_geo.setUserData("startloc", spawnloc);
            creep_geo.setUserData("playerdata", playerbase_node.getControl(PlayerBaseControl.class));
            creep_geo.addControl(new CreepControl()); 
        }
        creepNode.setUserData("CreepNum", creepNum);
        // add nodes to rootNode
        rootNode.attachChild(creepNode);
    }
    
    /** --------------------------------------------------------------------*/
    
     /**
     * freeze charges slow down the target and do a bit of damage.
     */
    public Charge getFreezeCharge() {
        Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beam_mat.setColor("Color", ColorRGBA.Cyan);
        // SpeedDamage, Healthdamage, Ammo, Range, Blastrange
        return new Charge(-.5f, -2f, 3, 2f * towerHeight, 1f, beam_mat);
    }

    /**
     * gatling charges do minimal damage but they can be shot more often at various targets.
     */
    public Charge getGatlingCharge() {
        Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beam_mat.setColor("Color", ColorRGBA.Yellow);
        // SpeedDamage, Healthdamage, Ammo, Range, Blastrange
        return new Charge(0f, -1f, 8, 2.5f * towerHeight, 0f, beam_mat);
    }

    /**
     * nuke charges do a lot of damage but they are expensive (only one shot per charge).
     */
    public Charge getNukeCharge() {
        Material beam_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        beam_mat.setColor("Color", ColorRGBA.Red);
        // SpeedDamage, Healthdamage, Ammo, Range, Blastrange
        return new Charge(0f, -8f, 1, 2f * towerHeight, 2f, beam_mat);
    }
}
