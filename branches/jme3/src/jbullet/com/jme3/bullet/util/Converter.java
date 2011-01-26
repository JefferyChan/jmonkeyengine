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
package com.jme3.bullet.util;

import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.dom.HeightfieldTerrainShape;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Nice convenience methods for conversion between javax.vecmath and com.jme3.math
 * Objects, also some jme to jbullet mesh conversion.
 * @author normenhansen
 */
public class Converter {

    private Converter() {
    }

    public static com.jme3.math.Vector3f convert(javax.vecmath.Vector3f oldVec) {
        com.jme3.math.Vector3f newVec = new com.jme3.math.Vector3f();
        convert(oldVec, newVec);
        return newVec;
    }

    public static com.jme3.math.Vector3f convert(javax.vecmath.Vector3f oldVec, com.jme3.math.Vector3f newVec) {
        newVec.x = oldVec.x;
        newVec.y = oldVec.y;
        newVec.z = oldVec.z;
        return newVec;
    }

    public static javax.vecmath.Vector3f convert(com.jme3.math.Vector3f oldVec) {
        javax.vecmath.Vector3f newVec = new javax.vecmath.Vector3f();
        convert(oldVec, newVec);
        return newVec;
    }

    public static javax.vecmath.Vector3f convert(com.jme3.math.Vector3f oldVec, javax.vecmath.Vector3f newVec) {
        newVec.x = oldVec.x;
        newVec.y = oldVec.y;
        newVec.z = oldVec.z;
        return newVec;
    }

    public static javax.vecmath.Quat4f convert(com.jme3.math.Quaternion oldQuat, javax.vecmath.Quat4f newQuat) {
        newQuat.w = oldQuat.getW();
        newQuat.x = oldQuat.getX();
        newQuat.y = oldQuat.getY();
        newQuat.z = oldQuat.getZ();
        return newQuat;
    }

    public static javax.vecmath.Quat4f convert(com.jme3.math.Quaternion oldQuat) {
        javax.vecmath.Quat4f newQuat = new javax.vecmath.Quat4f();
        convert(oldQuat, newQuat);
        return newQuat;
    }

    public static com.jme3.math.Quaternion convert(javax.vecmath.Quat4f oldQuat, com.jme3.math.Quaternion newQuat) {
        newQuat.set(oldQuat.x, oldQuat.y, oldQuat.z, oldQuat.w);
        return newQuat;
    }

    public static com.jme3.math.Quaternion convert(javax.vecmath.Quat4f oldQuat) {
        com.jme3.math.Quaternion newQuat = new com.jme3.math.Quaternion();
        convert(oldQuat, newQuat);
        return newQuat;
    }

    public static com.jme3.math.Matrix3f convert(javax.vecmath.Matrix3f oldMatrix) {
        com.jme3.math.Matrix3f newMatrix = new com.jme3.math.Matrix3f();
        convert(oldMatrix, newMatrix);
        return newMatrix;
    }

    public static com.jme3.math.Matrix3f convert(javax.vecmath.Matrix3f oldMatrix, com.jme3.math.Matrix3f newMatrix) {
        newMatrix.set(0, 0, oldMatrix.m00);
        newMatrix.set(0, 1, oldMatrix.m01);
        newMatrix.set(0, 2, oldMatrix.m02);
        newMatrix.set(1, 0, oldMatrix.m10);
        newMatrix.set(1, 1, oldMatrix.m11);
        newMatrix.set(1, 2, oldMatrix.m12);
        newMatrix.set(2, 0, oldMatrix.m20);
        newMatrix.set(2, 1, oldMatrix.m21);
        newMatrix.set(2, 2, oldMatrix.m22);
        return newMatrix;
    }

    public static javax.vecmath.Matrix3f convert(com.jme3.math.Matrix3f oldMatrix) {
        javax.vecmath.Matrix3f newMatrix = new javax.vecmath.Matrix3f();
        convert(oldMatrix, newMatrix);
        return newMatrix;
    }

    public static javax.vecmath.Matrix3f convert(com.jme3.math.Matrix3f oldMatrix, javax.vecmath.Matrix3f newMatrix) {
        newMatrix.m00 = oldMatrix.get(0, 0);
        newMatrix.m01 = oldMatrix.get(0, 1);
        newMatrix.m02 = oldMatrix.get(0, 2);
        newMatrix.m10 = oldMatrix.get(1, 0);
        newMatrix.m11 = oldMatrix.get(1, 1);
        newMatrix.m12 = oldMatrix.get(1, 2);
        newMatrix.m20 = oldMatrix.get(2, 0);
        newMatrix.m21 = oldMatrix.get(2, 1);
        newMatrix.m22 = oldMatrix.get(2, 2);
        return newMatrix;
    }

    public static com.bulletphysics.linearmath.Transform convert(com.jme3.math.Transform in, com.bulletphysics.linearmath.Transform out) {
        convert(in.getTranslation(), out.origin);
        //TODO: reuse matrix
        convert(in.getRotation().toRotationMatrix(), out.basis);
        return out;
    }

    public static IndexedMesh convert(Mesh mesh) {

        IndexedMesh jBulletIndexedMesh = new IndexedMesh();
        jBulletIndexedMesh.triangleIndexBase = ByteBuffer.allocate(mesh.getTriangleCount() * 3 * 4);
        jBulletIndexedMesh.vertexBase = ByteBuffer.allocate(mesh.getVertexCount() * 3 * 4);

        IndexBuffer indices = mesh.getIndexBuffer();
        FloatBuffer vertices = mesh.getFloatBuffer(Type.Position);
        vertices.rewind();

        int verticesLength = mesh.getVertexCount() * 3;
        jBulletIndexedMesh.numVertices = mesh.getVertexCount();
        jBulletIndexedMesh.vertexStride = 12; //3 verts * 4 bytes per.
        for (int i = 0; i < verticesLength; i++) {
            float tempFloat = vertices.get();
            jBulletIndexedMesh.vertexBase.putFloat(tempFloat);
        }

        int indicesLength = mesh.getTriangleCount() * 3;
        jBulletIndexedMesh.numTriangles = mesh.getTriangleCount();
        jBulletIndexedMesh.triangleIndexStride = 12; //3 index entries * 4 bytes each.
        for (int i = 0; i < indicesLength; i++) {
            jBulletIndexedMesh.triangleIndexBase.putInt(indices.get(i));
        }

        return jBulletIndexedMesh;
    }

    public static Mesh convert(IndexedMesh mesh) {
        Mesh jmeMesh = new Mesh();

        jmeMesh.setBuffer(Type.Index, 3, BufferUtils.createShortBuffer(mesh.numTriangles * 3));
        jmeMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(mesh.numVertices * 3));

        IndexBuffer indicess = jmeMesh.getIndexBuffer();
        FloatBuffer vertices = jmeMesh.getFloatBuffer(Type.Position);

        for (int i = 0; i < mesh.numTriangles * 3; i++) {
            indicess.put(i, mesh.triangleIndexBase.getInt(i*4));
        }

        for (int i = 0; i < mesh.numVertices * 3; i++) {
            vertices.put(i, mesh.vertexBase.getFloat(i*4));
        }
        jmeMesh.getFloatBuffer(Type.Position).clear();
        jmeMesh.updateCounts();
        jmeMesh.updateBound();

        return jmeMesh;
    }

	public static Mesh convert(HeightfieldTerrainShape heightfieldShape) {
		return null; //TODO!!
	}

}
