#include <jni.h>
#include <stdio.h>

JNIEXPORT void JNICALL
Java_com_dpforge_doom_DoomMain_print(JNIEnv *env, jobject obj)
{
    printf("Hello From C++ World!\n");
    return;
}