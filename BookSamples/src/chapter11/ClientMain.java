package chapter11;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.JmeContext;
import java.io.IOException;
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
    myClient.addMessageListener(new ClientListener(), GreetingMessage.class);
    myClient.addClientStateListener(this);

    Message m = new GreetingMessage("Hi server, do you hear me?");
    myClient.send(m);
    Message message = new InetAddressMessage(InetAddress.getByName("jmonkeyengine.org"));
    myClient.send(message);

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
