/*
 * Copyright (c) 2009-2012 jMonkeyEngine
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
package com.jme3.asset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.jme3.animation.Animation;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.post.Filter;
import com.jme3.scene.CameraNode;
import com.jme3.scene.LightNode;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

/**
 * Blender key. Contains path of the blender file and its loading properties.
 * @author Marcin Roguski (Kaelthas)
 */
public class BlenderKey extends ModelKey {

    protected static final int          DEFAULT_FPS               = 25;
    /**
     * FramesPerSecond parameter describe how many frames there are in each second. It allows to calculate the time
     * between the frames.
     */
    protected int                       fps                       = DEFAULT_FPS;
    /**
     * This variable is a bitwise flag of FeatureToLoad interface values; By default everything is being loaded.
     */
    protected int                       featuresToLoad            = FeaturesToLoad.ALL;
    /** This variable determines if assets that are not linked to the objects should be loaded. */
    protected boolean                   loadUnlinkedAssets;
    /** The root path for all the assets. */
    protected String                    assetRootPath;
    /** This variable indicate if Y axis is UP axis. If not then Z is up. By default set to true. */
    protected boolean                   fixUpAxis                 = true;
    /** Generated textures resolution (PPU - Pixels Per Unit). */
    protected int                       generatedTexturePPU       = 128;
    /**
     * The name of world settings that the importer will use. If not set or specified name does not occur in the file
     * then the first world settings in the file will be used.
     */
    protected String                    usedWorld;
    /**
     * User's default material that is set fo objects that have no material definition in blender. The default value is
     * null. If the value is null the importer will use its own default material (gray color - like in blender).
     */
    protected Material                  defaultMaterial;
    /** Face cull mode. By default it is disabled. */
    protected FaceCullMode              faceCullMode              = FaceCullMode.Back;
    /**
     * Variable describes which layers will be loaded. N-th bit set means N-th layer will be loaded.
     * If set to -1 then the current layer will be loaded.
     */
    protected int                       layersToLoad              = -1;
    /** A variable that toggles the object custom properties loading. */
    protected boolean                   loadObjectProperties      = true;
    /**
     * Maximum texture size. Might be dependant on the graphic card.
     * This value is taken from <b>org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE</b>.
     */
    protected int                       maxTextureSize            = 8192;
    /** Allows to toggle generated textures loading. Disabled by default because it very often takes too much memory and needs to be used wisely. */
    protected boolean                   loadGeneratedTextures;
    /** Tells if the mipmaps will be generated by jme or not. By default generation is dependant on the blender settings. */
    protected MipmapGenerationMethod    mipmapGenerationMethod    = MipmapGenerationMethod.GENERATE_WHEN_NEEDED;
    /**
     * If the sky has only generated textures applied then they will have the following size (both width and height). If 2d textures are used then the generated
     * textures will get their proper size.
     */
    protected int                       skyGeneratedTextureSize   = 1000;
    /** The radius of a shape that will be used while creating the generated texture for the sky. The higher it is the larger part of the texture will be seen. */
    protected float                     skyGeneratedTextureRadius = 1;
    /** The shape against which the generated texture for the sky will be created. */
    protected SkyGeneratedTextureShape  skyGeneratedTextureShape  = SkyGeneratedTextureShape.SPHERE;
    /**
     * This field tells if the importer should optimise the use of textures or not. If set to true, then textures of the same mapping type will be merged together
     * and textures that in the final result will never be visible - will be discarded.
     */
    protected boolean                   optimiseTextures;
    /** A map between node name and its animation names. */
    protected Map<String, List<String>> nodeAnimationMap          = new HashMap<String, List<String>>();
    /** A map between node name and its skeleton animation names. */
    protected Map<String, List<String>> skeletonAnimationMap      = new HashMap<String, List<String>>();

    /**
     * Constructor used by serialization mechanisms.
     */
    public BlenderKey() {
    }

    /**
     * Constructor. Creates a key for the given file name.
     * @param name
     *            the name (path) of a file
     */
    public BlenderKey(String name) {
        super(name);
    }

    /**
     * This method returns frames per second amount. The default value is BlenderKey.DEFAULT_FPS = 25.
     * @return the frames per second amount
     */
    public int getFps() {
        return fps;
    }

    /**
     * This method sets frames per second amount.
     * @param fps
     *            the frames per second amount
     */
    public void setFps(int fps) {
        this.fps = fps;
    }

    /**
     * This method returns the face cull mode.
     * @return the face cull mode
     */
    public FaceCullMode getFaceCullMode() {
        return faceCullMode;
    }

    /**
     * This method sets the face cull mode.
     * @param faceCullMode
     *            the face cull mode
     */
    public void setFaceCullMode(FaceCullMode faceCullMode) {
        this.faceCullMode = faceCullMode;
    }

    /**
     * This method sets layers to be loaded.
     * @param layersToLoad
     *            layers to be loaded
     */
    public void setLayersToLoad(int layersToLoad) {
        this.layersToLoad = layersToLoad;
    }

    /**
     * This method returns layers to be loaded.
     * @return layers to be loaded
     */
    public int getLayersToLoad() {
        return layersToLoad;
    }

    /**
     * This method sets the properies loading policy.
     * By default the value is true.
     * @param loadObjectProperties
     *            true to load properties and false to suspend their loading
     */
    public void setLoadObjectProperties(boolean loadObjectProperties) {
        this.loadObjectProperties = loadObjectProperties;
    }

    /**
     * @return the current properties loading properties
     */
    public boolean isLoadObjectProperties() {
        return loadObjectProperties;
    }

    /**
     * The default value for this parameter is the same as defined by: org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE.
     * If by any means this is too large for user's hardware configuration use the 'setMaxTextureSize' method to change that.
     * @return maximum texture size (width/height)
     */
    public int getMaxTextureSize() {
        return maxTextureSize;
    }

    /**
     * This method sets the maximum texture size.
     * @param maxTextureSize
     *            the maximum texture size
     */
    public void setMaxTextureSize(int maxTextureSize) {
        this.maxTextureSize = maxTextureSize;
    }

    /**
     * This method sets the flag that toggles the generated textures loading.
     * @param loadGeneratedTextures
     *            <b>true</b> if generated textures should be loaded and <b>false</b> otherwise
     */
    public void setLoadGeneratedTextures(boolean loadGeneratedTextures) {
        this.loadGeneratedTextures = loadGeneratedTextures;
    }

    /**
     * @return tells if the generated textures should be loaded (<b>false</b> is the default value)
     */
    public boolean isLoadGeneratedTextures() {
        return loadGeneratedTextures;
    }

    /**
     * This method sets the asset root path.
     * @param assetRootPath
     *            the assets root path
     */
    public void setAssetRootPath(String assetRootPath) {
        this.assetRootPath = assetRootPath;
    }

    /**
     * This method returns the asset root path.
     * @return the asset root path
     */
    public String getAssetRootPath() {
        return assetRootPath;
    }

    /**
     * This method adds features to be loaded.
     * @param featuresToLoad
     *            bitwise flag of FeaturesToLoad interface values
     */
    public void includeInLoading(int featuresToLoad) {
        this.featuresToLoad |= featuresToLoad;
    }

    /**
     * This method removes features from being loaded.
     * @param featuresNotToLoad
     *            bitwise flag of FeaturesToLoad interface values
     */
    public void excludeFromLoading(int featuresNotToLoad) {
        featuresToLoad &= ~featuresNotToLoad;
    }

    public boolean shouldLoad(int featureToLoad) {
        return (featuresToLoad & featureToLoad) != 0;
    }

    /**
     * This method returns bitwise value of FeaturesToLoad interface value. It describes features that will be loaded by
     * the blender file loader.
     * @return features that will be loaded by the blender file loader
     */
    public int getFeaturesToLoad() {
        return featuresToLoad;
    }

    /**
     * This method determines if unlinked assets should be loaded.
     * If not then only objects on selected layers will be loaded and their assets if required.
     * If yes then all assets will be loaded even if they are on inactive layers or are not linked
     * to anything.
     * @return <b>true</b> if unlinked assets should be loaded and <b>false</b> otherwise
     */
    public boolean isLoadUnlinkedAssets() {
        return loadUnlinkedAssets;
    }

    /**
     * This method sets if unlinked assets should be loaded.
     * If not then only objects on selected layers will be loaded and their assets if required.
     * If yes then all assets will be loaded even if they are on inactive layers or are not linked
     * to anything.
     * @param loadUnlinkedAssets
     *            <b>true</b> if unlinked assets should be loaded and <b>false</b> otherwise
     */
    public void setLoadUnlinkedAssets(boolean loadUnlinkedAssets) {
        this.loadUnlinkedAssets = loadUnlinkedAssets;
    }

    /**
     * This method creates an object where loading results will be stores. Only those features will be allowed to store
     * that were specified by features-to-load flag.
     * @return an object to store loading results
     */
    public LoadingResults prepareLoadingResults() {
        return new LoadingResults(featuresToLoad);
    }

    /**
     * This method sets the fix up axis state. If set to true then Y is up axis. Otherwise the up i Z axis. By default Y
     * is up axis.
     * @param fixUpAxis
     *            the up axis state variable
     */
    public void setFixUpAxis(boolean fixUpAxis) {
        this.fixUpAxis = fixUpAxis;
    }

    /**
     * This method returns the fix up axis state. If set to true then Y is up axis. Otherwise the up i Z axis. By
     * default Y is up axis.
     * @return the up axis state variable
     */
    public boolean isFixUpAxis() {
        return fixUpAxis;
    }

    /**
     * This method sets the generated textures resolution.
     * @param generatedTexturePPU
     *            the generated textures resolution
     */
    public void setGeneratedTexturePPU(int generatedTexturePPU) {
        this.generatedTexturePPU = generatedTexturePPU;
    }

    /**
     * @return the generated textures resolution
     */
    public int getGeneratedTexturePPU() {
        return generatedTexturePPU;
    }

    /**
     * @return mipmaps generation method
     */
    public MipmapGenerationMethod getMipmapGenerationMethod() {
        return mipmapGenerationMethod;
    }

    /**
     * @param mipmapGenerationMethod
     *            mipmaps generation method
     */
    public void setMipmapGenerationMethod(MipmapGenerationMethod mipmapGenerationMethod) {
        this.mipmapGenerationMethod = mipmapGenerationMethod;
    }

    /**
     * @return the size of the generated textures for the sky (used if no flat textures are applied)
     */
    public int getSkyGeneratedTextureSize() {
        return skyGeneratedTextureSize;
    }

    /**
     * @param skyGeneratedTextureSize
     *            the size of the generated textures for the sky (used if no flat textures are applied)
     */
    public void setSkyGeneratedTextureSize(int skyGeneratedTextureSize) {
        if (skyGeneratedTextureSize <= 0) {
            throw new IllegalArgumentException("The texture size must be a positive value (the value given as a parameter: " + skyGeneratedTextureSize + ")!");
        }
        this.skyGeneratedTextureSize = skyGeneratedTextureSize;
    }

    /**
     * @return the radius of a shape that will be used while creating the generated texture for the sky, the higher it is the larger part of the texture will be seen
     */
    public float getSkyGeneratedTextureRadius() {
        return skyGeneratedTextureRadius;
    }

    /**
     * @param skyGeneratedTextureRadius
     *            the radius of a shape that will be used while creating the generated texture for the sky, the higher it is the larger part of the texture will be seen
     */
    public void setSkyGeneratedTextureRadius(float skyGeneratedTextureRadius) {
        this.skyGeneratedTextureRadius = skyGeneratedTextureRadius;
    }

    /**
     * @return the shape against which the generated texture for the sky will be created (by default it is a sphere).
     */
    public SkyGeneratedTextureShape getSkyGeneratedTextureShape() {
        return skyGeneratedTextureShape;
    }

    /**
     * @param skyGeneratedTextureShape
     *            the shape against which the generated texture for the sky will be created
     */
    public void setSkyGeneratedTextureShape(SkyGeneratedTextureShape skyGeneratedTextureShape) {
        if (skyGeneratedTextureShape == null) {
            throw new IllegalArgumentException("The sky generated shape type cannot be null!");
        }
        this.skyGeneratedTextureShape = skyGeneratedTextureShape;
    }

    /**
     * If set to true, then textures of the same mapping type will be merged together
     * and textures that in the final result will never be visible - will be discarded.
     * @param optimiseTextures
     *            the variable that tells if the textures should be optimised or not
     */
    public void setOptimiseTextures(boolean optimiseTextures) {
        this.optimiseTextures = optimiseTextures;
    }

    /**
     * @return the variable that tells if the textures should be optimised or not (by default the optimisation is disabled)
     */
    public boolean isOptimiseTextures() {
        return optimiseTextures;
    }

    /**
     * This mehtod sets the name of the WORLD data block taht should be used during file loading. By default the name is
     * not set. If no name is set or the given name does not occur in the file - the first WORLD data block will be used
     * during loading (assumin any exists in the file).
     * @param usedWorld
     *            the name of the WORLD block used during loading
     */
    public void setUsedWorld(String usedWorld) {
        this.usedWorld = usedWorld;
    }

    /**
     * This mehtod returns the name of the WORLD data block taht should be used during file loading.
     * @return the name of the WORLD block used during loading
     */
    public String getUsedWorld() {
        return usedWorld;
    }

    /**
     * This method sets the default material for objects.
     * @param defaultMaterial
     *            the default material
     */
    public void setDefaultMaterial(Material defaultMaterial) {
        this.defaultMaterial = defaultMaterial;
    }

    /**
     * This method returns the default material.
     * @return the default material
     */
    public Material getDefaultMaterial() {
        return defaultMaterial;
    }

    /**
     * Adds spatial animation name for specified node.
     * @param nodeName
     *            the name of the node
     * @param animationName
     *            the spatial animation name
     */
    public void addNodeAnimation(String nodeName, String animationName) {
        List<String> animations = nodeAnimationMap.get(nodeName);
        if (animations == null) {
            animations = new ArrayList<String>();
            nodeAnimationMap.put(nodeName, animations);
        }
        animations.add(animationName);
    }

    /**
     * Returns all spatial animation names for the given node.
     * @param nodeName
     *            the name of the node
     * @return all spatial animations names or null if none are defined
     */
    public List<String> getNodeAnimationNames(String nodeName) {
        return nodeAnimationMap.get(nodeName);
    }

    /**
     * Adds bone animation name for specified node.
     * @param nodeName
     *            the name of the node
     * @param animationName
     *            the bone animation name
     */
    public void addSkeletonAnimation(String nodeName, String animationName) {
        List<String> animations = skeletonAnimationMap.get(nodeName);
        if (animations == null) {
            animations = new ArrayList<String>();
            skeletonAnimationMap.put(nodeName, animations);
        }
        animations.add(animationName);
    }

    /**
     * Returns all bone animation names for the given node.
     * @param nodeName
     *            the name of the node
     * @return all bone animations names or null if none are defined
     */
    public List<String> getSkeletonAnimationNames(String nodeName) {
        return skeletonAnimationMap.get(nodeName);
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule oc = e.getCapsule(this);
        oc.write(fps, "fps", DEFAULT_FPS);
        oc.write(featuresToLoad, "features-to-load", FeaturesToLoad.ALL);
        oc.write(loadUnlinkedAssets, "load-unlinked-assets", false);
        oc.write(assetRootPath, "asset-root-path", null);
        oc.write(fixUpAxis, "fix-up-axis", true);
        oc.write(generatedTexturePPU, "generated-texture-ppu", 128);
        oc.write(usedWorld, "used-world", null);
        oc.write(defaultMaterial, "default-material", null);
        oc.write(faceCullMode, "face-cull-mode", FaceCullMode.Off);
        oc.write(layersToLoad, "layers-to-load", -1);
        oc.write(mipmapGenerationMethod, "mipmap-generation-method", MipmapGenerationMethod.GENERATE_WHEN_NEEDED);
        oc.write(skyGeneratedTextureSize, "sky-generated-texture-size", 1000);
        oc.write(skyGeneratedTextureRadius, "sky-generated-texture-radius", 1f);
        oc.write(skyGeneratedTextureShape, "sky-generated-texture-shape", SkyGeneratedTextureShape.SPHERE);
        oc.write(optimiseTextures, "optimise-textures", false);

        if (nodeAnimationMap == null) {
            oc.write(0, "node-anims-map-size", 0);
        } else {
            oc.write(nodeAnimationMap.size(), "node-anims-map-size", 0);
            int counter = 0;
            for (Entry<String, List<String>> entry : nodeAnimationMap.entrySet()) {
                oc.write(entry.getKey(), "node-anim-" + counter, null);
                oc.write(entry.getValue().toArray(new String[entry.getValue().size()]), "node-anims-" + counter, null);
                ++counter;
            }
        }

        if (skeletonAnimationMap == null) {
            oc.write(0, "skeleton-anims-map-size", 0);
        } else {
            oc.write(skeletonAnimationMap.size(), "skeleton-anims-map-size", 0);
            int counter = 0;
            for (Entry<String, List<String>> entry : skeletonAnimationMap.entrySet()) {
                oc.write(entry.getKey(), "skeleton-anim-" + counter, null);
                oc.write(entry.getValue().toArray(new String[entry.getValue().size()]), "skeleton-anims-" + counter, null);
                ++counter;
            }
        }
    }

    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);
        InputCapsule ic = e.getCapsule(this);
        fps = ic.readInt("fps", DEFAULT_FPS);
        featuresToLoad = ic.readInt("features-to-load", FeaturesToLoad.ALL);
        loadUnlinkedAssets = ic.readBoolean("load-unlinked-assets", false);
        assetRootPath = ic.readString("asset-root-path", null);
        fixUpAxis = ic.readBoolean("fix-up-axis", true);
        generatedTexturePPU = ic.readInt("generated-texture-ppu", 128);
        usedWorld = ic.readString("used-world", null);
        defaultMaterial = (Material) ic.readSavable("default-material", null);
        faceCullMode = ic.readEnum("face-cull-mode", FaceCullMode.class, FaceCullMode.Off);
        layersToLoad = ic.readInt("layers-to=load", -1);
        mipmapGenerationMethod = ic.readEnum("mipmap-generation-method", MipmapGenerationMethod.class, MipmapGenerationMethod.GENERATE_WHEN_NEEDED);
        skyGeneratedTextureSize = ic.readInt("sky-generated-texture-size", 1000);
        skyGeneratedTextureRadius = ic.readFloat("sky-generated-texture-radius", 1f);
        skyGeneratedTextureShape = ic.readEnum("sky-generated-texture-shape", SkyGeneratedTextureShape.class, SkyGeneratedTextureShape.SPHERE);
        optimiseTextures = ic.readBoolean("optimise-textures", false);

        int animsSize = ic.readInt("node-anims-map-size", 0);
        nodeAnimationMap = new HashMap<String, List<String>>(animsSize);
        if (animsSize > 0) {
            for (int i = 0; i < animsSize; ++i) {
                String nodeName = ic.readString("node-anim-" + i, null);
                String[] anims = ic.readStringArray("node-anims-" + i, null);
                nodeAnimationMap.put(nodeName, new ArrayList<String>(Arrays.asList(anims)));// must create ArrayList because 'asList' method returns unmodifiable list
            }
        }

        animsSize = ic.readInt("skeleton-anims-map-size", 0);
        skeletonAnimationMap = new HashMap<String, List<String>>(animsSize);
        if (animsSize > 0) {
            for (int i = 0; i < animsSize; ++i) {
                String nodeName = ic.readString("skeleton-anim-" + i, null);
                String[] anims = ic.readStringArray("skeleton-anims-" + i, null);
                skeletonAnimationMap.put(nodeName, new ArrayList<String>(Arrays.asList(anims)));// must create ArrayList because 'asList' method returns unmodifiable list
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (assetRootPath == null ? 0 : assetRootPath.hashCode());
        result = prime * result + (defaultMaterial == null ? 0 : defaultMaterial.hashCode());
        result = prime * result + (faceCullMode == null ? 0 : faceCullMode.hashCode());
        result = prime * result + featuresToLoad;
        result = prime * result + (fixUpAxis ? 1231 : 1237);
        result = prime * result + fps;
        result = prime * result + generatedTexturePPU;
        result = prime * result + layersToLoad;
        result = prime * result + (loadGeneratedTextures ? 1231 : 1237);
        result = prime * result + (loadObjectProperties ? 1231 : 1237);
        result = prime * result + (loadUnlinkedAssets ? 1231 : 1237);
        result = prime * result + maxTextureSize;
        result = prime * result + (mipmapGenerationMethod == null ? 0 : mipmapGenerationMethod.hashCode());
        result = prime * result + (nodeAnimationMap == null ? 0 : nodeAnimationMap.hashCode());
        result = prime * result + (optimiseTextures ? 1231 : 1237);
        result = prime * result + (skeletonAnimationMap == null ? 0 : skeletonAnimationMap.hashCode());
        result = prime * result + Float.floatToIntBits(skyGeneratedTextureRadius);
        result = prime * result + (skyGeneratedTextureShape == null ? 0 : skyGeneratedTextureShape.hashCode());
        result = prime * result + skyGeneratedTextureSize;
        result = prime * result + (usedWorld == null ? 0 : usedWorld.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlenderKey)) {
            return false;
        }
        BlenderKey other = (BlenderKey) obj;
        if (assetRootPath == null) {
            if (other.assetRootPath != null) {
                return false;
            }
        } else if (!assetRootPath.equals(other.assetRootPath)) {
            return false;
        }
        if (defaultMaterial == null) {
            if (other.defaultMaterial != null) {
                return false;
            }
        } else if (!defaultMaterial.equals(other.defaultMaterial)) {
            return false;
        }
        if (faceCullMode != other.faceCullMode) {
            return false;
        }
        if (featuresToLoad != other.featuresToLoad) {
            return false;
        }
        if (fixUpAxis != other.fixUpAxis) {
            return false;
        }
        if (fps != other.fps) {
            return false;
        }
        if (generatedTexturePPU != other.generatedTexturePPU) {
            return false;
        }
        if (layersToLoad != other.layersToLoad) {
            return false;
        }
        if (loadGeneratedTextures != other.loadGeneratedTextures) {
            return false;
        }
        if (loadObjectProperties != other.loadObjectProperties) {
            return false;
        }
        if (loadUnlinkedAssets != other.loadUnlinkedAssets) {
            return false;
        }
        if (maxTextureSize != other.maxTextureSize) {
            return false;
        }
        if (mipmapGenerationMethod != other.mipmapGenerationMethod) {
            return false;
        }
        if (nodeAnimationMap == null) {
            if (other.nodeAnimationMap != null) {
                return false;
            }
        } else if (!nodeAnimationMap.equals(other.nodeAnimationMap)) {
            return false;
        }
        if (optimiseTextures != other.optimiseTextures) {
            return false;
        }
        if (skeletonAnimationMap == null) {
            if (other.skeletonAnimationMap != null) {
                return false;
            }
        } else if (!skeletonAnimationMap.equals(other.skeletonAnimationMap)) {
            return false;
        }
        if (Float.floatToIntBits(skyGeneratedTextureRadius) != Float.floatToIntBits(other.skyGeneratedTextureRadius)) {
            return false;
        }
        if (skyGeneratedTextureShape != other.skyGeneratedTextureShape) {
            return false;
        }
        if (skyGeneratedTextureSize != other.skyGeneratedTextureSize) {
            return false;
        }
        if (usedWorld == null) {
            if (other.usedWorld != null) {
                return false;
            }
        } else if (!usedWorld.equals(other.usedWorld)) {
            return false;
        }
        return true;
    }

    /**
     * This enum tells the importer if the mipmaps for textures will be generated by jme. <li>NEVER_GENERATE and ALWAYS_GENERATE are quite understandable <li>GENERATE_WHEN_NEEDED is an option that checks if the texture had 'Generate mipmaps' option set in blender, mipmaps are generated only when the option is set
     * @author Marcin Roguski (Kaelthas)
     */
    public static enum MipmapGenerationMethod {
        NEVER_GENERATE, ALWAYS_GENERATE, GENERATE_WHEN_NEEDED;
    }

    /**
     * This interface describes the features of the scene that are to be loaded.
     * @author Marcin Roguski (Kaelthas)
     */
    public static interface FeaturesToLoad {

        int SCENES     = 0x0000FFFF;
        int OBJECTS    = 0x0000000B;
        int ANIMATIONS = 0x00000004;
        int MATERIALS  = 0x00000003;
        int TEXTURES   = 0x00000001;
        int CAMERAS    = 0x00000020;
        int LIGHTS     = 0x00000010;
        int WORLD      = 0x00000040;
        int ALL        = 0xFFFFFFFF;
    }

    /**
     * The shape againts which the sky generated texture will be created.
     * 
     * @author Marcin Roguski (Kaelthas)
     */
    public static enum SkyGeneratedTextureShape {
        CUBE, SPHERE;
    }

    /**
     * This class holds the loading results according to the given loading flag.
     * @author Marcin Roguski (Kaelthas)
     */
    public static class LoadingResults extends Spatial {

        /** Bitwise mask of features that are to be loaded. */
        private final int        featuresToLoad;
        /** The scenes from the file. */
        private List<Node>       scenes;
        /** Objects from all scenes. */
        private List<Node>       objects;
        /** Materials from all objects. */
        private List<Material>   materials;
        /** Textures from all objects. */
        private List<Texture>    textures;
        /** Animations of all objects. */
        private List<Animation>  animations;
        /** All cameras from the file. */
        private List<CameraNode> cameras;
        /** All lights from the file. */
        private List<LightNode>  lights;
        /** Loaded sky. */
        private Spatial          sky;
        /** Scene filters (ie. FOG). */
        private List<Filter>     filters;
        /**
         * The background color of the render loaded from the horizon color of the world. If no world is used than the gray color
         * is set to default (as in blender editor.
         */
        private ColorRGBA        backgroundColor = ColorRGBA.Gray;

        /**
         * Private constructor prevents users to create an instance of this class from outside the
         * @param featuresToLoad
         *            bitwise mask of features that are to be loaded
         * @see FeaturesToLoad FeaturesToLoad
         */
        private LoadingResults(int featuresToLoad) {
            this.featuresToLoad = featuresToLoad;
            if ((featuresToLoad & FeaturesToLoad.SCENES) != 0) {
                scenes = new ArrayList<Node>();
            }
            if ((featuresToLoad & FeaturesToLoad.OBJECTS) != 0) {
                objects = new ArrayList<Node>();
                if ((featuresToLoad & FeaturesToLoad.MATERIALS) != 0) {
                    materials = new ArrayList<Material>();
                    if ((featuresToLoad & FeaturesToLoad.TEXTURES) != 0) {
                        textures = new ArrayList<Texture>();
                    }
                }
                if ((featuresToLoad & FeaturesToLoad.ANIMATIONS) != 0) {
                    animations = new ArrayList<Animation>();
                }
            }
            if ((featuresToLoad & FeaturesToLoad.CAMERAS) != 0) {
                cameras = new ArrayList<CameraNode>();
            }
            if ((featuresToLoad & FeaturesToLoad.LIGHTS) != 0) {
                lights = new ArrayList<LightNode>();
            }
        }

        /**
         * This method returns a bitwise flag describing what features of the blend file will be included in the result.
         * @return bitwise mask of features that are to be loaded
         * @see FeaturesToLoad FeaturesToLoad
         */
        public int getLoadedFeatures() {
            return featuresToLoad;
        }

        /**
         * This method adds a scene to the result set.
         * @param scene
         *            scene to be added to the result set
         */
        public void addScene(Node scene) {
            if (scenes != null) {
                scenes.add(scene);
            }
        }

        /**
         * This method adds an object to the result set.
         * @param object
         *            object to be added to the result set
         */
        public void addObject(Node object) {
            if (objects != null) {
                objects.add(object);
            }
        }

        /**
         * This method adds a material to the result set.
         * @param material
         *            material to be added to the result set
         */
        public void addMaterial(Material material) {
            if (materials != null) {
                materials.add(material);
            }
        }

        /**
         * This method adds a texture to the result set.
         * @param texture
         *            texture to be added to the result set
         */
        public void addTexture(Texture texture) {
            if (textures != null) {
                textures.add(texture);
            }
        }

        /**
         * This method adds a camera to the result set.
         * @param camera
         *            camera to be added to the result set
         */
        public void addCamera(CameraNode camera) {
            if (cameras != null) {
                cameras.add(camera);
            }
        }

        /**
         * This method adds a light to the result set.
         * @param light
         *            light to be added to the result set
         */
        public void addLight(LightNode light) {
            if (lights != null) {
                lights.add(light);
            }
        }

        /**
         * This method sets the sky of the scene. Only one sky can be set.
         * @param sky
         *            the sky to be set
         */
        public void setSky(Spatial sky) {
            this.sky = sky;
        }

        /**
         * This method adds a scene filter. Filters are used to load FOG or other
         * scene effects that blender can define.
         * @param filter
         *            the filter to be added
         */
        public void addFilter(Filter filter) {
            if (filter != null) {
                if (filters == null) {
                    filters = new ArrayList<Filter>(5);
                }
                filters.add(filter);
            }
        }

        /**
         * @param backgroundColor
         *            the background color
         */
        public void setBackgroundColor(ColorRGBA backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        /**
         * @return all loaded scenes
         */
        public List<Node> getScenes() {
            return scenes;
        }

        /**
         * @return all loaded objects
         */
        public List<Node> getObjects() {
            return objects;
        }

        /**
         * @return all loaded materials
         */
        public List<Material> getMaterials() {
            return materials;
        }

        /**
         * @return all loaded textures
         */
        public List<Texture> getTextures() {
            return textures;
        }

        /**
         * @return all loaded animations
         */
        public List<Animation> getAnimations() {
            return animations;
        }

        /**
         * @return all loaded cameras
         */
        public List<CameraNode> getCameras() {
            return cameras;
        }

        /**
         * @return all loaded lights
         */
        public List<LightNode> getLights() {
            return lights;
        }

        /**
         * @return the scene's sky
         */
        public Spatial getSky() {
            return sky;
        }

        /**
         * @return scene filters
         */
        public List<Filter> getFilters() {
            return filters;
        }

        /**
         * @return the background color
         */
        public ColorRGBA getBackgroundColor() {
            return backgroundColor;
        }

        @Override
        public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
            return 0;
        }

        @Override
        public void updateModelBound() {
        }

        @Override
        public void setModelBound(BoundingVolume modelBound) {
        }

        @Override
        public int getVertexCount() {
            return 0;
        }

        @Override
        public int getTriangleCount() {
            return 0;
        }

        @Override
        public Spatial deepClone() {
            return null;
        }

        @Override
        public void depthFirstTraversal(SceneGraphVisitor visitor) {
        }

        @Override
        protected void breadthFirstTraversal(SceneGraphVisitor visitor, Queue<Spatial> queue) {
        }
    }
}
