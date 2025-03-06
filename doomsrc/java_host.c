#ifdef JNI
#include "java_host.h"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    g_jvm = vm;
    printf("JNI JNI_OnLoad\n");
    return JNI_VERSION_1_6;
}

void javaCallStaticVoid(const char *className, const char *methodName)
{
    JNIEnv *env;
    (*g_jvm)->AttachCurrentThread(g_jvm, (void**)&env, NULL);
    jclass cls = (*env)->FindClass(env, className);
    jmethodID mid = (*env)->GetStaticMethodID(env, cls, methodName, "()V");
    (*env)->CallStaticVoidMethod(env, cls, mid);
    (*g_jvm)->DetachCurrentThread(g_jvm);
}
#endif