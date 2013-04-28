package chapter10.test;

import com.jme3.math.ColorRGBA;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * @author ruth
 */
@Serializable(id = 0)
public class CubeMessage extends AbstractMessage {

    ColorRGBA color;

    public CubeMessage() {} // empty default constructor
    public CubeMessage(ColorRGBA color) {
        this.color = color;
    }
    public ColorRGBA getColor(){
        return color; 
    }
}
