package chapter10.test;

import com.jme3.math.ColorRGBA;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.concurrent.Callable;

public class ServerListener implements MessageListener<HostedConnection> {

    private ServerMain app;
    private Server server;

    public void messageReceived(HostedConnection source, Message message) {
        if (message instanceof CubeMessage) {
            CubeMessage cubeMessage = (CubeMessage) message;
            /* tell all clients! */
            server.broadcast(new CubeMessage(ColorRGBA.randomColor()));
        }
    }
    /* A custom contructor to inform our client listener about the app. */

    public ServerListener(ServerMain app, Server server) {
        this.app = app;
        this.server = server;
    }
}
