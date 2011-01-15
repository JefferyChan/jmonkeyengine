package atmosphere;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

/**
 *
 * @author jiyarza
 */
public class PlanetRenderer {

    private static final String MESH_SPHERE = "Models/Sphere.mesh.j3o";
    private static final String MAT_GROUND_FROM_SPACE ="MatDefs/GroundFromSpace.j3md";
    private static final String MAT_GROUND_FROM_ATMOSPHERE ="MatDefs/GroundFromAtmosphere.j3md";
    private static final String MAT_SKY_FROM_SPACE ="MatDefs/SkyFromSpace.j3md";
    private static final String MAT_SKY_FROM_ATMOSPHERE ="MatDefs/SkyFromAtmosphere.j3md";

    private PlanetView main;

    private Planet planet;

    // Light position used in shaders
    private Vector3f lightPosition;

    /*
     * 3D Rendering
     */
    // Ground sphere
    private Spatial ground;
    // Outer atmosphere sphere
    private Spatial atmosphere;
    // Surface texture (diffuse1)
    private Texture t_diffuse1;
    // Surface texture (diffuse2)
    private Texture t_diffuse2;
    // Materials
    private Material mGroundFromSpace, mSkyFromSpace;
    private Material mGroundFromAtmosphere, mSkyFromAtmosphere;
    // time acc for rotation
    private float time;
    
    public PlanetRenderer(PlanetView main, Planet planet, Vector3f lightPosition) {
        this.main = main;
        this.planet = planet;
        this.lightPosition = lightPosition;
        this.time = 0.0f;
    }

    public void init() {
        // x2048
        // t_diffuse1 = main.getAssetManager().loadTexture("Textures/world.topo.bathy.200404.3x5400x2700.jpg");
        // x1024
        t_diffuse1 = main.getAssetManager().loadTexture("Textures/land_ocean_ice_2048.jpg");
        t_diffuse2 = main.getAssetManager().loadTexture("Textures/cloud_combined_2048.jpg");
        // Create materials
        createGroundFromSpaceMaterial();
        createSkyFromSpaceMaterial();
        createGroundFromAtmosphereMaterial();
        createSkyFromAtmosphereMaterial();

        // Create spatials
        createGround();
        createAtmosphere();

        update(0);

        // draw
        main.getRootNode().attachChild(ground);
        main.getRootNode().attachChild(atmosphere);
    }

    public void update(float tpf) {
        time += tpf;
        
        Vector3f cameraLocation = main.getCamera().getLocation();
        Vector3f planetToCamera = cameraLocation.subtract(planet.getPosition());
        float cameraHeight = planetToCamera.length();
        float cameraHeight2 = cameraHeight * cameraHeight;
        Vector3f lightPosNormalized = lightPosition.normalize();

        // easy collision detection
        float r = planet.getInnerRadius();
        if (cameraHeight < (r + 1.0f)) {
            main.getCamera().setLocation(planetToCamera.normalize().mult(r + 1.0f));
        }

        // change speed if necessary
//        if (cameraHeight < (r * 1.025)) {
//            main.atmosphericSpeed();
//        } else {
//            main.outerSpaceSpeed();
//        }


        // choose correct material according to camera position
        if (cameraHeight > planet.getOuterRadius()) {            
            Material mat = mGroundFromSpace;            
            mat.setFloat("Time", time);
            mat.setVector3("v3CameraPos", cameraLocation);
            mat.setVector3("v3LightPos", lightPosNormalized);
            mat.setFloat("fCameraHeight2", cameraHeight2);

            mat.setVector3("v3InvWavelength", planet.getInvWavelength4());
            mat.setFloat("fKrESun", planet.getKrESun());
            mat.setFloat("fKmESun", planet.getKmESun());
            mat.setFloat("fKr4PI", planet.getKr4PI());
            mat.setFloat("fKm4PI", planet.getKm4PI());
            mat.setFloat("fExposure", planet.getExposure());
            ground.setMaterial(mGroundFromSpace);
            
            mat = mSkyFromSpace;
            mat.setVector3("v3CameraPos", cameraLocation);
            mat.setVector3("v3LightPos", lightPosNormalized);
            mat.setFloat("fCameraHeight", cameraHeight);
            mat.setFloat("fCameraHeight2", cameraHeight2);

            mat.setVector3("v3InvWavelength", planet.getInvWavelength4());
            mat.setFloat("fKrESun", planet.getKrESun());
            mat.setFloat("fKmESun", planet.getKmESun());
            mat.setFloat("fKr4PI", planet.getKr4PI());
            mat.setFloat("fKm4PI", planet.getKm4PI());
            mat.setFloat("fg", planet.getG());
            mat.setFloat("fg2", planet.getG() * planet.getG());
            mat.setFloat("fExposure", planet.getExposure());
            atmosphere.setMaterial(mSkyFromSpace);
            
        } else {
            Material mat = mGroundFromSpace;
            mat.setFloat("Time", time);
            mat.setVector3("v3CameraPos", cameraLocation);
            mat.setVector3("v3LightPos", lightPosNormalized);
            mat.setFloat("fCameraHeight2", cameraHeight2);

            mat.setVector3("v3InvWavelength", planet.getInvWavelength4());
            mat.setFloat("fKrESun", planet.getKrESun());
            mat.setFloat("fKmESun", planet.getKmESun());
            mat.setFloat("fKr4PI", planet.getKr4PI());
            mat.setFloat("fKm4PI", planet.getKm4PI());
            mat.setFloat("fExposure", planet.getExposure());
            ground.setMaterial(mGroundFromSpace);
            
            mat = mSkyFromAtmosphere;
            mat.setVector3("v3CameraPos", cameraLocation);
            mat.setVector3("v3LightPos", lightPosNormalized);
            mat.setFloat("fCameraHeight", cameraHeight);
            mat.setFloat("fCameraHeight2", cameraHeight2);

            mat.setVector3("v3InvWavelength", planet.getInvWavelength4());
            mat.setFloat("fKrESun", planet.getKrESun());
            mat.setFloat("fKmESun", planet.getKmESun());
            mat.setFloat("fKr4PI", planet.getKr4PI());
            mat.setFloat("fKm4PI", planet.getKm4PI());
            mat.setFloat("fg", planet.getG());
            mat.setFloat("fg2", planet.getG() * planet.getG());
            mat.setFloat("fExposure", planet.getExposure());
            atmosphere.setMaterial(mSkyFromAtmosphere);
        }
    }

    /**
     * Sets planet constant material params for the ground
     * @param mat
     */
    private void setupGroundMaterial(Material mat) {
            mat.setTexture("Diffuse1", t_diffuse1);
            mat.setTexture("Diffuse2", t_diffuse2);
            mat.setFloat("Speed", planet.getRotationSpeed());
            mat.setVector3("v3LightPos", lightPosition.normalize());
            mat.setVector3("v3InvWavelength", planet.getInvWavelength4());
            mat.setFloat("fKrESun", planet.getKrESun());
            mat.setFloat("fKmESun", planet.getKmESun());
            mat.setFloat("fOuterRadius", planet.getOuterRadius());
            mat.setFloat("fInnerRadius", planet.getInnerRadius());
            mat.setFloat("fInnerRadius2", planet.getInnerRadius() * planet.getInnerRadius());
            mat.setFloat("fKr4PI", planet.getKr4PI());
            mat.setFloat("fKm4PI", planet.getKm4PI());
            mat.setFloat("fScale", planet.getScale());
            mat.setFloat("fScaleDepth", planet.getScaleDepth());
            mat.setFloat("fScaleOverScaleDepth", planet.getScaleOverScaleDepth());
            mat.setFloat("fSamples", planet.getfSamples());
            mat.setInt("nSamples", planet.getnSamples());
            mat.setFloat("fExposure", planet.getExposure());
    }

    /**
     * Sets planet constant material params for the sky
     * @param mat
     */
    private void setupSkyMaterial(Material mat) {
        mat.setVector3("v3LightPos", lightPosition.normalize());
        mat.setVector3("v3InvWavelength", planet.getInvWavelength4());
        mat.setFloat("fKrESun", planet.getKrESun());
        mat.setFloat("fKmESun", planet.getKmESun());
        mat.setFloat("fOuterRadius", planet.getOuterRadius());
        mat.setFloat("fInnerRadius", planet.getInnerRadius());
        mat.setFloat("fOuterRadius2", planet.getOuterRadius() * planet.getOuterRadius());
        mat.setFloat("fInnerRadius2", planet.getInnerRadius() * planet.getInnerRadius());
        mat.setFloat("fKr4PI", planet.getKr4PI());
        mat.setFloat("fKm4PI", planet.getKm4PI());
        mat.setFloat("fScale", planet.getScale());
        mat.setFloat("fScaleDepth", planet.getScaleDepth());
        mat.setFloat("fScaleOverScaleDepth", planet.getScaleOverScaleDepth());
        mat.setFloat("fSamples", planet.getfSamples());
        mat.setInt("nSamples", planet.getnSamples());
        mat.setFloat("fg", planet.getG());
        mat.setFloat("fg2", planet.getG() * planet.getG());
        mat.setFloat("fExposure", planet.getExposure());
    }

    private void createGround() {
        Spatial geom = createSphere();
        geom.scale(planet.getInnerRadius() * 0.25f);
        geom.setLocalTranslation(planet.getPosition());
        geom.updateModelBound();
        geom.setMaterial(mGroundFromSpace);
        // calling this method produces many warnings
        //TangentBinormalGenerator.generate(geom);
        ground = geom;
    }

    private void createAtmosphere() {
        Spatial geom = createSphere();
        geom.scale(planet.getOuterRadius() * 0.25f);
        geom.setLocalTranslation(planet.getPosition());
        geom.updateModelBound();
        geom.setQueueBucket(Bucket.Transparent);
        geom.setMaterial(mSkyFromSpace);
        // calling this method produces many warnings
        //TangentBinormalGenerator.generate(geom);
        
        atmosphere = geom;
    }

    private Spatial createSphere() {
        return main.getAssetManager().loadModel(MESH_SPHERE);
    }

    private void createGroundFromSpaceMaterial() {
        Material mat = new Material(main.getAssetManager(), MAT_GROUND_FROM_SPACE);
        setupGroundMaterial(mat);
        RenderState rs = mat.getAdditionalRenderState();
        rs.setBlendMode(BlendMode.Alpha);
        rs.setFaceCullMode(FaceCullMode.Back);
        rs.setDepthTest(true);
        rs.setDepthWrite(true);

        mGroundFromSpace = mat;
    }

    private void createSkyFromSpaceMaterial() {
        Material mat = new Material(main.getAssetManager(), MAT_SKY_FROM_SPACE);
        setupSkyMaterial(mat);        
        RenderState rs = mat.getAdditionalRenderState();
        rs.setBlendMode(BlendMode.AlphaAdditive);
        rs.setFaceCullMode(FaceCullMode.Front);
        rs.setDepthTest(true);
        rs.setDepthWrite(true);

        mSkyFromSpace = mat;
    }

    private void createGroundFromAtmosphereMaterial() {
        Material mat = new Material(main.getAssetManager(), MAT_GROUND_FROM_ATMOSPHERE);
        setupGroundMaterial(mat);
        RenderState rs = mat.getAdditionalRenderState();
        rs.setBlendMode(BlendMode.Alpha);
        rs.setFaceCullMode(FaceCullMode.Back);
        rs.setDepthTest(true);
        rs.setDepthWrite(true);

        mGroundFromAtmosphere = mat;
    }

    private void createSkyFromAtmosphereMaterial() {
        Material mat = new Material(main.getAssetManager(), MAT_SKY_FROM_ATMOSPHERE);
        setupSkyMaterial(mat);
        RenderState rs = mat.getAdditionalRenderState();
        rs.setBlendMode(BlendMode.AlphaAdditive);
        rs.setFaceCullMode(FaceCullMode.Front);
        rs.setDepthTest(true);
        rs.setDepthWrite(true);       

        mSkyFromAtmosphere = mat;
    }

    public Vector3f getLightPosition() {
        return lightPosition;
    }

    public void setLightPosition(Vector3f lightPosition) {
        this.lightPosition = lightPosition;
    }
}
