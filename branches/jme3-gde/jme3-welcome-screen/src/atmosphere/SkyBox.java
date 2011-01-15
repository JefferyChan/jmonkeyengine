package atmosphere;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Image;
import com.jme3.texture.TextureCubeMap;

/**
 *
 * @author jyarza
 */
public class SkyBox {

    private String name;
    private Material material;
    private TextureCubeMap cubemap;
    private Geometry geometry;
    private Sphere sphere;
    private AssetManager assetManager;
    
    public SkyBox(AssetManager assetManager, String name) {
        this.name = name;
        this.assetManager = assetManager;
        init();
    }

    private void init() {
        sphere = new Sphere(32, 32, 10f);
        geometry = new Geometry("SkyBox", sphere);
        geometry.setQueueBucket(Bucket.Sky);
        geometry.setShadowMode(ShadowMode.Off);
        geometry.updateModelBound();

        Image cube = assetManager.loadTexture("Textures/" + name + ".dds").getImage();       
        cubemap = new TextureCubeMap(cube);
        
        material = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
        material.setBoolean("SphereMap", false);
        material.setTexture("Texture", cubemap);
        material.setVector3("NormalScale", new Vector3f(1, 1, 1));
        geometry.setMaterial(material);
    }

    public void attach(Node node) {
        node.attachChild(geometry);
    }

    public void detach(Node node) {
        node.detachChild(geometry);
    }
}
