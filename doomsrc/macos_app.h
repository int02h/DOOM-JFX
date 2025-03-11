#ifndef __MACOS_APP__
#define __MACOS_APP__

/*************************************************/
/* Video bridge for macOS app. Used in i_video.c */
/*************************************************/
typedef void (*InitGraphicsCallback)(int width, int height);
void Callback_InitGraphics(InitGraphicsCallback cb);

typedef void (*SetPaletteCallback)(const unsigned char* palette);
void Callback_SetPalette(SetPaletteCallback cb);

typedef void (*StartFrameCallback)();
void Callback_StartFrame(StartFrameCallback cb);

typedef void (*FinishUpdateCallback)(const unsigned char* screen);
void Callback_FinishUpdate(FinishUpdateCallback cb);

void onKeyDown(int keyCode);
void onKeyUp(int keyCode);

/*************************************************/
/* Sound bridge for macOS app. Used in i_sound.c */
/*************************************************/

typedef void (*InitSoundCallback)(const signed short* mixbuffer, unsigned int size);
void Callback_InitSound(InitSoundCallback cb);

typedef void (*SubmitSoundCallback)();
void Callback_SubmitSound(SubmitSoundCallback cb);

#endif
