package chapter11;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;

public class ClientListener implements MessageListener<Client> {

  public void messageReceived(Client source, Message message) {
    if (message instanceof InetAddressMessage) {
      InetAddressMessage m = (InetAddressMessage) message;
      System.out.println(m.getAddress().toString());
    }
    if (message instanceof GreetingMessage) {
      GreetingMessage helloMessage = (GreetingMessage) message;
      System.out.println("Client #" + source.getId()
              + " received the message: '"
              + helloMessage.getGreeting() + "'");
    }
  }
}
