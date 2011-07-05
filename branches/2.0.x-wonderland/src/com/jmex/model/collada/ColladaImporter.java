/*
 * Copyright (c) 2003-2009 jMonkeyEngine
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
package com.jmex.model.collada;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.jme.animation.TextureKeyframeController;
import com.jme.light.LightNode;
import com.jme.scene.CameraNode;
import com.jme.scene.Geometry;
import com.jme.scene.Node;

/**
 * <code>ColladaNode</code> provides a mechanism to parse and load a COLLADA
 * (COLLAborative Design Activity) model. Making use of a DOM parse, the XML
 * formatted COLLADA file is parsed into Java Type classes and then processed by
 * jME. This processing is currently aimed at the 1.4.1 release of the COLLADA
 * Specification, and will, in most likelyhood, require updating with a new
 * release of COLLADA.
 *
 * @author Mark Powell, Rikard Herlitz, and others
 */
public class ColladaImporter {
    private static final Logger logger = Logger.getLogger(ColladaImporter.class
            .getName());
    // asset information
    private static ThreadSafeColladaImporter instance;

    // If true, models loaded by ColladaImporter will automatically have
    // geometry optimization applied. default: true.
    public static boolean OPTIMIZE_GEOMETRY = true;

    public static OptimizeCallback optimizeCallBack = null;

    public static boolean hasUserInformation(String key) {
        return hasUserInformation(key);
    }

    public static void addUserInformation(String key, Object value) {
        instance.addUserInformation(key, value);
    }

    public static Object getUserInformation(String key) {
        return instance.getUserInformation(key);
    }

    /**
     * load takes the model path as a string object and uses the
     * COLLADASchemaDoc object to load it. This is then stored as a heirarchy of
     * data objects. This heirarchy is passed to the processCollada method to
     * build the jME data structures necessary to view the model.
     *
     * @param source
     *            the source to import.
     * @param textureDirectory
     *            the location of the textures.
     * @param name
     *            the name of the node.
     */
    public static void load(InputStream source, String name) {
        if (instance == null) {
            instance = new ThreadSafeColladaImporter(name);
        }
        instance.load(source);
    }

    /**
     * @return
     */
    public static ArrayList<String> getUVControllerNames() {
        if (instance == null) {
            return null;
        }
        return instance.getUVControllerNames();
    }

    public static void addUVControllerName(String name) {
        instance.addUVControllerName(name);
    }

    /**
     * Returns the camera node names associated with this model.
     *
     * @return the list of camera names that are referenced in this file.
     */
    public static ArrayList<String> getCameraNodeNames() {
        if (instance == null) {
            return null;
        }
        return instance.getCameraNodeNames();
    }

    public static ArrayList<String> getLightNodeNames() {
        if (instance == null) {
            return null;
        }
        return instance.getLightNodeNames();
    }

    public static ArrayList<String> getGeometryNames() {
        if (instance == null) {
            return null;
        }
        return instance.getGeometryNames();
    }

    public static Node getModel() {
        if (instance == null) {
            return null;
        }
        return instance.getModel();
    }

    public static CameraNode getCameraNode(String id) {
        if (instance == null) {
            return null;
        }
        return (CameraNode) instance.getCameraNode(id);
    }

    public static LightNode getLightNode(String id) {
        if (instance == null) {
            return null;
        }
        return (LightNode) instance.getLightNode(id);
    }

    public static Object get(Object id) {
        return instance.get(id);
    }

    /**
     * places an object into the resource library with a given key. If there is
     * an object referenced by this key and it is not the same object that is to
     * be added to the library, a warning is issued. If this object already
     * exists in the library we do not readd it.
     *
     * @param key
     *            the key to obtain the object from the library.
     * @param value
     *            the object to store in the library.
     */
    public static void put(String key, Object value) {
        instance.put(key, value);
    }

    public static TextureKeyframeController getUVAnimationController(String id) {
        if (instance == null) {
            return null;
        }
        return (TextureKeyframeController) instance.getUVAnimationController(id);
    }

    public static Geometry getGeometry(String id) {
        if (instance == null) {
            return null;
        }
        return (Geometry) instance.getGeometry(id);
    }

    public static void cleanUp() {
        if (instance != null) {
            instance.cleanUp();
        }
    }

}
