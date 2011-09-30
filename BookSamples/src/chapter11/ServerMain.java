package chapter11;

import com.jme3.app.SimpleApplication;
import com.jme3.network.ClientStateListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author ruth
 */
public class ServerMain extends SimpleApplication implements ConnectionListener {

  Server myServer;
  int connections = 0;
  int connectionsOld = -1;

  public static void main(String[] args) {
    java.util.logging.Logger.getLogger("").setLevel(Level.SEVERE);
    ServerMain app = new ServerMain();
    app.start(JmeContext.Type.Headless);

  }

  @Override
  public void simpleInitApp() {
    try {
      myServer = Network.createServer("My Cool Game", 1, 6143, 6143);
      myServer.start();
    } catch (IOException ex) {
    }
    Serializer.registerClass(GreetingMessage.class);
    Serializer.registerClass(InetAddress.class, new InetAddressSerializer());
    myServer.addMessageListener(new ServerListener(), GreetingMessage.class);
    myServer.addConnectionListener(this);
    myServer.addMessageListener(new ServerListener(), InetAddressMessage.class);

  }

  @Override
  public void update() {
    connections = myServer.getConnections().size();
    if (connectionsOld != connections) {
      System.out.println("Server connections: " + connections);
      connectionsOld = connections;
    }
  }

  @Override
  public void destroy() {
    try {
      myServer.close();
    } catch (Exception ex) {}
    super.destroy();
  }

  public void connectionAdded(Server server, HostedConnection conn) {
    System.out.println("Server knows that client #"
            + conn.getId() + " is ready.");


  }

  public void connectionRemoved(Server server, HostedConnection conn) {
    System.out.println("Server knows that client #"
            + conn.getId() + " has left.");
    
  }
}
