/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#include "jmeMotionState.h"

/**
 * Author: Normen Hansen
 */

jmeMotionState::jmeMotionState(btTransform worldTrans) {
    worldTransform=worldTrans;
    dirty=true;
}

void jmeMotionState::getWorldTransform(btTransform& worldTrans ) const{
    worldTrans=worldTransform;
}

void jmeMotionState::setWorldTransform(const btTransform& worldTrans){
    worldTransform=worldTrans;
    dirty=true;
//    if(env!=NULL && javaRigidBody!=NULL){
////        jvm->AttachCurrentThread((void**)&env, NULL);
//        env->CallVoidMethod(this->javaRigidBody,physicsNode_setWorldTranslation,
//                worldTransform.getOrigin().m_floats[0],
//                worldTransform.getOrigin().m_floats[1],
//                worldTransform.getOrigin().m_floats[2]);
//        if (env->ExceptionCheck()) env->Throw(env->ExceptionOccurred());
//
//        env->CallVoidMethod(this->javaRigidBody,physicsNode_setWorldRotation,
//                worldTransform.getBasis().getRow(0).m_floats[0],
//                worldTransform.getBasis().getRow(0).m_floats[1],
//                worldTransform.getBasis().getRow(0).m_floats[2],
//                worldTransform.getBasis().getRow(1).m_floats[0],
//                worldTransform.getBasis().getRow(1).m_floats[1],
//                worldTransform.getBasis().getRow(1).m_floats[2],
//                worldTransform.getBasis().getRow(2).m_floats[0],
//                worldTransform.getBasis().getRow(2).m_floats[1],
//                worldTransform.getBasis().getRow(2).m_floats[2]);
//        if (env->ExceptionCheck()) env->Throw(env->ExceptionOccurred());
//    }
}

jobject jmeMotionState::getJavaRigidBody(){
    return this->javaRigidBody;
}

void jmeMotionState::setJavaRigidBody(JNIEnv* env, jobject javaRigidBody){
//    initJavaMethodHandles(env);
//    this->env=env;
    this->javaRigidBody=env->NewGlobalRef(javaRigidBody);
}

//bool jmeMotionState::initJavaMethodHandles(JNIEnv* env) {
////    env->GetJavaVM(&jvm);
//
//    physicsNodeClass = env->FindClass("com/jmex/bullet/nodes/PhysicsNode");
//    if (env->ExceptionCheck()) {
//        env->Throw(env->ExceptionOccurred());
//        return false;
//    }
//
//    physicsNode_setWorldTranslation = env->GetMethodID(physicsNodeClass, "setWorldTranslation", "(FFF)V");
//    if (env->ExceptionCheck()) {
//        env->Throw(env->ExceptionOccurred());
//        return false;
//    }
//
//    physicsNode_setWorldRotation = env->GetMethodID(physicsNodeClass, "setWorldRotation", "(FFFFFFFFF)V");
//    if (env->ExceptionCheck()) {
//        env->Throw(env->ExceptionOccurred());
//        return false;
//    }
//
//    return true;
//}

jmeMotionState::~jmeMotionState() {
    // TODO Auto-generated destructor stub
}
