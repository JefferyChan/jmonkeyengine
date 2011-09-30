package chapter11;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class InetAddressMessage extends AbstractMessage {
     private InetAddress address = new InetAddress();
     public InetAddressMessage() {}                  // empty default constructor
     public InetAddressMessage(InetAddress a) {      // custom constructor
       address = a;
     }                  
     public void setAddress(InetAddress a){address = a;}
     public InetAddress getAddress(){return address;}
}
