package chapter11;

import com.jme3.network.serializing.Serializable;

@Serializable
public class InetAddress {

  static InetAddress getByName(String string) {
    return address;
  }
     private byte[] address = new byte[4];
}
