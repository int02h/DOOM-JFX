#include <jni.h>
#include <stdio.h>
#include "../../../doomsrc/d_main.h"

JNIEXPORT void JNICALL
Java_com_dpforge_doom_DoomMain_start(JNIEnv *env, jobject obj)
{
    printf("Starting doom\n");
    D_DoomMain();
    return;
}