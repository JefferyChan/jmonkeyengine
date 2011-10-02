package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Dome;

/**
 * 
 * @author zathras
 */
public class CreepNode extends Node {

  public CreepNode(AssetManager as, CreepData cd, float creepRadius, float scale ) {
    Material creep_mat = new Material(as, "Common/MatDefs/Misc/Unshaded.j3md");
    creep_mat.setColor("Color", ColorRGBA.Black);
    
    Dome creep_shape = new Dome(Vector3f.ZERO, 10, 10, scale * creepRadius, false);
    Geometry creep_geo = new Geometry("Creep", creep_shape);
    creep_geo.setMaterial(creep_mat);
    this.attachChild(creep_geo);
    
    creep_geo.setLocalTranslation(cd.getLoc());
    creep_geo.addControl(new CreepControl(cd));
  }

}
