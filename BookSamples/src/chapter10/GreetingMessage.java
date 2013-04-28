package chapter10;

import com.jme3.network.AbstractMessage;
import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;

/**
 * Each message has a unique ID number, so they are serialized 
 * and deserialized in the right order.
 * @author ruth
 */
@Serializable(id=0)
public class GreetingMessage extends AbstractMessage {
     private String greeting = "Hello SpiderMonkey!"; // your message data
     public GreetingMessage() {}                  // empty default constructor
     public GreetingMessage(String s) {           // custom constructor
       greeting = s;
     }                  
     public void setGreeting(String s){greeting = s;}
     public String getGreeting(){return greeting;}

}
