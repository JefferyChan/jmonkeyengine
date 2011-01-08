package com.jme3.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

public final class Screenshots {
    public static void convertScreenShot(ByteBuffer bgraBuf, BufferedImage out){
        WritableRaster wr = out.getRaster();
        DataBufferByte db = (DataBufferByte) wr.getDataBuffer();

        byte[] cpuArray = db.getData();

        // copy native memory to java memory
        bgraBuf.clear();
        bgraBuf.get(cpuArray);
        bgraBuf.clear();

        int width  = wr.getWidth();
        int height = wr.getHeight();

        // flip the components the way AWT likes them
        for (int y = 0; y < height / 2; y++){
            for (int x = 0; x < width; x++){
                int inPtr  = (y * width + x) * 4;
                int outPtr = ((height-y-1) * width + x) * 4;

                byte b = cpuArray[inPtr+0];
                byte g = cpuArray[inPtr+1];
                byte r = cpuArray[inPtr+2];
                byte a = cpuArray[inPtr+3];

                cpuArray[outPtr+0] = a;
                cpuArray[outPtr+1] = b;
                cpuArray[outPtr+2] = g;
                cpuArray[outPtr+3] = r;
            }
        }
    }
}
