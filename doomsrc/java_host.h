#ifndef __JAVA_HOST__
#define __JAVA_HOST__

#include <jni.h>

JavaVM *g_jvm;

void javaCallStaticVoid(const char *className, const char *methodName);

#endif