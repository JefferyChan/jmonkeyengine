/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.EmitterSphereShape;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.SpatialAssetDataObject;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.gde.core.undoredo.AbstractUndoableSceneEdit;
import com.jme3.gde.core.undoredo.SceneUndoRedoManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.AssetLinkNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.Control;
import com.jme3.ui.Picture;
import com.jme3.util.TangentBinormalGenerator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author normenhansen
 */
public class SceneEditorController implements PropertyChangeListener, NodeListener {

    private JmeSpatial jmeRootNode;
    private JmeSpatial selectedSpat;
    private DataObject currentFileObject;
//    private boolean needSave = false;

    public SceneEditorController(JmeSpatial jmeRootNode, DataObject currentFileObject) {
        this.jmeRootNode = jmeRootNode;
        this.currentFileObject = currentFileObject;
    }

    public JmeSpatial getJmeRootNode() {
        return jmeRootNode;
    }

    public JmeSpatial getSelectedSpat() {
        return selectedSpat;
    }

    public void setSelectedSpat(JmeSpatial selectedSpat) {
        if (this.selectedSpat == selectedSpat) {
            return;
        }
        if (this.selectedSpat != null) {
            this.selectedSpat.removePropertyChangeListener(this);
            this.selectedSpat.removeNodeListener(this);
        }
        this.selectedSpat = selectedSpat;
        if (selectedSpat != null) {
            selectedSpat.addPropertyChangeListener(this);//WeakListeners.propertyChange(this, selectedSpat));
            selectedSpat.addNodeListener(this);//WeakListeners.propertyChange(this, selectedSpat));
        }
    }

    public FileObject getCurrentFileObject() {
        return currentFileObject.getPrimaryFile();
    }

    public DataObject getCurrentDataObject() {
        return currentFileObject;
    }

    public void addSpatial(final String name) {
        addSpatial(name, new Vector3f(0, 0, 0));
    }

    public void addSpatial(final String name, final Vector3f point) {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                setNeedsSave(true);
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doAddSpatial(node, name, point);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doAddSpatial(Spatial selected, String name, Vector3f point) {
        Node undoParent = null;
        Light undoLight = null;
        Spatial undoSpatial = null;
        if (selected instanceof Node) {
            if ("Node".equals(name)) {
                Node node = new Node("Node");
                ((Node) selected).attachChild(node);
                refreshSelected();
                undoSpatial = node;
                undoParent = ((Node) selected);
            } else if ("Particle Emitter".equals(name)) {
                ParticleEmitter emit = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 200);
                emit.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
                emit.setGravity(0);
                emit.setLowLife(5);
                emit.setHighLife(10);
                emit.setInitialVelocity(new Vector3f(0, 0, 0));
                emit.setImagesX(15);
                Material mat = new Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                //                    mat.setTexture("Texture", SceneApplication.getApplication().getAssetManager().loadTexture("Effects/Smoke/Smoke.png"));
                emit.setMaterial(mat);
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    emit.setLocalTranslation(localVec);
                }
                ((Node) selected).attachChild(emit);
                refreshSelected();
                undoSpatial = emit;
                undoParent = ((Node) selected);
            } else if ("Audio Node".equals(name)) {
                AudioNode node = new AudioNode();
                node.setName("Audio Node");
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    node.setLocalTranslation(localVec);
                }
                ((Node) selected).attachChild(node);
                refreshSelected();
                undoSpatial = node;
                undoParent = ((Node) selected);
            } else if ("Picture".equals(name)) {
                Picture pic = new Picture("Picture");
                Material mat = new Material(SceneApplication.getApplication().getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                pic.setMaterial(mat);
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    pic.setLocalTranslation(localVec);
                }
                ((Node) selected).attachChild(pic);
                refreshSelected();
                undoSpatial = pic;
                undoParent = ((Node) selected);
            } else if ("Point Light".equals(name)) {
                PointLight light = new PointLight();
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    light.setPosition(localVec);
                }
                light.setColor(ColorRGBA.White);
                ((Node) selected).addLight(light);
                refreshSelected();
                undoLight = light;
                undoParent = ((Node) selected);
            } else if ("Directional Light".equals(name)) {
                DirectionalLight dl = new DirectionalLight();
                dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
                dl.setColor(ColorRGBA.White);
                ((Node) selected).addLight(dl);
                refreshSelected();
                undoLight = dl;
                undoParent = ((Node) selected);
            } else if ("Node".equals(name)) {
                Node node = new Node("Node");
                ((Node) selected).attachChild(node);
                refreshSelected();
                undoSpatial = node;
                undoParent = ((Node) selected);
            }
        } else if (selected instanceof Geometry) {
            if ("Point Light".equals(name)) {
                PointLight light = new PointLight();
                if (point != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(point, localVec);
                    light.setPosition(localVec);
                }
                light.setColor(ColorRGBA.White);
                selected.addLight(light);
                refreshSelected();
                undoLight = light;
                undoParent = ((Node) selected);
            } else if ("Directional Light".equals(name)) {
                DirectionalLight dl = new DirectionalLight();
                dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
                dl.setColor(ColorRGBA.White);
                selected.addLight(dl);
                refreshSelected();
                undoLight = dl;
                undoParent = ((Node) selected);
            } else if ("Ambient Light".equals(name)) {
                AmbientLight dl = new AmbientLight();
                dl.setColor(ColorRGBA.White);
                selected.addLight(dl);
                refreshSelected();
                undoLight = dl;
                undoParent = ((Node) selected);
            }
        }
        AbstractSceneExplorerNode selectedSpat = this.selectedSpat;
        addSpatialUndo(undoParent, undoSpatial, undoLight, selectedSpat);
    }

    private void addSpatialUndo(final Node undoParent, final Spatial undoSpatial, final Light undoLight, final AbstractSceneExplorerNode parentNode) {
        //add undo
        if (undoParent != null && undoSpatial != null) {
            Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

                @Override
                public void sceneUndo() throws CannotUndoException {
                    //undo stuff here
                    undoSpatial.removeFromParent();
                }

                @Override
                public void sceneRedo() throws CannotRedoException {
                    //redo stuff here
                    undoParent.attachChild(undoSpatial);
                }

                @Override
                public void awtRedo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }

                @Override
                public void awtUndo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }
            });
        }
        if (undoParent != null && undoLight != null) {
            Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

                @Override
                public void sceneUndo() throws CannotUndoException {
                    //undo stuff here
                    undoParent.removeLight(undoLight);
                }

                @Override
                public void sceneRedo() throws CannotRedoException {
                    //redo stuff here
                    undoParent.addLight(undoLight);
                }

                @Override
                public void awtRedo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }

                @Override
                public void awtUndo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }
            });
        }
    }

    public void moveSelectedSpatial(final Vector3f point) {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                setNeedsSave(true);
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doMoveSpatial(node, point);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doMoveSpatial(Spatial selected, Vector3f translation) {
        Vector3f localTranslation = selected.getLocalTranslation();
        Vector3f before = new Vector3f(localTranslation);
        Node parent = selected.getParent();
        if (parent != null) {
            localTranslation.set(translation).subtractLocal(parent.getWorldTranslation());
            localTranslation.divideLocal(parent.getWorldScale());
            //TODO: reuse quaternion..
            new Quaternion().set(parent.getWorldRotation()).inverseLocal().multLocal(localTranslation);
        } else {
            localTranslation.set(translation);
        }
        Vector3f after = new Vector3f(localTranslation);
        selected.setLocalTranslation(localTranslation);
        AbstractSceneExplorerNode selectedSpat = this.selectedSpat;
        moveUndo(selected, before, after, selectedSpat);
    }

    private void moveUndo(final Spatial spatial, final Vector3f before, final Vector3f after, final AbstractSceneExplorerNode parentNode) {
        if (spatial != null && before != null) {
            Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

                @Override
                public void sceneUndo() throws CannotUndoException {
                    //undo stuff here
                    spatial.setLocalTranslation(before);
                }

                @Override
                public void sceneRedo() throws CannotRedoException {
                    //redo stuff here
                    spatial.setLocalTranslation(after);
                }
            });
        }
    }

    public void createTangentsForSelectedSpatial() {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                setNeedsSave(true);
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doCreateTangents(node);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCreateTangents(Spatial selected) {
        if (selected instanceof Geometry) {
            Geometry geom = (Geometry) selected;
            Mesh mesh = geom.getMesh();
            if (mesh != null) {
                TangentBinormalGenerator.generate(mesh);
                createTrangentsUndo(mesh);
            }
        }
    }

    private void createTrangentsUndo(final Mesh mesh) {
        if (mesh != null) {
            Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

                @Override
                public void sceneUndo() throws CannotUndoException {
                    mesh.clearBuffer(Type.Tangent);
                }

                @Override
                public void sceneRedo() throws CannotRedoException {
                    TangentBinormalGenerator.generate(mesh);
                }
            });
        }
    }

    public void createPhysicsMeshForSelectedSpatial() {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            setNeedsSave(true);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doCreatePhysicsMesh(node);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCreatePhysicsMesh(Spatial selected) {
        RigidBodyControl control = selected.getControl(RigidBodyControl.class);
        if (control != null) {
            selected.removeControl(control);
        }
        Node parent = selected.getParent();
        selected.removeFromParent();
        control = new RigidBodyControl(0);
        selected.addControl(new RigidBodyControl(0));
        if (parent != null) {
            parent.attachChild(selected);
        }
        refreshSelected();
        AbstractSceneExplorerNode selectedSpat = this.selectedSpat;
        addControlUndo(parent, control, selectedSpat);
    }

    public void createDynamicPhysicsMeshForSelectedSpatial(final float weight) {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            setNeedsSave(true);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doCreateDynamicPhysicsMesh(node, weight);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCreateDynamicPhysicsMesh(Spatial selected, float weight) {
        RigidBodyControl control = selected.getControl(RigidBodyControl.class);
        if (control != null) {
            selected.removeControl(control);
        }
        Node parent = selected.getParent();
        selected.removeFromParent();
        control = new RigidBodyControl(weight);
        selected.addControl(control);
        if (parent != null) {
            parent.attachChild(selected);
        }
        refreshSelected();
        AbstractSceneExplorerNode selectedSpat = this.selectedSpat;
        addControlUndo(parent, control, selectedSpat);
    }

    public void createCharacterControlForSelectedSpatial(final boolean auto, final float radius, final float height) {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            setNeedsSave(true);
            if (node != null) {
                SceneApplication.getApplication().enqueue(new Callable() {

                    public Object call() throws Exception {
                        doCreateCharacterControl(node, auto, radius, height);
                        return null;

                    }
                }).get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCreateCharacterControl(Spatial selected, boolean auto, float radius, float height) {
        CharacterControl control = selected.getControl(CharacterControl.class);
        if (control != null) {
            selected.removeControl(control);
        }
        Node parent = selected.getParent();
        selected.removeFromParent();
        if (!auto) {
            control = new CharacterControl(new CapsuleCollisionShape(radius, height), 0.03f);
        } else {
            //TODO: auto-creation by bounding volume
            control = new CharacterControl(new CapsuleCollisionShape(radius, height), 0.03f);
        }
        selected.addControl(control);
        if (parent != null) {
            parent.attachChild(selected);
        }
        refreshSelected();
        AbstractSceneExplorerNode selectedSpat = this.selectedSpat;
        addControlUndo(parent, control, selectedSpat);
    }

    private void addControlUndo(final Node undoParent, final Control undoControl, final AbstractSceneExplorerNode parentNode) {
        if (undoParent != null && undoControl != null) {
            Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

                @Override
                public void sceneUndo() throws CannotUndoException {
                    //undo stuff here
                    undoParent.removeControl(undoControl);
                }

                @Override
                public void sceneRedo() throws CannotRedoException {
                    //redo stuff here
                    undoParent.addControl(undoControl);
                }

                @Override
                public void awtRedo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }

                @Override
                public void awtUndo() {
                    if (parentNode != null) {
                        parentNode.refresh(true);
                    }
                }
            });
        }
    }

    public void addModel(final SpatialAssetDataObject file, final Vector3f location) {
        if (selectedSpat == null) {
            return;
        }
        final Node selected = selectedSpat.getLookup().lookup(Node.class);
        ProjectAssetManager manager = file.getLookup().lookup(ProjectAssetManager.class);
        if (manager != null) {
            ((DesktopAssetManager) manager.getManager()).clearCache();
        }
        if (selected != null) {
            setNeedsSave(true);
            SceneApplication.getApplication().enqueue(new Callable<Object>() {

                public Object call() throws Exception {
                    doAddModel(file, selected, location);
                    return null;
                }
            });
        }
    }

    public void doAddModel(SpatialAssetDataObject file, Node selected, Vector3f location) {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Adding Model..");
        progressHandle.start();
        try {
            Spatial linkNode = (Spatial) file.loadAsset();
            if (linkNode != null) {
                selected.attachChild(linkNode);
                if (location != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(location, localVec);
                    linkNode.setLocalTranslation(localVec);
                }
            }
            refreshSelected();
            addSpatialUndo(selected, linkNode, null, selectedSpat);
        } catch (Exception ex) {
            Confirmation msg = new NotifyDescriptor.Confirmation(
                    "Error importing " + file.getName() + "\n" + ex.toString(),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
        }
        progressHandle.finish();

    }

    public void linkModel(final AssetManager manager, final String assetName, final Vector3f location) {
        if (selectedSpat == null) {
            return;
        }
        final Node selected = selectedSpat.getLookup().lookup(Node.class);
        if (selected != null) {
            setNeedsSave(true);
            SceneApplication.getApplication().enqueue(new Callable<Object>() {

                public Object call() throws Exception {
                    doLinkModel(manager, assetName, selected, location);
                    return null;
                }
            });
        }
    }

    public void doLinkModel(AssetManager manager, String assetName, Node selected, Vector3f location) {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Adding Model..");
        progressHandle.start();
        try {
            if (selected instanceof AssetLinkNode) {
                AssetLinkNode linkNode = (AssetLinkNode) selected;
                linkNode.attachLinkedChild(manager, new ModelKey(assetName));
            } else {
                ((DesktopAssetManager) manager).clearCache();
                ModelKey key = new ModelKey(assetName);
                AssetLinkNode linkNode = new AssetLinkNode(key);
                linkNode.attachLinkedChildren(manager);
                selected.attachChild(linkNode);
                if (location != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(location, localVec);
                    linkNode.setLocalTranslation(localVec);
                }
                addSpatialUndo(selected, linkNode, null, selectedSpat);
            }
            refreshSelected();
        } catch (Exception ex) {
            Confirmation msg = new NotifyDescriptor.Confirmation(
                    "Error importing " + assetName + "\n" + ex.toString(),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
        }
        progressHandle.finish();

    }

    public void addModel(final Spatial file) {
        addModel(file, null);
    }

    public void addModel(final Spatial file, final Vector3f location) {
        if (selectedSpat == null) {
            return;
        }
        final Node selected = selectedSpat.getLookup().lookup(Node.class);
        if (selected != null) {
            setNeedsSave(true);
            SceneApplication.getApplication().enqueue(new Callable<Object>() {

                public Object call() throws Exception {
                    doAddModel(file, selected, location);
                    return null;
                }
            });
        }
    }

    public void doAddModel(Spatial file, Node selected, Vector3f location) {
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Adding Model..");
        progressHandle.start();
        try {
            if (file != null) {
                selected.attachChild(file);
                if (location != null) {
                    Vector3f localVec = new Vector3f();
                    selected.worldToLocal(location, localVec);
                    file.setLocalTranslation(localVec);
                }
            }
            refreshSelected();
            addSpatialUndo(selected, file, null, selectedSpat);
        } catch (Exception ex) {
            Confirmation msg = new NotifyDescriptor.Confirmation(
                    "Error importing " + file.getName() + "\n" + ex.toString(),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
        }
        progressHandle.finish();

    }

    public void setNeedsSave(boolean state) {
        currentFileObject.setModified(state);
    }

    public boolean isNeedSave() {
        return currentFileObject.isModified();
    }

    public void propertyChange(PropertyChangeEvent evt) {
//        if ((evt.getOldValue() == null && !(evt.getNewValue() == null)) || ((evt.getOldValue() != null) && !evt.getOldValue().equals(evt.getNewValue()))) {
//            setNeedsSave(true);
//        }
    }

    public void childrenAdded(NodeMemberEvent ev) {
//        setNeedsSave(true);
    }

    public void childrenRemoved(NodeMemberEvent ev) {
//        setNeedsSave(true);
    }

    public void childrenReordered(NodeReorderEvent ev) {
//        setNeedsSave(true);
    }

    public void nodeDestroyed(NodeEvent ev) {
//        setNeedsSave(true);
    }

    public void saveScene() {
        try {
            currentFileObject.getLookup().lookup(SaveCookie.class).save();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void refreshSelected(final JmeSpatial spat) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (spat != null) {
                    spat.refresh(false);
                }
            }
        });

    }

    private void refreshSelected() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getSelectedSpat() != null) {
                    getSelectedSpat().refresh(false);
                }
            }
        });

    }

    private void refreshSelectedParent() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getSelectedSpat() != null) {
                    ((JmeSpatial) getSelectedSpat().getParentNode()).refresh(false);
                }
            }
        });

    }

    private void refreshRoot() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (getJmeRootNode() != null) {
                    getJmeRootNode().refresh(true);
                }
            }
        });

    }

    public void cleanup() {
        final Node node = jmeRootNode.getLookup().lookup(Node.class);
        if (selectedSpat != null) {
            selectedSpat.removePropertyChangeListener(this);
        }
        SceneApplication.getApplication().enqueue(new Callable() {

            public Object call() throws Exception {
                doCleanup(node);
                return null;
            }
        });
    }

    public void doCleanup(Node node) {
        node.removeFromParent();
    }
}
