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

package com.jme3.terrain.geomipmap;

import com.jme3.math.Vector3f;
import java.util.List;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.terrain.Terrain;
import java.util.ArrayList;

/**
 * Tells the terrain to update its Level of Detail.
 * It needs the cameras to do this, and there could possibly
 * be several cameras in the scene, so it accepts a list
 * of cameras.
 * NOTE: right now it just uses the first camera passed in,
 * in the future it will use all of them to determine what
 * LOD to set.
 * 
 * @author Brent Owens
 */
public class TerrainLodControl extends AbstractControl {
	
	private Terrain terrain;
	private List<Camera> cameras;


    public TerrainLodControl() {

    }
    
	/**
	 * Only uses the first camera right now.
	 * @param terrain to act upon (must me a Spatial)
	 * @param cameras one or more cameras to reference for LOD calc
	 */
	public TerrainLodControl(Terrain terrain, List<Camera> cameras) {
		super((Spatial)terrain);
		this.terrain = terrain;
		this.cameras = cameras;
	}
	
	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		
	}

	@Override
	protected void controlUpdate(float tpf) {
		//list of cameras for when terrain supports multiple cameras (ie split screen)
        List<Vector3f> cameraLocations = new ArrayList<Vector3f>();
        if (cameras != null) {
            for (Camera c : cameras)
                cameraLocations.add(c.getLocation());
            terrain.update(cameraLocations);
        }
	}

	public Control cloneForSpatial(Spatial spatial) {
		if (spatial instanceof Terrain) {
            List<Camera> cameraClone = new ArrayList<Camera>();
            if (cameras != null) {
            for (Camera c : cameras)
                cameraClone.add(c);
            }
			return new TerrainLodControl((Terrain)spatial, cameraClone);
        }
		return null;
	}


    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

}
