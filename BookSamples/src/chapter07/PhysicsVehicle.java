package chapter07;
 
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
 
/**
 * A physical first-person vehicle driving over boxes.
 */
public class PhysicsVehicle extends SimpleApplication implements ActionListener {
 
  public static void main(String args[]) {
    PhysicsVehicle app = new PhysicsVehicle();
    app.start();
  }
 
 
    private BulletAppState bulletAppState;
    private VehicleControl vehicle;
    private final float accelerationForce = 1000.0f;
    private final float brakeForce = 100.0f;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private Vector3f jumpForce = new Vector3f(0, 3000, 0);

  

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        viewPort.setBackgroundColor(ColorRGBA.Cyan);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        setupKeys();
        buildPlayer(); 
        
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(1.3f,-1.3f,-1.3f).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun); 
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(2));
    rootNode.addLight(ambient); 
    }


    private void setupKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Accelerate", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Brake", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Reverse", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Accelerate");
        inputManager.addListener(this, "Brake");
        inputManager.addListener(this, "Reverse");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Reset");
    }

    private void buildPlayer() {
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Red);

        //create a compound shape and attach the BoxCollisionShape for the car body at 0,2,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-2,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.2f, 0.5f, 2.4f));
        compoundShape.addChildShape(box, new Vector3f(0, 2, 0));

        //create vehicle node
        Node vehicleNode=new Node("vehicleNode");
        vehicle = new VehicleControl(compoundShape, 400);
        vehicleNode.addControl(vehicle);

        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 60.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMaxSuspensionForce(10000.0f);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 0.5f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1f;
        float zOff = 2f;

        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.6f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        vehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        vehicle.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        vehicle.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        vehicle.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        vehicleNode.attachChild(node1);
        vehicleNode.attachChild(node2);
        vehicleNode.attachChild(node3);
        vehicleNode.attachChild(node4);
        rootNode.attachChild(vehicleNode);

        bulletAppState.getPhysicsSpace().add(vehicle);
    }

    @Override
    public void simpleUpdate(float tpf) {
      Vector3f loc = vehicle.getPhysicsLocation();
        cam.setLocation(new Vector3f(loc.x,loc.y+2,loc.z));
        cam.setRotation(vehicle.getPhysicsRotation());
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) {
                steeringValue += .5f;
            } else {
                steeringValue += -.5f;
            }
            vehicle.steer(steeringValue);
        } else if (binding.equals("Right")) {
            if (value) {
                steeringValue += -.5f;
            } else {
                steeringValue += .5f;
            }
            vehicle.steer(steeringValue);
        } else if (binding.equals("Accelerate")) {
            if (value) {
                accelerationValue += accelerationForce;
            } else {
                accelerationValue -= accelerationForce;
            }
            vehicle.accelerate(accelerationValue);
        } else if (binding.equals("Brake")) {
            if (value) {
                vehicle.brake(brakeForce);
            } else {
                vehicle.brake(0f);
            }
        } else if (binding.equals("Jump")) {
            if (value) {
                vehicle.applyImpulse(jumpForce, Vector3f.ZERO);
            }
        } else if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                vehicle.setPhysicsLocation(Vector3f.ZERO);
                vehicle.setPhysicsRotation(new Matrix3f());
                vehicle.setLinearVelocity(Vector3f.ZERO);
                vehicle.setAngularVelocity(Vector3f.ZERO);
                vehicle.resetSuspension();
            } else {
            }
        }
    }
}