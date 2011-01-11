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

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

public class BitmapText extends Node {
	
	private BitmapFont font;
    private StringBlock block;
    private float lineWidth = 0f;    
    private boolean needRefresh = true;
    private boolean rightToLeft = false;
    private final BitmapTextPage[] textPages;

    public BitmapText(BitmapFont font) {
        this(font, false, false);
    }

    public BitmapText(BitmapFont font, boolean rightToLeft) {
        this(font, rightToLeft, false);
    }

    public BitmapText(BitmapFont font, boolean rightToLeft, boolean arrayBased) {
        textPages = new BitmapTextPage[font.getPageSize()];
        for (int page = 0; page < textPages.length; page++) {
            textPages[page] = new BitmapTextPage(font, arrayBased, page);
            attachChild(textPages[page]);
        }
        
        this.font = font;
        this.block = new StringBlock();
        block.setSize(font.getPreferredSize());
    }

    @Override
    public BitmapText clone() {
        BitmapText clone = (BitmapText) super.clone();
        for (int i = 0; i < textPages.length; i++) {
            clone.textPages[i] = textPages[i].clone();
        }
        clone.block = block.clone();
        clone.needRefresh = true;
        return clone;
    }

    public BitmapFont getFont() {
        return font;
    }
    

    public void setSize(float size) {
        block.setSize(size);
        needRefresh = true;
    }

    public void setText(CharSequence text) {
        if (block.getText().equals(text)) {
            return;
        }

        block.setText(text);
        needRefresh = true;
    }

    public String getText() {
        return block.getText();
    }

    public ColorRGBA getColor() {
        return block.getColor();
    }

    public void setColor(ColorRGBA color) {
        if (block.getColor().equals(color)) {
            return;
        }

        block.setColor(color);
        needRefresh = true;
    }

    public void setBox(Rectangle rect) {
        block.setTextBox(rect);
        needRefresh = true;
    }
    
    public float getLineHeight() {
        return font.getLineHeight(block);
    }
    
    public float getLineWidth() {
        if (needRefresh) {
            assemble();
        }
        return lineWidth;
    }
    
    public void setAlignment(BitmapFont.Align align) {
        block.setAlignment(align);
    }

    public BitmapFont.Align getAlignment() {
        return block.getAlignment();
    }
    
    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);
        if (needRefresh) {
            assemble();
        }
    }

    private void assemble() {
        for (BitmapTextPage page : textPages) {
            page.assemble(font, block, rightToLeft);
        }
        needRefresh = false;
    }
    
    public void render(RenderManager rm, Material mat) {
        for (BitmapTextPage entry : textPages) {
            mat.render(entry, rm);
        }
    }
}
