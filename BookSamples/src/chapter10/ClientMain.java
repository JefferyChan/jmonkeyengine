package chapter10;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 *
 * @author ruth
 */
public class ClientMain extends SimpleApplication implements ClientStateListener {

    private Client myClient;

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("").setLevel(Level.SEVERE);
        ClientMain app = new ClientMain();
        app.start(JmeContext.Type.Display);
    }

    @Override
    public void simpleInitApp() {
        try {
            myClient = Network.connectToServer("My Cool Game", 1, "localhost", 6143);
            myClient.start();
        } catch (IOException ex) {
        }
        Serializer.registerClass(GreetingMessage.class);
        Serializer.registerClass(InetAddressMessage.class);
        Serializer.registerClass(InetAddress.class, new InetAddressSerializer());

        myClient.addMessageListener(new ClientListener(this,myClient),GreetingMessage.class);
        myClient.addMessageListener(new ClientListener(this,myClient),InetAddressMessage.class);

        myClient.addClientStateListener(this);

        // example 1
        Message m = new GreetingMessage("Hi server, do you hear me?");
        myClient.send(m);
        
        // example 2
        try {
            Message message = new InetAddressMessage(InetAddress.getByName("jmonkeyengine.org"));
            myClient.send(message);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

    }

    /* This example of a game action adds a cube at a random position 
    * and with random color. It's an example of a modification 
    * of the scenegraph in a networked game. */
    public void performExampleGameAction() {
        float x = FastMath.nextRandomFloat() * 10f - 5f;
        float y = FastMath.nextRandomFloat() * 10f - 5f;
        float z = FastMath.nextRandomFloat() * 10f - 5f;
        Box b = new Box(Vector3f.ZERO, x, y, z);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.randomColor());
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }

    @Override
    public void destroy() {
        try {
            myClient.close();
        } catch (Exception ex) {
        }
        super.destroy();
    }

    public void clientConnected(Client c) {
        System.out.println("Client #" + myClient.getId() + " is ready.");
    }

    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println("Client #" + myClient.getId() + " has left.");
    }
}
