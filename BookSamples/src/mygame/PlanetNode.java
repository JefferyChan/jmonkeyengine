package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 */
public class PlanetNode extends Node {

  public PlanetNode(AssetManager as) {
    Box b = new Box(Vector3f.ZERO, 200f, 1f, 200f);
    Geometry floor = new Geometry("Floor", b);
    Material mat_floor = new Material(as, "Common/MatDefs/Misc/Unshaded.j3md");
    mat_floor.setColor("Color", ColorRGBA.Orange);
    floor.setMaterial(mat_floor);
    this.attachChild(floor);
    floor.setLocalTranslation(0, -.5f, 0);
  }

  
}