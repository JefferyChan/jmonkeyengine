package jme3test.light;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;
import com.jme3.texture.Texture.WrapMode;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.GL11;

public class TestTransparentShadow extends SimpleApplication {

    public static void main(String[] args){
        TestTransparentShadow app = new TestTransparentShadow();
        app.start();
    }

    public void simpleInitApp() {
        GL11.glEnable(ARBMultisample.GL_SAMPLE_ALPHA_TO_COVERAGE_ARB);

        cam.setLocation(new Vector3f(2.0606942f, 3.20342f, 6.7860126f));
        cam.setRotation(new Quaternion(-0.017481906f, 0.98241085f, -0.12393151f, -0.13857932f));

        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        Quad q = new Quad(20, 20);
        q.scaleTextureCoordinates(Vector2f.UNIT_XY.mult(5));
        Geometry geom = new Geometry("floor", q);
        Material mat = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m");
        geom.setMaterial(mat);
        
        geom.rotate(-FastMath.HALF_PI, 0, 0);
        geom.center();
        geom.setShadowMode(ShadowMode.Receive);
        rootNode.attachChild(geom);

        // create the geometry and attach it
        Spatial teaGeom = assetManager.loadModel("Models/Tree/Tree2.mesh.xml");
        teaGeom.setQueueBucket(Bucket.Transparent);
        teaGeom.setShadowMode(ShadowMode.Cast);

        DirectionalLight dl1 = new DirectionalLight();
        dl1.setDirection(new Vector3f(1, -1, 1).normalizeLocal());
        dl1.setColor(new ColorRGBA(0.965f, 0.949f, 0.772f, 1f).mult(0.7f));
        rootNode.addLight(dl1);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.965f, 0.949f, 0.772f, 1f).mult(0.7f));
        rootNode.addLight(dl);

        rootNode.attachChild(teaGeom);
        
        PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 1);
        pssmRenderer.setDirection(new Vector3f(0, -1f, 0f).normalizeLocal());
        pssmRenderer.setLambda(0.55f);
        pssmRenderer.setShadowIntensity(0.6f);
        pssmRenderer.setCompareMode(CompareMode.Software);
        pssmRenderer.setFilterMode(FilterMode.PCF4);
        pssmRenderer.displayDebug();
        viewPort.addProcessor(pssmRenderer);
    }
}
