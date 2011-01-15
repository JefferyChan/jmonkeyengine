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
package com.jme3.cinematic;

import com.jme3.cinematic.events.MotionTrack;
import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Motion path is used to create a path between way points.
 * @author Nehon
 */
public class MotionPath implements Savable {

    private List<Vector3f> wayPoints = new ArrayList<Vector3f>();
    private Node debugNode;
    private AssetManager assetManager;
    private List<MotionPathListener> listeners;
    private List<Float> segmentsLength;
    private float totalLength;
    private List<Vector3f> CRcontrolPoints;
    private float curveTension = 0.5f;
    private boolean cycle = false;
    private float eps = 0.0001f;

    public enum PathInterpolation {

        /**
         * Compute a linear path between the waypoints
         */
        Linear,
        /**
         * Compute a Catmull-Rom spline path between the waypoints
         * see http://www.mvps.org/directx/articles/catmull/
         */
        CatmullRom
    }
    private PathInterpolation pathInterpolation = PathInterpolation.CatmullRom;

    /**
     * Create a motion Path
     */
    public MotionPath() {
    }

    /**
     * interpolate the path giving the tpf and the motionControl
     * @param tpf
     * @param control
     * @return
     */
    public Vector3f interpolatePath(float tpf, MotionTrack control) {
        Vector3f temp = null;
        float val;
        switch (pathInterpolation) {
            case CatmullRom:

                val = tpf * (totalLength / control.getDuration());
                control.setCurrentValue(control.getCurrentValue() + eps);
                temp = FastMath.interpolateCatmullRom(control.getCurrentValue(), curveTension, CRcontrolPoints.get(control.getCurrentWayPoint()), CRcontrolPoints.get(control.getCurrentWayPoint() + 1), CRcontrolPoints.get(control.getCurrentWayPoint() + 2), CRcontrolPoints.get(control.getCurrentWayPoint() + 3));
                float dist = temp.subtract(control.getSpatial().getLocalTranslation()).length();

                while (dist < val) {
                    control.setCurrentValue(control.getCurrentValue() + eps);
                    temp = FastMath.interpolateCatmullRom(control.getCurrentValue(), curveTension, CRcontrolPoints.get(control.getCurrentWayPoint()), CRcontrolPoints.get(control.getCurrentWayPoint() + 1), CRcontrolPoints.get(control.getCurrentWayPoint() + 2), CRcontrolPoints.get(control.getCurrentWayPoint() + 3));
                    dist = temp.subtract(control.getSpatial().getLocalTranslation()).length();
                }
                if (control.needsDirection()) {
                    control.setDirection(temp.subtract(control.getSpatial().getLocalTranslation()).normalizeLocal());
                }
                break;
            case Linear:
                val = control.getDuration() * segmentsLength.get(control.getCurrentWayPoint()) / totalLength;
                control.setCurrentValue(Math.min(control.getCurrentValue() + tpf / val, 1.0f));
                temp = FastMath.interpolateLinear(control.getCurrentValue(), wayPoints.get(control.getCurrentWayPoint()), wayPoints.get(control.getCurrentWayPoint() + 1));
                if (control.needsDirection()) {
                    control.setDirection(wayPoints.get(control.getCurrentWayPoint() + 1).subtract(wayPoints.get(control.getCurrentWayPoint())).normalizeLocal());
                }
                break;
            default:
                break;
        }
        return temp;
    }

    private void attachDebugNode(Node root) {
        if (debugNode == null) {
            debugNode = new Node();
            Material m = assetManager.loadMaterial("Common/Materials/RedColor.j3m");
            for (Iterator<Vector3f> it = wayPoints.iterator(); it.hasNext();) {
                Vector3f cp = it.next();
                Geometry geo = new Geometry("box", new Box(cp, 0.3f, 0.3f, 0.3f));
                geo.setMaterial(m);
                debugNode.attachChild(geo);

            }
            switch (pathInterpolation) {
                case CatmullRom:
                    debugNode.attachChild(CreateCatmullRomPath());
                    break;
                case Linear:
                    debugNode.attachChild(CreateLinearPath());
                    break;
                default:
                    debugNode.attachChild(CreateLinearPath());
                    break;
            }

            root.attachChild(debugNode);
        }
    }

    private Geometry CreateLinearPath() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("Color", ColorRGBA.Blue);

        float[] array = new float[wayPoints.size() * 3];
        short[] indices = new short[(wayPoints.size() - 1) * 2];
        int i = 0;
        int cpt = 0;
        int k = 0;
        int j = 0;
        for (Iterator<Vector3f> it = wayPoints.iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            array[i] = vector3f.x;
            i++;
            array[i] = vector3f.y;
            i++;
            array[i] = vector3f.z;
            i++;
            if (it.hasNext()) {
                k = j;
                indices[cpt] = (short) k;
                cpt++;
                k++;
                indices[cpt] = (short) k;
                cpt++;
                j++;
            }
        }

        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);
        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, array);
        lineMesh.setBuffer(VertexBuffer.Type.Index, (wayPoints.size() - 1) * 2, indices);
        lineMesh.updateBound();
        lineMesh.updateCounts();

        Geometry lineGeometry = new Geometry("line", lineMesh);
        lineGeometry.setMaterial(mat);
        return lineGeometry;
    }

    private Geometry CreateCatmullRomPath() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        int nbSubSegments = 10;

        float[] array = new float[(((wayPoints.size() - 1) * nbSubSegments) + 1) * 3];
        short[] indices = new short[((wayPoints.size() - 1) * nbSubSegments) * 2];
        int i = 0;
        int cptCP = 0;
        for (Iterator<Vector3f> it = wayPoints.iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            array[i] = vector3f.x;
            i++;
            array[i] = vector3f.y;
            i++;
            array[i] = vector3f.z;
            i++;
            if (it.hasNext()) {
                for (int j = 1; j < nbSubSegments; j++) {
                    Vector3f temp = FastMath.interpolateCatmullRom((float) j / nbSubSegments, curveTension, CRcontrolPoints.get(cptCP),
                            CRcontrolPoints.get(cptCP + 1), CRcontrolPoints.get(cptCP + 2), CRcontrolPoints.get(cptCP + 3));
                    array[i] = temp.x;
                    i++;
                    array[i] = temp.y;
                    i++;
                    array[i] = temp.z;
                    i++;
                }
            }
            cptCP++;
        }

        i = 0;
        int k = 0;
        for (int j = 0; j < ((wayPoints.size() - 1) * nbSubSegments); j++) {
            k = j;
            indices[i] = (short) k;
            i++;
            k++;
            indices[i] = (short) k;
            i++;
        }



        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);
        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, array);
        lineMesh.setBuffer(VertexBuffer.Type.Index, ((wayPoints.size() - 1) * nbSubSegments) * 2, indices);
        lineMesh.updateBound();
        lineMesh.updateCounts();

        Geometry lineGeometry = new Geometry("line", lineMesh);
        lineGeometry.setMaterial(mat);
        return lineGeometry;
    }

    private void initCatmullRomWayPoints(List<Vector3f> list) {
        if (CRcontrolPoints == null) {
            CRcontrolPoints = new ArrayList<Vector3f>();
        } else {
            CRcontrolPoints.clear();
        }
        int nb = list.size() - 1;

        if (cycle) {
            CRcontrolPoints.add(list.get(list.size() - 2));
        } else {
            CRcontrolPoints.add(list.get(0).subtract(list.get(1).subtract(list.get(0))));
        }

        for (Iterator<Vector3f> it = list.iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            CRcontrolPoints.add(vector3f);
        }
        if (cycle) {
            CRcontrolPoints.add(list.get(1));
        } else {
            CRcontrolPoints.add(list.get(nb).add(list.get(nb).subtract(list.get(nb - 1))));
        }

    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.writeSavableArrayList((ArrayList) wayPoints, "wayPoints", null);
        oc.write(pathInterpolation, "pathInterpolation", PathInterpolation.CatmullRom);
        float list[] = new float[segmentsLength.size()];
        for (int i = 0; i < segmentsLength.size(); i++) {
            list[i] = segmentsLength.get(i);
        }
        oc.write(list, "segmentsLength", null);

        oc.write(totalLength, "totalLength", 0);
        oc.writeSavableArrayList((ArrayList) CRcontrolPoints, "CRControlPoints", null);
        oc.write(curveTension, "curveTension", 0.5f);
        oc.write(cycle, "cycle", false);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);

        wayPoints = (ArrayList<Vector3f>) in.readSavableArrayList("wayPoints", null);
        float list[] = in.readFloatArray("segmentsLength", null);
        if (list != null) {
            segmentsLength = new ArrayList<Float>();
            for (int i = 0; i < list.length; i++) {
                segmentsLength.add(new Float(list[i]));
            }
        }
        pathInterpolation = in.readEnum("pathInterpolation", PathInterpolation.class, PathInterpolation.CatmullRom);
        totalLength = in.readFloat("totalLength", 0);
        CRcontrolPoints = (ArrayList<Vector3f>) in.readSavableArrayList("CRControlPoints", null);
        curveTension = in.readFloat("curveTension", 0.5f);
        cycle = in.readBoolean("cycle", false);
    }

    /**
     * Addsa waypoint to the path
     * @param wayPoint a position in world space
     */
    public void addWayPoint(Vector3f wayPoint) {
        if (wayPoints.size() > 2 && this.cycle) {
            wayPoints.remove(wayPoints.size() - 1);
        }
        wayPoints.add(wayPoint);
        if (wayPoints.size() >= 2 && this.cycle) {
            wayPoints.add(wayPoints.get(0));
        }
        if (wayPoints.size() > 1) {
            computeTotalLentgh();
        }
    }

    private void computeTotalLentgh() {
        totalLength = 0;
        float l = 0;
        if (segmentsLength == null) {
            segmentsLength = new ArrayList<Float>();
        } else {
            segmentsLength.clear();
        }
        if (pathInterpolation == PathInterpolation.Linear) {
            if (wayPoints.size() > 1) {
                for (int i = 0; i < wayPoints.size() - 1; i++) {
                    l = wayPoints.get(i + 1).subtract(wayPoints.get(i)).length();
                    segmentsLength.add(l);
                    totalLength += l;
                }
            }
        } else {
            initCatmullRomWayPoints(wayPoints);
            computeCatmulLength();
        }
    }

    private void computeCatmulLength() {
        float l = 0;
        if (wayPoints.size() > 1) {
            for (int i = 0; i < wayPoints.size() - 1; i++) {
                l = getCatmullRomP1toP2Length(CRcontrolPoints.get(i),
                        CRcontrolPoints.get(i + 1), CRcontrolPoints.get(i + 2), CRcontrolPoints.get(i + 3), 0, 1);
                segmentsLength.add(l);
                totalLength += l;
            }
        }
    }

    /**
     * retruns the length of the path in world units
     * @return the length
     */
    public float getLength() {
        return totalLength;
    }
    //Compute lenght of p1 to p2 arc segment
    //TODO extract to FastMath class

    private float getCatmullRomP1toP2Length(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float startRange, float endRange) {

        float epsilon = 0.001f;
        float middleValue = (startRange + endRange) * 0.5f;
        Vector3f start = p1;
        if (startRange != 0) {
            start = FastMath.interpolateCatmullRom(startRange, curveTension, p0, p1, p2, p3);
        }
        Vector3f end = p2;
        if (endRange != 1) {
            end = FastMath.interpolateCatmullRom(endRange, curveTension, p0, p1, p2, p3);
        }
        Vector3f middle = FastMath.interpolateCatmullRom(middleValue, curveTension, p0, p1, p2, p3);
        float l = end.subtract(start).length();
        float l1 = middle.subtract(start).length();
        float l2 = end.subtract(middle).length();
        float len = l1 + l2;
        if (l + epsilon < len) {
            l1 = getCatmullRomP1toP2Length(p0, p1, p2, p3, startRange, middleValue);
            l2 = getCatmullRomP1toP2Length(p0, p1, p2, p3, middleValue, endRange);
        }
        l = l1 + l2;
        return l;
    }

    /**
     * returns the waypoint at the given index
     * @param i the index
     * @return returns the waypoint position
     */
    public Vector3f getWayPoint(int i) {
        return wayPoints.get(i);
    }

    /**
     * remove the waypoint from the path
     * @param wayPoint the waypoint to remove
     */
    public void removeWayPoint(Vector3f wayPoint) {
        wayPoints.remove(wayPoint);
        if (wayPoints.size() > 1) {
            computeTotalLentgh();
        }
    }

    /**
     * remove the waypoint at the given index from the path
     * @param i the index of the waypoint to remove
     */
    public void removeWayPoint(int i) {
        removeWayPoint(wayPoints.get(i));
    }

    /**
     * returns an iterator on the waypoints collection
     * @return
     */
    public Iterator<Vector3f> iterator() {
        return wayPoints.iterator();
    }

    /**
     * return the type of path interpolation for this path
     * @return the path interpolation
     */
    public PathInterpolation getPathInterpolation() {
        return pathInterpolation;
    }

    /**
     * sets the path interpolation for this path
     * @param pathInterpolation
     */
    public void setPathInterpolation(PathInterpolation pathInterpolation) {
        this.pathInterpolation = pathInterpolation;
        computeTotalLentgh();
        if (debugNode != null) {
            Node parent = debugNode.getParent();
            debugNode.removeFromParent();
            debugNode.detachAllChildren();
            debugNode = null;
            attachDebugNode(parent);
        }
    }

    /**
     * disable the display of the path and the waypoints
     */
    public void disableDebugShape() {

        debugNode.detachAllChildren();
        debugNode = null;
        assetManager = null;
    }

    /**
     * enable the display of the path and the waypoints
     * @param manager the assetManager
     * @param rootNode the node where the debug shapes must be attached
     */
    public void enableDebugShape(AssetManager manager, Node rootNode) {
        assetManager = manager;
        computeTotalLentgh();
        attachDebugNode(rootNode);
    }

    /**
     * Adds a motion pathListener to the path
     * @param listener the MotionPathListener to attach
     */
    public void addListener(MotionPathListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<MotionPathListener>();
        }
        listeners.add(listener);
    }

    /**
     * remove the given listener
     * @param listener the listener to remove
     */
    public void removeListener(MotionPathListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * return the number of waypoints of this path
     * @return
     */
    public int getNbWayPoints() {
        return wayPoints.size();
    }

    public void triggerWayPointReach(int wayPointIndex, MotionTrack control) {
        if(listeners!=null){
            for (Iterator<MotionPathListener> it = listeners.iterator(); it.hasNext();) {
                MotionPathListener listener = it.next();
                listener.onWayPointReach(control, wayPointIndex);
            }
        }
    }

    /**
     * Returns the curve tension
     * @return
     */
    public float getCurveTension() {
        return curveTension;
    }

    /**
     * sets the tension of the curve (only for catmull rom) 0.0 will give a linear curve, 1.0 a round curve
     * @param curveTension
     */
    public void setCurveTension(float curveTension) {
        this.curveTension = curveTension;
        computeTotalLentgh();
        if (debugNode != null) {
            Node parent = debugNode.getParent();
            debugNode.removeFromParent();
            debugNode.detachAllChildren();
            debugNode = null;
            attachDebugNode(parent);
        }
    }

    /**
     * Sets the path to be a cycle
     * @param cycle
     */
    public void setCycle(boolean cycle) {

        if (wayPoints.size() >= 2) {
            if (this.cycle && !cycle) {
                wayPoints.remove(wayPoints.size() - 1);
            }
            if (!this.cycle && cycle) {
                wayPoints.add(wayPoints.get(0));
                System.out.println("adding first wp");
            }
            this.cycle = cycle;
            computeTotalLentgh();
            if (debugNode != null) {
                Node parent = debugNode.getParent();
                debugNode.removeFromParent();
                debugNode.detachAllChildren();
                debugNode = null;
                attachDebugNode(parent);
            }
        } else {
            this.cycle = cycle;
        }
    }

    /**
     * returns true if the path is a cycle
     * @return
     */
    public boolean isCycle() {
        return cycle;
    }
}
