package com.jme3.bullet.collision.shapes;

import java.nio.FloatBuffer;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HullCollisionShape extends CollisionShape {

    private float[] points;
    protected FloatBuffer fbuf;

    public HullCollisionShape() {
    }

    public HullCollisionShape(Mesh mesh) {
        this.points = getPoints(mesh);
        createShape();
    }

    public HullCollisionShape(float[] points) {
        this.points = points;
        createShape();
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);

        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(points, "points", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);

        // for backwards compatability
        Mesh mesh = (Mesh) capsule.readSavable("hullMesh", null);
        if (mesh != null) {
            this.points = getPoints(mesh);
        } else {
            this.points = capsule.readFloatArray("points", null);

        }
//        fbuf = ByteBuffer.allocateDirect(points.length * 4).asFloatBuffer();
//        fbuf.put(points);
//        fbuf = FloatBuffer.wrap(points).order(ByteOrder.nativeOrder()).asFloatBuffer();
        createShape();
    }

    protected void createShape() {
//        ObjectArrayList<Vector3f> pointList = new ObjectArrayList<Vector3f>();
//        for (int i = 0; i < points.length; i += 3) {
//            pointList.add(new Vector3f(points[i], points[i + 1], points[i + 2]));
//        }
//        objectId = new ConvexHullShape(pointList);
//        objectId.setLocalScaling(Converter.convert(getScale()));
//        objectId.setMargin(margin);
        fbuf = ByteBuffer.allocateDirect(points.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fbuf.put(points);
        objectId = createShape(fbuf);
        setScale(scale);
        setMargin(margin);
    }

    private native long createShape(FloatBuffer points);

    protected float[] getPoints(Mesh mesh) {
        FloatBuffer vertices = mesh.getFloatBuffer(Type.Position);
        vertices.rewind();
        int components = mesh.getVertexCount() * 3;
        float[] pointsArray = new float[components];
        for (int i = 0; i < components; i += 3) {
            pointsArray[i] = vertices.get();
            pointsArray[i + 1] = vertices.get();
            pointsArray[i + 2] = vertices.get();
        }
        return pointsArray;
    }
}
