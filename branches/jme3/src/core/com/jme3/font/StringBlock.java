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

import com.jme3.font.BitmapFont.Align;
import com.jme3.math.ColorRGBA;

/**
 * Defines a String that is to be drawn in one block that can be constrained by a {@link Rectangle}. Also holds
 * formatting information for the StringBlock
 *
 * @author dhdd
 */
public class StringBlock implements Cloneable {

    private StringBuilder text;
    private Rectangle textBox;
    private BitmapFont.Align alignment;
    private float size;
    private ColorRGBA color = new ColorRGBA(ColorRGBA.White);
    private boolean kerning;
    private int lineCount;

    /**
     *
     * @param text the text that the StringBlock will hold
     * @param textBox the rectangle that constrains the text
     * @param alignment the initial alignment of the text
     * @param size the size in pixels (vertical size of a single line)
     * @param color the initial color of the text
     * @param kerning
     */
    public StringBlock(String text, Rectangle textBox, BitmapFont.Align alignment, float size, ColorRGBA color,
            boolean kerning) {
        this.text = new StringBuilder();
        this.text.append(text);
        this.textBox = textBox;
        this.alignment = alignment;
        this.size = size;
        this.color.set(color);
        this.kerning = kerning;
    }

    public StringBlock(){
        this.text = new StringBuilder();
        this.text.append("");
        this.textBox = null;
        this.alignment = Align.Left;
        this.size = 100;
        this.color.set(ColorRGBA.White);
        this.kerning = true;
    }

    @Override
    public StringBlock clone(){
        try {
            StringBlock clone = (StringBlock) super.clone();
            clone.color = color.clone();
            if (textBox != null)
                clone.textBox = textBox.clone();
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public String getText() {
        return text.toString();
    }

    public CharSequence getCharacters(){
        return text;
    }

    public void setText(CharSequence text){
        this.text.setLength(0);
        this.text.append(text);
    }

    public Rectangle getTextBox() {
        return textBox;
    }

    public void setTextBox(Rectangle textBox) {
        this.textBox = textBox;
    }

    public BitmapFont.Align getAlignment() {
        return alignment;
    }

    public void setAlignment(BitmapFont.Align alignment) {
        this.alignment = alignment;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color.set(color);
    }

    public boolean isKerning() {
        return kerning;
    }

    public void setKerning(boolean kerning) {
        this.kerning = kerning;
    }

    public int getLineCount() {
        return lineCount;
    }

    void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

}