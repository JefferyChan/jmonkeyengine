package jme3tools.optimize;

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.util.IntMap.Entry;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeometryBatchFactory {

    private static void doTransformVerts(FloatBuffer inBuf, int offset, FloatBuffer outBuf, Matrix4f transform){
        Vector3f pos = new Vector3f();

        // offset is given in element units
        // convert to be in component units
        offset *= 3;

        for (int i = 0; i < inBuf.capacity()/3; i++){
            pos.x = inBuf.get(i*3+0);
            pos.y = inBuf.get(i*3+1);
            pos.z = inBuf.get(i*3+2);

            transform.mult(pos, pos);

            outBuf.put(offset+i*3+0, pos.x);
            outBuf.put(offset+i*3+1, pos.y);
            outBuf.put(offset+i*3+2, pos.z);
        }
    }

    private static void doTransformNorms(FloatBuffer inBuf, int offset, FloatBuffer outBuf, Matrix4f transform){
        Vector3f norm = new Vector3f();

        // offset is given in element units
        // convert to be in component units
        offset *= 3;

        for (int i = 0; i < inBuf.capacity()/3; i++){
            norm.x = inBuf.get(i*3+0);
            norm.y = inBuf.get(i*3+1);
            norm.z = inBuf.get(i*3+2);

            transform.multNormal(norm, norm);

            outBuf.put(offset+i*3+0, norm.x);
            outBuf.put(offset+i*3+1, norm.y);
            outBuf.put(offset+i*3+2, norm.z);
        }
    }

    /**
     * Merges all geometries in the collection into
     * the output mesh. Does not take into account materials.
     * 
     * @param geometries
     * @param outMesh
     */
    private static void mergeGeometries(Collection<Geometry> geometries, Mesh outMesh){
        int[] compsForBuf = new int[VertexBuffer.Type.values().length];
        Format[] formatForBuf = new Format[compsForBuf.length];

        int totalVerts = 0;
        int totalTris  = 0;

        for (Geometry geom : geometries){
            totalVerts += geom.getVertexCount();
            totalTris  += geom.getTriangleCount();

            for (Entry<VertexBuffer> entry : geom.getMesh().getBuffers()){
                compsForBuf[entry.getKey()] = entry.getValue().getNumComponents();
                formatForBuf[entry.getKey()] = entry.getValue().getFormat();
            }
        }

        // generate output buffers based on retrieved info
        for (int i = 0; i < compsForBuf.length; i++){
            if (compsForBuf[i] == 0)
                continue;

            Buffer data;
            if (i == Type.Index.ordinal()){
                data = VertexBuffer.createBuffer(formatForBuf[i], compsForBuf[i], totalTris);
            }else{
                data = VertexBuffer.createBuffer(formatForBuf[i], compsForBuf[i], totalVerts);
            }

            VertexBuffer vb = new VertexBuffer(Type.values()[i]);
            vb.setupData(Usage.Static, compsForBuf[i], formatForBuf[i], data);
            outMesh.setBuffer(vb);
        }

        int globalVertIndex = 0;
        int globalTriIndex  = 0;

        for (Geometry geom : geometries){
            Mesh inMesh = geom.getMesh();
            geom.computeWorldMatrix();
            Matrix4f worldMatrix = geom.getWorldMatrix();

            int geomVertCount = inMesh.getVertexCount();
            int geomTriCount  = inMesh.getTriangleCount();

            for (int bufType = 0; bufType < compsForBuf.length; bufType++){
                VertexBuffer inBuf = inMesh.getBuffer(Type.values()[bufType]);
                VertexBuffer outBuf = outMesh.getBuffer(Type.values()[bufType]);

                if (outBuf == null)
                    continue;

                if (Type.Index.ordinal() == bufType){
                    int components = compsForBuf[bufType];

                    IndexBuffer inIdx = inMesh.getIndexBuffer();
                    IndexBuffer outIdx = outMesh.getIndexBuffer();
                    for (int tri = 0; tri < geomTriCount; tri++){

                        int i1 = inIdx.get(tri*3+0) + globalVertIndex;
                        int i2 = inIdx.get(tri*3+1) + globalVertIndex;
                        int i3 = inIdx.get(tri*3+2) + globalVertIndex;
                        outIdx.put((globalTriIndex + tri) * 3 + 0, i1);
                        outIdx.put((globalTriIndex + tri) * 3 + 1, i2);
                        outIdx.put((globalTriIndex + tri) * 3 + 2, i3);
                    }
                }else if (Type.Position.ordinal() == bufType){
                    FloatBuffer inPos = (FloatBuffer) inBuf.getData();
                    FloatBuffer outPos = (FloatBuffer) outBuf.getData();
                    doTransformVerts(inPos, globalVertIndex, outPos, worldMatrix);
                }else if (Type.Normal.ordinal() == bufType){
                    FloatBuffer inPos = (FloatBuffer) inBuf.getData();
                    FloatBuffer outPos = (FloatBuffer) outBuf.getData();
                    doTransformNorms(inPos, globalVertIndex, outPos, worldMatrix);
                }else{
                    for (int vert = 0; vert < geomVertCount; vert++){
                        int curGlobalVertIndex = globalVertIndex + vert;
                        inBuf.copyElement(vert, outBuf, curGlobalVertIndex);
                    }
                }
            }

            globalVertIndex += geomVertCount;
            globalTriIndex  += geomTriCount;
        }
    }


    public static List<Geometry> makeBatches(List<Geometry> geometries){
        ArrayList<Geometry> retVal = new ArrayList<Geometry>();
        HashMap<Material, List<Geometry>> matToGeom = new HashMap<Material, List<Geometry>>();

        for (Geometry geom : geometries){
            List<Geometry> outList = matToGeom.get(geom.getMaterial());
            if (outList == null){
                outList = new ArrayList<Geometry>();
                matToGeom.put(geom.getMaterial(), outList);
            }
            outList.add(geom);
        }

        int batchNum = 0;
        for (Map.Entry<Material, List<Geometry>> entry : matToGeom.entrySet()){
            Material mat = entry.getKey();
            List<Geometry> geomsForMat = entry.getValue();
            Mesh mesh = new Mesh();
            mergeGeometries(geomsForMat, mesh);
            mesh.updateCounts();
            mesh.updateBound();

            Geometry out = new Geometry("batch[" + (batchNum++) + "]", mesh);
            out.setMaterial(mat);
            retVal.add(out);
        }

        return retVal;
    }

    private static void gatherGeoms(Spatial scene, List<Geometry> geoms){
        if (scene instanceof Node){
            Node node = (Node) scene;
            for (Spatial child : node.getChildren()){
                gatherGeoms(child, geoms);
            }
        }else if (scene instanceof Geometry){
            geoms.add((Geometry)scene);
        }
    }

    public static Spatial optimize(Spatial scene){
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        gatherGeoms(scene, geoms);

        List<Geometry> batchedGeoms = makeBatches(geoms);

        Node node = new Node(scene.getName());
        for (Geometry geom : batchedGeoms){
            node.attachChild(geom);
        }

        return node;
    }

    public static void printMesh(Mesh mesh){
        for (int bufType = 0; bufType < Type.values().length; bufType++){
            VertexBuffer outBuf = mesh.getBuffer(Type.values()[bufType]);
            if (outBuf == null)
                continue;

            System.out.println(outBuf.getBufferType() + ": ");
            for (int vert = 0; vert < outBuf.getNumElements(); vert++){
                String str = "[";
                for (int comp = 0; comp < outBuf.getNumComponents(); comp++){
                    Object val = outBuf.getElementComponent(vert, comp);
                    outBuf.setElementComponent(vert, comp, val);
                    val = outBuf.getElementComponent(vert, comp);
                    str += val;
                    if (comp != outBuf.getNumComponents()-1)
                        str += ", ";
                }
                str += "]";
                System.out.println(str);
            }
            System.out.println("------");
        }
    }

    public static void main(String[] args){
        Quad q1 = new Quad(1, 1);
        Quad q2 = new Quad(1, 1);
        
        Geometry g1 = new Geometry("g1", q1);
        g1.center();
        
        Geometry g2 = new Geometry("g2", q2);
        g2.rotate(FastMath.HALF_PI, 0, 0);

        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        geoms.add(g1);
        geoms.add(g2);

        Mesh outMesh = new Mesh();
        mergeGeometries(geoms, outMesh);
        printMesh(outMesh);
    }
}
