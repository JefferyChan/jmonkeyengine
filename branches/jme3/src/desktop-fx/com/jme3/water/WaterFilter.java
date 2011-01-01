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
package com.jme3.water;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.Filter.Pass;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import java.io.IOException;

/**
 *
 * @author nehon
 */
public class WaterFilter extends Filter {

    private Pass positionPass;
    private Pass reflectionPass;
    protected Spatial reflectionScene;
    protected ViewPort reflectionView;
    private Material positionMaterial;
    private Texture2D normalTexture;
    private Texture2D foamTexture;
    private Texture2D heightTexture;
    private Plane plane;    
    private Camera reflectionCam;
    private float speed = 1;
    protected Ray ray = new Ray();
    private Vector3f targetLocation = new Vector3f();
    private Vector3f lightDirection;
    private ReflectionProcessor reflectionProcessor;
    private Matrix4f biasMatrix = new Matrix4f(0.5f, 0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.0f, 0.5f,
            0.0f, 0.0f, 0.0f, 0.5f,
            0.0f, 0.0f, 0.0f, 1.0f);
    private Matrix4f textureProjMatrix;
    private float waterHeight = 0.0f;
    private float waterTransparency = 0.1f;
    private float normalScale = 1.0f;
    private float refractionConstant = 0.5f;
    private float maxAmplitude = 1.0f;
    private ColorRGBA lightColor = ColorRGBA.White;
    private float shoreHardness = 0.1f;
    private float foamHardness = 1.0f;
    private float refractionStrength = 0.0f;
    private float waveScale = 0.005f;
    private Vector3f foamExistence = new Vector3f(0.45f, 4.35f, 1.0f);
    private Vector3f colorExtinction = new Vector3f(5.0f, 20.0f, 30.0f);
    private float sunScale = 3.0f;
    private float shininess = 0.7f;
    private ColorRGBA waterColor = new ColorRGBA(0.0078f, 0.5176f, 0.5f, 1.0f);
    private ColorRGBA deepWaterColor = new ColorRGBA(0.0039f, 0.00196f, 0.145f, 1.0f);
    private Vector2f windDirection = new Vector2f(0.0f, -1.0f);
    private int reflectionMapSize = 512;

    /**
     * Create a Screen Space Ambiant Occlusion Filter
     */
    public WaterFilter() {
        super("WaterFilter");
    }

    public WaterFilter(Node reflectionScene, Vector3f lightDirection) {
        super("WaterFilter");
        this.reflectionScene = reflectionScene;
        this.lightDirection = lightDirection;
    }

    @Override
    public boolean isRequiresDepthTexture() {
        return true;
    }

    @Override
    protected Format getDefaultPassDepthFormat() {
        return Format.Depth;
    }
    float time = 0;

    @Override
    public void preFrame(float tpf) {
        time = time + (tpf * speed);       
        material.setFloat("m_Time", time);
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
        Camera sceneCam = viewPort.getCamera();
        textureProjMatrix = biasMatrix.mult(sceneCam.getViewProjectionMatrix());
        material.setMatrix4("m_TextureProjMatrix", textureProjMatrix);
        material.setVector3("m_CameraPosition", sceneCam.getLocation());
        material.setMatrix4("m_ViewProjectionMatrixInverse", sceneCam.getViewProjectionMatrix().invert());

        Renderer r = renderManager.getRenderer();
        r.setFrameBuffer(positionPass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);
        renderManager.setForcedMaterial(positionMaterial);
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedMaterial(null);


        material.setTexture("m_PositionBuffer", positionPass.getRenderedTexture());
        material.setFloat("m_WaterHeight", waterHeight);

        //update reflection cam
        ray.setOrigin(sceneCam.getLocation());
        ray.setDirection(sceneCam.getDirection());
        plane = new Plane(Vector3f.UNIT_Y, new Vector3f(0, waterHeight, 0).dot(Vector3f.UNIT_Y));


        boolean inv = false;
        if (!ray.intersectsWherePlane(plane, targetLocation)) {
            ray.setDirection(ray.getDirection().negateLocal());
            ray.intersectsWherePlane(plane, targetLocation);
            inv = true;
        }
        Vector3f loc = plane.reflect(sceneCam.getLocation(), new Vector3f());
        reflectionCam.setLocation(loc);
        reflectionCam.setFrustum(sceneCam.getFrustumNear(),
                sceneCam.getFrustumFar(),
                sceneCam.getFrustumLeft(),
                sceneCam.getFrustumRight(),
                sceneCam.getFrustumTop(),
                sceneCam.getFrustumBottom());
        reflectionCam.lookAt(targetLocation, Vector3f.UNIT_Y);
        if (inv) {
            reflectionCam.setAxes(reflectionCam.getLeft().negateLocal(), reflectionCam.getUp(), reflectionCam.getDirection().negateLocal());
        }
        reflectionProcessor.setReflectionClipPlane(plane);

        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());

    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public void initFilter(AssetManager manager, RenderManager renderManager, ViewPort vp) {

        positionPass = new Pass();
        positionPass.init(vp.getCamera().getWidth(), vp.getCamera().getHeight(), Format.RGB32F, Format.Depth);
        positionMaterial = new Material(manager, "Common/MatDefs/Misc/ShowPos.j3md");
        reflectionPass = new Pass();
        reflectionPass.init(reflectionMapSize, reflectionMapSize, Format.RGBA8, Format.Depth);
        reflectionCam = new Camera(reflectionMapSize, reflectionMapSize);


        // create a pre-view. a view that is rendered before the main view
        reflectionView = renderManager.createPreView("Reflection View", reflectionCam);
        reflectionView.setClearEnabled(true);
        reflectionView.setBackgroundColor(ColorRGBA.Black);



        //set viewport to render to offscreen framebuffer
        plane = new Plane(Vector3f.UNIT_Y, new Vector3f(0, waterHeight, 0).dot(Vector3f.UNIT_Y));
        reflectionProcessor = new ReflectionProcessor(reflectionCam, reflectionPass.getRenderFrameBuffer(), plane);
        reflectionView.addProcessor(reflectionProcessor);
        // attach the scene to the viewport to be rendered
        reflectionView.attachScene(reflectionScene);


        normalTexture = (Texture2D) manager.loadTexture("Common/MatDefs/Water/Textures/gradient_map.jpg");
        foamTexture = (Texture2D) manager.loadTexture("Common/MatDefs/Water/Textures/foam.png");
        heightTexture = (Texture2D) manager.loadTexture("Common/MatDefs/Water/Textures/heightmap.png");
        normalTexture.setWrap(WrapMode.Repeat);
        foamTexture.setWrap(WrapMode.Repeat);
        heightTexture.setWrap(WrapMode.Repeat);
        material = new Material(manager, "Common/MatDefs/Water/Water.j3md");
        material.setTexture("m_HeightMap", heightTexture);
        material.setTexture("m_FoamMap", foamTexture);
        material.setTexture("m_NormalMap", normalTexture);
        material.setTexture("m_ReflectionMap", reflectionPass.getRenderedTexture());

        material.setFloat("m_WaterTransparency", waterTransparency);
        material.setFloat("m_NormalScale", normalScale);
        material.setFloat("m_R0", refractionConstant);
        material.setFloat("m_MaxAmplitude", maxAmplitude);
        material.setVector3("m_LightDir", lightDirection);
        material.setColor("m_LightColor", lightColor);
        material.setFloat("m_ShoreHardness", shoreHardness);
        material.setFloat("m_RefractionStrength", refractionStrength);
        material.setFloat("m_WaveScale", waveScale);
        material.setVector3("m_FoamExistence", foamExistence);
        material.setFloat("m_SunScale", sunScale);
        material.setVector3("m_ColorExtinction", colorExtinction);
        material.setFloat("m_Shininess", shininess);
        material.setColor("m_WaterColor", waterColor);
        material.setColor("m_DeepWaterColor", deepWaterColor);
        material.setVector2("m_WindDirection", windDirection);
        material.setFloat("m_FoamHardness", foamHardness);

    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
//        OutputCapsule oc = ex.getCapsule(this);
//        oc.write(sampleRadius, "sampleRadius", 5.1f);
//        oc.write(intensity, "intensity", 1.5f);
//        oc.write(scale, "scale", 0.2f);
//        oc.write(bias, "bias", 0.1f);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
//        sampleRadius = ic.readFloat("sampleRadius", 5.1f);
//        intensity = ic.readFloat("intensity", 1.5f);
//        scale = ic.readFloat("scale", 0.2f);
//        bias = ic.readFloat("bias", 0.1f);
    }

    /**
     * gets the height of the water plane
     * @return
     */
    public float getWaterHeight() {
        return waterHeight;
    }

    /**
     * Sets the height of the water plane
     * default is 0.0
     * @param waterHeight
     */
    public void setWaterHeight(float waterHeight) {
        this.waterHeight = waterHeight;
    }

    public void setReflectionScene(Spatial reflectionScene) {
        this.reflectionScene = reflectionScene;
    }

    /**
     * returns the waterTransparency value
     * @return
     */
    public float getWaterTransparency() {
        return waterTransparency;
    }

    /**
     * Sets how fast will colours fade out. You can also think about this
     * values as how clear water is. Therefore use smaller values (eg. 0.05)
     * to have crystal clear water and bigger to achieve "muddy" water.
     * default is 0.1f
     * @param waterTransparency
     */
    public void setWaterTransparency(float waterTransparency) {
        this.waterTransparency = waterTransparency;
        material.setFloat("m_WaterTransparency", waterTransparency);
    }

    /**
     * Returns the normal scales applied to the normal map
     * @return
     */
    public float getNormalScale() {
        return normalScale;
    }

    /**
     * Sets the normal scaling factors to apply to the normal map.
     * the higher the value the more small ripples will be visible on the waves.
     * default is 1.0
     * @param normalScale
     */
    public void setNormalScale(float normalScale) {
        this.normalScale = normalScale;
        material.setFloat("m_NormalScale", normalScale);
    }

    public float getRefractionConstant() {
        return refractionConstant;
    }

    /**
     * This is a constant related to the index of refraction (IOR) used to compute the fresnel term.
     * F = R0 + (1-R0)( 1 - N.V)^5
     * where F is the fresnel term, R0 the constant, N the normal vector and V tne view vector.
     * It usually depend on the material you are lookinh through (here water).
     * Default value is 0.3f
     * In practice, the lowest the value and the less the reflection can be seen on water
     * @param refractionConstant
     */
    public void setRefractionConstant(float refractionConstant) {
        this.refractionConstant = refractionConstant;
        material.setFloat("m_R0", refractionConstant);
    }

    public float getMaxAmplitude() {
        return maxAmplitude;
    }

    /**
     * Sets the maximum waves amplitude
     * default is 1.0
     * @param maxAmplitude
     */
    public void setMaxAmplitude(float maxAmplitude) {
        this.maxAmplitude = maxAmplitude;
        material.setFloat("m_MaxAmplitude", maxAmplitude);
    }

    /**
     * gets the light direction
     * @return
     */
    public Vector3f getLightDirection() {
        return lightDirection;
    }

    /**
     * Sets the light direction
     * @param lightDirection
     */
    public void setLightDirection(Vector3f lightDirection) {
        this.lightDirection = lightDirection;
        material.setVector3("m_LightDir", lightDirection);
    }

    /**
     * returns the light color
     * @return
     */
    public ColorRGBA getLightColor() {
        return lightColor;
    }

    /**
     * Sets the light color to use
     * default is white
     * @param lightColor
     */
    public void setLightColor(ColorRGBA lightColor) {
        this.lightColor = lightColor;
        material.setColor("m_LightColor", lightColor);
    }

    /**
     * Return the shoreHardeness
     * @return
     */
    public float getShoreHardness() {
        return shoreHardness;
    }

    /**
     * The smaller this value is, the softer the transition between
     * shore and water. If you want hard edges use very big value.
     * Default is 0.1f.
     * @param shoreHardness
     */
    public void setShoreHardness(float shoreHardness) {
        this.shoreHardness = shoreHardness;
        material.setFloat("m_ShoreHardness", shoreHardness);
    }

    /**
     * retunrs the foam hardness
     * @return
     */
    public float getFoamHardness() {
        return foamHardness;
    }

    /**
     * Sets the foam hardness : How much the foam will blend with the shore to avoid hard edged water plane.
     * Default is 1.0
     * @param foamHardness
     */
    public void setFoamHardness(float foamHardness) {
        this.foamHardness = foamHardness;
        material.setFloat("m_FoamHardness", foamHardness);
    }

    /**
     * returns the refractionStrenght
     * @return
     */
    public float getRefractionStrength() {
        return refractionStrength;
    }

    /**
     * This value modifies current fresnel term. If you want to weaken
     * reflections use bigger value. If you want to empasize them use
     * value smaller then 0. Default is 0.0f.
     * @param refractionStrength
     */
    public void setRefractionStrength(float refractionStrength) {
        this.refractionStrength = refractionStrength;
        material.setFloat("m_RefractionStrength", refractionStrength);
    }

    /**
     * returns the scale factor of the waves height map
     * @return
     */
    public float getWaveScale() {
        return waveScale;
    }

    /**
     * Sets the scale factor of the waves height map
     * the smaller the value the bigger the waves
     * default is 0.005f
     * @param waveScale
     */
    public void setWaveScale(float waveScale) {
        this.waveScale = waveScale;
        material.setFloat("m_WaveScale", waveScale);
    }

    /**
     * returns the foam existance vector
     * @return
     */
    public Vector3f getFoamExistence() {
        return foamExistence;
    }

    /**
     * Describes at what depth foam starts to fade out and
     * at what it is completely invisible. The third value is at
     * what height foam for waves appear (+ waterHeight).
     * default is (0.45, 4.35, 1.0);
     * @param foamExistence
     */
    public void setFoamExistence(Vector3f foamExistence) {
        this.foamExistence = foamExistence;
        material.setVector3("m_FoamExistence", foamExistence);
    }

    /**
     * gets the scale of the sun
     * @return
     */
    public float getSunScale() {
        return sunScale;
    }

    /**
     * Sets the scale of the sun for specular effect
     * @param sunScale
     */
    public void setSunScale(float sunScale) {
        this.sunScale = sunScale;
        material.setFloat("m_SunScale", sunScale);
    }

    /**
     * Returns the color exctinction vector of the water
     * @return
     */
    public Vector3f getColorExtinction() {
        return colorExtinction;
    }

    /**
     * Return at what depth the refraction color extinct
     * the first value is for red
     * the second is for green
     * the third is for blue
     * Play with thos parameters to "trouble" the water
     * default is (5.0, 20.0, 30.0f);
     * @param colorExtinction
     */
    public void setColorExtinction(Vector3f colorExtinction) {
        this.colorExtinction = colorExtinction;
        material.setVector3("m_ColorExtinction", colorExtinction);
    }

    /**
     * Sets the foam texture
     * @param foamTexture
     */
    public void setFoamTexture(Texture2D foamTexture) {
        this.foamTexture = foamTexture;
        foamTexture.setWrap(WrapMode.Repeat);
    }

    /**
     * Sets the height texture
     * @param heightTexture
     */
    public void setHeightTexture(Texture2D heightTexture) {
        this.heightTexture = heightTexture;
        heightTexture.setWrap(WrapMode.Repeat);
    }

    /**
     * Sets the normal Texture
     * @param normalTexture
     */
    public void setNormalTexture(Texture2D normalTexture) {
        this.normalTexture = normalTexture;
        normalTexture.setWrap(WrapMode.Repeat);
    }

    /**
     * return the shininess factor of the water
     * @return
     */
    public float getShininess() {
        return shininess;
    }

    /**
     * Sets the shinines factor of the water
     * default is 0.7f
     * @param shininess
     */
    public void setShininess(float shininess) {
        this.shininess = shininess;
        material.setFloat("m_Shininess", shininess);
    }

    /**
     * retruns the speed of the waves
     * @return
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the speed of the waves (0.0 is still) default is 1.0
     * @param speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * returns the color of the water
     *
     * @return
     */
    public ColorRGBA getWaterColor() {
        return waterColor;
    }

    /**
     * Sets the color of the water
     * see setDeepWaterColor for deep water color
     * default is (0.0078f, 0.5176f, 0.5f,1.0f) (greenish blue)
     * @param waterColour
     */
    public void setWaterColor(ColorRGBA waterColor) {
        this.waterColor = waterColor;
        material.setColor("m_WaterColor", waterColor);
    }

    /**
     * returns the deep water color
     * @return
     */
    public ColorRGBA getDeepWaterColor() {
        return deepWaterColor;
    }

    /**
     * sets the deep water color
     * see setWaterColor for general color
     * default is (0.0039f, 0.00196f, 0.145f,1.0f) (very dark blue)
     * @param deepWaterColor
     */
    public void setDeepWaterColor(ColorRGBA deepWaterColor) {
        this.deepWaterColor = deepWaterColor;
        material.setColor("m_DeepWaterColor", deepWaterColor);
    }

    /**
     * returns the wind direction
     * @return
     */
    public Vector2f getWindDirection() {
        return windDirection;
    }

    /**
     * sets the wind direction
     * the direction where the waves move
     * default is (0.0f, -1.0f)
     * @param windDirection
     */
    public void setWindDirection(Vector2f windDirection) {
        this.windDirection = windDirection;
        material.setVector2("m_WindDirection", windDirection);
    }

    /**
     * returns the size of the reflection map
     * @return
     */
    public int getReflectionMapSize() {
        return reflectionMapSize;
    }

    /**
     * Sets the size of the reflection map
     * default is 512, the higher, the better quality, but the slower the effect.
     * @param reflectionMapSize
     */
    public void setReflectionMapSize(int reflectionMapSize) {
        this.reflectionMapSize = reflectionMapSize;
    }
}
