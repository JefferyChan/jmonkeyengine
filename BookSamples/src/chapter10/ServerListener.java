package chapter10;

import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;

public class ServerListener implements MessageListener<HostedConnection> {

    public void messageReceived(HostedConnection source, Message message) {
        if (message instanceof GreetingMessage) {
            GreetingMessage helloMessage = (GreetingMessage) message;
            System.out.println("Server received '"
                    + helloMessage.getGreeting()
                    + "' from client #" + source.getId());
            // prepare and send an answer
            helloMessage.setGreeting("Welcome client #" + source.getId() + "!");
            source.send(helloMessage);
        }
    }
}
