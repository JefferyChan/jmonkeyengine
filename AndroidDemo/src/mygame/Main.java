package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.animation.Track;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.TempVars;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.LodGenerator;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {
  
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    private Node scene ;
    private Spatial ground ;

    @Override
    public void simpleInitApp() {
        scene = (Node)assetManager.loadModel("Scenes/Scene.j3o");        
        flyCam.setMoveSpeed(50);
        flyCam.setEnabled(false);
        
        rootNode.attachChild(scene);
        ground = scene.getChild("Ground");
        cam.setLocation(new Vector3f(5.0244403f, 4.2122016f, -30.357338f));
        cam.setRotation(new Quaternion(0.112140924f, 0.15460506f, -0.01766564f, 0.98143244f));
        
        RTSCameraHandler camHandler = new RTSCameraHandler(cam, rootNode);
        camHandler.registerInputs(inputManager);

        Node jaime = (Node) assetManager.loadModel("Models/Jaime/JaimeOptimized.j3o");
        jaime.getControl(SkeletonControl.class).setHardwareSkinningPreferred(true);
        jaime.setLocalTranslation(new Vector3f(12.0908f, 0, -12.063316f));
        
        rootNode.attachChild(jaime);
        
        
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1, -1, 1));
        rootNode.addLight(dl);
        
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.5f));       
        rootNode.addLight(al);
        
        camHandler.lookAt(jaime.getWorldTranslation());
       // cleanUPSkeleton(jaime);
       // LodGenerator gen = new LodGenerator((Geometry)jaime.getChild(0));
      //  gen.bakeLods(LodGenerator.TriangleReductionMethod.PROPORTIONAL, 0.6f);
        ((Geometry)jaime.getChild(0)).setLodLevel(1);
        
        jaime.getControl(AnimControl.class).createChannel().setAnim("Idle");
         /** Save a Node to a .j3o file. */
//   
//        BinaryExporter exporter = BinaryExporter.getInstance();
//        File file = new File("e:/somefile.j3o");
//        try {
//          exporter.save(jaime, file);
//        } catch (IOException ex) {
//          Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to save node!", ex);
//        } 
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }


    
    private Vector3f pick(float x, float y) {
        TempVars vars = TempVars.get();
        Vector2f v2 = vars.vect2d;
        v2.set(x, y);
        Vector3f origin = cam.getWorldCoordinates(v2, 0.0f, vars.vect1);
        Vector3f direction = cam.getWorldCoordinates(v2, 0.3f, vars.vect2);
        direction.subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        Vector3f contactPoint = null;
        ground.collideWith(ray, results);

        
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            contactPoint = closest.getContactPoint();         
        }

        vars.release();
        return contactPoint;

    }

    private void cleanUPSkeleton(Node jaime) {
        Geometry g = (Geometry) jaime.getChild(0);
        VertexBuffer vb = g.getMesh().getBuffer(VertexBuffer.Type.BoneIndex);
       
        
        ByteBuffer indices = (ByteBuffer)vb.getData();
        Set<Integer> usedIndices = new HashSet<Integer>();
        byte[] ids = new byte[indices.capacity()];
        indices.rewind();
        indices.get(ids);
       
        for (int i = 0; i < ids.length; i++) {
            usedIndices.add((int)ids[i]);
        }
   
        List<String> removed = new ArrayList<String>();
       
        Skeleton skeleton = jaime.getControl(SkeletonControl.class).getSkeleton(); 
        int[] newIndices = new int[skeleton.getBoneCount()];
        for (int i = 0; i < newIndices.length; i++) {
            newIndices[i]=i;
        }
        
        for (int i = 0; i < skeleton.getBoneCount(); i++) {
            if (!usedIndices.contains(i)) {
                Bone b = skeleton.getBone(i);
                if(b.getChildren().isEmpty()){
                    System.out.println("Unused bone : " + b.getName()+ " "+ skeleton.getBoneIndex(b));
                   removed.add(b.getName());
                }
                          
            }
        }
        
        for (String string : removed) {
           int index = skeleton.getBoneIndex(string);           
            for (int i = index; i < newIndices.length; i++) {
                newIndices[i]--;
            }
        }
        
        AnimControl animControl = jaime.getControl(AnimControl.class);
        for (String string : animControl.getAnimationNames()) {
            Animation anim = animControl.getAnim(string);
            List<Track> newTracks = new ArrayList<Track>();
            for (Track track : anim.getTracks()) {
                if(track instanceof BoneTrack){
                    BoneTrack t = (BoneTrack)track;
                    if(!removed.contains(skeleton.getBone(t.getTargetBoneIndex()).getName())){
                        newTracks.add(new BoneTrack(newIndices[t.getTargetBoneIndex()], t.getTimes(), t.getTranslations(), t.getRotations(), t.getScales()));
                    }
                }else{
                    newTracks.add(track);
                }
            }
            while(anim.getTracks().length>0){
                anim.removeTrack(anim.getTracks()[0]);
            }
            Track[] tracks = new Track[newTracks.size()];
            newTracks.toArray(tracks);
            anim.setTracks(tracks);
        }
        
        for (String string : removed) {           
            skeleton.removeBone(string);           
        }
         
        for (int i = 0; i < ids.length; i++) {
            int oldid = ids[i];
            int newid = newIndices[oldid];
            ids[i] = (byte)newid;
        }
       
        indices.rewind();
        indices.put(ids);
        vb.updateData(indices);
        
       
        
        System.out.println(jaime.getControl(SkeletonControl.class).getSkeleton().getBoneCount());
        System.out.println(skeleton);
    }
}


