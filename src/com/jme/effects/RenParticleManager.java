/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.jme.effects;

import com.jme.math.FastMath;
import com.jme.math.Line;
import com.jme.math.Matrix3f;
import com.jme.math.Rectangle;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TriMesh;

/**
 * <code>RenParticleManager</code>
 *
 * Example usage:
 * <code>
 *   RenParticleManager manager = new RenParticleManager(300, display.getRenderer().getCamera());
 *   someNode.attachChild(manager.getParticles());
 * </code>
 *
 * See the method comments for more usage information.
 *
 * note: The idea of using one TriMesh to control particles and much of the code
 *       related to picking angles was kindly donated by Java Cool Dude.
 *
 * @author Joshua Slack
 * @version $Id: RenParticleManager.java,v 1.9 2004-03-26 17:58:53 renanse Exp $
 *
 * @todo Points and Lines (not just quads)
 * @todo Particles stretched based on historical path
 * @todo Particle motion models
 */
public class RenParticleManager {

  private final static Vector2f sharedTextureData[] = {
      new Vector2f(0.0f, 0.0f), new Vector2f(1.0f, 0.0f),
      new Vector2f(1.0f, 1.0f), new Vector2f(0.0f, 1.0f)
  };

  private TriMesh particlesGeometry;
  private int noParticles;
  private int releaseRate; // particles per second
  private int released;
  private Vector3f upVector;
  private Vector3f gravityForce;
  private Vector3f emissionDirection;
  private Vector3f originCenter;
  private Matrix3f rotMatrix;
  private RenParticle particles[];
  private Vector3f geometryCoordinates[];
  private ColorRGBA appearanceColors[];
  private ColorRGBA startColor;
  private ColorRGBA endColor;
  private float particleSpeed;
  private float releaseVariance;
  private float minimumLifeTime;
  private float maximumAngle;
  private float startSize, endSize;
  private float randomMod;
  private long currentTime;
  private long previousTime;
  private long releaseTime;
  private double timePassed;
  private double timeSinceLastUpdate;
  private boolean respawnParticles;
  private boolean controlFlow;
  private boolean firstRun;

  private int geoToUse;
  private Line psLine;
  private Rectangle psRect;

  private Camera camera;

  /**
   * RenParticleManager constructor
   *
   * @param noParticles Desired number of particles in this system.
   * @param cam The camera to have the billboarded particles face.
   */
  public RenParticleManager(int noParticles, Camera cam) {
    camera = cam;
    this.noParticles = noParticles;

// init non-null, non-zero field members
    rotMatrix = new Matrix3f();
    originCenter = new Vector3f();
    upVector = new Vector3f(0.0f, 1.0f, 0.0f);
    gravityForce = new Vector3f(0.0f, 0.0f, 0.0f);
    emissionDirection = new Vector3f(0.0f, 1.0f, 0.0f);
    startColor = new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f);
    endColor = new ColorRGBA(1.0f, 1.0f, 0.0f, 0.0f);
    firstRun = true;
    particleSpeed = 1.0f;
    minimumLifeTime = 2500f;
    maximumAngle = 0.7853982f;
    startSize = 20f;
    endSize = 4f;
    randomMod = 1.0f;
    releaseRate = noParticles;
    releaseVariance = 0;
    respawnParticles = true;
    controlFlow = false;

    geometryCoordinates = new Vector3f[noParticles << 2];
    int[] indices = new int[noParticles * 6];
    for (int j = 0; j < noParticles; j++) {
      indices[0 + j * 6] = j * 4 + 2;
      indices[1 + j * 6] = j * 4 + 1;
      indices[2 + j * 6] = j * 4 + 0;
      indices[3 + j * 6] = j * 4 + 3;
      indices[4 + j * 6] = j * 4 + 2;
      indices[5 + j * 6] = j * 4 + 0;
    }

    appearanceColors = new ColorRGBA[noParticles << 2];
    particles = new RenParticle[noParticles];

    particlesGeometry = new TriMesh("particles");
    particlesGeometry.setVertices(new Vector3f[noParticles << 2]);
    particlesGeometry.setTextures(new Vector2f[noParticles << 2], 0);
    particlesGeometry.setIndices(indices);

    Vector3f speed = new Vector3f();
    Vector3f location = new Vector3f();
    updateRotationMatrix();
    for (int k = 0; k < noParticles; k++) {
      float life = getRandomLifeSpan();
      getRandomSpeed(location);
      particles[k] = new RenParticle(this, speed, location, life);
      for (int a = 3; a >= 0; a--) {
        particlesGeometry.setTextureCoord(0, (k << 2) + a, sharedTextureData[a]);
        geometryCoordinates[ (k << 2) + a] = particles[k].verts[a];
        appearanceColors[ (k << 2) + a] = particles[k].color;
      }

    }
    particlesGeometry.updateTextureBuffer();

    warmup();

    updateParticles();
    previousTime = getTimerTic();
  }

  /**
   * Runs the clock forward to ensure particles are all flowing.
   */
  public void warmup() {
    firstRun=true;
    timeSinceLastUpdate = 5000d; // run the clock a bit to make sure the particles are flowing...
  }


  /**
   * Update the particles managed by this manager.  If any particles are "dead"
   * recreate them at the origin position (which may be a point, line or
   * rectangle.)
   */
  public void updateParticles() {

    Vector3f speed = new Vector3f();
    RenParticle particle;
    boolean flag = false;
    currentTime = getTimerTic();
    if (controlFlow && currentTime - releaseTime > 1000.0) {
      released = 0;
      releaseTime = currentTime;
    }
    timePassed = currentTime - previousTime;
    timeSinceLastUpdate += timePassed;
    int particlesToCreate = 0;
    if (controlFlow)
      particlesToCreate = (int) ( (float) releaseRate * timeSinceLastUpdate *
                                 .001f
                                 *
                                 (1.0f +
                                  releaseVariance *
                                  (FastMath.nextRandomFloat() - 0.5f)));

    if (controlFlow && particlesToCreate <= 0) particlesToCreate = 1;

    if (controlFlow && releaseRate - released <= 0) particlesToCreate = 0;

    for (; timeSinceLastUpdate > 10d; ) {
      timeSinceLastUpdate -= 10d;
      flag = true;
      if (firstRun) {
        timeSinceLastUpdate = 0.0d;
        firstRun = false;
      }

      int i = 0;
      while (i < noParticles) {
        if (particles[i].updateAndCheck() && (!controlFlow || particlesToCreate > 0)) {
          if (particles[i].status == RenParticle.DEAD && !respawnParticles) {
            ;
          } else {
            if (controlFlow) {
              released++;
              particlesToCreate--;
            }
            particle = particles[i];
            getRandomSpeed(speed);
            particle.recreateParticle(speed, getRandomLifeSpan());
            particle.status = RenParticle.ALIVE;

            switch (getGeometry()) {
              case 1:
                particle.location.set(getLine().random());
                break;
              case 2:
                particle.location.set(getRectangle().random());
                break;
              default:
                particle.location.set(originCenter);
                break;
            }
            particle.updateVerts();
          }
        }
        i++;
      }
    }

    if (flag) {
      particlesGeometry.setVertices(geometryCoordinates);
      particlesGeometry.setColors(appearanceColors);
    }
    previousTime = currentTime;
  }

  /**
   * Force all dead particles back to life.
   */
  public void forceRespawn() {
    for (int i = noParticles; --i >= 0; ) {
      particles[i].status = RenParticle.AVAILABLE;
    }
  }

  /**
   * Setup the rotation matrix used to determine initial particle velocity
   * based on emission angle and emission direction.
   *
   * called automatically by the set* methods for those parameters.
   */
  public void updateRotationMatrix() {
    float emit = emissionDirection.length();
    if (emit < 0.1f) {
      return;
    }
    float matData[][] = new float[3][3];
    emissionDirection.multLocal(1.0f / emit);
    float upDotEmit = upVector.dot(emissionDirection);
    if ( ( (double) FastMath.abs(upDotEmit)) > 1.0d - FastMath.DBL_EPSILON) {
      Vector3f absUpVector = new Vector3f();
      Vector3f abUpMinUp = new Vector3f();
      absUpVector.x = upVector.x <= 0.0f ? -upVector.x : upVector.x;
      absUpVector.y = upVector.y <= 0.0f ? -upVector.y : upVector.y;
      absUpVector.z = upVector.z <= 0.0f ? -upVector.z : upVector.z;
      if (absUpVector.x < absUpVector.y) {
        if (absUpVector.x < absUpVector.z) {
          absUpVector.x = 1.0f;
          absUpVector.y = absUpVector.z = 0.0f;
        } else {
          absUpVector.z = 1.0f;
          absUpVector.x = absUpVector.y = 0.0f;
        }
      } else
      if (absUpVector.y < absUpVector.z) {
        absUpVector.y = 1.0f;
        absUpVector.x = absUpVector.z = 0.0f;
      } else {
        absUpVector.z = 1.0f;
        absUpVector.x = absUpVector.y = 0.0f;
      }
      abUpMinUp = absUpVector.subtract(upVector);
      Vector3f upXemit = absUpVector.subtract(emissionDirection);
      float f4 = 2.0f / abUpMinUp.dot(abUpMinUp);
      float f6 = 2.0f / upXemit.dot(upXemit);
      float f8 = f4 * f6 * abUpMinUp.dot(upXemit);
      float af1[] = {
          abUpMinUp.x, abUpMinUp.y, abUpMinUp.z
      };
      float af2[] = {
          upXemit.x, upXemit.y, upXemit.z
      };
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          matData[i][j] = ( -f4 * af1[i] * af1[j] - f6 * af2[i] * af2[j]) +
              f8 * af2[i] * af1[j];

        }
        matData[i][i]++;
      }

    } else {
      Vector3f upXemit = upVector.cross(emissionDirection);
      float f2 = 1.0f / (1.0f + upDotEmit);
      float f5 = f2 * upXemit.x;
      float f7 = f2 * upXemit.z;
      float f9 = f5 * upXemit.y;
      float f10 = f5 * upXemit.z;
      float f11 = f7 * upXemit.y;
      matData[0][0] = upDotEmit + f5 * upXemit.x;
      matData[0][1] = f9 - upXemit.z;
      matData[0][2] = f10 + upXemit.y;
      matData[1][0] = f9 + upXemit.z;
      matData[1][1] = upDotEmit + f2 * upXemit.y * upXemit.y;
      matData[1][2] = f11 - upXemit.x;
      matData[2][0] = f10 - upXemit.y;
      matData[2][1] = f11 + upXemit.x;
      matData[2][2] = upDotEmit + f7 * upXemit.z;
    }
    rotMatrix.set(matData);
  }

  /**
   * Uses the System.currentTimeMillis() to return a heartbeat for this manager.
   *
   * @return long
   */
  private long getTimerTic() {
    return System.currentTimeMillis();
  }

  /**
   * Generate a random velocity within the parameters of max angle and
   * the rotation matrix.
   *
   * @param speed a vector to store the results in.
   */
  private void getRandomSpeed(Vector3f speed) {
    float randDir = FastMath.TWO_PI * FastMath.nextRandomFloat();
    float clampAngle = clampToMaxAngle(FastMath.PI * FastMath.nextRandomFloat());
    speed.x = (float) (FastMath.cos(randDir) * FastMath.sin(clampAngle));
    speed.y = (float) FastMath.cos(clampAngle);
    speed.z = (float) (FastMath.sin(randDir) * FastMath.sin(clampAngle));
    rotateVectorSpeed(speed);
  }

  /**
   * Make sure the given angle is less than or equal to the max angle.
   *
   * @param angle the angle to check
   * @return the angle or max angle if the supplied angle was greater.
   */
  private float clampToMaxAngle(float angle) {
    if (angle > maximumAngle) {
      return maximumAngle;
    } else {
      return angle;
    }
  }

  /**
   * generate a random lifespan between 100% and 150% of the min life span.
   *
   * @return the generated lifespan value
   */
  private float getRandomLifeSpan() {
    float life = minimumLifeTime * (0.5f + FastMath.nextRandomFloat());
    if (life <= minimumLifeTime) {
      return minimumLifeTime;
    } else {
      return life;
    }
  }

  /**
   * Apply the rotation matrix to a given vector representing a particle velocity.
   *
   * @param speed the velocity vector to be modified.
   */
  private void rotateVectorSpeed(Vector3f speed) {

    float x = speed.x,
          y = speed.y,
          z = speed.z;

    speed.x = -1*((rotMatrix.m00 * x) +
                  (rotMatrix.m10 * y) +
                  (rotMatrix.m20 * z));

    speed.y =     (rotMatrix.m01 * x) +
                  (rotMatrix.m11 * y) +
                  (rotMatrix.m21 * z);

    speed.z = -1*((rotMatrix.m02 * x) +
                  (rotMatrix.m12 * y) +
                  (rotMatrix.m22 * z));
  }

  /**
   * Set the origin for any new particles created (or recreated) by this manager.
   * This is applicable only to managers generating from a point (not a line,
   * rectangle, etc..)
   *
   * @param origin new origin position
   */
  public void setParticlesOrigin(Vector3f origin) {
    originCenter.set(origin);
  }

  /**
   * Get the origin point set in this manager.
   *
   * @return origin
   */
  public Vector3f getParticlesOrigin() {
    return originCenter;
  }

  /**
   * Set the start color for particles.  This is the base color of the quad.
   *
   * @param color ColorRGBA
   */
  public void setStartColor(ColorRGBA color) {
    this.startColor = color;
  }

  /**
   * getStartColor
   *
   * @return ColorRGBA
   */
  public ColorRGBA getStartColor() {
    return startColor;
  }

  /**
   * Set the end color for particles.  The base color of the quad will linearly
   * approach this color from the start color over the lifetime of the particle.
   *
   * @param color ColorRGBA
   */
  public void setEndColor(ColorRGBA color) {
    this.endColor = color;
  }

  /**
   * getEndColor
   *
   * @return ColorRGBA
   */
  public ColorRGBA getEndColor() {
    return endColor;
  }

  /**
   * Set the speed modifier of the particle flow.  This modifier speeds up
   * or slows down the particle's velocity and the affect of gravity.
   *
   * The default value is 1.0f (i.e. 100%)
   *
   * @param speedMod new value of the speed modifier (should be >= zero)
   */
  public void setParticlesSpeed(float speedMod) {
    particleSpeed = speedMod;
  }

  /**
   * Get the current speed modifier.
   *
   * @see setParticlesSpeed(float)
   * @return the speed modifier.
   */
  public float getParticlesSpeed() {
    return particleSpeed;
  }

  /**
   * Set the Camera whose position should be used to determine billboard
   * facing direction.
   *
   * @param cam Camera
   */
  public void setCamera(Camera cam) {
    camera = cam;
  }

  /**
   * Get the currently set Camera used for billboad calcs.
   *
   * @return Camera
   */
  public Camera getCamera() {
    return camera;
  }

  /**
   * Set a vector describing the force of gravity on a particle.
   * Generally, the values should be less than .01f
   *
   * @param force Vector3f
   */
  public void setGravityForce(Vector3f force) {
    gravityForce.set(force);
  }

  /**
   * getGravityForce
   *
   * @return Vector3f
   */
  public Vector3f getGravityForce() {
    return gravityForce;
  }

  /**
   * Set the size of the new particles generated by this manager.
   * If a value less than zero is given, zero is used.
   *
   * @param size float
   */
  public void setStartSize(float size) {
    startSize = size >= 0.0f ? size : 0.0f;
  }

  /**
   * getStartSize
   *
   * @return float
   */
  public float getStartSize() {
    return startSize;
  }

  /**
   * Set the size particles will approach as they age.
   * If a value less than zero is given, zero is used.
   *
   * @param size float
   */
  public void setEndSize(float size) {
    endSize = size >= 0.0f ? size : 0.0f;
  }

  /**
   * getEndSize
   *
   * @return float
   */
  public float getEndSize() {
    return endSize;
  }

  /**
   * Set the general direction that particles are emitted in.
   * This will be modified by the emission angle.
   *
   * @param direction Vector3f
   */
  public void setEmissionDirection(Vector3f direction) {
    emissionDirection.set(direction);
    updateRotationMatrix();
  }

  /**
   * getEmissionDirection
   *
   * @return Vector3f
   */
  public Vector3f getEmissionDirection() {
    return emissionDirection;
  }

  /**
   * Set the maximum angle (in radians) that particles can be emitted away from
   * the emission direction.
   *
   * @param f float
   */
  public void setEmissionMaximumAngle(float f) {
    maximumAngle = f >= 0.0f ? f : 0.0f;
  }

  /**
   * getEmissionMaximumAngle
   *
   * @return float
   */
  public float getEmissionMaximumAngle() {
    return maximumAngle;
  }

  /**
   * Set the minimum lifespan of new particles (or recreated) managed by this
   * manager.
   *
   * if a value less than zero is given, 1.0f is used.
   *
   * @param lifeSpan float
   */
  public void setParticlesMinimumLifeTime(float lifeSpan) {
    minimumLifeTime = lifeSpan >= 0.0f ? lifeSpan : 1.0f;
  }

  /**
   * getParticlesMinimumLifeTime
   *
   * @return float
   */
  public float getParticlesMinimumLifeTime() {
    return minimumLifeTime;
  }

  /**
   * Set the "randomness" modifier.  0 = not random
   *
   * @param mod float
   */
  public void setRandomMod(float mod) {
    randomMod = mod;
  }

  /**
   * getRandomFactor
   *
   * @return float
   */
  public float getRandomMod() {
    return randomMod;
  }

  /**
   * Get the number of particles managed by this manager.
   *
   * @return int
   */
  public int getParticlesNumber() {
    return noParticles;
  }

  /**
   * Get the number of particles the manager should release per second.
   *
   * @return int
   */
  public int getReleaseRate() {
    return releaseRate;
  }

  /**
   * Set the number of particles the manager should release per second.
   *
   * @param particlesPerSecond number of particles per second
   */
  public void setReleaseRate(int particlesPerSecond) {
    this.releaseRate = particlesPerSecond;
  }

  /**
   * Get the variance possible on the release rate.
   * 0.0f = no variance
   * 0.5f = between releaseRate / 2f and  1.5f * releaseRate
   *
   * @return float
   */
  public float getReleaseVariance() {
    return releaseVariance;
  }

  /**
   * Set the variance possible on the release rate.
   *
   * @param variance release rate +/- variance as a percent  (eg. .5 = 50%)
   */
  public void setReleaseVariance(float variance) {
    this.releaseVariance = variance;
  }

  /**
   * Does this manager respawn particles?
   *
   * @return true if this manager recreates 'dead' particles.
   */
  public boolean getRespawnsParticles() {
    return respawnParticles;
  }

  /**
   * Set the respawn property on the manager.
   *
   * @param respawn recreate particles when they expire.
   */
  public void setRespawnsParticles(boolean respawn) {
    this.respawnParticles = respawn;
  }

  /**
   * Does this manager regulate the particle flow?
   *
   * @return true if this manager regulates how many particles per sec are emitted.
   */
  public boolean getControlFlow() {
    return controlFlow;
  }

  /**
   * Set the regulate flow property on the manager.
   *
   * @param regulate regulate particle flow.
   */
  public void setControlFlow(boolean regulate) {
    this.controlFlow = regulate;
  }

  /**
   * Get the TriMesh that holds all of the particle information for display.
   *
   * @return TriMesh
   */
  public TriMesh getParticles() {
    return particlesGeometry;
  }

  /**
   * Get which Geometry method is being used by the underlying system.
   * 0 = point
   * 1 = line
   * 2 = rectangle
   *
   * @return int
   */
  public int getGeometry() {
    return geoToUse;
  }

  /**
   * Set which Geometry method is being used by the underlying system.
   * 0 = point
   * 1 = line
   * 2 = rectangle
   * This is already done by setGeometry(Line) and setGeometry(Rectangle)
   * You should not need to use this method unless you are switching between
   * geometry already set by those methods.
   *
   * @param type Geometry type to use
   */
  public void setGeometry(int type) {
    geoToUse = type;
  }

  /**
   * Set a line segment to be used as the "emittor".
   *
   * @param line Line
   */
  public void setGeometry(Line line) {
    psLine = line;
    geoToUse = 1;
  }

  /**
   * Set a rectangular patch to be used as the "emittor".
   *
   * @param rect Rectangle
   */
  public void setGeometry(Rectangle rect) {
    psRect = rect;
    geoToUse = 2;
  }

  /**
   * getLine
   *
   * @return Line
   */
  public Line getLine() {
    return psLine;
  }

  /**
   * getRectangle
   *
   * @return Rectangle
   */
  public Rectangle getRectangle() {
    return psRect;
  }
}
