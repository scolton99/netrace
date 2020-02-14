//
// Created by me on 2020-01-26.
//

#include <jni.h>
#include <string.h>

extern "C" JNIEXPORT jstring JNICALL Java_tech_scolton_netrace_util_NDKTest_test(JNIEnv *env, jclass obj) {
    jstring jstr = env->NewStringUTF("Hello World!");
    return jstr;
}