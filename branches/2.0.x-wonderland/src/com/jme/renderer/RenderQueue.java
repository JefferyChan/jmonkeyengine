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

package com.jme.renderer;

import java.util.Arrays;
import java.util.Comparator;

import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.scene.state.ColorMaskState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.StencilState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.FragmentProgramState;
import com.jme.scene.state.VertexProgramState;
import com.jme.bounding.BoundingVolume;
import com.jme.system.DisplaySystem;
import com.jme.scene.shape.Quad;
import java.nio.FloatBuffer;
import com.jme.system.JmeException;
import com.jme.util.SortUtil;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * This optional class supports queueing of rendering states that are drawn when
 * displayBackBuffer is called on the renderer. All spatials in the opaque
 * bucket are rendered first in order closest to farthest. Then all spatials in
 * the opaque bucket are rendered in order farthest to closest. Finally all
 * spatials in the ortho bucket are rendered in ortho mode from highest to
 * lowest Z order. As a user, you shouldn't need to use this class directly. All
 * you'll need to do is call Spatial.setRenderQueueMode .
 *
 * @author Joshua Slack
 * @author Jack Lindamood (javadoc + SpatialList only)
 * @see com.jme.scene.Spatial#setRenderQueueMode(int)
 *
 */
public class RenderQueue {

    /** List of all transparent object to render. */
    private SpatialList transparentBucket;
    private SpatialList transparentBackBucket;

    /** List of all opaque object to render. */
    private SpatialList opaqueBucket;
    private SpatialList opaqueBackBucket;

    /** List of all ortho object to render. */
    private SpatialList orthoBucket;
    private SpatialList orthoBackBucket;

    /** The renderer. */
    private Renderer renderer;

    /** CullState for two pass transparency rendering. */
    private CullState tranCull;

    /** ZBufferState for two pass transparency rendering. */
    private ZBufferState tranZBuff;

    /** boolean for enabling / disabling two pass transparency rendering. */
    private boolean twoPassTransparent = true;

    private Vector3f tempVector = new Vector3f();

    private StencilState glowStencilState = null;
    private BlendState glowBlendState = null;
    private ZBufferState glowZBufferState = null;
    private CullState glowCullState = null;
    private TextureState glowTextureState = null;
    private LightState glowLightState = null;
    private GLSLShaderObjectsState glowGLSLShaderState = null;
    private FragmentProgramState glowFragShaderState = null;
    private VertexProgramState glowVertShaderState = null;
    private ColorRGBA oDefaultColor = new ColorRGBA();

    private int currentStencilValue = 2;

    /**
     * Geometry to be used for portal rendering.  Assume portal
     * technique if non-null.
     */
    private Geometry portalGeometry = null;
    private Vector3f lastLoc = null;
    private Vector3f lastDir = null;
    private Vector3f lastUp = null;
    private Vector3f lastLeft = null;
    private Vector3f loc = new Vector3f();
    private Vector3f dir = new Vector3f();
    private Vector3f up = new Vector3f();
    private Vector3f left = new Vector3f();

    /**
     * State for portal rendering
     */
    private StencilState portalStencilState = null;
    private StencilState savedStencilState = null;
    private ZBufferState portalZBufferState = null;
    private ZBufferState savedZBufferState = null;
    private BlendState portalBlendState = null;
    private CullState portalCullState = null;
    private TextureState portalTextureState = null;
    private LightState portalLightState = null;
    private GLSLShaderObjectsState portalGLSLShaderState = null;
    private FragmentProgramState portalFragShaderState = null;
    private VertexProgramState portalVertShaderState = null;
    private MaterialState portalMaterialState = null;
    private ColorMaskState portalColorMaskState = null;
    private Quad portalQuad = null;

    /**
     * Creates a new render queue that will work with the given renderer.
     *
     * @param r
     */
    public RenderQueue(Renderer r) {
        this.renderer = r;
        tranCull = r.createCullState();
        tranZBuff = r.createZBufferState();
        tranZBuff.setWritable(false);
        tranZBuff.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        setupBuckets();

        glowStencilState = r.createStencilState();
        glowBlendState = r.createBlendState();
        glowBlendState.setEnabled(true);
        glowBlendState.setBlendEnabled(true);
        glowBlendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        glowBlendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        glowZBufferState = r.createZBufferState();
        glowZBufferState.setEnabled(true);
        glowZBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        glowCullState = r.createCullState();
        glowCullState.setEnabled(true);
        glowCullState.setCullFace(CullState.Face.Back);
        glowTextureState = r.createTextureState();
        glowTextureState.setEnabled(false);
        glowLightState = r.createLightState();
        glowLightState.setEnabled(false);

        glowGLSLShaderState = r.createGLSLShaderObjectsState();
        glowGLSLShaderState.setEnabled(false);
        glowFragShaderState = r.createFragmentProgramState();
        glowFragShaderState.setEnabled(false);
        glowVertShaderState = r.createVertexProgramState();
        glowVertShaderState.setEnabled(false);

        portalStencilState = r.createStencilState();
        portalZBufferState = r.createZBufferState();
        portalColorMaskState = r.createColorMaskState();

        portalBlendState = r.createBlendState();
        portalBlendState.setEnabled(false);
        portalCullState = r.createCullState();
        portalCullState.setEnabled(false);
        portalTextureState = r.createTextureState();
        portalTextureState.setEnabled(false);
        portalLightState = r.createLightState();
        portalLightState.setEnabled(false);
        portalGLSLShaderState = r.createGLSLShaderObjectsState();
        portalGLSLShaderState.setEnabled(false);
        portalFragShaderState = r.createFragmentProgramState();
        portalFragShaderState.setEnabled(false);
        portalVertShaderState = r.createVertexProgramState();
        portalVertShaderState.setEnabled(false);
        portalMaterialState = r.createMaterialState();
        portalMaterialState.setEnabled(false);

        portalQuad = new Quad("", r.width, r.height);
        FloatBuffer vbuf = portalQuad.getVertexBuffer();
        vbuf.clear();
        vbuf.put(0).put(0).put(-1);
        vbuf.put(r.width).put(0).put(-1);
        vbuf.put(r.width).put(r.height).put(-1);
        vbuf.put(0).put(r.height).put(-1);
    }

    /**
     * Enables/Disables two pass transparency rendering. If enabled, objects in
     * the TRANSPARENT queue will be rendered in two passes. On the first pass,
     * objects are rendered with front faces culled. On the second pass, objects
     * are rendered with back faces culled.
     *
     * This allows complex transparent objects to be rendered whole without
     * concern as to the order of the faces drawn.
     *
     * @param enabled
     *            set true to turn on two pass transparency rendering
     */
    public void setTwoPassTransparency(boolean enabled) {
        twoPassTransparent = enabled;
    }

    /**
     * @return true if two pass transparency rendering is enabled.
     */
    public boolean isTwoPassTransparency() {
        return twoPassTransparent;
    }

    /**
     * Set the geometry for portal rendering.
     */
    public void setPortalGeometry(Geometry g, Vector3f lLoc, Vector3f lDir, Vector3f lUp, Vector3f lLeft) {
        portalGeometry = g;
        lastLoc = lLoc;
        lastDir = lDir;
        lastUp = lUp;
        lastLeft = lLeft;
    }

    /**
     * Get the geometry for portal rendering.
     */
    public Geometry getPortalGeometry() {
        return (portalGeometry);
    }

    /**
     * Creates the buckets needed.
     */
    private void setupBuckets() {
        opaqueBucket = new SpatialList(new OpaqueComp());
        opaqueBackBucket = new SpatialList(new OpaqueComp());
        transparentBucket = new SpatialList(new TransparentComp());
        transparentBackBucket = new SpatialList(new TransparentComp());
        orthoBucket = new SpatialList(new OrthoComp());
        orthoBackBucket = new SpatialList(new OrthoComp());
    }

    /**
     * Add a given Spatial to the RenderQueue. This is how jME adds data tothe
     * render queue. As a user, in 99% of casees you'll want to use the function
     * Spatail.setRenderQueueMode and let jME add the item to the queue itself.
     *
     * @param s
     *            Spatial to add.
     * @param bucket
     *            A bucket type to add to.
     * @see com.jme.scene.Spatial#setRenderQueueMode(int)
     * @see com.jme.renderer.Renderer#QUEUE_OPAQUE
     * @see com.jme.renderer.Renderer#QUEUE_ORTHO
     * @see com.jme.renderer.Renderer#QUEUE_TRANSPARENT
     */
    public void addToQueue(Spatial s, int bucket) {
        switch (bucket) {
        case Renderer.QUEUE_OPAQUE:
            opaqueBucket.add(s);
            break;
        case Renderer.QUEUE_TRANSPARENT:
            transparentBucket.add(s);
            break;
        case Renderer.QUEUE_ORTHO:
            orthoBucket.add(s);
            break;
        default:
            throw new JmeException("Illegal Render queue order of " + bucket);
        }
    }

    /**
     * Calculates the distance from a spatial to the camera. Distance is a
     * squared distance.
     *
     * @param spat
     *            Spatial to distancize.
     * @return Distance from Spatial to camera.
     */
    private float distanceToCam(Spatial spat) {
        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;
        Camera cam = renderer.getCamera();
        spat.queueDistance = 0;

        Vector3f camPosition = cam.getLocation();
        Vector3f spatPosition = null;
        Vector3f viewVector = cam.getDirection();

        if (Vector3f.isValidVector(cam.getLocation())) {
            if (spat.getWorldBound() != null && Vector3f.isValidVector(spat.getWorldBound().getCenter()))
                spatPosition = spat.getWorldBound().getCenter();
            else if (spat instanceof Spatial && Vector3f.isValidVector(((Spatial)spat).getWorldTranslation()))
                spatPosition = ((Spatial) spat).getWorldTranslation();
        }

        if (spatPosition != null) {
            spatPosition.subtract(camPosition, tempVector);

            float retval = Math.abs(tempVector.dot(viewVector)
                    / viewVector.dot(viewVector));
            tempVector = viewVector.mult(retval, tempVector);

            spat.queueDistance = tempVector.length();
        }

        return spat.queueDistance;
    }
    
    /**
     * Calculates the distance from the closest edge of a spatial's bounds to 
     * the camera. 
     * @param spat
     *            Spatial to distancize.
     * @return Distance from the closest edge of the spatial's world bounds 
     * to camera.
     */
    private float edgeDistanceToCam(Spatial spat) {
        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;
        Camera cam = renderer.getCamera();
        spat.queueDistance = 0;

        Vector3f camPosition = cam.getLocation();
        BoundingVolume bounds = spat.getWorldBound();
        if (Vector3f.isValidVector(camPosition) && bounds != null) {
            spat.queueDistance = bounds.distanceToEdge(camPosition);
        }
            
        return spat.queueDistance;
    }

    /**
     * clears all of the buckets.
     */
    public void clearBuckets() {
        transparentBucket.clear();
        opaqueBucket.clear();
        orthoBucket.clear();
    }

    /**
     * swaps all of the buckets with the back buckets.
     */
    public void swapBuckets() {
        SpatialList swap = transparentBucket;
        transparentBucket = transparentBackBucket;
        transparentBackBucket = swap;

        swap = orthoBucket;
        orthoBucket = orthoBackBucket;
        orthoBackBucket = swap;

        swap = opaqueBucket;
        opaqueBucket = opaqueBackBucket;
        opaqueBackBucket = swap;
    }

    /**
     * Renders the opaque, clone, transparent, and ortho buckets in that order.
     */
    public void renderBuckets() {
        currentStencilValue = 2;
        if (portalGeometry != null) {
            prepareForPortalRendering();
        }
        renderOpaqueBucket();
        renderTransparentBucket();
        renderOrthoBucket();
        if (portalGeometry != null) {
            clearPortalRendering();
        }
    }

    /**
     * Render the portal geometry, and set up state for the rest of the
     * render.
     */
    void prepareForPortalRendering() {
        RenderContext context = DisplaySystem.getDisplaySystem().getCurrentContext();

        savedStencilState = (StencilState)context.enforcedStateList[RenderState.StateType.Stencil.ordinal()];
        context.enforcedStateList[RenderState.StateType.Stencil.ordinal()] = portalStencilState;
        savedZBufferState = (ZBufferState)context.enforcedStateList[RenderState.StateType.ZBuffer.ordinal()];
        context.enforcedStateList[RenderState.StateType.ZBuffer.ordinal()] = portalZBufferState;
        ColorMaskState savedColorMaskState = (ColorMaskState)context.enforcedStateList[RenderState.StateType.ColorMask.ordinal()];
        context.enforcedStateList[RenderState.StateType.ColorMask.ordinal()] = portalColorMaskState;

        BlendState savedBlendState = (BlendState)context.enforcedStateList[RenderState.StateType.Blend.ordinal()];
        context.enforcedStateList[RenderState.StateType.Blend.ordinal()] = portalBlendState;
        CullState savedCullState = (CullState)context.enforcedStateList[RenderState.StateType.Cull.ordinal()];
        context.enforcedStateList[RenderState.StateType.Cull.ordinal()] = portalCullState;
        TextureState savedTextureState = (TextureState)context.enforcedStateList[RenderState.StateType.Texture.ordinal()];
        context.enforcedStateList[RenderState.StateType.Texture.ordinal()] = portalTextureState;
        LightState savedLightState = (LightState)context.enforcedStateList[RenderState.StateType.Light.ordinal()];
        context.enforcedStateList[RenderState.StateType.Light.ordinal()] = portalLightState;
        GLSLShaderObjectsState savedGLSLShaderState = (GLSLShaderObjectsState)context.enforcedStateList[RenderState.StateType.GLSLShaderObjects.ordinal()];
        context.enforcedStateList[RenderState.StateType.GLSLShaderObjects.ordinal()] = portalGLSLShaderState;
        FragmentProgramState savedFragmentProgramState = (FragmentProgramState)context.enforcedStateList[RenderState.StateType.FragmentProgram.ordinal()];
        context.enforcedStateList[RenderState.StateType.FragmentProgram.ordinal()] = portalFragShaderState;
        VertexProgramState savedVertexProgramState = (VertexProgramState)context.enforcedStateList[RenderState.StateType.VertexProgram.ordinal()];
        context.enforcedStateList[RenderState.StateType.VertexProgram.ordinal()] = portalVertShaderState;
        MaterialState savedMaterialState = (MaterialState)context.enforcedStateList[RenderState.StateType.Material.ordinal()];
        context.enforcedStateList[RenderState.StateType.Material.ordinal()] = portalMaterialState;

        portalStencilState.setStencilFunction(StencilState.StencilFunction.Always);
        portalStencilState.setStencilReference(1);
        portalStencilState.setStencilOpFail(StencilState.StencilOperation.Keep);
        portalStencilState.setStencilOpZPass(StencilState.StencilOperation.Replace);
        portalStencilState.setStencilOpZFail(StencilState.StencilOperation.Keep);
        portalStencilState.setEnabled(true);

        portalZBufferState.setWritable(false);
        portalZBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        portalZBufferState.setEnabled(true);
        portalZBufferState.apply();

        portalColorMaskState.setAll(false);
        portalColorMaskState.apply();

        Camera camera = renderer.getCamera();
        loc.set(camera.getLocation());
        dir.set(camera.getDirection());
        up.set(camera.getUp());
        left.set(camera.getLeft());
        camera.setLocation(lastLoc);
        camera.setDirection(lastDir);
        camera.setUp(lastUp);
        camera.setLeft(lastLeft);
        camera.update();
        camera.apply();

        portalGeometry.setLastFrustumIntersection(Camera.FrustumIntersect.Inside);
        portalGeometry.draw(renderer);

        renderer.setOrtho();
        portalZBufferState.setWritable(true);
        portalZBufferState.setFunction(ZBufferState.TestFunction.Always);
        portalZBufferState.apply();

        portalStencilState.setStencilFunction(StencilState.StencilFunction.EqualTo);
        portalStencilState.setStencilReference(1);
        portalStencilState.setStencilOpFail(StencilState.StencilOperation.Keep);
        portalStencilState.setStencilOpZPass(StencilState.StencilOperation.Keep);
        portalStencilState.setStencilOpZFail(StencilState.StencilOperation.Keep);
        portalStencilState.apply();

        portalQuad.draw(renderer);

        portalZBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        portalZBufferState.apply();
        renderer.unsetOrtho();

        context.enforcedStateList[RenderState.StateType.ZBuffer.ordinal()] = savedZBufferState;
        context.enforcedStateList[RenderState.StateType.ColorMask.ordinal()] = savedColorMaskState;
        context.enforcedStateList[RenderState.StateType.Blend.ordinal()] = savedBlendState;
        context.enforcedStateList[RenderState.StateType.Cull.ordinal()] = savedCullState;
        context.enforcedStateList[RenderState.StateType.Texture.ordinal()] = savedTextureState;
        context.enforcedStateList[RenderState.StateType.Light.ordinal()] = savedLightState;
        context.enforcedStateList[RenderState.StateType.GLSLShaderObjects.ordinal()] = savedGLSLShaderState;
        context.enforcedStateList[RenderState.StateType.FragmentProgram.ordinal()] = savedFragmentProgramState;
        context.enforcedStateList[RenderState.StateType.VertexProgram.ordinal()] = savedVertexProgramState;
        context.enforcedStateList[RenderState.StateType.Material.ordinal()] = savedMaterialState;

        portalStencilState.setStencilFunction(StencilState.StencilFunction.EqualTo);
        portalStencilState.setStencilReference(1);
        portalStencilState.setStencilOpFail(StencilState.StencilOperation.Keep);
        portalStencilState.setStencilOpZPass(StencilState.StencilOperation.Keep);
        portalStencilState.setStencilOpZFail(StencilState.StencilOperation.Keep);
        portalStencilState.apply();

        camera.setLocation(loc);
        camera.setDirection(dir);
        camera.setUp(up);
        camera.setLeft(left);
        camera.update();
        camera.apply();
    }

    /**
     * Return states to normal
     */
    void clearPortalRendering() {
        RenderContext context = DisplaySystem.getDisplaySystem().getCurrentContext();
        context.enforcedStateList[RenderState.StateType.Stencil.ordinal()] = savedStencilState;
    }

    /**
     * Renders the opaque buckets. Those closest to the camera are rendered
     * first.
     */
    private void renderOpaqueBucket() {
        opaqueBucket.sort();
        for (int i = 0; i < opaqueBucket.listSize; i++) {
            if (opaqueBucket.list[i] instanceof Geometry &&
                opaqueBucket.list[i].isGlowEnabled()) {
                drawWithGlow((Geometry)opaqueBucket.list[i]);
            } else {
                opaqueBucket.list[i].draw(renderer);
            }
        }
        opaqueBucket.clear();
    }

    private void drawWithGlow(Geometry s) {
        final GL2 gl = GLU.getCurrentGL().getGL2();

        StencilState oss = (StencilState) s.states[RenderState.StateType.Stencil.ordinal()];
        BlendState obs = (BlendState) s.states[RenderState.StateType.Blend.ordinal()];
        ZBufferState ozs = (ZBufferState) s.states[RenderState.StateType.ZBuffer.ordinal()];
        CullState ocs = (CullState) s.states[RenderState.StateType.Cull.ordinal()];
        TextureState ots = (TextureState) s.states[RenderState.StateType.Texture.ordinal()];
        LightState ols = (LightState)s.states[RenderState.StateType.Light.ordinal()];
        GLSLShaderObjectsState osos = (GLSLShaderObjectsState)s.states[RenderState.StateType.GLSLShaderObjects.ordinal()];
        FragmentProgramState ofs = (FragmentProgramState)s.states[RenderState.StateType.FragmentProgram.ordinal()];
        VertexProgramState ovs = (VertexProgramState)s.states[RenderState.StateType.VertexProgram.ordinal()];

        // Write the stencil value out
        glowStencilState.setStencilFunction(StencilState.StencilFunction.Always);
        glowStencilState.setStencilReference(currentStencilValue);
        glowStencilState.setStencilOpFail(StencilState.StencilOperation.Keep);
        glowStencilState.setStencilOpZPass(StencilState.StencilOperation.Replace);
        glowStencilState.setStencilOpZFail(StencilState.StencilOperation.Keep);
        s.states[RenderState.StateType.Stencil.ordinal()] = glowStencilState;
        s.draw(renderer);

        // Set up glow states
        glowStencilState.setStencilFunction(StencilState.StencilFunction.NotEqualTo);
        glowStencilState.setStencilReference(currentStencilValue);
        glowStencilState.setStencilOpFail(StencilState.StencilOperation.Keep);
        glowStencilState.setStencilOpZPass(StencilState.StencilOperation.Keep);
        glowStencilState.setStencilOpZFail(StencilState.StencilOperation.Keep);

        oDefaultColor = s.getDefaultColor();
        s.setDefaultColor(s.getGlowColor());

        s.states[RenderState.StateType.Texture.ordinal()] = glowTextureState;
        s.states[RenderState.StateType.Blend.ordinal()] = glowBlendState;
        s.states[RenderState.StateType.ZBuffer.ordinal()] = glowZBufferState;
        s.states[RenderState.StateType.Light.ordinal()] = glowLightState;
        s.states[RenderState.StateType.GLSLShaderObjects.ordinal()] = glowGLSLShaderState;
        s.states[RenderState.StateType.FragmentProgram.ordinal()] = glowFragShaderState;
        s.states[RenderState.StateType.VertexProgram.ordinal()] = glowVertShaderState;
        s.states[RenderState.StateType.Cull.ordinal()] = glowCullState;

        gl.glPushMatrix();
        BoundingVolume bv = s.getWorldBound();
        float dx = -(bv.getCenter().x);
        float dy = -(bv.getCenter().y);
        float dz = -(bv.getCenter().z);

        gl.glTranslatef(-dx, -dy, -dz);
        gl.glScalef(s.getGlowScale().x, s.getGlowScale().y, s.getGlowScale().z);
        gl.glTranslatef(dx, dy, dz);

        // Draw again
        s.draw(renderer);

        gl.glPopMatrix();

        // Restore
        s.states[RenderState.StateType.Stencil.ordinal()] = oss;
        s.states[RenderState.StateType.Blend.ordinal()] = obs;
        s.states[RenderState.StateType.ZBuffer.ordinal()] = ozs;
        s.states[RenderState.StateType.Cull.ordinal()] = ocs;
        s.states[RenderState.StateType.Texture.ordinal()] = ots;
        s.states[RenderState.StateType.Light.ordinal()] = ols;
        s.states[RenderState.StateType.GLSLShaderObjects.ordinal()] = osos;
        s.states[RenderState.StateType.FragmentProgram.ordinal()] = ofs;
        s.states[RenderState.StateType.VertexProgram.ordinal()] = ovs;
        s.setDefaultColor(oDefaultColor);
        currentStencilValue++;
    }

    /**
     * Renders the transparent buckets. Those farthest from the camera are
     * rendered first. Note that any items in the transparent bucket will
     * have their cullstate values overridden. Therefore, any settings assigned
     * to the cullstate of the rendered object will not be used.
     */
    private void renderTransparentBucket() {
        transparentBucket.sort();
            for (int i = 0; i < transparentBucket.listSize; i++) {
                Spatial obj = transparentBucket.list[i];

                if (twoPassTransparent && obj instanceof Geometry) {
                    /**
                     * Wishtree Technologies
                     * Glow effect added to transparent objects
                     */
                    if(obj.isGlowEnabled()) {
                        drawWithGlow((Geometry)obj);
                    } else {
                        Geometry geom = (Geometry)obj;
                        RenderState oldCullState = geom.states[RenderState.StateType.Cull.ordinal()];
                        geom.states[RenderState.StateType.Cull.ordinal()] = tranCull;
                        ZBufferState oldZState = (ZBufferState)geom.states[RenderState.StateType.ZBuffer.ordinal()];
                        geom.states[RenderState.StateType.ZBuffer.ordinal()] = tranZBuff;

                        // first render back-facing tris only
                        tranCull.setCullFace(CullState.Face.Front);
                        obj.draw(renderer);

                        // then render front-facing tris only
                        geom.states[RenderState.StateType.ZBuffer.ordinal()] = oldZState;
                        tranCull.setCullFace(CullState.Face.Back);
                        obj.draw(renderer);
                        geom.states[RenderState.StateType.Cull.ordinal()] = oldCullState;
                    }
                } else {
                    // draw as usual
                    if (obj instanceof Geometry &&
                        obj.isGlowEnabled()) {
                        drawWithGlow((Geometry) obj);
                    } else {
                        obj.draw(renderer);
                    }
                }
                obj.queueDistance = Float.NEGATIVE_INFINITY;
            }
        transparentBucket.clear();
    }

    /**
     * Renders the ortho buckets. Those will the highest ZOrder are rendered
     * first.
     */
    private void renderOrthoBucket() {
        orthoBucket.sort();
        if (orthoBucket.listSize > 0) {
            renderer.setOrtho();
            for (int i = 0; i < orthoBucket.listSize; i++) {
                if (orthoBucket.list[i] instanceof Geometry &&
                    orthoBucket.list[i].isGlowEnabled()) {
                    drawWithGlow((Geometry) orthoBucket.list[i]);
                } else {
                    orthoBucket.list[i].draw(renderer);
                }
            }
            renderer.unsetOrtho();
        }
        orthoBucket.clear();
    }

    /**
     * This class is a special function list of Spatial objects for render
     * queueing.
     *
     * @author Jack Lindamood
     * @author Three Rings - better sorting alg.
     */
    private class SpatialList {

        Spatial[] list, tlist;

        int listSize;

        private static final int DEFAULT_SIZE = 32;

        private Comparator<Spatial> c;

        SpatialList(Comparator<Spatial> c) {
            listSize = 0;
            list = new Spatial[DEFAULT_SIZE];
            this.c = c;
        }

        /**
         * Adds a spatial to the list. List size is doubled if there is no room.
         *
         * @param s
         *            The spatial to add.
         */
        void add(Spatial s) {
            if (listSize == list.length) {
                Spatial[] temp = new Spatial[listSize * 2];
                System.arraycopy(list, 0, temp, 0, listSize);
                list = temp;
            }
            list[listSize++] = s;
        }

        /**
         * Resets list size to 0.
         */
        void clear() {
            for (int i = 0; i < listSize; i++)
                list[i] = null;
            if (tlist != null)
                Arrays.fill(tlist, null);
            listSize = 0;
        }

        /**
         * Sorts the elements in the list acording to their Comparator.
         */
        void sort() {
            if (listSize > 1) {
                // resize or populate our temporary array as necessary
                if (tlist == null || tlist.length != list.length) {
                    tlist = list.clone();
                } else {
                    System.arraycopy(list, 0, tlist, 0, list.length);
                }
                // now merge sort tlist into list
                SortUtil.msort(tlist, list, 0, listSize, c);
            }
        }
    }

    private class OpaqueComp implements Comparator<Spatial> {

        public int compare(Spatial o1, Spatial o2) {
            if (o1 instanceof Geometry && o2 instanceof Geometry) {
                return compareByStates((Geometry) o1, (Geometry) o2);
            }

            float d1 = distanceToCam(o1);
            float d2 = distanceToCam(o2);
            if (d1 == d2)
                return 0;
            else if (d1 < d2)
                return -1;
            else
                return 1;
        }

        /**
         * Compare opaque items by their texture states - generally the most
         * expensive switch. Later this might expand to comparisons by other
         * states as well, such as lighting or material.
         */
        private int compareByStates(Geometry g1, Geometry g2) {
            TextureState ts1 = (TextureState)g1.states[RenderState.StateType.Texture.ordinal()];
            TextureState ts2 = (TextureState)g2.states[RenderState.StateType.Texture.ordinal()];
            if (ts1 == ts2) return 0;
            else if (ts1 == null && ts2 != null) return -1;
            else if (ts2 == null && ts1 != null) return  1;

            for (int x = 0, nots = Math.min(ts1.getNumberOfSetTextures(), ts2.getNumberOfSetTextures()); x < nots; x++) {

                int tid1 = ts1.getTextureID(x);
                int tid2 = ts2.getTextureID(x);
                if (tid1 == tid2)
                    continue;
                else if (tid1 < tid2)
                    return -1;
                else
                    return 1;
            }

            if (ts1.getNumberOfSetTextures() != ts2.getNumberOfSetTextures()) {
                return ts2.getNumberOfSetTextures() - ts1.getNumberOfSetTextures();
            }

            return 0;
        }
    }

    private class TransparentComp implements Comparator<Spatial> {

        public int compare(Spatial o1, Spatial o2) {
            // sort by distance to the edge of the object's bounds
            float d1 = distanceToCam(o1);
            float d2 = distanceToCam(o2);
            if (d1 == d2)
                return 0;
            else if (d1 < d2)
                return 1;
            else
                return -1;
        }
    }

    private class OrthoComp implements Comparator<Spatial> {
        public int compare(Spatial o1, Spatial o2) {
            if (o2.getZOrder() == o1.getZOrder()) {
                return 0;
            } else if (o2.getZOrder() < o1.getZOrder()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}

