package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 * This demo shows individually flying sparks, 
 * from a short-circuiting electric panel or from welding.
 */
public class Particle2Sparks extends SimpleApplication {

  @Override
  public void simpleInitApp() {
    ParticleEmitter sparks = new ParticleEmitter("Spark", Type.Triangle, 60);
    Material spark_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
    spark_mat.setTexture("Texture", assetManager.loadTexture("Effects/spark.png"));
    sparks.setMaterial(spark_mat);
    sparks.setImagesX(1);
    sparks.setImagesY(1);
    rootNode.attachChild(sparks);
    
    sparks.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 10, 0));
    sparks.getParticleInfluencer().setVelocityVariation(1);
    sparks.setStartColor(new ColorRGBA(1f, 1f, 1f, 1.0f)); // white
    sparks.setEndColor(new ColorRGBA(1f, 1f, 0f, 0f)); // yellow
    sparks.setGravity(0, 50, 0);
    sparks.setFacingVelocity(true);
    sparks.setStartSize(.5f);
    sparks.setEndSize(.5f);
    sparks.setLowLife(.9f);
    sparks.setHighLife(1.1f);
  }

  public static void main(String[] args) {
    Particle2Sparks app = new Particle2Sparks();
    app.start();
  }
}
