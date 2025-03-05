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

# JAVA_INCLUDES=-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin
# For a proper .dylib, you should enable Position-Independent Code (PIC) using -fPIC
# CFLAGS=-g -shared -dynamiclib -install_name @rpath/libmacosdoom.dylib -fPIC -Wall -DNORMALUNIX -DMACOS -DJNI

JAVA_INCLUDES=-I../../idoom/idoom -L$(O) -framework Foundation
CFLAGS=-g -shared -dynamiclib -install_name @rpath/libmacosdoom.dylib -fPIC -Wall -DNORMALUNIX -DMACOS -DMACOSAPP

# subdirectory for objects
O=build

# not too sophisticated dependency
OBJS=				\
		$(O)/doomdef.o		\
		$(O)/doomstat.o		\
		$(O)/dstrings.o		\
		$(O)/i_system.o		\
		$(O)/i_sound.o		\
		$(O)/i_video.o		\
		$(O)/i_net.o			\
		$(O)/tables.o			\
		$(O)/f_finale.o		\
		$(O)/f_wipe.o 		\
		$(O)/d_main.o			\
		$(O)/d_net.o			\
		$(O)/d_items.o		\
		$(O)/g_game.o			\
		$(O)/m_menu.o			\
		$(O)/m_misc.o			\
		$(O)/m_argv.o  		\
		$(O)/m_bbox.o			\
		$(O)/m_fixed.o		\
		$(O)/m_swap.o			\
		$(O)/m_cheat.o		\
		$(O)/m_random.o		\
		$(O)/am_map.o			\
		$(O)/p_ceilng.o		\
		$(O)/p_doors.o		\
		$(O)/p_enemy.o		\
		$(O)/p_floor.o		\
		$(O)/p_inter.o		\
		$(O)/p_lights.o		\
		$(O)/p_map.o			\
		$(O)/p_maputl.o		\
		$(O)/p_plats.o		\
		$(O)/p_pspr.o			\
		$(O)/p_setup.o		\
		$(O)/p_sight.o		\
		$(O)/p_spec.o			\
		$(O)/p_switch.o		\
		$(O)/p_mobj.o			\
		$(O)/p_telept.o		\
		$(O)/p_tick.o			\
		$(O)/p_saveg.o		\
		$(O)/p_user.o			\
		$(O)/r_bsp.o			\
		$(O)/r_data.o			\
		$(O)/r_draw.o			\
		$(O)/r_main.o			\
		$(O)/r_plane.o		\
		$(O)/r_segs.o			\
		$(O)/r_sky.o			\
		$(O)/r_things.o		\
		$(O)/w_wad.o			\
		$(O)/wi_stuff.o		\
		$(O)/v_video.o		\
		$(O)/st_lib.o			\
		$(O)/st_stuff.o		\
		$(O)/hu_stuff.o		\
		$(O)/hu_lib.o			\
		$(O)/s_sound.o		\
		$(O)/z_zone.o			\
		$(O)/info.o				\
		$(O)/sounds.o           \
		$(O)/java_host.o     	\

all: make_dirs build copy_output

clean:
	rm -f *.o *~ *.flc
	rm build/*.o build/*.dylib

make_dirs:
	mkdir -p $(O)
	mkdir -p $(O)/../../libs/

build: $(O)/macosdoom

copy_output:
	cp $(O)/libmacosdoom.dylib $(O)/../../libs/

$(O)/macosdoom:	$(OBJS)
	$(CC) $(CFLAGS) $(JAVA_INCLUDES) $(OBJS) -o $(O)/libmacosdoom.dylib

$(O)/%.o:	%.c
	$(CC) $(CFLAGS) $(JAVA_INCLUDES) -c $< -o $@

#############################################################
#
#############################################################