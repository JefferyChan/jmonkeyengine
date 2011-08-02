/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 * This demo shows a round swarm of sparkling embers.
 * <p/>
 * @author ruth
 */
public class Particle3Embers extends SimpleApplication {

  @Override
  public void simpleInitApp() {
    ParticleEmitter embers = new ParticleEmitter("embers", Type.Triangle, 20);
    Material embers_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
    embers_mat.setTexture("Texture", assetManager.loadTexture("Effects/roundspark.png"));
    embers.setMaterial(embers_mat);
    embers.setImagesX(1);
    embers.setImagesY(1);
    rootNode.attachChild(embers);
    
    embers.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, 1.0f));
    embers.setEndColor(new ColorRGBA(0, 0, 0, 0.5f));
    embers.setStartSize(1.2f);
    embers.setEndSize(1.8f);
    embers.setGravity(0, -.5f, 0);
    embers.setLowLife(1.8f);
    embers.setHighLife(2f);
    embers.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3, 0));
    embers.getParticleInfluencer().setVelocityVariation(.5f);
    embers.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
  }

  public static void main(String[] args) {
    Particle3Embers app = new Particle3Embers();
    app.start();
  }
}
