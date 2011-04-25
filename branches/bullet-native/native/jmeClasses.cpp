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
#include "jmeClasses.h"

/**
 * Author: Normen Hansen
 */
//public fields
jclass jmeClasses::PhysicsSpace;
//    static jmethodID physicsSpace_test;

jclass jmeClasses::Vector3f;
jmethodID jmeClasses::Vector3f_set;
jmethodID jmeClasses::Vector3f_toArray;
jmethodID jmeClasses::Vector3f_getX;
jmethodID jmeClasses::Vector3f_getY;
jmethodID jmeClasses::Vector3f_getZ;

jclass jmeClasses::Quaternion;
jmethodID jmeClasses::Quaternion_set;
jmethodID jmeClasses::Quaternion_getX;
jmethodID jmeClasses::Quaternion_getY;
jmethodID jmeClasses::Quaternion_getZ;
jmethodID jmeClasses::Quaternion_getW;

//private fields
JNIEnv* jmeClasses::env;

void jmeClasses::initJavaClasses(JNIEnv* env) {
    if(jmeClasses::env != NULL){
        return;
    }
    jmeClasses::env = env;
    
    PhysicsSpace = env->FindClass("com/jme3/bullet/PhysicsSpace");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }

//    physicsSpace_test = env->GetMethodID(physicsSpace, "test", "()V");
//    if (env->ExceptionCheck()) {
//        env->Throw(env->ExceptionOccurred());
//        return false;
//    }

    Vector3f = env->FindClass("com/jme3/math/Vector3f");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Vector3f_set = env->GetMethodID(Vector3f, "set", "(FFF)Lcom/jme3/math/Vector3f;");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Vector3f_toArray = env->GetMethodID(Vector3f, "toArray", "([F)[F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Vector3f_getX = env->GetMethodID(Vector3f, "getX", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Vector3f_getY = env->GetMethodID(Vector3f, "getY", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Vector3f_getZ = env->GetMethodID(Vector3f, "getZ", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }

    Quaternion = env->FindClass("com/jme3/math/Quaternion");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Quaternion_set = env->GetMethodID(Quaternion, "set", "(FFFF)Lcom/jme3/math/Quaternion;");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Quaternion_getW = env->GetMethodID(Quaternion, "getW", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Quaternion_getX = env->GetMethodID(Quaternion, "getX", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Quaternion_getY = env->GetMethodID(Quaternion, "getY", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Quaternion_getZ = env->GetMethodID(Quaternion, "getZ", "()F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }

    Matrix3f = env->FindClass("com/jme3/math/Matrix3f");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Matrix3f_set = env->GetMethodID(Matrix3f, "set", "(IIF)Lcom/jme3/math/Matrix3f;");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
    Matrix3f_get = env->GetMethodID(Matrix3f, "get", "(II)F");
    if (env->ExceptionCheck()) {
        env->Throw(env->ExceptionOccurred());
        return;
    }
}