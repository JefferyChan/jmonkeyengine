package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * A visualization of the player base.
 */
public class PlayerNode extends Node {

  AssetManager as;

  public PlayerNode(AssetManager as, Vector3f loc, float scale) {
    this.as = as;
    Material playerbase_mat = new Material(as,
            "Common/MatDefs/Misc/Unshaded.j3md");
    playerbase_mat.setColor("Color", ColorRGBA.Green);
    Box b = new Box(loc, scale*3f/2f,scale/2,scale/2);
    Geometry playerbase = new Geometry("Playerbase", b);
    playerbase.setMaterial(playerbase_mat);
    this.attachChild(playerbase);
    this.move(0,scale,0);
  }
}
