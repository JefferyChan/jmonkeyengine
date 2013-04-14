package chapter10;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.net.InetAddress;

@Serializable
public class InetAddressMessage extends AbstractMessage {
     private InetAddress address;
     public InetAddressMessage() {}                  // empty default constructor
     public InetAddressMessage(InetAddress a) {      // custom constructor
       address = a;
     }                  
     public void setAddress(InetAddress a){address = a;}
     public InetAddress getAddress(){return address;}
}
