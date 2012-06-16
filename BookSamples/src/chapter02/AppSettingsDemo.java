package chapter02;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * Sample 2.5 
 * How to set app settings before the application starts.
 */
public class AppSettingsDemo extends SimpleApplication {

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        // specify your settings here
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        settings.setResolution(modes[0].getWidth(), modes[0].getHeight());
        settings.setFrequency(modes[0].getRefreshRate());
        settings.setDepthBits(modes[0].getBitDepth());
        settings.setFullscreen(device.isFullScreenSupported());
        //settings.setTitle("My Cool Game"); // only visible if not fullscreen
        settings.setSamples(2); // anti-aliasing
        // start app and use settings
        AppSettingsDemo app = new AppSettingsDemo();
        app.setSettings(settings); // apply settings to app
        app.setShowSettings(false);

        app.start();               // use settings and run
    }

    @Override
    /** Initialize the scene here: 
     *  Create Geometries and attach them to the rootNode. */
    public void simpleInitApp() {
        setDisplayFps(false);
        setDisplayStatView(false);

        Box b = new Box(Vector3f.ZERO, 1, 1, 1);   // create box mesh
        Geometry geom = new Geometry("Box", b);    // create geometry from mesh

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);        // color the material blue
        geom.setMaterial(mat);                        // assign the material to geometry
        rootNode.attachChild(geom);                   // make geometry appear in scene
    }

    @Override
    /** (optional) Interact with update loop here. 
     *  This is where the action will happen in your game. */
    public void simpleUpdate(float tpf) {
        /* Nothing yet. */
    }

    @Override
    /** (optional) Advanced renderer/frameBuffer modifications.  */
    public void simpleRender(RenderManager rm) {
        /* Not used in this example. */
    }
}
