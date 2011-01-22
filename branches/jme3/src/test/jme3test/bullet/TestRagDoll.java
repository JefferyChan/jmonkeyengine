/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.bullet;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author normenhansen
 */
public class TestRagDoll extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState = new BulletAppState();
    private Node ragDoll = new Node();
    private PhysicsNode shoulders;
    private Vector3f upforce = new Vector3f(0, 200, 0);

    public static void main(String[] args) {
        TestRagDoll app = new TestRagDoll();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        inputManager.addMapping("Pull ragdoll up", new MouseButtonTrigger(0));
        inputManager.addListener(this, "Pull ragdoll up");
        setupFloor();
        createRagDoll();
    }

    private void setupFloor() {
        PhysicsNode node = new PhysicsNode(new BoxCollisionShape(new Vector3f(100, 1, 100)), 0);
        node.setName("floor");
        node.setLocalTranslation(new Vector3f(0f, -6, 0f));
        node.attachDebugShape(assetManager);
        rootNode.attachChild(node);
        bulletAppState.getPhysicsSpace().add(node);
    }

    private void createRagDoll() {
                    shoulders = createLimb(0.2f, 1.0f, new Vector3f( 0.00f, 1.5f, 0), true);
        PhysicsNode uArmL     = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, 0.8f, 0), false);
        PhysicsNode uArmR     = createLimb(0.2f, 0.5f, new Vector3f( 0.75f, 0.8f, 0), false);
        PhysicsNode lArmL     = createLimb(0.2f, 0.5f, new Vector3f(-0.75f,-0.2f, 0), false);
        PhysicsNode lArmR     = createLimb(0.2f, 0.5f, new Vector3f( 0.75f,-0.2f, 0), false);
        PhysicsNode body      = createLimb(0.2f, 1.0f, new Vector3f( 0.00f, 0.5f, 0), false);
        PhysicsNode hips      = createLimb(0.2f, 0.5f, new Vector3f( 0.00f,-0.5f, 0), true);
        PhysicsNode uLegL     = createLimb(0.2f, 0.5f, new Vector3f(-0.25f,-1.2f, 0), false);
        PhysicsNode uLegR     = createLimb(0.2f, 0.5f, new Vector3f( 0.25f,-1.2f, 0), false);
        PhysicsNode lLegL     = createLimb(0.2f, 0.5f, new Vector3f(-0.25f,-2.2f, 0), false);
        PhysicsNode lLegR     = createLimb(0.2f, 0.5f, new Vector3f( 0.25f,-2.2f, 0), false);
        
        join(body, shoulders, new Vector3f(0f, 1.4f, 0));
        join(body, hips, new Vector3f(0f, -0.5f, 0));

        join(uArmL, shoulders, new Vector3f(-0.75f, 1.4f, 0));
        join(uArmR, shoulders, new Vector3f(0.75f, 1.4f, 0));
        join(uArmL, lArmL, new Vector3f(-0.75f, .4f, 0));
        join(uArmR, lArmR, new Vector3f(0.75f, .4f, 0));

        join(uLegL, hips, new Vector3f(-.25f, -0.5f, 0));
        join(uLegR, hips, new Vector3f(.25f, -0.5f, 0));
        join(uLegL, lLegL, new Vector3f(-.25f, -1.7f, 0));
        join(uLegR, lLegR, new Vector3f(.25f, -1.7f, 0));

        ragDoll.attachChild(shoulders);
        ragDoll.attachChild(body);
        ragDoll.attachChild(hips);
        ragDoll.attachChild(uArmL);
        ragDoll.attachChild(uArmR);
        ragDoll.attachChild(lArmL);
        ragDoll.attachChild(lArmR);
        ragDoll.attachChild(uLegL);
        ragDoll.attachChild(uLegR);
        ragDoll.attachChild(lLegL);
        ragDoll.attachChild(lLegR);
        
        rootNode.attachChild(ragDoll);
        bulletAppState.getPhysicsSpace().addAll(ragDoll);
    }

    private PhysicsNode createLimb(float width, float height, Vector3f location, boolean rotate) {
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);
        PhysicsNode node = new PhysicsNode(shape);
        node.attachDebugShape(assetManager);
        node.setLocalTranslation(location);
        return node;
    }

    private PhysicsJoint join(PhysicsNode A, PhysicsNode B, Vector3f connectionPoint) {
        Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint joint = new ConeJoint(A.getRigidBody(), B.getRigidBody(), pivotA, pivotB);
        joint.setLimit(1f, 1f, 0);
        return joint;
    }

    public void onAction(String string, boolean bln, float tpf) {
        if ("Pull ragdoll up".equals(string)) {
            if (bln) {
                shoulders.activate();
                shoulders.applyContinuousForce(true, upforce);
            } else {
                shoulders.applyContinuousForce(false, upforce);
            }
        }
    }
}
