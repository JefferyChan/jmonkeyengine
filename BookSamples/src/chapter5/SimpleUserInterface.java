package chapter5;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.ui.Picture;

/**
 * A 2D user interface with text and icons.
 */
public class SimpleUserInterface extends SimpleApplication {
    private static SimpleUserInterface app;   // The application object
    private float distance=0;
    private BitmapText distanceText;

    @Override
    /** initialize the scene here */
    public void simpleInitApp() {
        // deactivate default statistics displays
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        
        Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);   // create box mesh
        Geometry geom = new Geometry("Box", mesh);    // create object from mesh

        Material mat = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);        // color the material blue
        geom.setMaterial(mat);                        // give object the blue material
        rootNode.attachChild(geom);                   // make object appear in scene
        
        // Display a line of text in the default font on depth layer 0
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        distanceText = new BitmapText(guiFont);
        distanceText.setSize(guiFont.getCharSet().getRenderedSize());
        distanceText.move( // x/y coordinates and depth layer 0
                settings.getWidth()/2+50, distanceText.getLineHeight()+20, 0);
        guiNode.attachChild(distanceText);
        
        // Display a 2D image or icon on depth layer -2
        Picture frame = new Picture("User interface frame");
        frame.setImage(assetManager, "Interface/frame.png", false);
        frame.move(settings.getWidth()/2-265, 0, -2);
        frame.setWidth(530);
        frame.setHeight(10);
        guiNode.attachChild(frame);
        
        
        // Display a 2D image or icon on depth layer -1
        Picture logo = new Picture("logo");
        logo.setImage(assetManager, "Interface/Monkey.png", true);
        logo.move(settings.getWidth()/2-47, 2, -1); 
        logo.setWidth(95);
        logo.setHeight(75);
        guiNode.attachChild(logo); 
    }

    @Override
    /** (optional) Interact with update loop here */
    public void simpleUpdate(float tpf) {
        distance = Vector3f.ZERO.distance(cam.getLocation());   
        distanceText.setText("Distance: "+distance); // update the display
    } 

    @Override
    /** (optional) Advanced renderer/frameBuffer modifications */
    public void simpleRender(RenderManager rm) {}

    /** Start the jMonkeyEngine application */
    public static void main(String[] args) {
        app = new SimpleUserInterface();
        app.start();
    }
}


