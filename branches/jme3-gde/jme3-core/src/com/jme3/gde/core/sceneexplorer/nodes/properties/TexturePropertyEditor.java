/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.sceneexplorer.nodes.properties;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.texture.Texture;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.LinkedList;

/**
 *
 * @author bowens
 */
public class TexturePropertyEditor implements PropertyEditor {

    private LinkedList<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();
    private Texture texture;


    public TexturePropertyEditor() {}

    public TexturePropertyEditor(Texture texture) {
        this.texture = texture;
    }

    public void setValue(Object value) {
        if (value instanceof Texture)
            texture = (Texture)value;
        else
            texture = null;
    }

    public Object getValue() {
        return texture;
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue(Graphics gfx, Rectangle box) {
        
    }

    public String getJavaInitializationString() {
        return null;
    }

    public String getAsText() {
        if (texture != null)
            return texture.getName();
        return null;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        
    }

    public String[] getTags() {
        return null;
    }

    public Component getCustomEditor() {
        ProjectAssetManager currentProjectAssetManager = null;
        //try {
            currentProjectAssetManager = (ProjectAssetManager) SceneApplication.getApplication().getAssetManager();
            TextureBrowser textureBrowser = new TextureBrowser(null, true, currentProjectAssetManager, this);
            return textureBrowser;
        //} catch (Exception e) {
            //Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage()+" Could not get project asset manager!", e);
            //return null;
        //}
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }


}
