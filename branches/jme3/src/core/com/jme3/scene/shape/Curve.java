/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.scene.shape;

import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.Iterator;

/**
 *
 * @author Nehon
 */
public class Curve extends Mesh {

    private Spline spline;
    private Vector3f temp = new Vector3f();

    /**
     * Create a curve mesh
     * Use a CatmullRom spline model that does not cycle.
     * @param controlPoints the control points to use to create this curve
     * @param nbSubSegments the number of subsegments between the control points
     */
    public Curve(Vector3f[] controlPoints, int nbSubSegments) {
        this(new Spline(Spline.SplineType.CatmullRom, controlPoints, 10, false), nbSubSegments);
    }

    /**
     * Create a curve mesh from a Spline
     * @param spline the spline to use
     * @param nbSubSegments the number of subsegments between the control points
     */
    public Curve(Spline spline, int nbSubSegments) {
        super();
        this.spline = spline;
        switch (spline.getType()) {

            case CatmullRom:
                createCatmullRomMesh(nbSubSegments);
                break;
            case Linear:
            case Bezier:
            default:
                createLinearMesh();
                break;
        }
    }

    private void createCatmullRomMesh(int nbSubSegments) {
        float[] array = new float[(((spline.getControlPoints().size() - 1) * nbSubSegments) + 1) * 3];
        short[] indices = new short[((spline.getControlPoints().size() - 1) * nbSubSegments) * 2];
        int i = 0;
        int cptCP = 0;
        for (Iterator<Vector3f> it = spline.getControlPoints().iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            array[i] = vector3f.x;
            i++;
            array[i] = vector3f.y;
            i++;
            array[i] = vector3f.z;
            i++;
            if (it.hasNext()) {
                for (int j = 1; j < nbSubSegments; j++) {
                    spline.interpolate((float) j / nbSubSegments, cptCP, temp);
                    array[i] = temp.getX();
                    i++;
                    array[i] = temp.getY();
                    i++;
                    array[i] = temp.getZ();
                    i++;
                }
            }
            cptCP++;
        }

        i = 0;
        int k = 0;
        for (int j = 0; j < ((spline.getControlPoints().size() - 1) * nbSubSegments); j++) {
            k = j;
            indices[i] = (short) k;
            i++;
            k++;
            indices[i] = (short) k;
            i++;
        }

        setMode(Mesh.Mode.Lines);
        setBuffer(VertexBuffer.Type.Position, 3, array);
        setBuffer(VertexBuffer.Type.Index, ((spline.getControlPoints().size() - 1) * nbSubSegments) * 2, indices);
        updateBound();
        updateCounts();
    }

    private void createLinearMesh() {
        float[] array = new float[spline.getControlPoints().size() * 3];
        short[] indices = new short[(spline.getControlPoints().size() - 1) * 2];
        int i = 0;
        int cpt = 0;
        int k = 0;
        int j = 0;
        for (Iterator<Vector3f> it = spline.getControlPoints().iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            array[i] = vector3f.getX();
            i++;
            array[i] = vector3f.getY();
            i++;
            array[i] = vector3f.getZ();
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

        setMode(Mesh.Mode.Lines);
        setBuffer(VertexBuffer.Type.Position, 3, array);
        setBuffer(VertexBuffer.Type.Index, (spline.getControlPoints().size() - 1) * 2, indices);
        updateBound();
        updateCounts();
    }
}
