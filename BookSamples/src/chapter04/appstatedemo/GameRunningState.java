package chapter04.appstatedemo;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.ArrayList;
import java.util.List;

/**
 * A template how to create an Application State. This example state simply
 * changes the background color depending on the camera position.
 */
public class GameRunningState extends AbstractAppState {

    private ViewPort viewPort;
    private Node rootNode;
    private Node guiNode;
    private AssetManager assetManager;
    private Node localRootNode = new Node("Game Screen RootNode");
    private Node localGuiNode = new Node("Game Screen GuiNode");
    private final ColorRGBA backgroundColor = ColorRGBA.Blue;
    private List<AppState> appstatelist;

    public GameRunningState(SimpleApplication app) {
        this.rootNode = app.getRootNode();
        this.viewPort = app.getViewPort();
        this.guiNode = app.getGuiNode();
        this.assetManager = app.getAssetManager();
        appstatelist = new ArrayList<AppState>();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        rootNode.attachChild(localRootNode);
        guiNode.attachChild(localGuiNode);
        
        viewPort.setBackgroundColor(backgroundColor);
        for (AppState appState : appstatelist) {
            stateManager.attach(appState);
        }

        /** Load this scene */
        viewPort.setBackgroundColor(backgroundColor);

        Box mesh = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", mesh);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        geom.setMaterial(mat);
        geom.setLocalTranslation(1, 0, 0);
        localRootNode.attachChild(geom);

        /** Load the HUD*/
        BitmapFont guiFont = assetManager.loadFont(
                "Interface/Fonts/Default.fnt");
        BitmapText displaytext = new BitmapText(guiFont);
        displaytext.setSize(guiFont.getCharSet().getRenderedSize());
        displaytext.move(10, displaytext.getLineHeight() + 20, 0);
        displaytext.setText("Game running. Press BACKSPACE to pause and return to the start screen.");
        localGuiNode.attachChild(displaytext);
    }

    @Override
    public void update(float tpf) {
        /** the action happens here */
        Vector3f v = viewPort.getCamera().getLocation();
        viewPort.setBackgroundColor(new ColorRGBA(v.getX() / 10, v.getY() / 10, v.getZ() / 10, 1));
        rootNode.getChild("Box").rotate(tpf, tpf, tpf);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        rootNode.detachChild(localRootNode);
        guiNode.detachChild(localGuiNode);
    }

}