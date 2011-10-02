package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Visual representation of a tower. <br />
 * @param assetManager : From SimpleApplication.
 */
public class TowerNode extends Node {
private AssetManager as;
  private Node dotNode = new Node("dot node");

  public TowerNode(AssetManager as, TowerData td, float towerRadius) {
    this.as=as;
     
    Box tower_shape = new Box(Vector3f.ZERO,
            td.getScale() * towerRadius,
            td.getScale() * td.getHeight()/2,
            td.getScale() * towerRadius );

    Geometry tower_geo = new Geometry(td.getIndex()+"", tower_shape);
    tower_geo.setMaterial(getStandardMaterial());
    this.attachChild(tower_geo);
    tower_geo.setLocalTranslation(td.getLoc());
    tower_geo.addControl(new TowerControl(td));
    this.attachChild(dotNode);

  }
  public final Material getSelectedMaterial() {
    Material tower_mat = new Material(as,
            "Common/MatDefs/Misc/Unshaded.j3md");
    tower_mat.setColor("Color", new ColorRGBA(0.5f,1,0.5f,1f));
    return tower_mat;
  }

  public final Material getStandardMaterial() {
    Material tower_mat = new Material(as,
            "Common/MatDefs/Misc/Unshaded.j3md");
    tower_mat.setColor("Color", ColorRGBA.Green);
    return tower_mat;
  }

}
