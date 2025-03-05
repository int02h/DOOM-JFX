// Emacs style mode select   -*- C++ -*-
//-----------------------------------------------------------------------------
//
// $Id:$
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This source is available for distribution and/or modification
// only under the terms of the DOOM Source Code License as
// published by id Software. All rights reserved.
//
// The source is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// FITNESS FOR A PARTICULAR PURPOSE. See the DOOM Source Code License
// for more details.
//
// $Log:$
//
// DESCRIPTION:
//	DOOM graphics stuff for X11, UNIX.
//
//-----------------------------------------------------------------------------

static const char
rcsid[] = "$Id: i_x.c,v 1.6 1997/02/03 22:45:10 b1 Exp $";

#include "doomtype.h"
#include "doomdef.h"
#include "v_video.h"
#include "d_event.h"
#include "d_main.h"

#ifdef MACOSAPP
#include "macos_app.h"

static InitGraphicsCallback cbInitGraphics = NULL;
void Callback_InitGraphics(InitGraphicsCallback cb) {
    cbInitGraphics = cb;
}

static SetPaletteCallback cbSetPalette = NULL;
void Callback_SetPalette(SetPaletteCallback cb) {
    cbSetPalette = cb;
}

static StartFrameCallback cbStartFrame = NULL;
void Callback_StartFrame(StartFrameCallback cb) {
    cbStartFrame = cb;
}

static FinishUpdateCallback cbFinishUpdate = NULL;
void Callback_FinishUpdate(FinishUpdateCallback cb) {
    cbFinishUpdate = cb;
}

void onKeyDown(int keyCode) {
    event_t event;
    event.type = ev_keydown;
    event.data1 = keyCode;
    D_PostEvent(&event);
}

void onKeyUp(int keyCode) {
    event_t event;
    event.type = ev_keyup;
    event.data1 = keyCode;
    D_PostEvent(&event);
}

#endif

#ifdef JNI
#include <jni.h>
#include "java_host.h"

JNIEXPORT jint JNICALL Java_com_dpforge_doom_DoomVideo_getScreenWidth(JNIEnv *env, jclass clazz)
{
    return SCREENWIDTH;
}

JNIEXPORT jint JNICALL Java_com_dpforge_doom_DoomVideo_getScreenHeight(JNIEnv *env, jclass clazz)
{
    return SCREENHEIGHT;
}

JNIEXPORT jobject JNICALL Java_com_dpforge_doom_DoomVideo_getScreenBuffer(JNIEnv *env, jclass clazz, jint index, jint size)
{
    // Create a direct ByteBuffer that wraps the native memory
    return (*env)->NewDirectByteBuffer(env, screens[index], size);
}

JNIEXPORT void JNICALL Java_com_dpforge_doom_DoomVideo_onKeyDown(JNIEnv *env, jclass clazz, jint keyCode)
{
    event_t event;
    event.type = ev_keydown;
    event.data1 = keyCode;
    D_PostEvent(&event);
}

JNIEXPORT void JNICALL Java_com_dpforge_doom_DoomVideo_onKeyUp(JNIEnv *env, jclass clazz, jint keyCode)
{
    event_t event;
    event.type = ev_keyup;
    event.data1 = keyCode;
    D_PostEvent(&event);
}

void callDoomVideo(const char *name)
{
    javaCallStaticVoid("com/dpforge/doom/DoomVideo", name);
}

#endif

void I_ShutdownGraphics(void)
{
}

void I_StartFrame (void)
{
#ifdef MACOSAPP
    cbStartFrame();
#endif
#ifdef JNI
    callDoomVideo("startFrame");
#endif
}

void I_GetEvent(void)
{
}

void I_StartTic (void)
{
}

void I_UpdateNoBlit (void)
{
}

void I_FinishUpdate (void)
{
#ifdef MACOSAPP
    cbFinishUpdate(screens[0]);
#endif
#ifdef JNI
    callDoomVideo("finishUpdate");
#endif
}

void I_ReadScreen (byte* scr)
{

}
void I_SetPalette (byte* palette)
{
#ifdef MACOSAPP
    cbSetPalette(palette);
#endif
#ifdef JNI
    JNIEnv *env;
    (*g_jvm)->AttachCurrentThread(g_jvm, (void**)&env, NULL);

    // 256 colors, 3 bytes each (RGB)
    jbyteArray result = (*env)->NewByteArray(env, 3 * 256);
    (*env)->SetByteArrayRegion(env, result, 0, 3 * 256, (jbyte*)palette);

    jclass cls = (*env)->FindClass(env, "com/dpforge/doom/DoomVideo");
    jmethodID mid = (*env)->GetStaticMethodID(env, cls, "setPalette", "([B)V");
    (*env)->CallStaticVoidMethod(env, cls, mid, result);

    (*g_jvm)->DetachCurrentThread(g_jvm);
#endif
}

void I_InitGraphics(void)
{
#ifdef MACOSAPP
    cbInitGraphics(SCREENWIDTH, SCREENHEIGHT);
#endif
#ifdef JNI
    callDoomVideo("initGraphics");
#endif
}
