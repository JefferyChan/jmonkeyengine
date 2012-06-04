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
package com.jmex.model.collada;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.animation.TextureKeyframeController;
import com.jme.bounding.BoundingBox;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.image.Texture.WrapAxis;
import com.jme.image.Texture.WrapMode;
import com.jme.light.DirectionalLight;
import com.jme.light.Light;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.light.SpotLight;
import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.CameraNode;
import com.jme.scene.Geometry;
import com.jme.scene.Line;
import com.jme.scene.MatrixTriMesh;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.ClipState;
import com.jme.scene.state.ColorMaskState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ShadeState;
import com.jme.scene.state.StencilState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.geom.BufferUtils;
import com.jme.util.geom.GeometryTool;
import com.jme.util.geom.VertMap;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ColladaAnimationGroup.NodeFinder;
import com.jmex.model.collada.ColladaControllerNode.JointFinder;
import com.jmex.model.collada.schema.COLLADAType;
import com.jmex.model.collada.schema.InputLocal;
import com.jmex.model.collada.schema.InputLocalOffset;
import com.jmex.model.collada.schema.InstanceWithExtra;
import com.jmex.model.collada.schema.TargetableFloat3;
import com.jmex.model.collada.schema.animationType;
import com.jmex.model.collada.schema.assetType;
import com.jmex.model.collada.schema.bind_materialType;
import com.jmex.model.collada.schema.blinnType;
import com.jmex.model.collada.schema.cameraType;
import com.jmex.model.collada.schema.collada_schema_1_4_1Doc;
import com.jmex.model.collada.schema.colorType;
import com.jmex.model.collada.schema.common_color_or_texture_type;
import com.jmex.model.collada.schema.common_float_or_param_type;
import com.jmex.model.collada.schema.common_newparam_type;
import com.jmex.model.collada.schema.common_transparent_type;
import com.jmex.model.collada.schema.controllerType;
import com.jmex.model.collada.schema.effectType;
import com.jmex.model.collada.schema.fx_sampler2D_common;
import com.jmex.model.collada.schema.fx_surface_common;
import com.jmex.model.collada.schema.geometryType;
import com.jmex.model.collada.schema.imageType;
import com.jmex.model.collada.schema.instance_controllerType;
import com.jmex.model.collada.schema.instance_geometryType;
import com.jmex.model.collada.schema.instance_materialType;
import com.jmex.model.collada.schema.instance_physics_modelType;
import com.jmex.model.collada.schema.lambertType;
import com.jmex.model.collada.schema.library_animationsType;
import com.jmex.model.collada.schema.library_camerasType;
import com.jmex.model.collada.schema.library_controllersType;
import com.jmex.model.collada.schema.library_effectsType;
import com.jmex.model.collada.schema.library_geometriesType;
import com.jmex.model.collada.schema.library_imagesType;
import com.jmex.model.collada.schema.library_lightsType;
import com.jmex.model.collada.schema.library_materialsType;
import com.jmex.model.collada.schema.library_nodesType;
import com.jmex.model.collada.schema.library_physics_modelsType;
import com.jmex.model.collada.schema.library_physics_scenesType;
import com.jmex.model.collada.schema.library_visual_scenesType;
import com.jmex.model.collada.schema.lightType;
import com.jmex.model.collada.schema.materialType;
import com.jmex.model.collada.schema.meshType;
import com.jmex.model.collada.schema.nodeType2;
import com.jmex.model.collada.schema.opticsType;
import com.jmex.model.collada.schema.orthographicType;
import com.jmex.model.collada.schema.passType3;
import com.jmex.model.collada.schema.perspectiveType;
import com.jmex.model.collada.schema.phongType;
import com.jmex.model.collada.schema.physics_modelType;
import com.jmex.model.collada.schema.physics_sceneType;
import com.jmex.model.collada.schema.rigid_bodyType;
import com.jmex.model.collada.schema.sceneType;
import com.jmex.model.collada.schema.shapeType2;
import com.jmex.model.collada.schema.skinType;
import com.jmex.model.collada.schema.sourceType;
import com.jmex.model.collada.schema.techniqueType2;
import com.jmex.model.collada.schema.techniqueType4;
import com.jmex.model.collada.schema.technique_commonType;
import com.jmex.model.collada.schema.technique_commonType2;
import com.jmex.model.collada.schema.technique_commonType4;
import com.jmex.model.collada.schema.textureType;
import com.jmex.model.collada.schema.trianglesType;
import com.jmex.model.collada.schema.vertex_weightsType;
import com.jmex.model.collada.schema.visual_sceneType;
import com.jmex.model.collada.schema.glsl_newparam;
import com.jmex.model.collada.schema.linesType;
import com.jmex.model.collada.schema.matrixType;
import com.jmex.model.collada.schema.polylistType;
import com.jmex.model.collada.schema.rotateType;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code>ColladaNode</code> provides a mechanism to parse and load a COLLADA
 * (COLLAborative Design Activity) model. Making use of a DOM parse, the XML
 * formatted COLLADA file is parsed into Java Type classes and then processed by
 * jME. This processing is currently aimed at the 1.4.1 release of the COLLADA
 * Specification, and will, in most likelyhood, require updating with a new
 * release of COLLADA.
 *
 * @author Mark Powell, Rikard Herlitz, and others
 */
public class ThreadSafeColladaImporter {
    private static final Logger globalLogger = Logger.getLogger(ThreadSafeColladaImporter.class
            .getName());
    private static final InstanceLogger logger = new InstanceLogger();
    
    private static final Pattern NOT_SPACE = Pattern.compile("[^\\s]+");
    
    // asset information
    private String modelAuthor;
    private String tool;
    private String revision;
    private String unitName;
    private float unitMeter;
    private String upAxis;
    private String name;
    private boolean squelch;
    private int textureIndex = 0;

    // If true, models loaded by ColladaImporter will automatically have
    // geometry optimization applied. default: true.
    public boolean OPTIMIZE_GEOMETRY = true;

    public OptimizeCallback optimizeCallBack = null;

    private Map<String, Object> resourceLibrary;
    private ArrayList<String> uvControllerNames;
    private ArrayList<String> cameraNodeNames;
    private ArrayList<String> lightNodeNames;
    private ArrayList<String> geometryNames;
    private Map<String, Object> userInformation;
    private Map<Geometry, int[]> meshVertices;
    private Map<String, ColladaJointNode> skeletons;
    private ColladaRootNode model;

    /**
     * Unique Serial ID for ColladaNode
     */
    private static final long serialVersionUID = -4024091270314000507L;

    // Flags so we only log unsuported feature errors once
    private boolean reportedAnimationClips=false;
    private boolean reportedForceFields=false;
    private boolean reportedRawDataImages=false;
    private boolean reportedSplines=false;

    private enum Semantic {
        BINORMAL, COLOR, NORMAL, POSITION, TANGENT, TEXBINORMAL, TEXCOORD,
        TEXTANGENT, UV, VERTEX
    }
    
    /**
     * Default constructor instantiates a ColladaImporter object. A basic Node
     * structure is built and no data is loaded until the <code>load</code>
     * method is called.
     *
     * @param name
     *            the name of the node.
     */
    public ThreadSafeColladaImporter(String name) {
        this.name = name;
    }

    public boolean hasUserInformation(String key) {
        if (userInformation == null) {
            return false;
        } else {
            return userInformation.containsKey(key);
        }
    }

    public void addUserInformation(String key, Object value) {
        if (userInformation == null) {
            userInformation = new HashMap<String, Object>();
        }
        userInformation.put(key, value);
    }

    public Object getUserInformation(String key) {
        if (userInformation == null) {
            return null;
        } else {
            return userInformation.get(key);
        }
    }

    /**
     * load is called by the static load method, creating an instance of the
     * model to be returned.
     *
     * @param source
     *            the source to import.
     * @param textureDirectory
     *            the location of the textures.
     */
    public void load(InputStream source) {
        model = new ColladaRootNode(name);
        resourceLibrary = new HashMap<String, Object>();
        meshVertices = new HashMap<Geometry, int[]>();
        skeletons = new HashMap<String, ColladaJointNode>();
        
        long startTime = System.nanoTime();
        collada_schema_1_4_1Doc doc = new collada_schema_1_4_1Doc(); 
        globalLogger.info("Doc creation took "+(System.nanoTime()-startTime)/1000000);
        try {
            startTime = System.nanoTime();
            COLLADAType root = new COLLADAType(doc.load(source));
            globalLogger.info("Load took "+(System.nanoTime()-startTime)/1000000);
            globalLogger.info("Version: " + root.getversion().getValue());
            startTime = System.nanoTime();
            processCollada(root);
            globalLogger.info("Parse took "+(System.nanoTime()-startTime)/1000000);
        } catch (Exception ex) {
            System.out.println(ex);
            logger.log(Level.SEVERE, "Unable to load Collada file. ", ex);
            return;
        } finally {
            // clear lists we created
            resourceLibrary.clear();
            meshVertices.clear();
            skeletons.clear();
        }
    }

    /**
     * Set the listener to track loader errors. Use null to disable the listener
     *
     * @param listener
     */
    public void setErrorListener(LoaderErrorListener listener) {
        logger.setErrorListener(listener);
    }

    /**
     * @return
     */
    public ArrayList<String> getUVControllerNames() {
        return uvControllerNames;
    }

    public void addUVControllerName(String name) {
        if (uvControllerNames == null) {
            uvControllerNames = new ArrayList<String>();
        }
        uvControllerNames.add(name);
    }

    /**
     * Returns the camera node names associated with this model.
     *
     * @return the list of camera names that are referenced in this file.
     */
    public ArrayList<String> getCameraNodeNames() {
        return cameraNodeNames;
    }

    public ArrayList<String> getLightNodeNames() {
        return lightNodeNames;
    }

    public ArrayList<String> getGeometryNames() {
        return geometryNames;
    }

    public Node getModel() {
        return model;
    }
    
    public ColladaRootNode getColladaRootNode() {
        return model;
    }

    public CameraNode getCameraNode(String id) {
        return (CameraNode) resourceLibrary.get(id);
    }

    public LightNode getLightNode(String id) {
        return (LightNode) resourceLibrary.get(id);
    }

    public Object get(Object id) {
        return resourceLibrary.get(id);
    }

    /**
     * places an object into the resource library with a given key. If there is
     * an object referenced by this key and it is not the same object that is to
     * be added to the library, a warning is issued. If this object already
     * exists in the library we do not readd it.
     *
     * @param key
     *            the key to obtain the object from the library.
     * @param value
     *            the object to store in the library.
     */
    public void put(String key, Object value) {
        Object data = resourceLibrary.get(key);
        if (data != value) {
            if (data != null) {
                if (!squelch) {
                    logger
                            .warning("Key: "
                                    + key
                                    + " already in use. Overriding previous data. This is probably not"
                                    + " desired.");
                }
            }
            resourceLibrary.put(key, value);
        }
    }

    public TextureKeyframeController getUVAnimationController(String id) {
        return (TextureKeyframeController) resourceLibrary.get(id);
    }

    public Geometry getGeometry(String id) {
        return (Geometry) resourceLibrary.get(id);
    }

    public void cleanUp() {
    }

    /**
     * Author of the last loaded collada model.
     *
     * @return the modelAuthor the author of the last loaded model.
     */
    public String getModelAuthor() {
        return modelAuthor;
    }

    /**
     * Revision number of the last loaded collada model.
     *
     * @return the revision revision number of the last loaded collada model.
     */
    public String getRevision() {
        return revision;
    }

    /**
     * the tool used to build the last collada model.
     *
     * @return the tool
     */
    public String getTool() {
        return tool;
    }

    /**
     * the unit scale of the last collada model.
     *
     * @return the unitMeter
     */
    public float getUnitMeter() {
        return unitMeter;
    }

    /**
     * the unit name of the last collada model.
     *
     * @return the unitName
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * The up axis for this model, X_UP, Y_UP or Z_UP
     * @return the up axis
     */
    public String getUpAxis() {
        return upAxis;
    }

    /**
     * getAssetInformation returns a string of the collected asset information
     * of this COLLADA model. The format is such: <br>
     * AUTHOR REVISION<br>
     * TOOL<br>
     * UNITNAME UNITMETER<br>
     * UPAXIS<br>
     *
     * @return the string representation of the asset information of this file.
     */
    public String getAssetInformation() {
        return modelAuthor + " " + revision + "\n" + tool + "\n" + unitName
                + " " + unitMeter + "\n" + upAxis;
    }

    /**
     * processCollada takes a COLLADAType object that contains the heirarchical
     * information obtained from the XML structure of a COLLADA model. This root
     * object is processed and sets the data structures for jME to render the
     * model to *this* object.
     *
     * @param root
     *            the COLLADAType data structure that contains the COLLADA model
     *            information.
     */
    public void processCollada(COLLADAType root) {
        // build the asset information about this model. This can be used
        // for debugging information. Only a single asset tag is allowed per
        // model.
        if (root.hasasset()) {
            try {
                processAssetInformation(root.getasset());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing asset information", e);
                }
            }
        }
        // user defined libraries may exist (for example, uv animations)
        if (root.hasextra()) {
            try {
                ExtraPluginManager.processExtra(root, root.getextra());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing extra information", e);
                }
            }
        }
        // builds the animation keyframes and places the controllers into a
        // node.
        if (root.haslibrary_animations()) {
            try {
                processAnimationLibrary(root.getlibrary_animations());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing animation information", e);
                }
            }
        }
        if (root.haslibrary_animation_clips()) {
            if (!squelch && !reportedAnimationClips) {
                logger.warning("Animation Clips not currently supported");
                reportedAnimationClips = true;
            }
        }
        if (root.haslibrary_cameras()) {
            try {
                processCameraLibrary(root.getlibrary_cameras());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing camera information", e);
                }
            }
        }
        if (root.haslibrary_force_fields()) {
            if (!squelch && !reportedForceFields) {
                logger.warning("Forcefields not currently supported");
                reportedForceFields = true;
            }
        }
        if (root.haslibrary_lights()) {
            try {
                processLightLibrary(root.getlibrary_lights());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing light information", e);
                }
            }
        }
        // build a map of images that the materials can use in the future.
        if (root.haslibrary_images()) {
            try {
                processImageLibrary(root.getlibrary_images());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing image library information", e);
                }
            }
        }
        // build all the material states that can be used later
        if (root.haslibrary_materials()) {
            try {
                processMaterialLibrary(root.getlibrary_materials());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing material library information", e);
                }
            }
        }
        // process the library of effects, filling in the appropriate
        // states.
        if (root.haslibrary_effects()) {
            try {
                processEffects(root.getlibrary_effects());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing effects library information", e);
                }
            }
        }
        // process the geometry information, creating the appropriate Geometry
        // object from jME (TriMesh, lines or point).
        if (root.haslibrary_geometries()) {
            try {
                processGeometry(root.getlibrary_geometries());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing geometry library information", e);
                }
            }
        }
        // controllers will define the action of another object. For example,
        // there may be a controller with a skin tag, defining how a mesh
        // is skinning a skeleton.
        if (root.haslibrary_controllers()) {
            try {
                processControllerLibrary(root.getlibrary_controllers());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing controller library information",
                            e);
                }
            }
        }
        if (root.haslibrary_nodes()) {
            try {
                processNodes(root.getlibrary_nodes());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing nodes library information", e);
                }
            }
        }
        // process the visual scene. This scene will define how the geometries
        // are structured in the world.
        if (root.haslibrary_visual_scenes()) {
            try {
                processVisualSceneLibrary(root.getlibrary_visual_scenes());
            } catch (Exception e) {
                if (!squelch) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "Error processing visual scene library information",
                                    e);
                }
            }
        }
        if (root.haslibrary_physics_scenes()) {
            try {
                library_physics_scenesType library = root
                        .getlibrary_physics_scenes();
                for (int i = 0; i < library.getphysics_sceneCount(); i++) {
                    physics_sceneType scene = library.getphysics_sceneAt(i);
                    put(scene.getid().toString(), scene);
                }
            } catch (Exception e) {
                if (!squelch) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "Error processing physics scene library information",
                                    e);
                }
            }
        }
        if (root.haslibrary_physics_models()) {
            try {
                library_physics_modelsType library = root
                        .getlibrary_physics_models();
                for (int i = 0; i < library.getphysics_modelCount(); i++) {
                    physics_modelType model = library.getphysics_modelAt(i);
                    put(model.getid().toString(), model);
                }
            } catch (Exception e) {
                if (!squelch) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "Error processing physics model library information",
                                    e);
                }
            }
        }
        // the scene tag actually takes instances of the visual scene defined
        // above
        // and attaches them to the model that is returned.
        if (root.hasscene()) {
            try {
                processScene(root.getscene());
            } catch (Exception e) {
                if (!squelch) {
                    logger.log(Level.WARNING,
                            "Error processing scene information", e);
                }
            }
        }
        
        // now that all nodes are defined, hook up all animations
        for (ColladaAnimationGroup anim : model.getAnimationGroups()) {
            attachAnimation(anim);
        }
        
        // now hook up any controllers
        attachControllers(model);
        
        try {
            optimizeGeometry();
        } catch (Exception e) {
            if (!squelch) {
                logger.log(Level.WARNING, "Error optimizing geometry", e);
            }
        }
        
        // make sure all world data is updated
        model.updateGeometricState(0, true);
    }

    private void attachAnimation(ColladaAnimationGroup anim) {
        anim.attach(new NodeFinder() {
            public ColladaNode findNode(String name) {
                return findNode(model, name);
            }

            public ColladaNode findNode(Node node, String name) {
                if (node instanceof ColladaNode && name.equals(node.getName())) {
                    return (ColladaNode) node;
                }

                if (node.getChildren() != null) {
                    for (Spatial child : node.getChildren()) {
                        if (child instanceof Node) {
                            ColladaNode res = findNode((Node) child, name);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                }

                return null;
            }
        });
    }
    
    private void attachControllers(Node node) {
        if (node instanceof ColladaControllerNode) {
            attachController((ColladaControllerNode) node);
        }
        
        if (node.getChildren() != null) {
            for (Spatial child : node.getChildren()) {
                if (child instanceof Node) {
                    attachControllers((Node) child);
                }
            }
        }
    }
    
    private void attachController(final ColladaControllerNode controller) {
        controller.attach(new JointFinder() {

            public ColladaJointNode findJoint(String sid) {
                for (String skeletonName : controller.getSkeletonNames()) {
                    ColladaJointNode skel = skeletons.get(skeletonName);
                    ColladaJointNode node = findJoint(skel, sid);
                    if (node != null) {
                        return node;
                    }
                }
                
                return null;
            }
            
            public ColladaJointNode findJoint(ColladaJointNode node, String sid) {
                if (sid.equals(node.getSid())) {
                    return node;
                }
                
                if (node.getChildren() != null) {
                    for (Spatial child : node.getChildren()) {
                        if (child instanceof ColladaJointNode) {
                            ColladaJointNode res = findJoint((ColladaJointNode) child, sid);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                }
                
                return null;
            }
            
        });
    }
    
    
    /**
     * optimizeGeometry
     */
    private void optimizeGeometry() {
        for (String key : resourceLibrary.keySet()) {
            Object val = resourceLibrary.get(key);

            if (val instanceof Spatial) {
                Spatial spatial = (Spatial) val;
                int options = GeometryTool.MV_SAME_COLORS
                        | GeometryTool.MV_SAME_NORMALS
                        | GeometryTool.MV_SAME_TEXS;
                
                if (spatial instanceof TriMesh && 
                        !(spatial instanceof ColladaSkinnedMesh)) 
                {
                    TriMesh mesh = (TriMesh) spatial;
                    if (OPTIMIZE_GEOMETRY) {
                        VertMap map = GeometryTool.minimizeVerts(mesh, options);
                        if (optimizeCallBack != null) {
                            optimizeCallBack.remapInfluences(mesh, map);
                        }
                    }
                }
            }
        }
    }

    /**
     * processLightLibrary
     *
     * @param libraryLights
     * @throws Exception
     */
    private void processLightLibrary(library_lightsType libraryLights)
            throws Exception {
        if (libraryLights.haslight()) {
            for (int i = 0; i < libraryLights.getlightCount(); i++) {
                processLight(libraryLights.getlightAt(i));
            }
        }
    }

    /**
     * @param light
     * @throws Exception
     */
    private void processLight(lightType light) throws Exception {
        technique_commonType4 common = light.gettechnique_common();
        Light l = null;
        if (common.hasdirectional()) {
            l = new DirectionalLight();
            l.setDiffuse(getLightColor(common.getdirectional().getcolor()));
        } else if (common.haspoint()) {
            l = new PointLight();
            l.setDiffuse(getLightColor(common.getpoint().getcolor()));
            l.setAttenuate(true);
            l.setConstant(Float.parseFloat(common.getpoint()
                    .getconstant_attenuation().getValue().toString()));
            l.setLinear(Float.parseFloat(common.getpoint()
                    .getlinear_attenuation().getValue().toString()));
            l.setQuadratic(Float.parseFloat(common.getpoint()
                    .getquadratic_attenuation().getValue().toString()));
        } else if (common.hasspot()) {
            l = new SpotLight();
            l.setDiffuse(getLightColor(common.getspot().getcolor()));
            l.setAttenuate(true);
            l.setConstant(Float.parseFloat(common.getspot()
                    .getconstant_attenuation().getValue().toString()));
            l.setLinear(Float.parseFloat(common.getspot()
                    .getlinear_attenuation().getValue().toString()));
            l.setQuadratic(Float.parseFloat(common.getspot()
                    .getquadratic_attenuation().getValue().toString()));
            ((SpotLight) l).setAngle(Float.parseFloat(common.getspot()
                    .getfalloff_angle().getValue().toString()));
            ((SpotLight) l).setExponent(Float.parseFloat(common.getspot()
                    .getfalloff_exponent().getValue().toString()));
        }
        if (l != null) {
            l.getSpecular().set(0, 0, 0, 1);
            if (common.hasambient()) {
                l.setAmbient(getLightColor(common.getambient().getcolor()));
            } else {
                l.getAmbient().set(0, 0, 0, 1);
            }
            l.setEnabled(true);
            LightNode lightNode = new LightNode(light.getid().toString());
            lightNode.setLight(l);
            if (lightNodeNames == null) {
                lightNodeNames = new ArrayList<String>();
            }
            lightNodeNames.add(lightNode.getName());
            put(lightNode.getName(), lightNode);
        }
    }

    /**
     * getLightColor
     *
     * @param color
     * @return c
     */
    private ColorRGBA getLightColor(TargetableFloat3 color) {
        StringTokenizer st = new StringTokenizer(color.getValue().toString());
        return new ColorRGBA(Float.parseFloat(st.nextToken()), Float
                .parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()),
                1);
    }

    /**
     * processScene finalizes the model node to be returned as the COLLADA
     * model. This looks up visual scene instances that were placed in the
     * resource library previously.
     *
     * @param scene
     *            the scene to process
     * @throws Exception
     *             thrown if there is an error processing the xml.
     */
    public void processScene(sceneType scene) throws Exception {
        if (scene.hasinstance_visual_scene()) {
            for (int i = 0; i < scene.getinstance_visual_sceneCount(); i++) {
                String key = scene.getinstance_visual_sceneAt(i).geturl()
                        .toString().substring(1);
                Node n = (Node) resourceLibrary.get(key);
                if (n != null) {
                    model.attachChild(n);
                }
            }
        }
        if (scene.hasinstance_physics_scene()) {
            for (int i = 0; i < scene.getinstance_physics_sceneCount(); i++) {
                String key = scene.getinstance_physics_sceneAt(i).geturl()
                        .toString().substring(1);
                physics_sceneType physScene = (physics_sceneType) resourceLibrary
                        .get(key);
                if (physScene != null) {
                    processPhysicsScene(physScene);
                }
            }
        }
    }

    private void processPhysicsScene(physics_sceneType physScene)
            throws Exception {
        if (physScene.hasinstance_physics_model()) {
            for (int i = 0; i < physScene.getinstance_physics_modelCount(); i++) {
                instance_physics_modelType instPhysModel = physScene
                        .getinstance_physics_modelAt(i);
                String key = instPhysModel.geturl().toString().substring(1);
                physics_modelType physModel = (physics_modelType) resourceLibrary
                        .get(key);
                if (physModel != null) {
                    processPhysicsModel(physModel);
                }
                if (instPhysModel.hasinstance_rigid_body()) {
                    // get the Spatial that is the collision mesh
                    String rigidBodyKey = instPhysModel
                            .getinstance_rigid_body().getbody().toString();
                    Spatial collisionMesh = (Spatial) resourceLibrary
                            .get(rigidBodyKey);
                    if (collisionMesh != null) {
                        // get the target
                        String targetKey = instPhysModel
                                .getinstance_rigid_body().gettarget()
                                .toString().substring(1);
                        Node n = (Node) resourceLibrary.get(targetKey);
                        if (n != null) {
                            n.setUserData("COLLISION", collisionMesh);
                        }
                    }
                }
            }
        }
    }

    private void processPhysicsModel(physics_modelType physModel)
            throws Exception {
        // we only care about the shape (which for now will only reference a
        // geometry), so simply store this geometry with the name of the rigid
        // body as the key. Initially, this only supports a single shape per
        // physics model. Will be enhanced first available chance.
        if (physModel.hasrigid_body()) {
            for (int i = 0; i < physModel.getrigid_bodyCount(); i++) {
                rigid_bodyType rigidBody = physModel.getrigid_bodyAt(i);
                String id = rigidBody.getsid().toString();
                if (rigidBody.hastechnique_common()) {
                    if (rigidBody.gettechnique_common().hasshape()) {
                        for (int j = 0; j < rigidBody.gettechnique_common()
                                .getshapeCount(); j++) {
                            shapeType2 shape = rigidBody.gettechnique_common()
                                    .getshapeAt(j);
                            if (shape.hasinstance_geometry()) {
                                String key = shape.getinstance_geometry()
                                        .geturl().toString().substring(1);
                                Spatial s = (Spatial) resourceLibrary.get(key);
                                if (s != null) {
                                    put(id, s);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * processAssetInformation will store the information about the collada file
     * for future reference. This will include the author, the tool used, the
     * revision, the unit information, and the defined up axis.
     *
     * @param asset
     *            the assetType for the root of the model.
     */
    private void processAssetInformation(assetType asset) throws Exception {
        if (asset.hascontributor()) {
            if (asset.getcontributor().hasauthor()) {
                modelAuthor = asset.getcontributor().getauthor().toString();
            }
            if (asset.getcontributor().hasauthoring_tool()) {
                tool = asset.getcontributor().getauthoring_tool().toString();
            }
        }
        if (asset.hasrevision()) {
            revision = asset.getrevision().toString();
        }
        if (asset.hasunit()) {
            unitName = asset.getunit().getname().toString();
            unitMeter = asset.getunit().getmeter().floatValue();
        }
        if (asset.hasup_axis()) {
            upAxis = asset.getup_axis().getValue();
        }
    }

    /**
     * processAnimationLibrary will store the individual
     * BoneAnimationControllers in the resource library for future use.
     * Animations at this level can be considered top level animations that
     * should be called from this level. These animations may contain children
     * animations the top level animation is responsible for calling.
     *
     * @param animLib
     *            the library of animations to parse.
     */
    private void processAnimationLibrary(library_animationsType animLib)
            throws Exception 
    {
        if (animLib.hasanimation()) {
            // create an animation group
            ColladaAnimationGroup animGroup = new ColladaAnimationGroup(name);
            
            // add all animations to the group
            for (int i = 0; i < animLib.getanimationCount(); i++) {
                animationType anim = animLib.getanimationAt(i);
                animGroup.addAnimation(processAnimation(anim));
            }
            
            // add the group to the root node
            model.addAnimationGroup(animGroup);
        }
    }

    /**
     * the animation element catgorizes an animation hierarchy with each
     * controller defining the animation's keyframe and sampler functions. These
     * interact on single bones, where a collection of controllers will build up
     * a complete animation.
     *
     * @param animation
     *            the animation to parse.
     * @return a ColladaAnimation object
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private ColladaAnimation processAnimation(animationType animation)
            throws Exception 
    {
        // process sources
        Map<String, Source<?>> sources = new LinkedHashMap<String, Source<?>>();
        if (animation.hassource()) {
            for (int i = 0; i < animation.getsourceCount(); i++) {
                Source<?> source = getSource(animation.getsourceAt(i));
                sources.put(source.getId(), source);
            }
        }
        
        String targetNode = null;
        String targetSid = null;
        String targetProperty = null;
        
        // parse the channel identifier to find the target for this animation
        if (animation.haschannel()) {
            String target = animation.getchannel().gettarget().toString();
            targetNode = target;
            
            int slashIdx = target.indexOf("/");
            if (slashIdx != -1) {
                targetNode = target.substring(0, slashIdx);
                targetSid = target.substring(slashIdx + 1);
                
                int dotIdx = targetSid.indexOf(".");
                if (dotIdx != -1) {
                    targetProperty = targetSid.substring(dotIdx + 1);
                    targetSid = targetSid.substring(0, dotIdx);
                }
            }
        }
        
        // create the animation object
        ColladaAnimation anim = new ColladaAnimation(targetNode, targetSid,
                                                     targetProperty);
        
        // handle keyframes
        if (animation.hassampler() && animation.getsampler().hasinput()) {
            // find the input and output sources
            Source<Float> input = null;
            Source<Float> output = null;
           
            for (int i = 0; i < animation.getsampler().getinputCount(); i++) {
                InputLocal il = animation.getsampler().getinputAt(i);
                String sourceId = il.getsource().getValue();
                if (sourceId.startsWith("#")) {
                    sourceId = sourceId.substring(1);
                }
                
                if (il.getsemantic().getValue().equals("INPUT")) {
                    input = (Source<Float>) sources.get(sourceId);
                    if (input == null) {
                        logger.warning("Unable to find input " + name);
                    }
                    if (input.getStride() != 1) {
                        logger.warning("Input stride must be 1");
                        input = null;
                    }
                } else if (il.getsemantic().getValue().equals("OUTPUT")) {
                    output = (Source<Float>) sources.get(sourceId);
                    if (output == null) {
                        logger.warning("Unable to find output " + name);
                    }
                }
            }
            
            // now turn input and output into keyframes
            if (input != null && output != null) {
                for (int i = 0; i < input.getCount(); i++) {
                    float in = input.get(i, 0);
                    
                    float[] out = new float[output.getStride()];
                    for (int j = 0; j < output.getStride(); j++) {
                        out[j] = output.get(i, j);
                    }
                    
                    anim.addKeyframe(in, out);
                }
            }
        }
            
        // if the animation has children attach them
        if (animation.hasanimation()) {
            for (int i = 0; i < animation.getanimationCount(); i++) {
                anim.addChild(processAnimation(animation.getanimationAt(i)));
            }
        }
        
        return anim;
    }

    private void processCameraLibrary(library_camerasType libraryCam)
            throws Exception {
        if (libraryCam.hascamera()) {
            for (int i = 0; i < libraryCam.getcameraCount(); i++) {
                // processCamera(libraryCam.getcameraAt(i));
            }
        }
    }

    private void processCamera(cameraType camera) throws Exception {
        opticsType optics = camera.getoptics();
        technique_commonType2 common = optics.gettechnique_common();
        Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        int width = r.getWidth();
        int height = r.getHeight();
        // FIXME: THIS LINE IS SUPPOSED TO ONLY BE DONE IN A GL THREAD.
        Camera c = r.createCamera(width, height);
        float near = c.getFrustumNear();
        float far = c.getFrustumFar();
        float aspect = (float) width / (float) height;
        if (common.hasorthographic()) {
            orthographicType ortho = common.getorthographic();
            float xmag = 1.0f;
            float ymag = 1.0f;
            if (ortho.hasznear()) {
                near = Float.parseFloat(ortho.getznear().getValue().toString());
            }
            if (ortho.haszfar()) {
                far = Float.parseFloat(ortho.getzfar().getValue().toString());
            }
            if (ortho.hasxmag() && ortho.hasymag()) {
                xmag = Float.parseFloat(ortho.getxmag().getValue().toString());
                ymag = Float.parseFloat(ortho.getymag().getValue().toString());
            } else {
                if (ortho.hasaspect_ratio()) {
                    aspect = Float.parseFloat(ortho.getaspect_ratio()
                            .getValue().toString());
                }
                if (ortho.hasxmag()) {
                    assert (!ortho.hasymag());
                    xmag = Float.parseFloat(ortho.getxmag().getValue()
                            .toString());
                    ymag = xmag / aspect;
                } else {
                    assert (ortho.hasymag());
                    ymag = Float.parseFloat(ortho.getymag().getValue()
                            .toString());
                    xmag = ymag * aspect;
                }
            }
            c.setParallelProjection(true);
            c.setFrustum(near, far, -xmag, xmag, -ymag, ymag);
        } else {
            assert (common.hasperspective());
            perspectiveType persp = common.getperspective();
            float xfov = 1.0f;
            float yfov = 1.0f;
            if (persp.hasznear()) {
                near = Float.parseFloat(persp.getznear().getValue().toString());
            }
            if (persp.haszfar()) {
                far = Float.parseFloat(persp.getzfar().getValue().toString());
            }
            if (persp.hasxfov() && persp.hasyfov()) {
                xfov = Float.parseFloat(persp.getxfov().getValue().toString());
                yfov = Float.parseFloat(persp.getyfov().getValue().toString());
            } else {
                if (persp.hasaspect_ratio()) {
                    aspect = Float.parseFloat(persp.getaspect_ratio()
                            .getValue().toString());
                }
                if (persp.hasxfov()) {
                    assert (!persp.hasyfov());
                    xfov = Float.parseFloat(persp.getxfov().getValue()
                            .toString());
                    yfov = xfov / aspect;
                } else {
                    assert (persp.hasyfov());
                    yfov = Float.parseFloat(persp.getyfov().getValue()
                            .toString());
                    xfov = yfov * aspect;
                }
            }
            c.setParallelProjection(false);
            c.setFrustumPerspective(yfov, aspect, near, far);
        }
        if (cameraNodeNames == null) {
            cameraNodeNames = new ArrayList<String>();
        }
        CameraNode nodeCamera = new CameraNode(camera.getid().toString(), c);
        // cameras are odd in that their rotation is typically exported
        // backwards from the direction that they're looking in the scene
        if ("X_UP".equals(upAxis))
            nodeCamera.setLocalRotation(new Quaternion(1, 0, 0, 0));
        else if ("Y_UP".equals(upAxis))
            nodeCamera.setLocalRotation(new Quaternion(0, 1, 0, 0));
        else if ("Z_UP".equals(upAxis))
            nodeCamera.setLocalRotation(new Quaternion(0, 0, 1, 0));
        cameraNodeNames.add(nodeCamera.getName());
        put(nodeCamera.getName(), nodeCamera);
    }

    /**
     * processImageLibrary will build a collection of image filenames. The image
     * tag contains the full directory path of the image from the artists
     * working directory. Therefore, the directory will be stripped off leaving
     * only the filename. This filename will be associated with a id key that
     * can be obtained by the material that wishes to make use of it.
     *
     * @param libraryImg
     *            the library of images (name/image pair).
     */
    private void processImageLibrary(library_imagesType libraryImg)
            throws Exception {
        if (libraryImg.hasimage()) {
            for (int i = 0; i < libraryImg.getimageCount(); i++) {
                processImage(libraryImg.getimageAt(i));
            }
        }
    }

    /**
     * processImage takes an image type and places the necessary information in
     * the resource library.
     *
     * @param image
     *            the image to process.
     * @throws Exception
     *             thrown if there is a problem with the imagetype.
     */
    private void processImage(imageType image) throws Exception {
        if (image.hasdata()) {
            if (!squelch && !reportedRawDataImages) {
                logger.warning("Raw data images not supported.");
                reportedRawDataImages = true;
            }
        }
        if (image.hasinit_from()) {
            put(image.getid().toString(), image.getinit_from().toString());
        }
    }

    /**
     * processMaterialLibrary will build a collection (Map) of MaterialStates,
     * with the defined material id as the key in the Map. This map and
     * corresponding key will then be used to apply materials to the appropriate
     * node. The library only defines the id of the material and the url of the
     * instance effect that defines its qualities, it won't be until the
     * library_effects tag is processed that the material state information is
     * filled in.
     *
     * @param libraryMat
     *            the material library type.
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private void processMaterialLibrary(library_materialsType libraryMat)
            throws Exception {
        if (libraryMat.hasmaterial()) {
            for (int i = 0; i < libraryMat.getmaterialCount(); i++) {
                processMaterial(libraryMat.getmaterialAt(i));
            }
        }
    }

    /**
     * process Material which typically contains an id and a reference URL to an
     * effect.
     *
     * @param mat
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private void processMaterial(materialType mat) throws Exception {
        ColladaMaterial material = new ColladaMaterial();
        String url = null;
        if (mat.hasinstance_effect()) {
            url = mat.getinstance_effect().geturl().toString();
            if (url.startsWith("#")) {
                url = url.substring(1);
            }
            put(url, material);
            put(mat.getid().toString(), url);
        }
        if (mat.hasextra()) {
            ExtraPluginManager.processExtra(material, mat.getextra());
        }
    }

    /**
     * processEffects will build effects as defined by the techinque. The
     * appropriate render state will be obtained from the materialMap hashmap
     * based on the the name of the effect. Currently, the id of the effect is
     * ignored as it is directly tied to the material id. However, in the future
     * this may require support.
     *
     * @param libraryEffects
     *            the library of effects to build.
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private void processEffects(library_effectsType libraryEffects)
            throws Exception {
        if (libraryEffects.haseffect()) {
            for (int i = 0; i < libraryEffects.geteffectCount(); i++) {
                String key = libraryEffects.geteffectAt(i).getid().toString();
                ColladaMaterial mat = (ColladaMaterial) resourceLibrary
                        .get(key);
                if (mat != null) {
                    fillMaterial(libraryEffects.geteffectAt(i), mat);
                }
            }
        }
    }

    /**
     * fillMaterial will use the provided effectType to generate the material
     * setting for the collada model. The effect can handle both programmable
     * pipelines and fixed pipelines. This is defined by what sort of profile it
     * is using (profile_COMMON, profile_GLSL, profile_CG). Currently,
     * profile_CG is ignored. There may be multiple profiles, describing a path
     * of fallbacks. Currently, only one profile will be supported at a time.<br>
     * <br>
     * There is a possibility that each profile may have multiple techniques,
     * defining different materials for different situations, i.e. LOD. This
     * version of the loader will assume a single technique.
     *
     * @param effect
     *            the collada effect to process.
     * @param mat
     *            the ColladaMaterial that will hold the RenderStates needed to
     *            express this material.
     * @throws Exception
     *             thrown if there is a problem processing the file.
     */
    private void fillMaterial(effectType effect, ColladaMaterial mat)
            throws Exception {
        // process the fixed pipeline information
        if (effect.hasprofile_COMMON()) {
            for (int i = 0; i < effect.getprofile_COMMON().getnewparamCount(); i++) {
                processNewParam(effect.getprofile_COMMON().getnewparamAt(i),
                        mat);
            }
            for (int i = 0; i < effect.getprofile_COMMON().gettechniqueCount(); i++) {
                processTechniqueCOMMON(effect.getprofile_COMMON()
                        .gettechniqueAt(i), mat);
            }
            if (effect.getprofile_COMMON().hasextra()) {
                for (int i = 0; i < effect.getprofile_COMMON().getextraCount(); i++) {
                    ExtraPluginManager.processExtra(mat, effect
                            .getprofile_COMMON().getextraAt(i));
                }
            }
        }
        // process the programmable pipeline
        // profile_GLSL defines all of OpenGL states as well as GLSL shaders.
        if (effect.hasprofile_GLSL()) {
            // Get the shader code
            for (int i=0; i<effect.getprofile_GLSL().getcodeCount(); i++) {
                String shader = effect.getprofile_GLSL().getcodeAt(i).getsid().getValue();
                String code = effect.getprofile_GLSL().getcodeAt(i).getValue().getValue();
                //System.out.println("Shader: " + shader);
                //System.out.println("Code: " + code);
                mat.putGLSLCode(shader, code);
            }

            for (int i=0; i<effect.getprofile_GLSL().getnewparamCount(); i++) {
                processGLSLnewparam(effect.getprofile_GLSL().getnewparamAt(i), mat);
            }
            
            for (int i = 0; i < effect.getprofile_GLSL().gettechniqueCount(); i++) {
                processTechniqueGLSL(
                        effect.getprofile_GLSL().gettechniqueAt(i), mat);
            }
        }
    }

    class BoolVals {
        int numVals = 0;
        boolean val1 = false;
        boolean val2 = false;
        boolean val3 = false;
        boolean val4 = false;
    }

    class FloatVals {
        int numVals = 0;
        float val1 = 0.0f;
        float val2 = 0.0f;
        float val3 = 0.0f;
        float val4 = 0.0f;
    }

    class IntVals {
        int numVals = 0;
        int val1 = 0;
        int val2 = 0;
        int val3 = 0;
        int val4 = 0;
    }

    class MatVals {
        int numVals = 0;
        Matrix3f val1 = new Matrix3f();
        Matrix4f val2 = new Matrix4f();
    }

    class SamplerVals {
        String surface = null;
        MinificationFilter minFilter = null;
        MagnificationFilter magFilter = null;
    }

    void processGLSLnewparam(glsl_newparam param, ColladaMaterial mat) {
        try {
            //System.out.println("Processing: " + param.getsid());

            if (param.hasbool()) {
                //System.out.println("Bool: " + param.getbool());
                BoolVals vals = new BoolVals();
                vals.numVals = 1;
                vals.val1 = param.getbool().booleanValue();
                mat.putGLSLParam(param.getsid().getValue(), vals);
            } else if (param.hasfloat2()) {
                //System.out.println("Float2: " + param.getfloat2());
                FloatVals vals = new FloatVals();
                vals.numVals = 1;
                vals.val1 = param.getfloat2().floatValue();
                mat.putGLSLParam(param.getsid().getValue(), vals);
            } else if (param.hasfloat22()) {
                //System.out.println("Float22: " + param.getfloat22());
                FloatVals vals = new FloatVals();
                vals.numVals = 2;
                String[] strs = param.getfloat22().toString().split("\\ ");
                vals.val1 = Float.parseFloat(strs[0]);
                vals.val2 = Float.parseFloat(strs[1]);
                mat.putGLSLParam(param.getsid().getValue(), vals);
            } else if (param.hasfloat2x2()) {
                //System.out.println("Float2x2: " + param.getfloat2x2());
            } else if (param.hasfloat3()) {
                FloatVals vals = new FloatVals();
                vals.numVals = 3;
                String[] strs = param.getfloat3().toString().split("\\ ");
                vals.val1 = Float.parseFloat(strs[0]);
                vals.val2 = Float.parseFloat(strs[1]);
                vals.val3 = Float.parseFloat(strs[2]);
                mat.putGLSLParam(param.getsid().getValue(), vals);
                //System.out.println("Float3: " + vals.val1 + ", " + vals.val2 + ", " + vals.val3);
            } else if (param.hasfloat3x3()) {
                //System.out.println("Float3x3: " + param.getfloat3x3());
            } else if (param.hasfloat4()) {
                FloatVals vals = new FloatVals();
                vals.numVals = 4;
                String[] strs = param.getfloat4().toString().split("\\ ");
                vals.val1 = Float.parseFloat(strs[0]);
                vals.val2 = Float.parseFloat(strs[1]);
                vals.val3 = Float.parseFloat(strs[2]);
                vals.val4 = Float.parseFloat(strs[3]);
                mat.putGLSLParam(param.getsid().getValue(), vals);
                //System.out.println("Float4: " + vals.val1 + ", " + vals.val2 + ", " + vals.val3 + ", " + vals.val4);
            } else if (param.hasfloat4x4()) {
                //System.out.println("Float4x4: " + param.getfloat4x4());
            } else if (param.hasint2()) {
                //System.out.println("Int2: " + param.getint2());
            } else if (param.hasint22()) {
                //System.out.println("Int22: " + param.getint22());
            } else if (param.hasint3()) {
                //System.out.println("Int3: " + param.getint3());
            } else if (param.hasint4()) {
                //System.out.println("Int4: " + param.getint4());
            } else if (param.hassampler1D()) {
            } else if (param.hassampler2D()) {
                //System.out.println("Sampler2D: " + param.getsid());
                processSampler2D(param.getsid().toString(), param.getsampler2D(),
                    mat);
                SamplerVals sv = new SamplerVals();
                sv.surface = param.getsampler2D().getsource().toString();
                sv.minFilter = mat.getMinFilterConstant();
                sv.magFilter = mat.getMagFilterConstant();
                mat.putGLSLParam(param.getsid().getValue(), sv);
            } else if (param.hassampler3D()) {
            } else if (param.hassamplerCUBE()) {
            } else if (param.hassamplerDEPTH()) {
            } else if (param.hassurface()) {
                //System.out.println("Surface: " + param.getsid());
                processSurface(param.getsid().toString(), param.getsurface());
            }
        } catch (java.lang.Exception e) {
            System.out.println(e);
        }
    }

    /**
     * processNewParam sets specific properties of a material (surface
     * properties, sampler properties, etc).
     *
     * @param param
     *            the xml element of the new parameter.
     * @param mat
     *            the material to store the parameters in.
     * @throws Exception
     *             thrown if there is a problem reading the xml.
     */
    private void processNewParam(common_newparam_type param, ColladaMaterial mat)
            throws Exception {
        if (param.hassampler2D()) {
            processSampler2D(param.getsid().toString(), param.getsampler2D(),
                    mat);
        }
        if (param.hassurface()) {
            processSurface(param.getsid().toString(), param.getsurface());
        }
    }

    /**
     * processes images information, defining the min and mag filter for
     * mipmapping.
     *
     * @param id
     *            the id on the sampler
     * @param sampler
     *            the sampler xml element.
     * @param mat
     *            the material to store the values in.
     * @throws Exception
     *             thrown if there is a problem reading the file.
     */
    private void processSampler2D(String id, fx_sampler2D_common sampler,
            ColladaMaterial mat) throws Exception {
        if (sampler.hasmagfilter()) {
            mat.setMagFilterConstant(sampler.getmagfilter().getValue());
        }
        if (sampler.hasminfilter()) {
            mat.setMinFilterConstant(sampler.getminfilter().getValue());
        }
        
        mat.setWrapSConstant(WrapMode.Repeat);
        mat.setWrapTConstant(WrapMode.Repeat);

        put(id, sampler.getsource().getValue());
    }

    private void processSurface(String id, fx_surface_common surface)
            throws Exception {
        put(id, surface.getinit_from().getValue().toString());
    }

    /**
     * processes rendering information defined to be GLSL standard, which
     * includes all OpenGL state information and GLSL shader information.
     *
     * @param technique
     * @param mat
     * @throws Exception
     */
    private void processTechniqueGLSL(techniqueType4 technique,
            ColladaMaterial mat) throws Exception {
        if (technique.haspass()) {
            for (int i = 0; i < technique.getpassCount(); i++) {
                processPassGLSL(technique.getpassAt(i), mat);
            }
        }
    }

    private void processShaderParam(String uniform, String key, ColladaMaterial mat) {
        GLSLShaderObjectsState shader = (GLSLShaderObjectsState) mat.getState(RenderState.StateType.GLSLShaderObjects);
        TextureState textureState = (TextureState)mat.getState(RenderState.StateType.Texture);

        Object vals = mat.getGLSLParam(key);

        if (vals instanceof FloatVals) {
            FloatVals fv = (FloatVals)vals;
            if (fv.numVals == 1) {
                shader.setUniform(uniform, fv.val1);
            } else if (fv.numVals == 2) {
                shader.setUniform(uniform, fv.val1, fv.val2);
            } else if (fv.numVals == 3) {
                shader.setUniform(uniform, fv.val1, fv.val2, fv.val3);
            } else if (fv.numVals == 4) {
                shader.setUniform(uniform, fv.val1, fv.val2, fv.val3, fv.val4);
            }
        } else if (vals instanceof BoolVals) {

        } else if (vals instanceof IntVals ) {

        } else if (vals instanceof MatVals) {

        } else if (vals instanceof SamplerVals) {
            SamplerVals sv = (SamplerVals)vals;

            if (textureState == null) {
                textureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
                textureState.setEnabled(true);
                mat.setState(textureState);
            }
            mat.setWrapSConstant(WrapMode.Repeat);
            mat.setWrapTConstant(WrapMode.Repeat);
            mat.setMinFilterConstant(sv.minFilter);
            mat.setMagFilterConstant(sv.magFilter);
            String imageName = (String) resourceLibrary.get(sv.surface);
            String filename = (String) resourceLibrary.get(imageName);
            //System.out.println("Texture " + textureIndex + ": " + uniform);
            shader.setUniform(uniform, textureIndex);
            loadTexture(textureState, filename, mat, textureIndex++);
        }
    }

    private void processPassGLSL(passType3 pass, ColladaMaterial mat)
            throws Exception {
        // XXX only a single pass supported currently. If multiple passes
        // XXX are defined under a profile_GLSL the states will be combined
        // XXX to a single pass. If the same render state is defined in
        // XXX different passes, the last pass will override the previous.

        if (pass.hasshader()) {
            String vertSource = null;
            String fragSource = null;
            GLSLShaderObjectsState shader = (GLSLShaderObjectsState) mat.getState(RenderState.StateType.GLSLShaderObjects);

            if (shader == null) {
                shader = DisplaySystem.getDisplaySystem().getRenderer().createGLSLShaderObjectsState();
                mat.setState(shader);
            }

            for (int i=0; i<pass.getshaderCount(); i++) {
                textureIndex = 0;
                if (pass.getshaderAt(i).getstage().toString().equals("VERTEXPROGRAM")) {
                    vertSource = pass.getshaderAt(i).getname().getsource().toString();
                } else if (pass.getshaderAt(i).getstage().toString().equals("FRAGMENTPROGRAM")) {
                    fragSource = pass.getshaderAt(i).getname().getsource().toString();
                }

                for (int j=0; j<pass.getshaderAt(i).getbindCount(); j++) {
                    processShaderParam(pass.getshaderAt(i).getbindAt(j).getsymbol().toString(), pass.getshaderAt(i).getbindAt(j).getparam().getref().toString(), mat);
                }
            }
            //System.out.println("VERTEX SHADER SOURCE: " + mat.glslCode.get(vertSource));
            shader.load(mat.getGLSLCode(vertSource), mat.getGLSLCode(fragSource));
            shader.setEnabled(true);
        }

        if (pass.hasclip_plane()) {
            ClipState cs = (ClipState) mat.getState(RenderState.StateType.Clip);
            if (cs == null) {
                cs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createClipState();
                mat.setState(cs);
            }
            if (pass.getclip_plane().hasindex()
                    && pass.getclip_plane().hasvalue2()) {
                int index = pass.getclip_plane().getindex().intValue();
                StringTokenizer st = new StringTokenizer(pass.getclip_plane()
                        .getvalue2().toString());
                float[] clip = new float[4];
                for (int i = 0; i < 4; i++) {
                    clip[i] = Float.parseFloat(st.nextToken());
                }
                cs.setClipPlaneEquation(index, clip[0], clip[1], clip[2],
                        clip[3]);
            }
        }
        if (pass.hasclip_plane_enable()) {
            ClipState cs = (ClipState) mat.getState(RenderState.StateType.Clip);
            if (cs == null) {
                cs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createClipState();
                mat.setState(cs);
            }
            if (pass.getclip_plane_enable().hasindex()
                    && pass.getclip_plane_enable().hasvalue2()) {
                int index = pass.getclip_plane().getindex().intValue();
                cs.setEnableClipPlane(index, pass.getclip_plane_enable()
                        .getvalue2().booleanValue());
            }
        }
        if (pass.hascolor_mask()) {
            ColorMaskState cms = (ColorMaskState) mat
                    .getState(RenderState.StateType.ColorMask);
            if (cms == null) {
                cms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createColorMaskState();
                mat.setState(cms);
            }
            if (pass.getcolor_mask().hasvalue2()) {
                StringTokenizer st = new StringTokenizer(pass.getcolor_mask()
                        .getvalue2().toString());
                boolean[] color = new boolean[4];
                for (int i = 0; i < 4; i++) {
                    color[i] = Boolean.parseBoolean(st.nextToken());
                }
                cms.setRed(color[0]);
                cms.setGreen(color[1]);
                cms.setBlue(color[2]);
                cms.setAlpha(color[3]);
            }
        }
        if (pass.hasdepth_func()) {
            ZBufferState zbs = (ZBufferState) mat
                    .getState(RenderState.StateType.ZBuffer);
            if (zbs == null) {
                zbs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createZBufferState();
                mat.setState(zbs);
            }
            if (pass.getdepth_func().hasvalue2()) {
                String depth = pass.getdepth_func().getvalue2().toString();
                if ("NEVER".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.Never);
                } else if ("LESS".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.LessThan);
                } else if ("LEQUAL".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
                } else if ("EQUAL".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.EqualTo);
                } else if ("GREATER".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.GreaterThan);
                } else if ("NOTEQUAL".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.NotEqualTo);
                } else if ("GEQUAL".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.GreaterThanOrEqualTo);
                } else if ("ALWAYS".equals(depth)) {
                    zbs.setFunction(ZBufferState.TestFunction.Always);
                }
            }
        }
        if (pass.hasdepth_mask()) {
            ZBufferState zbs = (ZBufferState) mat
                    .getState(RenderState.StateType.ZBuffer);
            if (zbs == null) {
                zbs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createZBufferState();
                mat.setState(zbs);
            }
            if (pass.getdepth_mask().hasvalue2()) {
                zbs
                        .setWritable(pass.getdepth_mask().getvalue2()
                                .booleanValue());
            }
        }
        if (pass.hasdepth_test_enable()) {
            ZBufferState zbs = (ZBufferState) mat
                    .getState(RenderState.StateType.ZBuffer);
            if (zbs == null) {
                zbs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createZBufferState();
                mat.setState(zbs);
            }
            if (pass.getdepth_test_enable().hasvalue2()) {
                zbs.setEnabled(pass.getdepth_test_enable().getvalue2()
                        .booleanValue());
            }
        }
        if (pass.hascolor_material()) {
            MaterialState ms = (MaterialState) mat
                    .getState(RenderState.StateType.Material);
            if (ms == null) {
                ms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createMaterialState();
                mat.setState(ms);
            }
            if (pass.getcolor_material().hasface()) {
                String face = pass.getcolor_material().getface().getvalue2()
                        .toString();
                if ("FRONT".equals(face)) {
                    ms.setMaterialFace(MaterialState.MaterialFace.Front);
                } else if ("BACK".equals(face)) {
                    ms.setMaterialFace(MaterialState.MaterialFace.Back);
                } else if ("FRONT_AND_BACK".equals(face)) {
                    ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
                }
            }
            if (pass.getcolor_material().hasmode()) {
                String mode = pass.getcolor_material().getmode().getvalue2()
                        .toString();
                if ("AMBIENT".equals(mode)) {
                    ms.setColorMaterial(MaterialState.ColorMaterial.Ambient);
                } else if ("EMISSION".equals(mode)) {
                    ms.setColorMaterial(MaterialState.ColorMaterial.Emissive);
                } else if ("DIFFUSE".equals(mode)) {
                    ms.setColorMaterial(MaterialState.ColorMaterial.Diffuse);
                } else if ("SPECULAR".equals(mode)) {
                    ms.setColorMaterial(MaterialState.ColorMaterial.Specular);
                } else if ("AMBIENT_AND_DIFFUSE".equals(mode)) {
                    ms.setColorMaterial(MaterialState.ColorMaterial.AmbientAndDiffuse);
                }
            }
        }
        if (pass.hasfog_color()) {
            FogState fs = (FogState) mat.getState(RenderState.StateType.Fog);
            if (fs == null) {
                fs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createFogState();
                mat.setState(fs);
            }
            if (pass.getfog_color().hasvalue2()) {
                StringTokenizer st = new StringTokenizer(pass.getfog_color()
                        .getvalue2().toString());
                float[] color = new float[4];
                for (int i = 0; i < 4; i++) {
                    color[i] = Float.parseFloat(st.nextToken());
                }
                fs.setColor(new ColorRGBA(color[0], color[1], color[2],
                        color[3]));
            }
        }
        if (pass.hasfog_density()) {
            FogState fs = (FogState) mat.getState(RenderState.StateType.Fog);
            if (fs == null) {
                fs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createFogState();
                mat.setState(fs);
            }
            if (pass.getfog_density().hasvalue2()) {
                fs.setDensity(pass.getfog_density().getvalue2().floatValue());
            }
        }
        if (pass.hasfog_enable()) {
            FogState fs = (FogState) mat.getState(RenderState.StateType.Fog);
            if (fs == null) {
                fs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createFogState();
                mat.setState(fs);
            }
            if (pass.getfog_enable().hasvalue2()) {
                fs.setEnabled(pass.getfog_enable().getvalue2().booleanValue());
            }
        }
        if (pass.hasfog_end()) {
            FogState fs = (FogState) mat.getState(RenderState.StateType.Fog);
            if (fs == null) {
                fs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createFogState();
                mat.setState(fs);
            }
            if (pass.getfog_end().hasvalue2()) {
                fs.setEnd(pass.getfog_end().getvalue2().floatValue());
            }
        }
        if (pass.hasfog_mode()) {
            FogState fs = (FogState) mat.getState(RenderState.StateType.Fog);
            if (fs == null) {
                fs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createFogState();
                mat.setState(fs);
            }
            if (pass.getfog_mode().hasvalue2()) {
                String mode = pass.getfog_mode().getvalue2().toString();
                if ("LINEAR".equals(mode)) {
                    fs.setDensityFunction(FogState.DensityFunction.Linear);
                } else if ("EXP".equals(mode)) {
                    fs.setDensityFunction(FogState.DensityFunction.Exponential);
                } else if ("EXP2".equals(mode)) {
                    fs.setDensityFunction(FogState.DensityFunction.ExponentialSquared);
                }
            }
        }
        if (pass.hasfog_start()) {
            FogState fs = (FogState) mat.getState(RenderState.StateType.Fog);
            if (fs == null) {
                fs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createFogState();
                mat.setState(fs);
            }
            if (pass.getfog_start().hasvalue2()) {
                fs.setStart(pass.getfog_start().getvalue2().floatValue());
            }
        }
        if (pass.hasalpha_test_enable()) {
            BlendState as = (BlendState) mat.getState(RenderState.StateType.Blend);
            if (as == null) {
                as = DisplaySystem.getDisplaySystem().getRenderer()
                        .createBlendState();
                mat.setState(as);
            }
            as.setTestEnabled(pass.getalpha_test_enable().getvalue2()
                    .booleanValue());
        }
        if (pass.hasalpha_func()) {
            BlendState as = (BlendState) mat.getState(RenderState.StateType.Blend);
            if (as == null) {
                as = DisplaySystem.getDisplaySystem().getRenderer()
                        .createBlendState();
                mat.setState(as);
            }
            if (pass.getalpha_func().hasfunc()) {
                String func = pass.getalpha_func().getfunc().getvalue2()
                        .toString();
                if ("NEVER".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.Never);
                } else if ("LESS".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.LessThan);
                } else if ("LEQUAL".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.LessThanOrEqualTo);
                } else if ("EQUAL".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.EqualTo);
                } else if ("GREATER".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.GreaterThan);
                } else if ("NOTEQUAL".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.NotEqualTo);
                } else if ("GEQUAL".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.GreaterThanOrEqualTo);
                } else if ("ALWAYS".equals(func)) {
                    as.setTestFunction(BlendState.TestFunction.Always);
                }
            }
            if (pass.getalpha_func().hasvalue2()) {
                as.setReference(pass.getalpha_func().getvalue2().getvalue2()
                        .floatValue());
            }
        }
        if (pass.hasblend_enable()) {
            BlendState as = (BlendState) mat.getState(RenderState.StateType.Blend);
            if (as == null) {
                as = DisplaySystem.getDisplaySystem().getRenderer()
                        .createBlendState();
                mat.setState(as);
            }
            as.setBlendEnabled(pass.getblend_enable().getvalue2()
                    .booleanValue());
        }
        if (pass.hasblend_func()) {
            BlendState as = (BlendState) mat.getState(RenderState.StateType.Blend);
            if (as == null) {
                as = DisplaySystem.getDisplaySystem().getRenderer()
                        .createBlendState();
                mat.setState(as);
            }
            if (pass.getblend_func().hasdest()) {
                String dest = pass.getblend_func().getdest().getvalue2()
                        .toString();
                if ("ZERO".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.Zero);
                } else if ("ONE".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.One);
                } else if ("SRC_COLOR".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.SourceColor);
                } else if ("ONE_MINUS_SRC_COLOR".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceColor);
                } else if ("SRC_ALPHA".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.SourceAlpha);
                } else if ("ONE_MINUS_SRC_ALPHA".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
                } else if ("DST_ALPHA".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.DestinationAlpha);
                } else if ("ONE_MINUS_DST_ALPHA".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusDestinationAlpha);
                } else if ("CONSTANT_COLOR".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.ConstantColor);
                } else if ("ONE_MINUS_CONSTANT_COLOR".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusConstantColor);
                } else if ("CONSTANT_ALPHA".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.ConstantAlpha);
                } else if ("ONE_MINUS_CONSTANT_ALPHA".equals(dest)) {
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusConstantAlpha);
                }
            }
            if (pass.getblend_func().hassrc()) {
                String src = pass.getblend_func().getsrc().getvalue2()
                        .toString();
                if ("ZERO".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.Zero);
                } else if ("ONE".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.One);
                } else if ("DEST_COLOR".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.DestinationColor);
                } else if ("ONE_MINUS_DEST_COLOR".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.OneMinusDestinationColor);
                } else if ("SRC_ALPHA".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
                } else if ("ONE_MINUS_SRC_ALPHA".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.OneMinusDestinationAlpha);
                } else if ("DST_ALPHA".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.DestinationAlpha);
                } else if ("ONE_MINUS_DST_ALPHA".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.OneMinusDestinationAlpha);
                } else if ("CONSTANT_COLOR".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.ConstantColor);
                } else if ("ONE_MINUS_CONSTANT_COLOR".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.OneMinusConstantColor);
                } else if ("CONSTANT_ALPHA".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.ConstantAlpha);
                } else if ("ONE_MINUS_CONSTANT_ALPHA".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.OneMinusConstantAlpha);
                } else if ("SRC_ALPHA_SATURATE".equals(src)) {
                    as.setSourceFunction(BlendState.SourceFunction.SourceAlphaSaturate);
                }
            }
        }
        if (pass.hascull_face_enable()) {
            CullState cs = (CullState) mat.getState(RenderState.StateType.Cull);
            if (cs == null) {
                cs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createCullState();
                mat.setState(cs);
            }
            cs
                    .setEnabled(pass.getcull_face_enable().getvalue2()
                            .booleanValue());
        }
        if (pass.hascull_face()) {
            CullState cs = (CullState) mat.getState(RenderState.StateType.Cull);
            if (cs == null) {
                cs = DisplaySystem.getDisplaySystem().getRenderer()
                        .createCullState();
                mat.setState(cs);
            }
            if (pass.getcull_face().hasvalue2()) {
                String face = pass.getcull_face().getvalue2().toString();
                if ("FRONT".equals(face)) {
                    cs.setCullFace(CullState.Face.Front);
                } else if ("BACK".equals(face)) {
                    cs.setCullFace(CullState.Face.Back);
                } else if ("FRONT_AND_BACK".equals(face)) {
                    cs.setCullFace(CullState.Face.FrontAndBack);
                }
            }
        }
        // Define the ShadeState (FLAT OR SMOOTH);
        if (pass.hasshade_model()) {
            ShadeState ss = (ShadeState) mat.getState(RenderState.StateType.Shade);
            if (ss == null) {
                ss = DisplaySystem.getDisplaySystem().getRenderer()
                        .createShadeState();
                mat.setState(ss);
            }
            if (pass.getshade_model().hasvalue2()) {
                String shade = pass.getshade_model().getvalue2().toString();
                if ("FLAT".equals(shade)) {
                    ss.setShadeMode(ShadeState.ShadeMode.Flat);
                } else if ("SMOOTH".equals(shade)) {
                    ss.setShadeMode(ShadeState.ShadeMode.Smooth);
                }
            }
        }
        if (pass.hasmaterial_ambient()) {
            MaterialState ms = (MaterialState) mat
                    .getState(RenderState.StateType.Material);
            if (ms == null) {
                ms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createMaterialState();
                mat.setState(ms);
            }
            if (pass.getmaterial_ambient().hasvalue2()) {
                StringTokenizer st = new StringTokenizer(pass
                        .getmaterial_ambient().getvalue2().toString());
                float[] color = new float[4];
                for (int i = 0; i < 4; i++) {
                    color[i] = Float.parseFloat(st.nextToken());
                }
                ms.setAmbient(new ColorRGBA(color[0], color[1], color[2],
                        color[3]));
            }
        }
        if (pass.hasmaterial_diffuse()) {
            MaterialState ms = (MaterialState) mat
                    .getState(RenderState.StateType.Material);
            if (ms == null) {
                ms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createMaterialState();
                mat.setState(ms);
            }
            if (pass.getmaterial_diffuse().hasvalue2()) {
                StringTokenizer st = new StringTokenizer(pass
                        .getmaterial_diffuse().getvalue2().toString());
                float[] color = new float[4];
                for (int i = 0; i < 4; i++) {
                    color[i] = Float.parseFloat(st.nextToken());
                }
                ms.setDiffuse(new ColorRGBA(color[0], color[1], color[2],
                        color[3]));
            }
        }
        if (pass.hasmaterial_emission()) {
            MaterialState ms = (MaterialState) mat
                    .getState(RenderState.StateType.Material);
            if (ms == null) {
                ms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createMaterialState();
                mat.setState(ms);
            }
            if (pass.getmaterial_emission().hasvalue2()) {
                StringTokenizer st = new StringTokenizer(pass
                        .getmaterial_emission().getvalue2().toString());
                float[] color = new float[4];
                for (int i = 0; i < 4; i++) {
                    color[i] = Float.parseFloat(st.nextToken());
                }
                ms.setEmissive(new ColorRGBA(color[0], color[1], color[2],
                        color[3]));
            }
        }
        if (pass.hasmaterial_shininess()) {
            MaterialState ms = (MaterialState) mat
                    .getState(RenderState.StateType.Material);
            if (ms == null) {
                ms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createMaterialState();
                mat.setState(ms);
            }
            if (pass.getmaterial_shininess().hasvalue2()) {
                ms.setShininess(pass.getmaterial_shininess().getvalue2()
                        .floatValue());
            }
        }
        if (pass.hasmaterial_specular()) {
            MaterialState ms = (MaterialState) mat
                    .getState(RenderState.StateType.Material);
            if (ms == null) {
                ms = DisplaySystem.getDisplaySystem().getRenderer()
                        .createMaterialState();
                mat.setState(ms);
            }
            if (pass.getmaterial_specular().hasvalue2()) {
                StringTokenizer st = new StringTokenizer(pass
                        .getmaterial_specular().getvalue2().toString());
                float[] color = new float[4];
                for (int i = 0; i < 4; i++) {
                    color[i] = Float.parseFloat(st.nextToken());
                }
                ms.setSpecular(new ColorRGBA(color[0], color[1], color[2],
                        color[3]));
            }
        }
        if (pass.hasstencil_func()) {
            StencilState ss = (StencilState) mat
                    .getState(RenderState.StateType.Stencil);
            if (ss == null) {
                ss = DisplaySystem.getDisplaySystem().getRenderer()
                        .createStencilState();
                // FIXME: This, and other if == null sections do not set new state back into mat.
            }
            if (pass.getstencil_func().hasfunc()) {
                String func = pass.getstencil_func().getfunc().toString();
                if ("NEVER".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.Never);
                } else if ("LESS".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.LessThan);
                } else if ("LEQUAL".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.LessThanOrEqualTo);
                } else if ("EQUAL".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.EqualTo);
                } else if ("GREATER".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.GreaterThan);
                } else if ("NOTEQUAL".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.NotEqualTo);
                } else if ("GEQUAL".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.GreaterThanOrEqualTo);
                } else if ("ALWAYS".equals(func)) {
                    ss.setStencilFunction(StencilState.StencilFunction.Always);
                }
            }
            if (pass.getstencil_func().hasref()) {
                ss.setStencilReference(pass.getstencil_func().getref().getvalue2()
                        .intValue());
            }
            if (pass.getstencil_func().hasmask()) {
                ss.setStencilReference(pass.getstencil_func().getmask().getvalue2()
                        .intValue());
            }
        }
        if (pass.hasstencil_op()) {
            StencilState ss = (StencilState) mat
                    .getState(RenderState.StateType.Stencil);
            if (ss == null) {
                ss = DisplaySystem.getDisplaySystem().getRenderer()
                        .createStencilState();
            }
            if (pass.getstencil_op().hasfail()) {
                ss.setStencilOpFail(evaluateStencilOp(pass.getstencil_op()
                        .getfail().toString()));
            }
            if (pass.getstencil_op().haszfail()) {
                ss.setStencilOpZFail(evaluateStencilOp(pass.getstencil_op()
                        .getzfail().toString()));
            }
            if (pass.getstencil_op().haszpass()) {
                ss.setStencilOpZPass(evaluateStencilOp(pass.getstencil_op()
                        .getzpass().toString()));
            }
        }
        if (pass.hasstencil_test_enable()) {
            StencilState ss = (StencilState) mat
                    .getState(RenderState.StateType.Stencil);
            if (ss == null) {
                ss = DisplaySystem.getDisplaySystem().getRenderer()
                        .createStencilState();
            }
            ss.setEnabled(pass.getstencil_test_enable().getvalue2()
                    .booleanValue());
        }
    }

    public StencilState.StencilOperation evaluateStencilOp(String value) {
        if ("KEEP".equals(value)) {
            return StencilState.StencilOperation.Keep;
        } else if ("ZERO".equals(value)) {
            return StencilState.StencilOperation.Zero;
        } else if ("REPLACE".equals(value)) {
            return StencilState.StencilOperation.Replace;
        } else if ("INCR".equals(value)) {
            return StencilState.StencilOperation.Increment;
        } else if ("DECR".equals(value)) {
            return StencilState.StencilOperation.Decrement;
        } else if ("INVERT".equals(value)) {
            return StencilState.StencilOperation.Invert;
        } else if ("INCR_WRAP".equals(value)) {
            return StencilState.StencilOperation.IncrementWrap;
        } else if ("DECT_WRAP".equals(value)) {
            return StencilState.StencilOperation.DecrementWrap;
        } else {
            return StencilState.StencilOperation.Keep;
        }
    }

    /**
     * processTechniqueCOMMON process a technique of techniqueType2 which are
     * defined to be returned from a profile_COMMON object. This technique
     * contains images, lambert shading, phong shading and blinn shading.
     *
     * @param technique
     *            the fixed pipeline technique.
     * @param mat
     *            the material to store the technique in.
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private void processTechniqueCOMMON(techniqueType2 technique,
            ColladaMaterial mat) throws Exception {

        techniqueCOMMONMaterialType wrapper = null;

        if (technique.haslambert()) {
            wrapper = new lambertWrapper(technique.getlambert());
        } else if (technique.hasphong()) {
            wrapper = new phongWrapper(technique.getphong());
        } else if (technique.hasblinn()) {
            wrapper = new blinnWrapper(technique.getblinn());
        }

        if (wrapper != null) {
            processTechniqueCOMMONMaterial(wrapper, mat);
        }

        if (technique.hasextra()) {
            for (int i = 0; i < technique.getextraCount(); i++) {
                ExtraPluginManager.processExtra(mat, technique.getextraAt(i));
            }
        }
    }

    private void processTechniqueCOMMONMaterial(techniqueCOMMONMaterialType pt, ColladaMaterial mat)
            throws Exception {
        // obtain the colors for the material
        MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer()
                .createMaterialState();
        boolean alphaTexture = false;
        // set the ambient color value of the material
        if (pt.hasambient() && pt.getambient().hascolor()) {
            ms.setAmbient(getColor(pt.getambient().getcolor()));
        } else {
            // JME defaults to a non-zero ambient value
            ms.setAmbient(ColorRGBA.black);
        }
        
        // set the diffuse color value of the material
        if (pt.hasdiffuse()) {
            if (pt.getdiffuse().hascolor()) {
                ms.setDiffuse(getColor(pt.getdiffuse().getcolor()));
            }
            if (pt.getdiffuse().hastexture()) {
                // create a texturestate, and we will need to make use of
                // texcoord to put this texture in the correct "unit"
                for (int i = 0; i < pt.getdiffuse().gettextureCount(); i++) {
                    mat.setState(processTexture(
                            pt.getdiffuse().gettextureAt(i), mat));
                }
            }
        } else {
            // JME defaults to non-zero diffuse
            ms.setDiffuse(ColorRGBA.black);
        }
        
        // set the emmission color value of the material
        if (pt.hasemission() && pt.getemission().hascolor()) {
            ms.setEmissive(getColor(pt.getemission().getcolor()));
        }

        // set the specular color value of the material
        if (pt.hasspecular() && pt.getspecular().hascolor()) {
            ms.setSpecular(getColor(pt.getspecular().getcolor()));
        }

        // set the shininess value of the material
        if (pt.hasshininess()) {
          float shininess = pt.getshininess().getfloat2().getValue().floatValue();
          if (shininess<0.0f || shininess>128.0f) {
              logger.warning("Shininess "+shininess+" out of range (0-128), clamping value");
              if (shininess<0.0f)
                  shininess=0f;
              else shininess=128f;
          }
          ms.setShininess(shininess);
        }

        float transparency = 1.0f;
        BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        
        if (pt.hastransparency()) {
            transparency = pt.gettransparency().getfloat2().getValue().floatValue();
        }

        if (pt.hastransparent()) {
            // refactored after consulting OpenSceneGraph implementation:
            // http://www.openscenegraph.org/projects/osg/browser/OpenSceneGraph/trunk/src/osgPlugins/dae/daeRMaterials.cpp

            ColorRGBA transparentColor = new ColorRGBA(transparency, transparency, transparency, transparency);
            if (pt.gettransparent().hascolor()) {
                transparentColor.set(getColor(pt.gettransparent().getcolor()));

                if (pt.gettransparent().hasopaque() &&
                    pt.gettransparent().getopaque().toString().equals("RGB_ZERO"))
                {
                    transparentColor.set(
                            1.0f - transparentColor.r * transparency,
                            1.0f - transparentColor.g * transparency,
                            1.0f - transparentColor.b * transparency,
                            1.0f - (transparentColor.r * 0.212671f +
                                    transparentColor.g * 0.715160f +
                                    transparentColor.b * 0.072169f) * transparency);
                } else {
                    float a = transparentColor.a * transparency;
                    transparentColor.set(a, a, a, a);
                }
            } else if (pt.gettransparent().hastexture() && !pt.getdiffuse().hastexture()) {
                // in most cases, the transparent texture and diffuse texture
                // are the same. Ignore transparent textures if the diffuse
                // texture exists (?)
                
                // create a texturestate, and we will need to make use of
                // texcoord to put this texture in the correct "unit"
                for (int i = 0; i < pt.gettransparent().gettextureCount(); i++) {
                    mat.setState(processTexture(
                            pt.gettransparent().gettextureAt(i), mat));
                }
            }

            // we only process transparency if a transparent color or texture
            // is specified, or if there is an alpha texture
            boolean transparent = pt.gettransparent().hastexture() ||
                                  !transparentColor.equals(ColorRGBA.white) ||
                                  hasAlphaTexture(mat);
            if (transparent) {
                if (pt.gettransparent().hastexture()) {
                    as.setBlendEnabled(true);
                    as.setTestEnabled(true);
                    as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
                } else {
                    as.setBlendEnabled(true);
                    as.setTestEnabled(true);
                    as.setConstantColor(transparentColor);
                    as.setSourceFunction(BlendState.SourceFunction.ConstantAlpha);
                    as.setDestinationFunction(BlendState.DestinationFunction.OneMinusConstantAlpha);
                }
                
                mat.setState(as);
                
                // add a no-write ZBuffer state
//                ZBufferState zs = 
//                        DisplaySystem.getDisplaySystem().getRenderer().createZBufferState();
//                zs.setEnabled(true);
//                zs.setWritable(false);
//                zs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
//                mat.setState(zs);
            }
        }

        mat.setState(ms);
    }

    /**
     * processTexture generates a texture state that contains the image and
     * texture coordinate unit information. This texture state is returned to be
     * placed in the Collada material.
     *
     * @param texture
     *            the texture type to process.
     * @return the generated TextureState that handles this texture tag.
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    public TextureState processTexture(textureType texture, ColladaMaterial mat)
            throws Exception 
    {
        TextureState ts = (TextureState) mat.getState(RenderState.StateType.Texture);
        if (ts == null) {
            ts = DisplaySystem.getDisplaySystem().getRenderer()
                    .createTextureState();
        }
        
        String key = texture.gettexture().toString();
        String texCoords = texture.gettexcoord().toString();
       
        int index = ts.getNumberOfSetTextures();
        
        String surfaceName = (String) resourceLibrary.get(key);
        if (surfaceName == null) {
            return null;
        }
        String imageName = (String) resourceLibrary.get(surfaceName);
        if (imageName == null) {
            // HACK for directly referenced textures generated by
            // Maya FBX exporter
            imageName = key;
        }
        String filename = (String) resourceLibrary.get(imageName);
        loadTexture(ts, filename, mat, index);
        
        // update the map from materials to textures
        mat.addTextureRef(texCoords, index);
        
        return ts;
    }
    
    /**
     * Determine if the given state has an alpha texture (and therefore
     * blending needs to be enabled
     */
    private static boolean hasAlphaTexture(ColladaMaterial mat) {
        TextureState ts = (TextureState) mat.getState(RenderState.StateType.Texture);
        if (ts != null) {
            for (int i = 0; i < TextureState.getNumberOfFixedUnits(); i++) {
                Texture t = ts.getTexture(i);
                if (t != null) {
                    Image.Format f = t.getImage().getFormat();
                    switch (f) {
                        case RGBA_TO_DXT1:
                        case RGBA_TO_DXT3:
                        case RGBA_TO_DXT5:
                        case RGBA12:
                        case RGBA16:
                        case RGBA16F:
                        case RGBA2:
                        case RGBA32F:
                        case RGBA4:
                        case RGBA8:
                            return true;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * @param ts
     * @param textureURL
     * @param filename
     */
    private void loadTexture(TextureState ts, String filename,
            ColladaMaterial mat, int index) {
        URL textureURL = ResourceLocatorTool.locateResource(
                ResourceLocatorTool.TYPE_TEXTURE, filename);

        if (textureURL != null) {
            Texture t0 = TextureManager.loadTexture(textureURL, mat
                    .getMinFilterConstant(), mat.getMagFilterConstant(),
                    Image.Format.GuessNoCompression, 0, true);

            t0.setWrap(WrapAxis.S, mat.getWrapSConstant());
            t0.setWrap(WrapAxis.T, mat.getWrapTConstant());

            ts.setTexture(t0, index);
        } else {
            if (!squelch) {
                logger.warning("Invalid or missing texture: \"" + filename
                        + "\"");
            }
        }
    }

    /**
     * Process Geometry will build a number of Geometry objects attaching them
     * to the supplied parent.
     *
     * @param geometryLibrary
     *            the geometries to process individually.
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private void processGeometry(library_geometriesType geometryLibrary)
            throws Exception {
        // go through each geometry one at a time
        for (int i = 0; i < geometryLibrary.getgeometryCount(); i++) {
            geometryType geom = geometryLibrary.getgeometryAt(i);
            if (geom.hasmesh()) {
                for (int j = 0; j < geom.getmeshCount(); j++) {
                    Spatial s = processMesh(geom.getmeshAt(j), geom);
                    put(geom.getid().toString(), s);
                    if (geometryNames == null) {
                        geometryNames = new ArrayList<String>();
                    }
                    geometryNames.add(geom.getid().toString());
                }
            }
            // splines are not currently supported.
            if (geom.hasspline()) {
                if (!squelch && !reportedSplines) {
                    logger.warning("splines not yet supported.");
                    reportedSplines = true;
                }
            }
        }
    }

    /**
     * processControllerLibrary builds a controller for each controller tag in
     * the file.
     *
     * @param controllerLibrary
     *            the controller library object to parse.
     * @throws Exception
     *             thrown if there is a problem with the loader.
     */
    private void processControllerLibrary(
            library_controllersType controllerLibrary) throws Exception {
        if (controllerLibrary.hascontroller()) {
            for (int i = 0; i < controllerLibrary.getcontrollerCount(); i++) {
                processController(controllerLibrary.getcontrollerAt(i));
            }
        }
    }

    /**
     * controllers define how one object interacts with another. Typically, this
     * is skinning and morph targets.
     *
     * @param controller
     *            the controller to process
     */
    private void processController(controllerType controller) throws Exception {
        // skin and morph are mutually exclusive.
        if (controller.hasskin()) {
            // there can only be one skin per controller
            processSkin(controller.getid().toString(), controller.getskin());
        } else if (controller.hasmorph()) {
            // more not currently supported.
        }
    }

    /**
     * processSkin builds a SkinnedMesh object that defines the vertex
     * information of a model and the skeletal system that supports it.
     *
     * @param skin
     *            the skin to process
     * @throws Exception
     *             thrown if there is a problem parsing the skin.
     */
    private void processSkin(String id, skinType skin) throws Exception {
        // find the source mesh
        String meshName = skin.getsource().getValue();
        if (meshName.startsWith("#")) {
            meshName = meshName.substring(1);
        }
        ColladaNode meshParent = (ColladaNode) resourceLibrary.get(meshName);
        if (meshParent == null) {
            logger.warning("Cannot find mesh " + meshName);
            return;
        }
        
        // read the bind shape matrix
        Matrix4f bindMatrix = new Matrix4f();
        if (skin.hasbind_shape_matrix()) {
            float[] bFloats = getFloats(16, skin.getbind_shape_matrix().getValue());
            bindMatrix.set(bFloats, true);
        }
        
        // create the controller node
        ColladaControllerNode controller = new ColladaControllerNode(id, bindMatrix);
        
        // read sources -- note the variable is called "source2" because there
        // is also the source attribute
        Map<String, Source<?>> sources = new LinkedHashMap<String, Source<?>>();
        if (skin.hassource2()) {
            for (int i = 0; i < skin.getsource2Count(); i++) {
                Source<?> source = getSource(skin.getsource2At(i));
                sources.put(source.getId(), source);
            }
        }
        
        // process joints
        if (skin.hasjoints() && skin.getjoints().hasinput()) {
            Source<String> jointSids = null;
            Source<Float> invBindMatrices = null;
            
            for (int i = 0; i < skin.getjoints().getinputCount(); i++) {
                InputLocal il = skin.getjoints().getinputAt(i);
                String semantic = il.getsemantic().getValue();
                String sourceId = il.getsource().getValue();
                if (sourceId.startsWith("#")) {
                    sourceId = sourceId.substring(1);
                }
                
                if (semantic.equals("JOINT")) {
                    jointSids = (Source<String>) sources.get(sourceId);
                    if (jointSids == null) {
                        logger.warning("Unable to find source " + sourceId);
                    }
                } else if (semantic.equals("INV_BIND_MATRIX")) {
                    invBindMatrices = (Source<Float>) sources.get(sourceId);
                    if (invBindMatrices == null) {
                        logger.warning("Unable to find source " + sourceId);
                    }
                }
            }
            
            Matrix4f invBindMatrix = new Matrix4f();
            for (int i = 0; i < jointSids.getCount(); i++) {
                if (invBindMatrices != null) {
                    invBindMatrix = getMatrixAt(invBindMatrices, i);
                }
                
                controller.addJoint(jointSids.get(i, 0), invBindMatrix);
            }
        }
        
        // process vertex weights
        if (skin.hasvertex_weights()) {
            vertex_weightsType vw = skin.getvertex_weights();
            
            int maxOffset = 0;
            int jointOffset = 0;            
            int weightOffset = 0;
            Source<Float> weights = null;
            
            if (vw.hasinput()) {
                for (int i = 0; i < vw.getinputCount(); i++) {
                    InputLocalOffset ilo = vw.getinputAt(i);
                    
                    String semantic = ilo.getsemantic().getValue();
                    String sourceId = ilo.getsource().getValue();
                    if (sourceId.startsWith("#")) {
                        sourceId = sourceId.substring(1);
                    }
                    
                    int offset = ilo.getoffset().intValue();
                    if (offset + 1 > maxOffset) {
                        maxOffset = offset + 1;
                    }
                    
                    if (semantic.equals("JOINT")) {
                        jointOffset = offset;
                    } else if (semantic.equals("WEIGHT")) {
                        weightOffset = offset;
                        weights = (Source<Float>) sources.get(sourceId);
                        if (weights == null) {
                            logger.warning("Unable to find source " + sourceId);
                        }
                    }
                }
            }
            
            int[] vcount = getInts(vw.getcount().intValue(),
                                   vw.getvcount().getValue());
            int[] v = getInts(vw.getv().getValue());
        
            int vIdx = 0;
            for (int i = 0; i < vcount.length; i++) {
                int count = vcount[i];
                int[] jointIndices = new int[count];
                float[] jointWeights = new float[count];
                
                for (int j = 0; j < count; j++) {
                    for (int k = 0; k < maxOffset; k++) {
                        if (k == jointOffset) {
                            jointIndices[j] = v[vIdx];
                        }
                        
                        if (k == weightOffset) {
                            jointWeights[j] = weights.get(v[vIdx], 0);
                        }
                        
                        vIdx++;
                    }
                }
                
                controller.addVertex(jointIndices, jointWeights);
            }
        }
        
        // attach children
        for (Spatial child : meshParent.getChildren()) {
            if (!(child instanceof TriMesh)) {
                logger.warning("Unexpected child type for " + child + ": " +
                               child.getClass());
                continue;
            }
            
            // find the mesh vertices mapping for this child
            int[] meshVerts = meshVertices.get((Geometry) child);
            if (meshVerts == null) {
                logger.warning("Unable to find mesh vertices for " + child);
                continue;
            }
            
            // create the skinned mesh and attach it
            ColladaSkinnedMesh mesh = new ColladaSkinnedMesh(
                    child.getName(), (TriMesh) child, meshVerts);
            controller.attachChild(mesh);
        }
        
        // add the controller to the resource library
        resourceLibrary.put(id, controller);
    }
    
    /**
     * processBindMaterial
     *
     * @param material
     * @param spatial
     * @throws Exception
     *             the matrix to parse.
     */
    private void processBindMaterial(bind_materialType material,
            Spatial geomBindTo) throws Exception {
        technique_commonType common = material.gettechnique_common();
        for (int i = 0; i < common.getinstance_materialCount(); i++) {
            processInstanceMaterial(common.getinstance_materialAt(i),
                    geomBindTo);
        }
    }

    /**
     * processMesh will create either lines or a TriMesh. This means that the
     * only supported child elements are: triangles and lines or linestrips.
     * Polygons, trifans and tristrips are ignored.
     *
     * @param mesh
     *            the mesh to parse.
     * @param geom
     *            the geometryType of the Geometry to build.
     * @return the created Geometry built from the mesh data.
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private Spatial processMesh(meshType mesh, geometryType geom)
            throws Exception 
    {    
        Map<String, Source<?>> sources = new LinkedHashMap<String, Source<?>>();
        Vertices vertices = null;
        
        Node parentNode = new ColladaNode(geom.getid().toString());
        
        // we need to build all the source data objects.
        for (int i = 0; i < mesh.getsourceCount(); i++) {
            Source<?> source = getSource(mesh.getsourceAt(i));
            sources.put(source.getId(), source);
        }
        
        // next we have to define what source defines the vertices positional
        // information
        if (mesh.hasvertices() && mesh.getvertices().hasinput()) {
            vertices = new Vertices(mesh.getvertices().getid().toString());
            
            for (int i = 0; i < mesh.getvertices().getinputCount(); i++) {
                InputLocal input = mesh.getvertices().getinputAt(i);
                
                Semantic semantic = Semantic.valueOf(input.getsemantic().toString());
                String sourceId = input.getsource().toString();
                if (sourceId.startsWith("#")) {
                    sourceId = sourceId.substring(1);
                }
                
                Source<Float> source = (Source<Float>) sources.get(sourceId);
                if (source == null) {
                    throw new IllegalArgumentException("Unable to find source " +
                            sourceId);
                }
                
                vertices.addInput(new VerticesInput(semantic, source));
            }
        }
        
        // collect all the meshes of each type
        List<MeshTypeWrapper> meshes = new LinkedList<MeshTypeWrapper>();
        
        for (int i = 0; i < mesh.gettrianglesCount(); i++) {
            meshes.add(new TrianglesMeshType(mesh.gettrianglesAt(i)));
        }
        
        for (int i = 0; i < mesh.getlinesCount(); i++) {
            meshes.add(new LinesMeshType(mesh.getlinesAt(i)));
        }
        
        for (int i = 0; i < mesh.getpolylistCount(); i++) {
            meshes.add(new PolylistMeshType(mesh.getpolylistAt(i)));
        }
        
        // now hook up the inputs and process each mesh
        for (MeshTypeWrapper wrapper : meshes) {
            Geometry spatial = wrapper.createGeometry();
            
            if (wrapper.getMaterial() != null) {
                // do not set up materials here -- this has to wait until the
                // binding has happened in processInstanceMaterials()
                spatial.setName(geom.getid().toString() + "-" + wrapper.getMaterial());
                
                if (spatial instanceof ColladaGeometry) {
                    ((ColladaGeometry) spatial).setMaterial(wrapper.getMaterial());
                } else {
                    logger.warning("Unable to add material to " + spatial);
                }
            } else {
                spatial.setName(geom.getid().toString());
            }
            
            // create the processor
            MeshProcessor processor = wrapper.createProcessor();
            
            // create an index buffer writer
            int vertexCount = processor.getShapeSize() * processor.getShapeCount();
            
            // OWL issue #210: some meshes have no vertices. Skip them as they
            // will cause other problems later
            if (vertexCount == 0) {
                continue;
            }
            
            IntBuffer indexBuffer = createIndexBuffer(spatial, vertexCount);
            if (indexBuffer != null) {
                processor.addInput(new IndexBufferWriter(indexBuffer));
            }
            
            // create the vertex indices array
            int[] vertexIndices = new int[vertexCount];
            meshVertices.put(spatial, vertexIndices);
            
            // parse each input and add it to the processor
            for (int i = 0; i < wrapper.getInputCount(); i++) {
                InputLocalOffset input = wrapper.getInputAt(i);
                
                Semantic semantic = Semantic.valueOf(input.getsemantic().getValue());
                String sourceId = input.getsource().getValue();
                if (sourceId.startsWith("#")) {
                    sourceId = sourceId.substring(1);
                }
                
                int offset = 0;
                if (input.hasoffset()) {
                    offset = input.getoffset().intValue();
                }
                
                int set = 0;
                if (input.hasset()) {
                    offset = input.getoffset().intValue();
                }
                
                // create the correct buffer
                FloatBuffer buffer = null;
                Source<Float> source = null;
                if (semantic == Semantic.VERTEX && vertices != null && 
                    sourceId.equals(vertices.getId())) 
                {
                    // add a vertex index writer
                    processor.addInput(new VertexIndexWriter(offset, set, vertexIndices));
                    
                    // create processors for all the vertices entries
                    for (VerticesInput vi : vertices.getInputs()) {
                        buffer = createBuffer(vi.getSemantic(), vi.getSource(),
                                              spatial, vertexCount);
                        source = vi.getSource();
       
                        // create a writer from the buffer
                        processor.addInput(
                                createVertexWriter(vi.getSemantic(), offset, set,
                                                   source, buffer));
                    }
                } else {
                    source = (Source<Float>) sources.get(sourceId);                    
                    buffer = createBuffer(semantic, source, spatial, vertexCount);
                    
                    processor.addInput(
                            createVertexWriter(semantic, offset, set, 
                                               source, buffer));
                }
            }
            
            // everything is set up, go ahead an process the inputs
            processor.process();
            
            // setup bounds and attach
            spatial.setModelBound(new BoundingBox());
            parentNode.attachChild(spatial);
        }
        
        // update the parent node to handle all the newly added children
        parentNode.updateModelBound();
        return parentNode;
    }
    
    /**
     * Create an index buffer
     * @param geom the geometry to create an index buffer for
     * @param vertexCount the number of vertices
     * @return an index buffer
     */
    private IntBuffer createIndexBuffer(Geometry geom, int vertexCount) {
        IntBuffer out = BufferUtils.createIntBuffer(vertexCount);
        
        if (geom instanceof SharedMesh) {
            ((SharedMesh) geom).getTarget().setIndexBuffer(out);
        } else if (geom instanceof TriMesh) {
            ((TriMesh) geom).setIndexBuffer(out);
        } else if (geom instanceof Line) {
            ((Line) geom).setIndexBuffer(out);
        } else {
            // unknown type
            return null;
        }
        
        return out;
    }
    
    /**
     * Create an appropriate buffer for the given semantic
     * @param semantic the semantic to create a buffer for
     * @param source the source of the buffer information
     * @param geom the geometry to create the buffer for
     * @param vertexCount the number of vertices
     * @return the created buffer
     */
    private FloatBuffer createBuffer(Semantic semantic, Source<Float> source,
                                     Geometry geom, int vertexCount) 
    {
        // create buffers in the target of a shared mesh
        if (geom instanceof SharedMesh) {
            geom = ((SharedMesh) geom).getTarget();
        }
        
        int vertexSize = 3;
        switch (semantic) {
            case COLOR:
                vertexSize = 4;
                break;
            case TEXCOORD:
                vertexSize = source.getStride();
                break;
            case TEXBINORMAL:
                // not handled
                return null;
            case TEXTANGENT:
                // not handled
                return null;
        }
        
        int size = vertexCount * vertexSize;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
        
        switch (semantic) {
            case BINORMAL:
                geom.setBinormalBuffer(buffer);
                break;
            case COLOR:
                geom.setColorBuffer(buffer);
                break;
            case NORMAL:
                geom.setNormalBuffer(buffer);
                break;
            case POSITION:
                geom.setVertexBuffer(buffer);
                break;
            case TANGENT:
                geom.setTangentBuffer(buffer);
                break;
            case TEXCOORD:
                // find the first empty texture unit (should use set instead?)
                int i = 0;
                while (geom.getTextureCoords(i) != null) {
                    i++;
                }
                geom.setTextureCoords(new TexCoords(buffer, source.getStride()), i);
                break;
        }
        
        return buffer;
    }
    
    /**
     * Create a vertex writer for the given buffer and source
     * @param semantic the semantic of the source
     * @param offset the offset to write to
     * @param set the set to write to
     * @param source the source itself
     * @param buffer the buffer to write to (may be null)
     * @return a vertex writer to write to the given buffer
     */
    private VertexWriter createVertexWriter(Semantic semantic, int offset,
            int set, Source<Float> source, FloatBuffer buffer)
    {
        // handle the null case with a placeholder
        if (buffer == null) {
            return new NullVertexWriter(offset, set);
        }
        
        // handle particular types
        switch (semantic) {
            case COLOR:
                return new ColorBufferWriter(offset, set, source, buffer);
            default:
                return new FloatBufferWriter(offset, set, source, buffer);
        }
    }

    /**
     * the nodes library is a collection of nodes that can be instanced later by
     * the visual scene.
     *
     * @param type
     *            the nodes library to process.
     * @throws Exception
     *             thrown if there is a problem with the processing.
     */
    private void processNodes(library_nodesType type)
            throws Exception {
        Node tempParent = new Node("temp_parent");
        for (int i = 0; i < type.getnodeCount(); i++) {
            processNode(type.getnodeAt(i), tempParent, false);
        }
        // should all be in the resource library now.
    }

    /**
     * The library of visual scenes defines how the loaded geometry is stored in
     * the scene graph, including scaling, translation, rotation, etc.
     *
     * @param libScene
     *            the library of scenes
     * @throws Exception
     *             thrown if there is a problem processing the xml.
     */
    private void processVisualSceneLibrary(library_visual_scenesType libScene)
            throws Exception {
        for (int i = 0; i < libScene.getvisual_sceneCount(); i++) {
            ColladaNode scene = new ColladaNode(libScene.getvisual_sceneAt(i).getid()
                    .toString());
            put(scene.getName(), scene);
            processVisualScene(libScene.getvisual_sceneAt(i), scene);
        }
    }

    /**
     * the visual scene will contain any number of nodes that define references
     * to geometry. These are then placed into the scene as needed.
     *
     * @param scene
     *            the scene to process.
     * @param node
     *            the jME node to attach this scene to.
     * @throws Exception
     *             thrown if there is a problem with the processing.
     */
    private void processVisualScene(visual_sceneType scene, Node node)
            throws Exception {
        for (int i = 0; i < scene.getnodeCount(); i++) {
            processNode(scene.getnodeAt(i), node, true);
        }
    }

    /**
     * a node tag
     *
     * @param xmlNode
     * @param parent
     * @throws Exception
     */
    private void processNode(nodeType2 xmlNode, Node parent, boolean instantiateNodes) 
            throws Exception {
        String childName = null;
        String globalName = null;

        if (xmlNode.hasid()) {
            childName = xmlNode.getid().toString();
            globalName = childName;
        } else if (xmlNode.hassid()) {
            childName = xmlNode.getsid().toString();
        } else if (xmlNode.hasname()) {
            childName = xmlNode.getname().toString();
        }

        ColladaNode child = null;
        if (xmlNode.hastype() && "JOINT".equals(xmlNode.gettype().toString())
                && (xmlNode.hassid() || xmlNode.hasid())) 
        {
            String id = xmlNode.getid().getValue();
            String sid = xmlNode.getsid().getValue();
            
            if (id == null) {
                id = sid;
            }
            
            child = new ColladaJointNode(id, sid);
            
            // if the parent is not a joint, then this node is the root of
            // a skeleton
            if (!(parent instanceof ColladaJointNode) && xmlNode.hasid()) {
                skeletons.put(id, (ColladaJointNode) child);
            }
        }
        
        if (xmlNode.hasextra()) {
            for (int i = 0; i < xmlNode.getextraCount(); i++) {
                try {
                    Object o = ExtraPluginManager.processExtra(childName,
                            xmlNode.getextraAt(i));
                    if (o instanceof ColladaNode) {
                        child = (ColladaNode) o;
                    }
                } catch (Exception e) {
                    if (!squelch) {
                        logger.log(Level.WARNING, "Error processing extra information", e);
                    }
                }
            }
        }
        
        if (child == null) {
            child = new ColladaNode(childName);
        }
        
        // first parse all translations. We have to do this by dom node since
        // it is critical that they be parsed in order
        Element elem = (Element) xmlNode.getDomNode();
        NodeList elemChildren = elem.getChildNodes();
        for (int i = 0; i < elemChildren.getLength(); i++) {
            org.w3c.dom.Node elemChild = elemChildren.item(i);
            if (!(elemChild instanceof Element)) {
                continue;
            }
            
            if (elemChild.getNodeName().equals("translate")) {
                TargetableFloat3 trans = xmlNode.gettranslateValueAtCursor(elemChild);
                float[] floats = getFloats(3, trans.getValue().toString());
                 
                child.addTransform(new TranslateTransform(trans.getsid().getValue(), 
                                      new Vector3f(floats[0], floats[1], floats[2])));
                 
            } else if (elemChild.getNodeName().equals("rotate")) {
                rotateType rot = xmlNode.getrotateValueAtCursor(elemChild);
                float[] floats = getFloats(4, rot.getValue().toString());
                
                child.addTransform(new RotateTransform(rot.getsid().toString(), 
                        new Vector3f(floats[0], floats[1], floats[2]), floats[3]));
                
            } else if (elemChild.getNodeName().equals("scale")) {
                TargetableFloat3 scale = xmlNode.getscaleValueAtCursor(elemChild);
                float[] floats = getFloats(3, scale.getValue().toString());
                 
                child.addTransform(new ScaleTransform(scale.getsid().getValue(), 
                                      new Vector3f(floats[0], floats[1], floats[2])));
                
            } else if (elemChild.getNodeName().equals("matrix")) {
                matrixType mat = xmlNode.getmatrixValueAtCursor(elemChild);
                float[] floats = getFloats(16, mat.getValue().toString());
                
                // matrix from Collada is in row-major order
                Matrix4f tmat = new Matrix4f();
                tmat.set(floats, true);
                child.addTransform(new MatrixTransform(mat.getsid().toString(), tmat));
            }
            
        }
        
        parent.attachChild(child);
        
        // only add the node if it has a global identifier, otherwise we could
        // overlap with other nodes
        if (globalName != null) {
            put(globalName, child);
        }

        if (xmlNode.hasinstance_camera()) {
            for (int i = 0; i < xmlNode.getinstance_cameraCount(); i++) {
                processInstanceCamera(xmlNode.getinstance_cameraAt(i), child);
            }
        }
        // this node has a skeleton and skin
        if (xmlNode.hasinstance_controller()) {
            for (int i = 0; i < xmlNode.getinstance_controllerCount(); i++) {
                processInstanceController(xmlNode.getinstance_controllerAt(i),
                        child);
            }
        }
        if (xmlNode.hasinstance_geometry()) {
            for (int i = 0; i < xmlNode.getinstance_geometryCount(); i++) {
                processInstanceGeom(xmlNode.getinstance_geometryAt(i), child);
            }
        }
        if (xmlNode.hasinstance_node()) {
            for (int i = 0; i < xmlNode.getinstance_nodeCount(); i++) {
                if (instantiateNodes) {
                    processInstanceNode(xmlNode.getinstance_nodeAt(i), child);
                } else {
                    processDelayedInstanceNode(xmlNode.getinstance_nodeAt(i), child);
                }
            }
        }
        if (xmlNode.hasinstance_light()) {
            for (int i = 0; i < xmlNode.getinstance_lightCount(); i++) {
                processInstanceLight(xmlNode.getinstance_lightAt(i), child);
            }
        }
        
        // parse subnodes
        if (xmlNode.hasnode()) {
            for (int i = 0; i < xmlNode.getnodeCount(); i++) {
                processNode(xmlNode.getnodeAt(i), child, instantiateNodes);
            }
        }
    }

    /**
     * processInstanceCamera
     *
     * @param camera
     * @param node
     * @throws Exception
     */
    private void processInstanceCamera(InstanceWithExtra camera, Node node)
            throws Exception {
        String key = camera.geturl().toString();
        if (key.startsWith("#")) {
            key = key.substring(1);
        }
        CameraNode cn = (CameraNode) resourceLibrary.get(key);
        if (cn != null) {
            node.attachChild(cn);
        }
    }

    /**
     * processInstanceLight
     *
     * @param light
     * @param node
     * @throws Exception
     */
    private void processInstanceLight(InstanceWithExtra light, Node node)
            throws Exception {
        String key = light.geturl().toString();
        if (key.startsWith("#")) {
            key = key.substring(1);
        }
//        LightNode ln = (LightNode) resourceLibrary.get(key);
//        if (ln != null) {
//            node.attachChild(ln);
//        }
        logger.warning("Lights not supported");
    }

    /**
     * processInstanceController
     *
     * @param controller
     * @param node
     * @throws Exception
     */
    private void processInstanceController(instance_controllerType controller,
            Node node) throws Exception 
    {
        String key = controller.geturl().toString();
        if (key.startsWith("#")) {
            key = key.substring(1);
        }
        
        ColladaControllerNode inst = (ColladaControllerNode) resourceLibrary.get(key);
        if (inst != null) {
            // make a copy
            inst = (ColladaControllerNode) inst.cloneTree();
            node.attachChild(inst);
            
            if (controller.hasbind_material()) {
                processBindMaterial(controller.getbind_material(), inst);
            }
            
            if (controller.hasskeleton()) {
                for (int i = 0; i < controller.getskeletonCount(); i++) {
                    String skelName = controller.getskeletonAt(i).getValue();
                    if (skelName.startsWith("#")) {
                        skelName = skelName.substring(1);
                    }
                    
                    inst.addSkeletonName(skelName);
                }
            }
        }
    }

    /**
     * processInstanceNode
     *
     * @param instance
     * @param parent
     * @throws Exception
     */
    private void processInstanceNode(InstanceWithExtra instance, Node parent)
            throws Exception {
        String key = instance.geturl().toString();
        processInstanceNode(key, parent);
    }
    
    private void processInstanceNode(String key, Node parent) {
        if (key.startsWith("#")) {
            key = key.substring(1);
        }
        
        ColladaNode inst = (ColladaNode) resourceLibrary.get(key);
        if (inst != null) {
            // make a copy
            inst = inst.cloneTree();
            parent.attachChild(inst);
        
            // handle instantiation for any added children
            processDelayedInstances(inst);
        }
    }
    
    private void processDelayedInstances(ColladaNode parent) {
        // store list of children before we add any
        List<ColladaNode> children = new LinkedList<ColladaNode>();
        for (int i = 0; i < parent.getQuantity(); i++) {
            Spatial child = parent.getChild(i);
            if (child instanceof ColladaNode) {
                children.add((ColladaNode) child);
            }
        }
        
        // see if there are delayed instantiations for this node
        List<String> nodeList = parent.getInstanceNodes();
        if (nodeList != null) {
            for (String node : nodeList) {
                processInstanceNode(node, parent);
            }
        } 
        
        // continue walking only the original children
        for (ColladaNode child : children) {
            processDelayedInstances(child);
        }
    }
    
    private void processDelayedInstanceNode(InstanceWithExtra instance, ColladaNode parent)
            throws Exception {
        parent.addInstanceNode(instance.geturl().toString());
    }

    /**
     * processInstanceGeom
     *
     * @param geometry
     * @param node
     * @throws Exception
     */
    private void processInstanceGeom(instance_geometryType geometry, Node node)
            throws Exception {
        String key = geometry.geturl().toString();
        if (key.startsWith("#")) {
            key = key.substring(1);
        }
        
        ColladaNode inst = (ColladaNode) resourceLibrary.get(key);
        if (inst != null) {
            // make a copy of the node
            inst = inst.cloneTree();
            node.attachChild(inst);
            
            if (geometry.hasbind_material()) {
                processBindMaterial(geometry.getbind_material(), inst);
            }
        }
    }

    /**
     * processInstanceMaterial
     *
     * @param material
     * @param node
     * @throws Exception
     */
    private void processInstanceMaterial(instance_materialType material,
            Spatial geomBindTo) throws Exception {
        String key = material.gettarget().toString();
        if (key.startsWith("#")) {
            key = key.substring(1);
        }
        ColladaMaterial cm = (ColladaMaterial) resourceLibrary
                .get(resourceLibrary.get(key));
        if (cm == null) {
            // no material found!
            logger.warning("Could not find material for " + key);
            return;
        }
        
        
        Spatial target = geomBindTo;

        String symbol = material.getsymbol().toString();

        if (target instanceof Node) {
            Node targetNode = (Node) target;
            for (int i = 0; i < targetNode.getQuantity(); ++i) {
                Spatial child = targetNode.getChild(i);
                if (child instanceof ColladaGeometry &&
                        symbol.equals(((ColladaGeometry) child).getMaterial())) 
                {
                    target = child;
                    break;
                }
            }
        }
        
        // copy render states into target object
        for (RenderState.StateType type : RenderState.StateType.values()) {
            if (cm.getState(type) != null) {
                if (type == RenderState.StateType.Blend) {
                    target.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
                }
                // clone the state as different mesh's may have
                // different attributes
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    BinaryExporter.getInstance().save(cm.getState(type), out);
                    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                    RenderState rs = (RenderState) BinaryImporter.getInstance().load(in);
                    if (rs instanceof GLSLShaderObjectsState) {
                        GLSLShaderObjectsState oshader = (GLSLShaderObjectsState) cm.getState(type);
                        GLSLShaderObjectsState shader = (GLSLShaderObjectsState) rs;
                        shader.load(oshader.getVertexShader(), oshader.getFragmentShader());
                    }
                    target.setRenderState(rs);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error cloning state", e);
                }
            }
        }
        
        // update target render states based on bindings
        if (target instanceof Geometry) {
            //System.out.println("Assigning " + cm + " to " + target);
            Geometry geo = (Geometry) target;
            for (int i = 0; i < material.getbind_vertex_inputCount(); i++) {
                String attribute = material.getbind_vertex_inputAt(i).getsemantic().toString();
                String bufferName = material.getbind_vertex_inputAt(i).getinput_semantic().toString();
                boolean applyToShader = false;

                //System.out.println("Setting Attribute: " + attribute + " to " + bufferName);
                FloatBuffer fb = null;
                if (bufferName.equals("TANGENT")) {
                    fb = geo.getTangentBuffer();
                    if (geo instanceof SharedMesh) {
                        ((SharedMesh) geo).getTarget().setTangentBuffer(null);
                    } else {
                        geo.setTangentBuffer(null);
                    }
                    applyToShader = true;
                } else if (bufferName.equals("BINORMAL")) {
                    fb = geo.getBinormalBuffer();
                    if (geo instanceof SharedMesh) {
                        ((SharedMesh) geo).getTarget().setBinormalBuffer(null);
                    } else {
                        geo.setBinormalBuffer(null);
                    }
                    applyToShader = true;
                } else if (bufferName.equals("TEXCOORD")) {
                    //System.out.println("Currect TC: " + geo.getTextureCoords().get(0).coords);
                    
                    // what texture unit are the texture coordinates in?
                    int coordsUnit = 0;
                    if (material.getbind_vertex_inputAt(i).hasinput_set()) {
                        coordsUnit = material.getbind_vertex_inputAt(i).getinput_set().intValue();
                    }

                    // what texture unit is the texture image in?
                    int imageUnit = cm.getTextureRef(attribute);
                    
                    // if they are different, we need to do something. We choose
                    // to move the image into the unit corresponding to the
                    // coordinates
                    if (coordsUnit != imageUnit) {
                        TextureState ts = (TextureState) 
                                geo.getRenderState(RenderState.StateType.Texture);
                        if (ts != null) {
                            // get a reference to each texture
                            Texture it = ts.getTexture(imageUnit);
                            Texture ct = ts.getTexture(coordsUnit);
                        
                            // swap the textures
                            ts.setTexture(it, coordsUnit);
                            ts.setTexture(ct, imageUnit);
                        }
                    }
                }

                if (applyToShader) {
                    GLSLShaderObjectsState shader = (GLSLShaderObjectsState) 
                            geo.getRenderState(RenderState.StateType.GLSLShaderObjects);
                    if (shader != null && fb != null) {
                        shader.setAttributePointer(attribute, 3, false, 0, fb);
                    }
                }
            }

            MaterialState ms = (MaterialState) cm.getState(RenderState.StateType.Material);
            if (ms != null) {
                ColorRGBA diffuse = ms.getDiffuse();
                ColorRGBA c = new ColorRGBA(diffuse.r, diffuse.g, diffuse.b, diffuse.a);
                geo.setDefaultColor(c);
            }
        }        
    }
    
    /**
     * getFloats splits a string into an array of floats
     *
     * @param count
     *            the number of floats to process
     * @param floatList
     *            the string to parse
     * @return an array of floats found by splitting the string on spaces
     *            and parsing the result with Float.parseFloat()
     */
    private static float[] getFloats(int count, String floatList) {
        float[] out = new float[count];
        
        Matcher m = NOT_SPACE.matcher(floatList);
        int i = 0;
        while (m.find() && i < count) {
            out[i++] = Float.parseFloat(m.group());
        }
        
        return out;
    }
    
    /**
     * getInts splits a string into an array of ints
     *
     * @param count
     *            the number of ints to process
     * @param intList
     *            the string to parse
     * @return an array of ints found by splitting the string on spaces
     *            and parsing the result with Integer.parseInt()
     */
    private static int[] getInts(int count, String intList) {
        int[] out = new int[count];
        
        Matcher m = NOT_SPACE.matcher(intList);
        int i = 0;
        while (m.find() && i < count) {
            out[i++] = Integer.parseInt(m.group());
        }
        
        return out;
    }
    
    /**
     * getInts splits a string into an array of ints
     *
     * @param intList
     *            the string to parse
     * @return an array of ints found by splitting the string on spaces
     *            and parsing the result with Integer.parseInt()
     */
    private static int[] getInts(String intList) {
        List<Integer> l = new ArrayList<Integer>();
        
        Matcher m = NOT_SPACE.matcher(intList);
        while (m.find()) {
            l.add(Integer.valueOf(m.group()));
        }
        
        int[] out = new int[l.size()];
        for (int i = 0; i < l.size(); i++) {
            out[i] = l.get(i);
        }
        return out;
    }
    
    /**
     * getStrings splits a string into an array of strings
     * 
     * @param count
     *            the number of strings to process
     * @param stringList
     *             the string to parse
     * @return an array of strings found by splitting the string on spaces
     */
    private static String[] getStrings(int count, String strList) {
        String[] out = new String[count];
        
        // we can't use split() here because split will return an empty list
        // if there are no spaces in a one element list
        Matcher m = NOT_SPACE.matcher(strList);
        int i = 0;
        while (m.find() && i < count) {
            out[i++] = m.group();
        }
        
        return out;
    }
    
     /**
     * getBooleans splits a string into an array of booleans
     *
     * @param count
     *            the number of booleans to process
     * @param boolList
     *            the string to parse
     * @return an array of booleans found by splitting the string on spaces
     *            and parsing the result with Boolean.parseBoolean()
     */
    private static boolean[] getBooleans(int count, String boolList) {
        boolean[] out = new boolean[count];
        
        Matcher m = NOT_SPACE.matcher(boolList);
        int i = 0;
        while (m.find() && i < count) {
            out[i++] = Boolean.parseBoolean(m.group());
        }
        
        return out;
    }
    
    /**
     * Get a UV from a float source (with stride 2)
     * @param index
     *          the index of the UV to get
     * @return a Vector2f constructed from the data
     */
    private static Vector2f getUVAt(Source<Float> source, int index) {
        Vector2f out = new Vector2f();
        out.x = source.get(index, 0);
        out.y = source.get(index, 1);
        
        return out;
    }
    
    /**
     * Get a vertex from a float source (with stride 3)
     * @param index
     *          the index of the vertex to get
     * @return a Vector3f constructed from the data
     */
    private static Vector3f getVertexAt(Source<Float> source, int index) {
        Vector3f out = new Vector3f();
        out.x = source.get(index, 0);
        out.y = source.get(index, 1);
        out.z = source.get(index, 2);
        
        return out;
    }
    
    /**
     * Get a color from a float source (with stride 4)
     * @param index
     *          the index of the color to get
     * @return a color constructed from the data
     */
    private static ColorRGBA getColorAt(Source<Float> source, int index) {
        ColorRGBA out = new ColorRGBA();
        out.r = source.get(index, 0);
        out.g = source.get(index, 1);
        out.b = source.get(index, 2);
        out.a = source.get(index, 3);
        
        return out;
    }
    
    /**
     * Get a matrix from a float source (with stride 16)
     * @param index
     *          the index of the matrix to get
     * @return a matrix constructed from the data
     */
    private static Matrix4f getMatrixAt(Source<Float> source, int index) {
        Matrix4f out = new Matrix4f();
        float[] floats = new float[16];
        for (int i = 0; i < 16; i++) {
            floats[i] = source.get(index, i);
        }
        
        // data is stored in row-major order
        out.set(floats, true);
        return out;
    }
    
    /**
     * getColor uses a string tokenizer to parse the value of a colorType into a
     * ColorRGBA type used internally by jME.
     *
     * @param color
     *            the colorType to parse (RGBA format).
     * @return the ColorRGBA object to be used by jME.
     */
    private static ColorRGBA getColor(colorType color) {
        ColorRGBA out = new ColorRGBA();
        StringTokenizer st = new StringTokenizer(color.getValue().toString());
        out.r = Float.parseFloat(st.nextToken());
        out.g = Float.parseFloat(st.nextToken());
        out.b = Float.parseFloat(st.nextToken());
        out.a = Float.parseFloat(st.nextToken());
        return out;
    }
    
    /**
     * Parse a source
     */
    private static <T> Source<T> getSource(sourceType source) 
            throws Exception
    {
        // if the source has no accessors, return a dummy
        if (!source.hastechnique_common() ||
            !source.gettechnique_common().hasaccessor())
        {
            return new Source<T>(source.getid().toString(),
                                 0, 0, (T[]) new Object[0]);
        }
        
        T[] data = null;
        int count = source.gettechnique_common().getaccessor().getcount().intValue();
        int stride = source.gettechnique_common().getaccessor().getstride().intValue();
        
        if (source.hasfloat_array()) {
            data = (T[]) new Float[count * stride];
            float[] floats = getFloats(count * stride, 
                                       source.getfloat_array().getValue().toString());
            for (int i = 0; i < count * stride; i++) {
                data[i] = (T) Float.valueOf(floats[i]);
            }
        } else if (source.hasint_array()) {
            data = (T[]) new Integer[count * stride];
            int[] ints = getInts(count * stride, 
                                 source.getint_array().getValue().toString());
            for (int i = 0; i < count * stride; i++) {
                data[i] = (T) Integer.valueOf(ints[i]);
            }
        } else if (source.hasbool_array()) {
            data = (T[]) new Boolean[count * stride];
            boolean[] bools = getBooleans(count * stride, 
                                          source.getbool_array().getValue().toString());
            for (int i = 0; i < count * stride; i++) {
                data[i] = (T) Boolean.valueOf(bools[i]);
            }
        } else if (source.hasName_array()) {
            data = (T[]) getStrings(count * stride,
                                    source.getName_array().getValue().toString());
        } else if (source.hasIDREF_array()) {
            data = (T[]) getStrings(count * stride,
                                    source.getIDREF_array().getValue().toString());
        } else {
            throw new IllegalArgumentException("Unsupportd source type for " +
                                               source.getid().toString());
        }
        
        return new Source<T>(source.getid().toString(), count, stride, data);
    }

    /**
     * squelchErrors sets if the ColladaImporter should spit out errors or not
     *
     * @param b
     */
    public void squelchErrors(boolean b) {
        squelch = b;
    }

    public ThreadSafeColladaImporter getInstance() {
        return this;
    }

    /**
     * A logger that logs errors per instance of this class and also passes
     * the log messages to the global logger for this class
     */
    static class InstanceLogger {

        private LoaderErrorListener listener = null;

        public void warning(String msg) {
            globalLogger.warning(msg);
            if (listener!=null)
                listener.error(Level.WARNING, msg, null);
        }

        public void severe(String msg) {
            globalLogger.severe(msg);
            if (listener!=null)
                listener.error(Level.SEVERE, msg, null);
        }

        public void log(Level level, String msg, Throwable throwable) {
            globalLogger.log(level, msg, throwable);
            if (listener!=null)
                listener.error(level, msg, throwable);
        }

        void setErrorListener(LoaderErrorListener listener) {
            this.listener = listener;
        }
    }

    public interface LoaderErrorListener {
        /**
         * Called when the loader experiences an error. Once this callback
         * returns loading will continue to the best of the loaders ability
         *
         * @param level the severity of the error
         * @param msg the error message
         * @param throwable any associated exception, may be null
         */
        public void error(Level level, String msg, Throwable throwable);
    }

    private static class Source<T> {
        private final String id;
        private final int count;
        private final int stride;
        private final T[] data;
        
        public Source(String id, int count, int stride, T[] data) {
            this.id = id;
            this.count = count;
            this.stride = stride;
            this.data = data;
        }
        
        public String getId() {
            return id;
        }
        
        public int getCount() {
            return count;
        }
        
        public int getStride() {
            return stride;
        }
        
        // get stride elements at index
        public T get(int count, int index) {
            int idx = (count * getStride()) + index;
            return data[idx];
        }
        
        // fetch all data into an array of size stride
        public void get(int count, T[] dest) {
            int idx = (count * getStride());
            System.arraycopy(data, idx, dest, 0, count);
        }
    }

    private static class Vertices {
        private final String id;
        private final List<VerticesInput> inputs = new LinkedList<VerticesInput>();
        
        public Vertices(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        public void addInput(VerticesInput input) {
            inputs.add(input);
        }
        
        public List<VerticesInput> getInputs() {
            return inputs;
        }
    }
    
    private static class VerticesInput {
        private final Semantic semantic;
        private final Source<Float> source;
    
        public VerticesInput(Semantic semantic, Source<Float> source) {
            this.semantic = semantic;
            this.source = source;
        }
        
        public Semantic getSemantic() {
            return semantic;
        }
        
        public Source<Float> getSource() {
            return source;
        }
    }
    
    private static abstract class VertexWriter {
        private final int offset;
        private final int set;
        
        public VertexWriter(int offset, int set) {
            this.offset = offset;
            this.set = set;
        }
        
        public int getOffset() {
            return offset;
        }
        
        public int getSet() {
            return set;
        }
        
        /**
         * Write a vertex from the given indices
         * @param indices the indices
         */
        public abstract void writeVertices(int[] indices);
    }
    
    private static class FloatBufferWriter extends VertexWriter {
        private final Source<Float> source;
        private final FloatBuffer buffer;
        
        public FloatBufferWriter(int offset, int set, Source<Float> source, 
                                 FloatBuffer buffer) 
        {
            super (offset, set);
            
            this.source = source;
            this.buffer = buffer;
        }

        protected Source<Float> getSource() {
            return source;
        }
        
        protected FloatBuffer getBuffer() {
            return buffer;
        }
        
        public void writeVertices(int[] indices) {
            for (int i = 0; i < indices.length; i++) {
                for (int j = 0; j < source.getStride(); j++) {
                    buffer.put(source.get(indices[i], j));
                }
            }
        }
    }
    
    private static class ColorBufferWriter extends FloatBufferWriter {
        public ColorBufferWriter(int offset, int set, Source<Float> source,
                                 FloatBuffer buffer)
        {
            super (offset, set, source, buffer);
        }
        
        @Override
        public void writeVertices(int[] indices) {
            Source<Float> source = getSource();
            FloatBuffer buffer = getBuffer();
            
            // if the source only provides three values, we need to add an
            // alpha value
            boolean addAlpha = (source.getStride() == 3);
            
            for (int i = 0; i < indices.length; i++) {
                for (int j = 0; j < source.getStride(); j++) {
                    buffer.put(source.get(indices[i], j));
                }
                
                if (addAlpha) {
                    buffer.put(1.0f);
                }
            }
        }
    }
    
    private static class IndexBufferWriter extends VertexWriter {
        private final IntBuffer buffer;
        private int count;
        
        public IndexBufferWriter(IntBuffer buffer) {
            super (0, 0);
            
            this.buffer = buffer;
        }
        
        public void writeVertices(int[] indices) {
            for (int i = 0; i < indices.length; i++) {
                buffer.put(count);
                count++;
            }
        }
    }
    
    private static class VertexIndexWriter extends VertexWriter {
        private final int[] vertices;
        int count;
        
        public VertexIndexWriter(int offset, int set, int[] vertices) {
            super (offset, set);
            
            this.vertices = vertices;
        }

        @Override
        public void writeVertices(int[] indices) {
            for (int i = 0; i < indices.length; i++) {
                vertices[count] = indices[i];
                count++;
            }
        }        
    }
    
    private static class NullVertexWriter extends VertexWriter {
        public NullVertexWriter(int offset, int set) {
            super (offset, set);
        }
        
        public void writeVertices(int[] indices) {
            // do nothing
        }
    }
    
    private static abstract class MeshProcessor {
        private final String material;
        private final List<VertexWriter> inputs = new LinkedList<VertexWriter>();
        
        private int stride = 0;
        
        public MeshProcessor(String material) {
            this.material = material;
        }
        
        public String getMaterial() {
            return material;
        }
        
        public void addInput(VertexWriter writer) {
            inputs.add(writer);
            
            // figure out what the maximum offset value is, and set
            // the stride to that
            if (writer.getOffset() + 1 > stride) {
                stride = writer.getOffset() + 1;
            }
        }
        
        public void process() {
            while (hasMoreShapes()) {
                // get all vertices for the current shapes
                int[] shapes = nextShapes();
                int totalShapeSize = getShapeSize() * getStride();
                
                // for each individual shape
                for (int s = 0; s < shapes.length; s += totalShapeSize) {
                
                    // create an array for each offset value
                    for (int i = 0; i < getStride(); i++) { 
                        
                        // get data for a single shape
                        int[] indices = new int[getShapeSize()];
                        for (int j = 0; j < getShapeSize(); j++) {
                    
                            int index = s + (j * getStride()) + i;
                            indices[j] = shapes[index];
                        }
                    
                        // now pass the veritices into any writer at the
                        // given offset
                        for (VertexWriter writer : inputs) {
                            if (writer.getOffset() == i) {
                                writer.writeVertices(indices);
                            }
                        }
                    }
                }
            }
        }
        
        protected int getStride() {
            return stride;
        }
        
        // get the number of shapes that will be written
        public abstract int getShapeCount();
        
        // get the number of vertices per shape (ie 3 for a triangle or
        // 2 for a line)
        public abstract int getShapeSize();
        
        // return true if there are more shapes to be processed
        public abstract boolean hasMoreShapes();
        
        // get the data for the next set of shapes. The array may contain
        // data for multiple shapes, for example if a polygon is turned into
        // triangles
        public abstract int[] nextShapes();
    }
    
    private static class LinesProcessor extends MeshProcessor {
        private final int count;
        private final int[] p;
        
        private int cursor = 0;
        
        public LinesProcessor(String material, int count, int[] p) {
            super (material);
            
            this.count = count;
            this.p = p;
        }
        
        public int getShapeCount() {
            return count;
        }
        
        public int getShapeSize() {
            return 2;
        }
        
        public boolean hasMoreShapes() {
            return cursor < count;
        }
        
        public int[] nextShapes() {
            // three entries for every output
            int[] out = new int[2 * getStride()];
        
            int pIndex = cursor * 2 * getStride();
            System.arraycopy(p, pIndex, out, 0, 2 * getStride());
            
            cursor++;
            return out;
        }
    }
    
    private static class TrianglesProcessor extends MeshProcessor {
        private final int count;
        private final int[] p;
        
        private int cursor = 0;
        
        public TrianglesProcessor(String material, int count, int[] p) {
            super (material);
            
            this.count = count;
            this.p = p;
        }
        
        public int getShapeCount() {
            return count;
        }
        
        public int getShapeSize() {
            return 3;
        }
        
        public boolean hasMoreShapes() {
            return cursor < count;
        }
        
        public int[] nextShapes() {
            // three entries for every output
            int[] out = new int[3 * getStride()];
        
            int pIndex = cursor * 3 * getStride();
            System.arraycopy(p, pIndex, out, 0, 3 * getStride());
            
            cursor++;
            return out;
        }
    }
    
    private static class PolylistProcessor extends MeshProcessor {
        private final int count;
        private final int[] vcount;
        private final int[] p;
        private final int triangleCount;
        
        private int cursor = 0;
        private int pCursor = 0;
        
        public PolylistProcessor(String material, int count, int[] vcount, int[] p) {
            super (material);
            
            this.count = count;
            this.vcount = vcount;
            this.p = p;
            
            // calculate the number of triangles. For a given polygon, the
            // number of triangles is the number of vertices minux two
            int tris = 0;
            for (int i = 0; i < vcount.length; i++) {
                tris += vcount[i] - 2;
            }
            this.triangleCount = tris;
        }
        
        public int getShapeCount() {
            return triangleCount;
        }
        
        public int getShapeSize() {
            return 3;
        }
        
        public boolean hasMoreShapes() {
            return cursor < count;
        }
        
        public int[] nextShapes() {
            // we need to triangulate the polygon, so the total number of
            // entries will be 3 * trianglesPerPolygon * stride
            int verts = vcount[cursor];
            int tris = verts - 2;
            int dataSize = 3 * tris * getStride();
            int[] out = new int[dataSize];
        
            // generate each triangle
            for (int i = 0; i < tris; i++) {
                if (i == 0) {
                    // first triangle is vertices 0, 1, 2
                    System.arraycopy(p, pCursor, out, 0, 3 * getStride());
                } else {
                    // anything after the first is i+1, i+2, 0
                    int pIndex = pCursor + ((i + 1) * getStride());
                    int oIndex = i * 3 * getStride();
                    System.arraycopy(p, pIndex, out, oIndex, 2 * getStride());
                    System.arraycopy(p, pCursor, out, oIndex + (2 * getStride()), getStride());
                }
            }
            
            cursor++;
            pCursor += verts * getStride();
            return out;
        }
    }
    
    private interface MeshTypeWrapper {
        public int getCount() throws Exception;
        public String getMaterial() throws Exception;
        public int getInputCount() throws Exception;
        public InputLocalOffset getInputAt(int index) throws Exception;
        public MeshProcessor createProcessor() throws Exception;
        public Geometry createGeometry();
    }
    
    private static class TrianglesMeshType implements MeshTypeWrapper {
        private final trianglesType tris;
        
        public TrianglesMeshType(trianglesType tris) {
            this.tris = tris;
        }

        public int getCount() throws Exception {
            return tris.getcount().intValue();
        }

        public String getMaterial() throws Exception {
            if (!tris.hasmaterial()) {
                return null;
            }
            
            return tris.getmaterial().getValue();
        }

        public int getInputCount() throws Exception {
            return tris.getinputCount();
        }

        public InputLocalOffset getInputAt(int index) throws Exception {
            return tris.getinputAt(index);
        }

        public MeshProcessor createProcessor() throws Exception {
            int[] p = getInts(tris.getp().getValue());
            return new TrianglesProcessor(getMaterial(), getCount(), p);
        }
        
        public Geometry createGeometry() {
            ColladaTriMesh out = new ColladaTriMesh();
            out.setTarget(new MatrixTriMesh());
            return out;
        }
    }
    
    private static class LinesMeshType implements MeshTypeWrapper {
        private final linesType lines;
        
        public LinesMeshType(linesType lines) {
            this.lines = lines;
        }

        public int getCount() throws Exception {
            return lines.getcount().intValue();
        }

        public String getMaterial() throws Exception {
            if (!lines.hasmaterial()) {
                return null;
            }
            
            return lines.getmaterial().getValue();
        }

        public int getInputCount() throws Exception {
            return lines.getinputCount();
        }

        public InputLocalOffset getInputAt(int index) throws Exception {
            return lines.getinputAt(index);
        }

        public MeshProcessor createProcessor() throws Exception {
            int[] p = getInts(lines.getp().getValue());
            return new LinesProcessor(getMaterial(), getCount(), p);
        }
        
        public Geometry createGeometry() {
            return new ColladaLine();
        }
    }
    
    private static class PolylistMeshType implements MeshTypeWrapper {
        private final polylistType polys;
        
        public PolylistMeshType(polylistType polys) {
            this.polys = polys;
        }

        public int getCount() throws Exception {
            return polys.getcount().intValue();
        }

        public String getMaterial() throws Exception {
            if (!polys.hasmaterial()) {
                return null;
            }
            
            return polys.getmaterial().getValue();
        }

        public int getInputCount() throws Exception {
            return polys.getinputCount();
        }

        public InputLocalOffset getInputAt(int index) throws Exception {
            return polys.getinputAt(index);
        }

        public MeshProcessor createProcessor() throws Exception {
            int[] vcount = getInts(getCount(), polys.getvcount().getValue());
            int[] p = getInts(polys.getp().getValue());
            
            return new PolylistProcessor(getMaterial(), getCount(), vcount, p);
        }
        
        public Geometry createGeometry() {
            ColladaTriMesh out = new ColladaTriMesh();
            out.setTarget(new MatrixTriMesh());
            return out;
        }
    }
    
    private interface techniqueCOMMONMaterialType {
        com.jmex.xml.xml.Node getOriginal();

        boolean hasambient();
        common_color_or_texture_type getambient() throws Exception;

        boolean hasdiffuse();
        common_color_or_texture_type getdiffuse() throws Exception;

        boolean hasemission();
        common_color_or_texture_type getemission() throws Exception;

        boolean hasspecular();
        common_color_or_texture_type getspecular() throws Exception;

        boolean hasshininess();
        common_float_or_param_type getshininess() throws Exception;

        boolean hastransparency();
        common_float_or_param_type gettransparency() throws Exception;

        boolean hastransparent();
        common_transparent_type gettransparent() throws Exception;
    }

    class blinnWrapper implements techniqueCOMMONMaterialType {
        private final blinnType bt;

        public blinnWrapper(blinnType bt) {
            this.bt = bt;
        }

        public com.jmex.xml.xml.Node getOriginal() {
            return bt;
        }

        public boolean hasambient() {
            return bt.hasambient();
        }
        public common_color_or_texture_type getambient() throws Exception {
            return bt.getambient();
        }

        public boolean hasdiffuse() {
            return bt.hasdiffuse();
        }
        public common_color_or_texture_type getdiffuse() throws Exception {
            return bt.getdiffuse();
        }

        public boolean hasemission() {
            return bt.hasemission();
        }
        public common_color_or_texture_type getemission() throws Exception {
            return bt.getemission();
        }

        public boolean hasspecular() {
            return bt.hasspecular();
        }
        public common_color_or_texture_type getspecular() throws Exception {
            return bt.getspecular();
        }

        public boolean hasshininess() {
            return bt.hasshininess();
        }
        public common_float_or_param_type getshininess() throws Exception {
            return bt.getshininess();
        }

        public boolean hastransparency() {
            return bt.hastransparency();
        }
        public common_float_or_param_type gettransparency() throws Exception {
            return bt.gettransparency();
        }

        public boolean hastransparent() {
            return bt.hastransparent();
        }
        public common_transparent_type gettransparent() throws Exception {
            return bt.gettransparent();
        }
    }

    class phongWrapper implements techniqueCOMMONMaterialType {
        private final phongType pt;

        public phongWrapper(phongType pt) {
            this.pt = pt;
        }

        public com.jmex.xml.xml.Node getOriginal() {
            return pt;
        }

        public boolean hasambient() {
            return pt.hasambient();
        }
        public common_color_or_texture_type getambient() throws Exception {
            return pt.getambient();
        }

        public boolean hasdiffuse() {
            return pt.hasdiffuse();
        }
        public common_color_or_texture_type getdiffuse() throws Exception {
            return pt.getdiffuse();
        }

        public boolean hasemission() {
            return pt.hasemission();
        }
        public common_color_or_texture_type getemission() throws Exception {
            return pt.getemission();
        }

        public boolean hasspecular() {
            return pt.hasspecular();
        }
        public common_color_or_texture_type getspecular() throws Exception {
            return pt.getspecular();
        }

        public boolean hasshininess() {
            return pt.hasshininess();
        }
        public common_float_or_param_type getshininess() throws Exception {
            return pt.getshininess();
        }

        public boolean hastransparency() {
            return pt.hastransparency();
        }
        public common_float_or_param_type gettransparency() throws Exception {
            return pt.gettransparency();
        }

        public boolean hastransparent() {
            return pt.hastransparent();
        }
        public common_transparent_type gettransparent() throws Exception {
            return pt.gettransparent();
        }
    }

    class lambertWrapper implements techniqueCOMMONMaterialType {
        private final lambertType lt;

        public lambertWrapper(lambertType pt) {
            this.lt = pt;
        }

        public com.jmex.xml.xml.Node getOriginal() {
            return lt;
        }

        public boolean hasambient() {
            return lt.hasambient();
        }
        public common_color_or_texture_type getambient() throws Exception {
            return lt.getambient();
        }

        public boolean hasdiffuse() {
            return lt.hasdiffuse();
        }
        public common_color_or_texture_type getdiffuse() throws Exception {
            return lt.getdiffuse();
        }

        public boolean hasemission() {
            return lt.hasemission();
        }
        public common_color_or_texture_type getemission() throws Exception {
            return lt.getemission();
        }

        public boolean hasspecular() {
            return false;
        }
        public common_color_or_texture_type getspecular() throws Exception {
            throw new UnsupportedOperationException("No specular in lambert");
        }

        public boolean hasshininess() {
            return false;
        }
        public common_float_or_param_type getshininess() throws Exception {
            throw new UnsupportedOperationException("No shininess in lambert");
        }

        public boolean hastransparency() {
            return lt.hastransparency();
        }
        public common_float_or_param_type gettransparency() throws Exception {
            return lt.gettransparency();
        }

        public boolean hastransparent() {
            return lt.hastransparent();
        }
        public common_transparent_type gettransparent() throws Exception {
            return lt.gettransparent();
        }
    }
}
