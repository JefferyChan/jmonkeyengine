/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package chapter08;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 *
 * @author ruth
 */
public class Particle6Fire extends SimpleApplication {

  @Override
  public void simpleInitApp() {
 ParticleEmitter fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);    
    Material fire_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
    fire_mat.setTexture("Texture", assetManager.loadTexture("Effects/flame.png"));
    fire.setMaterial(fire_mat);
    fire.setImagesX(2); 
    fire.setImagesY(2);
    fire.setRandomAngle(true);
    rootNode.attachChild(fire);
    
    fire.setStartColor(new ColorRGBA(1f, 1f, .5f, 1f)); 
    fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 0f));  
    fire.setGravity(0,0,0);
    fire.setStartSize(1.5f);
    fire.setEndSize(0.05f);
    fire.setLowLife(0.5f);
    fire.setHighLife(2f);
    fire.getParticleInfluencer().setVelocityVariation(0.3f);
    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0,3f,0)); 
  }

  public static void main(String[] args) {
    Particle6Fire app = new Particle6Fire();
    app.start();

  }
}
