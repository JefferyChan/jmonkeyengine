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
 * This demo shows a burst of fire, which can be used as the center of an explosion effect.
 * <p/>
 * @author ruth
 */
public class Particle5Burst extends SimpleApplication {

  @Override
  public void simpleInitApp() {
    ParticleEmitter burst = new ParticleEmitter("Flash", Type.Triangle, 5);
    Material burst_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
    burst_mat.setTexture("Texture", assetManager.loadTexture("Effects/flash.png"));
    burst.setMaterial(burst_mat);
    burst.setImagesX(2);
    burst.setImagesY(2);
    burst.setSelectRandomImage(true);
    rootNode.attachChild(burst);
    
    burst.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
    burst.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
    burst.setStartSize(.1f);
    burst.setEndSize(3.0f);
    burst.setGravity(0, 0, 0);
    burst.setLowLife(.2f);
    burst.setHighLife(.2f);
    burst.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 5f, 0));
    burst.getParticleInfluencer().setVelocityVariation(1);
    burst.setShape(new EmitterSphereShape(Vector3f.ZERO, .5f));
  }

  public static void main(String[] args) {
    Particle5Burst app = new Particle5Burst();
    app.start();
  }
}
