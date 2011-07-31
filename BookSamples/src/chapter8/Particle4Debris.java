/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package chapter8;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 * This demo shows pieces of spinning flying debris.
 */
public class Particle4Debris extends SimpleApplication {

  @Override
  public void simpleInitApp() {
    ParticleEmitter debris = new ParticleEmitter("Debris", Type.Triangle, 5);
    Material debris_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
    debris_mat.setTexture("Texture", assetManager.loadTexture("Effects/debris.png"));
    debris.setMaterial(debris_mat);
    debris.setImagesX(3);
    debris.setImagesY(3);
    debris.setSelectRandomImage(false);
    rootNode.attachChild(debris);
    
    debris.setRandomAngle(true);
    debris.setRotateSpeed(FastMath.TWO_PI * 4);
    debris.setStartColor(new ColorRGBA(0.8f, 0.8f, 1f, 1.0f));
    debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 1f));
    debris.setStartSize(.2f);
    debris.setEndSize(.7f);
    debris.setGravity(0, 30f, 0);
    debris.setLowLife(1.4f);
    debris.setHighLife(1.5f);
    debris.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 15, 0));
    debris.getParticleInfluencer().setVelocityVariation(.50f);
  }

  public static void main(String[] args) {
    Particle4Debris app = new Particle4Debris();
    app.start();

  }
}
