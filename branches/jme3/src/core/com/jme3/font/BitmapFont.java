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

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import java.io.IOException;

/**
 * Represents a font within jME that is generated with the AngelCode Bitmap Font Generator
 * @author dhdd
 */
public class BitmapFont implements Savable {

    public enum Align {
        Left, Center, Right
    }

    private BitmapCharacterSet charSet;
    private Material[] pages;
    private ColorRGBA textColor = new ColorRGBA(ColorRGBA.White);

    public BitmapFont() {
    }

    public BitmapText createLabel(String content){
        BitmapText label = new BitmapText(this);
        label.setSize(getCharSet().getRenderedSize());
        label.setText(content);
        return label;
    }

    public float getPreferredSize(){
        return getCharSet().getRenderedSize();
    }

    public void setCharSet(BitmapCharacterSet charSet) {
        this.charSet = charSet;
    }

    public void setPages(Material[] pages) {
        this.pages = pages;
    }

    public Material getPage(int index) {
        return pages[index];
    }

    public int getPageSize() {
        return pages.length;
    }

    public BitmapCharacterSet getCharSet() {
        return charSet;
    }
    
    /**
     * Gets the line height of a StringBlock.
     * @param sb
     * @return
     */
    public float getLineHeight(StringBlock sb) {
        return charSet.getLineHeight() * (sb.getSize() / charSet.getRenderedSize());
    }

    public float getCharacterAdvance(char curChar, char nextChar, float size){
        BitmapCharacter c = charSet.getCharacter(curChar);
        if (c == null)
            return 0f;

        int kerning = c.getKerning((int)nextChar);
        float advance = size * c.getXAdvance();
        if (kerning != -1){
            advance += kerning * size;
        }
        return advance;
    }

    private int findKerningAmount(int newLineLastChar, int nextChar) {
        BitmapCharacter c = charSet.getCharacter(newLineLastChar);
        if (c == null)
            return 0;
        return c.getKerning(nextChar);
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(charSet, "charSet", null);
        oc.write(pages, "pages", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        charSet = (BitmapCharacterSet) ic.readSavable("charSet", null);
        Savable[] pagesSavable = ic.readSavableArray("pages", null);
        pages = new Material[pagesSavable.length];
        System.arraycopy(pagesSavable, 0, pages, 0, pages.length);
    }

    public float getLineWidth(CharSequence text){
        float lineWidth = 0f;
        float maxLineWidth = 0f;
        char lastChar = 0;
        boolean firstCharOfLine = true;
//        float sizeScale = (float) block.getSize() / charSet.getRenderedSize();
        float sizeScale = 1f;
        for (int i = 0; i < text.length(); i++){
            char theChar = text.charAt(i);
            if (theChar == '\n'){
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0f;
            }
            BitmapCharacter c = charSet.getCharacter((int) theChar);
            if (c != null){
                if (theChar == '\\' && i<text.length()-1 && text.charAt(i+1)=='#'){
                    if (i+5<text.length() && text.charAt(i+5)=='#'){
                        i+=5;
                        continue;
                    }else if (i+8<text.length() && text.charAt(i+8)=='#'){
                        i+=8;
                        continue;
                    }
                }
                if (!firstCharOfLine){
                    int amount = findKerningAmount(lastChar, theChar);
                    if (amount != -1){
                        lineWidth += amount * sizeScale;
                    }
                }
                float xAdvance = c.getXAdvance() * sizeScale;
                lineWidth += xAdvance;
            }
        }
        return Math.max(maxLineWidth, lineWidth);
    }

    float updateText(StringBlock block, QuadList target, boolean rightToLeft, int page) {

        CharSequence text = block.getCharacters();
        Align alignment = block.getAlignment();
        float x = 0;
        float y = 0;
        float lineWidth = 0f;
        float maxLineWidth = 0f;
        float sizeScale = (float) block.getSize() / charSet.getRenderedSize();
        BitmapCharacter lastChar = null;
        int lineNumber = 1;
        int wordNumber = 1;
        float wordWidth = 0f;
        boolean useKerning = block.isKerning();
        target.clear();

        float incrScale = rightToLeft ? -1f : 1f;
        textColor.set(block.getColor());

        for (int i = 0; i < text.length(); i++){
            char theChar = text.charAt(i);
            BitmapCharacter c = charSet.getCharacter((int) theChar);

            if (theChar == '\\' && i<text.length()-1 && text.charAt(i+1)=='#'){
                // NOTE: Here we accept in-text commands.
                if (i+5<text.length() && text.charAt(i+5)=='#'){
                    // parse shortened color
                    // convert from hex to int from 0 to 15, then multiply by 16
                    try {
                        float r = Integer.parseInt(Character.toString(text.charAt(i+2)), 16) / 15f;
                        float g = Integer.parseInt(Character.toString(text.charAt(i+3)), 16) / 15f;
                        float b = Integer.parseInt(Character.toString(text.charAt(i+4)), 16) / 15f;
                        textColor.set(r, g, b, 1f);
                        i += 5;
                        continue;
                    } catch (NumberFormatException ex){
                        // if an issue happens, ignore this color directive.
                    }
                }else if (i+8<text.length() && text.charAt(i+8)=='#'){
                    try {
                        float r = Integer.parseInt(text.subSequence(i+2,i+4).toString(), 16) / 255f;
                        float g = Integer.parseInt(text.subSequence(i+4,i+6).toString(), 16) / 255f;
                        float b = Integer.parseInt(text.subSequence(i+6,i+8).toString(), 16) / 255f;
                        textColor.set(r, g, b, 1f);
                        i += 8;
                        continue;
                    } catch (NumberFormatException ex){
                        // if an issue happens, ignore this color directive.
                    }
                }
            }

            if (theChar == '\n'){
                x = 0;
                y -= charSet.getLineHeight() * sizeScale;

                // Justify the last (now complete) line
                if (alignment == Align.Center){
                    for (int k = 0; k < target.getQuantity(); k++){
                        FontQuad q = target.getQuad(k);
                        if (q.getLineNumber() == lineNumber){
                            q.setX(q.getX() - lineWidth / 2f);
                        }
                    }
                }
                if (alignment == Align.Right){
                    for (int k = 0; k < target.getQuantity(); k++){
                        FontQuad q = target.getQuad(k);
                        if (q.getLineNumber() == lineNumber){
                            q.setX(q.getX() - lineWidth);
                        }
                    }
                }
                if (rightToLeft){
                    // move all characters so that the current X = 0
                    for (int k = 0; k < target.getQuantity(); k++){
                        FontQuad q = target.getQuad(k);
                        if (q.getLineNumber() == lineNumber){
                            q.setX(q.getX() + lineWidth);
                        }
                    }
                }

                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0f;
                wordWidth = 0f;

                wordNumber = 1;
                lineNumber++;
            }else if (theChar == '\r' || theChar == '\t'){
                // dont print these characters
                continue;
            }else if (c != null){
                float xOffset = c.getXOffset() * sizeScale;
                float yOffset = c.getYOffset() * sizeScale;
                float xAdvance = c.getXAdvance() * sizeScale;
                float width = c.getWidth() * sizeScale;
                float height = c.getHeight() * sizeScale;

                // Adjust for kerning
                float kernAmount = 0f;
                if (lastChar != null && useKerning){
                    int amount = lastChar.getKerning(theChar);
                    if (amount != -1){
                        kernAmount = amount * sizeScale;
                        x += kernAmount * incrScale;
                        lineWidth += kernAmount;
                        wordWidth += kernAmount;
                    }
                }

                final float x0 = x;
                final float wordWidth0 = wordWidth;

                x += xAdvance * incrScale;
                wordWidth += xAdvance;
                lineWidth += xAdvance;

                if (c.getPage() != page) {
                    continue;
                }

                // Create the quad
                FontQuad q = target.newQuad();

                // Determine quad position
                float quadPosX = x0 + (xOffset * incrScale);
                if (rightToLeft){
                    quadPosX -= width;
                }

                float quadPosY = y - yOffset;
                q.setPosition(quadPosX, quadPosY);
                q.setSize(width, height);

                float u0 = (float) c.getX() / charSet.getWidth();
                float v0 = (float) c.getY() / charSet.getHeight();
                float w = (float) c.getWidth() / charSet.getWidth();
                float h = (float) c.getHeight() / charSet.getHeight();
                q.setUV(u0, v0, w, h);

                q.setColor(textColor);
                q.setLineNumber(lineNumber);

                if (theChar == ' '){
                    // since this is a space,
                    // increment wordnumber and reset wordwidth
                    wordNumber++;
                    wordWidth = xAdvance;
                }

                // set data
                q.setWordWidth(wordWidth0);
                q.setWordWidth(wordWidth);
                q.setBitmapChar(c);
                q.setSizeScale(sizeScale);
                q.setCharacter(text.charAt(i));
                q.setTotalWidth(kernAmount + xAdvance);

                lastChar = c;
            }
        }
        block.setLineCount(lineNumber);

        return Math.max(lineWidth, maxLineWidth);
    }

    float updateTextRect(StringBlock b, QuadList target, int page) {

        String text = b.getText();
        float x = b.getTextBox().x;
        float y = b.getTextBox().y;
        float maxWidth = b.getTextBox().width;
        float lastLineWidth = 0f;
        float lineWidth = 0f;
        float maxLineWidth = 0f;
        float sizeScale = b.getSize() / charSet.getRenderedSize();
        char lastChar = 0;
        int lineNumber = 1;
        int wordNumber = 1;
        float wordWidth = 0f;
        boolean firstCharOfLine = true;
        boolean useKerning = b.isKerning();
        Align alignment = b.getAlignment();

        target.clear();

        for (int i = 0; i < text.length(); i++){
            BitmapCharacter c = charSet.getCharacter((int) text.charAt(i));
            boolean newLine = text.charAt(i) == '\n';
            if (newLine){
                x = b.getTextBox().x;
                y -= charSet.getLineHeight() * sizeScale;

                firstCharOfLine = true;
                maxLineWidth = Math.max(lineWidth, maxLineWidth);
                lastLineWidth = lineWidth;
                lineWidth = 0f;

                wordNumber = 1;
                lineNumber++;
            }else if (c == null){
                 System.out.println("Character '" + text.charAt(i) + "' is not in alphabet, skipping it.");
            }else{
                float xOffset = c.getXOffset() * sizeScale;
                float yOffset = c.getYOffset() * sizeScale;
                float xAdvance = c.getXAdvance() * sizeScale;
                float width = c.getWidth() * sizeScale;
                float height = c.getHeight() * sizeScale;

                // Newline
                if (lineWidth + xAdvance >= maxWidth){
                    x = b.getTextBox().x;
                    y -= charSet.getLineHeight() * sizeScale;
//                    float offset = 0f;
                    if ((lineWidth + xAdvance >= maxWidth) && (wordNumber != 1)){
                        // Next character extends past text box width
                        // We have to move the last word down one line
                        char newLineLastChar = 0;
                        maxLineWidth = Math.max(lineWidth, maxLineWidth);
                        lastLineWidth = lineWidth;
                        lineWidth = 0f;


                        for (int j = 0; j < target.getActualSize(); j++){
                            FontQuad q = target.getQuad(j);
                            BitmapCharacter localChar = q.getBitmapChar();

                            float localxOffset = localChar.getXOffset() * sizeScale;
                            float localyOffset = localChar.getYOffset() * sizeScale;
                            float localxAdvance = localChar.getXAdvance() * sizeScale;

                            // Move current word to the left side of the text box
                            if ((q.getLineNumber() == lineNumber) && (q.getWordNumber() == wordNumber)){
                                if (alignment == Align.Left && q.getCharacter() == ' '){
                                    continue;
                                }
                                q.setLineNumber(q.getLineNumber() + 1);
                                q.setWordNumber(1);
                                float quadPosX = x + localxOffset;
                                float quadPosY = y - localyOffset;
                                q.setPosition(quadPosX, quadPosY);

                                x += localxAdvance;
                                lastLineWidth -= localxAdvance;
                                lineWidth += localxAdvance;
                                int amount = findKerningAmount(newLineLastChar, q.getCharacter());
                                if (amount != -1 && useKerning){
                                    x += amount * sizeScale;
                                    lineWidth += amount * sizeScale;
                                }
                            }

                            newLineLastChar = q.getCharacter();
                        }

                        // Justify the previous (now complete) line
                        if (alignment == Align.Center){
                            for (int k = 0; k < target.getQuantity(); k++){
                                FontQuad q = target.getQuad(k);

                                if (q.getLineNumber() == lineNumber){
                                    q.setX(q.getX() + b.getTextBox().width / 2f - lastLineWidth / 2f);
                                }
                            }
                        }
                        if (alignment == Align.Right){
                            for (int k = 0; k < target.getQuantity(); k++){
                                FontQuad q = target.getQuad(k);
                                if (q.getLineNumber() == lineNumber){
                                    q.setX(q.getX() + b.getTextBox().width - lastLineWidth);
                                }
                            }
                        }

                    }else{
                        // New line without any "carry-down" word
                        firstCharOfLine = true;
                        maxLineWidth = Math.max(lineWidth, maxLineWidth);
                        lastLineWidth = lineWidth;
                        lineWidth = 0f;
                    }

                    wordNumber = 1;
                    lineNumber++;

                } // End new line check

                // Dont print these
                if (text.charAt(i) == '\n' || text.charAt(i) == '\r' || text.charAt(i) == '\t'){
                    continue;
                }

                // Set starting cursor for alignment
                if (firstCharOfLine){
                    x = b.getTextBox().x;
                }

                // Adjust for kerning
                float kernAmount = 0f;
                if (!firstCharOfLine && useKerning){
                    int amount = findKerningAmount(lastChar, (char) text.charAt(i));
                    if (amount != -1){
                        kernAmount = amount * sizeScale;
                        x += kernAmount;
                        lineWidth += kernAmount;
                        wordWidth += kernAmount;
                    }
                }
                firstCharOfLine = false;

                final float x0 = x;

                x += xAdvance;
                lineWidth += xAdvance;
                if (c.getPage() != page) {
                    continue;
                }

                // edit the quad
                FontQuad q = target.newQuad();

                float quadPosX = x0 + (xOffset);
                float quadPosY = y - yOffset;
                q.setPosition(quadPosX, quadPosY);
                q.setSize(width, height);

                float u0 = (float) c.getX() / charSet.getWidth();
                float v0 = (float) c.getY() / charSet.getHeight();
                float w = (float) c.getWidth() / charSet.getWidth();
                float h = (float) c.getHeight() / charSet.getHeight();
                q.setUV(u0, v0, w, h);
                q.setColor(b.getColor());

                q.setLineNumber(lineNumber);
                if (text.charAt(i) == ' '){
                    wordNumber++;
                    wordWidth = 0f;
                }
                q.setWordNumber(wordNumber);
                wordWidth += xAdvance;
                q.setWordWidth(wordWidth);
                q.setBitmapChar(c);
                q.setSizeScale(sizeScale);
                q.setCharacter(text.charAt(i));

                lastChar = text.charAt(i);

            }
        }

        // Justify the last (now complete) line
        if (alignment == Align.Center){
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() + b.getTextBox().width / 2f - lineWidth / 2f);
                }
            }
        }
        if (alignment == Align.Right){
            for (int k = 0; k < target.getQuantity(); k++){
                FontQuad q = target.getQuad(k);
                if (q.getLineNumber() == lineNumber){
                    q.setX(q.getX() + b.getTextBox().width - lineWidth);
                }
            }
        }
        b.setLineCount(lineNumber);

        return Math.max(maxLineWidth,lineWidth);
    }

}