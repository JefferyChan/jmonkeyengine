package chapter10;

import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Spatial;
import java.util.concurrent.Callable;

/** WORK IN PROGRESS */

public class ClientListener implements MessageListener<Client> {

    private ClientMain app;
    private Client client;

    public void messageReceived(Client source, Message message) {
        if (message instanceof GreetingMessage) {
            GreetingMessage helloMessage = (GreetingMessage) message;
            System.out.println("Client #" + source.getId()
                    + " received the message: '"
                    + helloMessage.getGreeting() + "'");
            //next, we modify the scene graph
            //final Vector3f location = helloMessage.getLocation();
            //final int spatial = helloMessage.getSpatial();
//            app.enqueue(new Callable(){
//		public Void call(){
//                    
//		    //spatial.setLocalTranslation(location);
//		    return null;
//		}
//	});
        } else if (message instanceof InetAddressMessage) {
            InetAddressMessage addrMessage = (InetAddressMessage) message;
            // unused
        }
    }

    /*A custom contructor to inform our client listener about the app. */
    public ClientListener(ClientMain app, Client client) {
        this.app = app;
        this.client = client;
    }
}
