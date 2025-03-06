################################################################
#
# $Id:$
#
# $Log:$
#

ifndef JAVA_HOME
    $(error JAVA_HOME is not set or empty. Please set JAVA_HOME before running make.)
endif

CC=  gcc  # gcc or g++

# subdirectory for objects
MACOS_O=build/macos
JNI_O=build/jni
LIBS=../libs

JNI_INCLUDES=-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin
# For a proper .dylib, you should enable Position-Independent Code (PIC) using -fPIC
JNI_CFLAGS=-g -shared -dynamiclib -install_name @rpath/libjnidoom.dylib -fPIC -Wall -DNORMALUNIX -DMACOS -DJNI

MACOS_INCLUDES=-I../../idoom/idoom -L$(MACOS_O) -framework Foundation
MACOS_CFLAGS=-g -shared -dynamiclib -install_name @rpath/libmacosdoom.dylib -fPIC -Wall -DNORMALUNIX -DMACOS -DMACOSAPP

# not too sophisticated dependency
COMMON_OBJS=				\
		doomdef.o		\
		doomstat.o		\
		dstrings.o		\
		i_system.o		\
		i_sound.o		\
		i_video.o		\
		i_net.o			\
		tables.o			\
		f_finale.o		\
		f_wipe.o 		\
		d_main.o			\
		d_net.o			\
		d_items.o		\
		g_game.o			\
		m_menu.o			\
		m_misc.o			\
		m_argv.o  		\
		m_bbox.o			\
		m_fixed.o		\
		m_swap.o			\
		m_cheat.o		\
		m_random.o		\
		am_map.o			\
		p_ceilng.o		\
		p_doors.o		\
		p_enemy.o		\
		p_floor.o		\
		p_inter.o		\
		p_lights.o		\
		p_map.o			\
		p_maputl.o		\
		p_plats.o		\
		p_pspr.o			\
		p_setup.o		\
		p_sight.o		\
		p_spec.o			\
		p_switch.o		\
		p_mobj.o			\
		p_telept.o		\
		p_tick.o			\
		p_saveg.o		\
		p_user.o			\
		r_bsp.o			\
		r_data.o			\
		r_draw.o			\
		r_main.o			\
		r_plane.o		\
		r_segs.o			\
		r_sky.o			\
		r_things.o		\
		w_wad.o			\
		wi_stuff.o		\
		v_video.o		\
		st_lib.o			\
		st_stuff.o		\
		hu_stuff.o		\
		hu_lib.o			\
		s_sound.o		\
		z_zone.o			\
		info.o				\
		sounds.o			\
		i_main.o

MACOS_OBJS = $(addprefix $(MACOS_O)/, $(COMMON_OBJS))
JNI_OBJS   = $(addprefix $(JNI_O)/, $(COMMON_OBJS)) $(JNI_O)/java_host.o

all: make_dirs build_macos build_jni

clean:
	rm -rf build

make_dirs:
	mkdir -p $(MACOS_O)
	mkdir -p $(JNI_O)
	mkdir -p $(LIBS)

build_macos: $(MACOS_O)/macosdoom

$(MACOS_O)/macosdoom:	$(MACOS_OBJS)
	$(CC) $(MACOS_CFLAGS) $(MACOS_INCLUDES) $(MACOS_OBJS) -o $(LIBS)/libmacosdoom.dylib

$(MACOS_O)/%.o:	%.c
	$(CC) $(MACOS_CFLAGS) $(MACOS_INCLUDES) -c $< -o $@

build_jni: $(JNI_O)/jnidoom

$(JNI_O)/jnidoom:	$(JNI_OBJS)
	$(CC) $(JNI_CFLAGS) $(JNI_INCLUDES) $(JNI_OBJS) -o $(LIBS)/libjnidoom.dylib

$(JNI_O)/%.o:	%.c
	$(CC) $(JNI_CFLAGS) $(JNI_INCLUDES) -c $< -o $@

#############################################################
#
#############################################################