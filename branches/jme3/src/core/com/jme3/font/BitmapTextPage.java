/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.font;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

class BitmapTextPage extends Geometry {
    
    private QuadList quadList = new QuadList();
    private final float[] pos;
    private final float[] tc;
    private final short[] idx;
    private final byte[] color;
    private int page;

    BitmapTextPage(BitmapFont font, boolean arrayBased, int page) {
        super("BitmapFont", new Mesh());

        if (font == null) {
            throw new NullPointerException("'font' cannot be null.");
        }

        this.page = page;

        Material mat = font.getPage(page);
        if (mat == null) {
            throw new IllegalStateException("The font's texture was not found!");
        }

        setMaterial(mat);

        // initialize buffers
        Mesh m = getMesh();
        m.setBuffer(Type.Position, 3, new float[0]);
        m.setBuffer(Type.TexCoord, 2, new float[0]);
        m.setBuffer(Type.Color, 4, new byte[0]);
        m.setBuffer(Type.Index, 3, new short[0]);

        // scale colors from 0 - 255 range into 0 - 1
        m.getBuffer(Type.Color).setNormalized(true);

        arrayBased = true;

        if (arrayBased) {
            pos = new float[4 * 3];  // 4 verticies * 3 floats
            tc = new float[4 * 2];  // 4 verticies * 2 floats
            idx = new short[2 * 3];  // 2 triangles * 3 indices
            color = new byte[4 * 4];   // 4 verticies * 4 bytes
        } else {
            pos = null;
            tc = null;
            idx = null;
            color = null;
        }
    }
    
    BitmapTextPage(BitmapFont font, boolean arrayBased) {
        this(font, arrayBased, 0);
    }

    BitmapTextPage(BitmapFont font) {
        this(font, false, 0);
    }

    @Override
    public BitmapTextPage clone() {
        BitmapTextPage clone = (BitmapTextPage) super.clone();
        clone.mesh = mesh.deepClone();
        clone.quadList = new QuadList();
        return clone;
    }

    


    float assemble(BitmapFont font, StringBlock block, boolean rightToLeft) {
        float lineWidth = 0;
        // first generate quadlist
        if (block.getTextBox() == null) {
            lineWidth = font.updateText(block, quadList, rightToLeft, page);
        } else {
            lineWidth = font.updateTextRect(block, quadList, page);
        }

        Mesh m = getMesh();
        m.setVertexCount(quadList.getQuantity() * 4);
        m.setTriangleCount(quadList.getQuantity() * 2);

        VertexBuffer pb = m.getBuffer(Type.Position);
        VertexBuffer tb = m.getBuffer(Type.TexCoord);
        VertexBuffer ib = m.getBuffer(Type.Index);
        VertexBuffer cb = m.getBuffer(Type.Color);

        FloatBuffer fpb = (FloatBuffer) pb.getData();
        FloatBuffer ftb = (FloatBuffer) tb.getData();
        ShortBuffer sib = (ShortBuffer) ib.getData();
        ByteBuffer bcb = (ByteBuffer) cb.getData();

        // increase capacity of buffers as needed
        fpb.rewind();
        fpb = BufferUtils.ensureLargeEnough(fpb, m.getVertexCount() * 3);
        pb.updateData(fpb);

        ftb.rewind();
        ftb = BufferUtils.ensureLargeEnough(ftb, m.getVertexCount() * 2);
        tb.updateData(ftb);

        bcb.rewind();
        bcb = BufferUtils.ensureLargeEnough(bcb, m.getVertexCount() * 4);
        cb.updateData(bcb);

        sib.rewind();
        sib = BufferUtils.ensureLargeEnough(sib, m.getTriangleCount() * 3);
        ib.updateData(sib);

        // go for each quad and append it to the buffers
        if (pos != null) {
            for (int i = 0; i < quadList.getQuantity(); i++) {
                FontQuad fq = quadList.getQuad(i);
                fq.storeToArrays(pos, tc, idx, color, i);
                fpb.put(pos);
                ftb.put(tc);
                sib.put(idx);
                bcb.put(color);
            }
        } else {
            for (int i = 0; i < quadList.getQuantity(); i++) {
                FontQuad fq = quadList.getQuad(i);
                fq.appendPositions(fpb);
                fq.appendTexCoords(ftb);
                fq.appendIndices(sib, i);
                fq.appendColors(bcb);
            }
        }

        fpb.rewind();
        ftb.rewind();
        sib.rewind();
        bcb.rewind();
        
        return lineWidth;
    }
}
