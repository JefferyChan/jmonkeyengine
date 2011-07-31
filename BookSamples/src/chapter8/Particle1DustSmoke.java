package chapter8;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.FastMath;

/**
 * This demo shows a moving smoke or dust cloud.
 */
public class Particle1DustSmoke extends SimpleApplication {

  private ParticleEmitter dust;
  private float angle = 0;

  public static void main(String[] args) {
    Particle1DustSmoke app = new Particle1DustSmoke();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    dust = new ParticleEmitter("dust emitter", Type.Triangle, 100);
    Material smoke_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
    smoke_mat.setTexture("Texture", assetManager.loadTexture("Effects/smoke.png"));
    dust.setMaterial(smoke_mat);
    dust.setImagesX(2);
    dust.setImagesY(2);
    dust.setSelectRandomImage(true);
    rootNode.attachChild(dust);
    
    dust.setRandomAngle(true);
    dust.getParticleInfluencer().setVelocityVariation(1);
  }

  @Override
  public void simpleUpdate(float tpf) {
    /** make the emitter fly in circles */
    angle += tpf;
    angle %= FastMath.TWO_PI;
    float x = FastMath.cos(angle) * 2;
    float y = FastMath.sin(angle) * 2;
    dust.setLocalTranslation(x, 0, y);
  }
}
