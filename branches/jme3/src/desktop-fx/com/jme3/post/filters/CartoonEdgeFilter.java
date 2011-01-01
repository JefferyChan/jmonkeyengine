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

package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.post.Filter.Pass;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Image.Format;

/**
 * Applies a cartoon-style edge detection filter to all objects in the scene.
 *
 * @author Kirill Vainer
 */
public class CartoonEdgeFilter extends Filter {

    private Pass normalPass;
    private Material normalMaterial;

    public CartoonEdgeFilter() {
        super("CartoonEdgeFilter");
    }

    @Override
    public boolean isRequiresDepthTexture() {
        return true;
    }

    @Override
    public void preRender(RenderManager renderManager, ViewPort viewPort) {
        Renderer r = renderManager.getRenderer();
        r.setFrameBuffer(normalPass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);
        renderManager.setForcedMaterial(normalMaterial);
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedMaterial(null);
        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());
    }

    @Override
    public Material getMaterial() {
        material.setTexture("m_NormalsTexture", normalPass.getRenderedTexture());
        return material;
    }

    @Override
    public void initFilter(AssetManager manager,RenderManager renderManager,ViewPort vp) {
        normalPass = new Pass();
        normalPass.init(vp.getCamera().getWidth(), vp.getCamera().getHeight(), Format.RGB8, Format.Depth);
        material = new Material(manager, "Common/MatDefs/Post/CartoonEdge.j3md");
        normalMaterial = new Material(manager, "Common/MatDefs/SSAO/normal.j3md");
    }

}
