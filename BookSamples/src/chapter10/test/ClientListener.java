package chapter10.test;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.concurrent.Callable;

/** WORK IN PROGRESS */

public class ClientListener implements MessageListener<Client> {

    private ClientMain app;
    private Client client;

    public void messageReceived(Client source, Message message) {
        if (message instanceof CubeMessage) {
            System.out.println("I received cubemessage from server");
            final CubeMessage cubeMessage = (CubeMessage) message;
            //app.enqueue(new Callable() {
            //    public Void call() {
                    app.changeCubeColor(cubeMessage.getColor());
            //        return null;
            //    }
            //});
        } 
    }

    
    
    /*A custom contructor to inform our client listener about the app. */
    public ClientListener(ClientMain app, Client client) {
        this.app = app;
        this.client = client;
    }
}
