

LIB_NAME	= OpenCV
PROJECT_DIR	= $(shell pwd)
BUILD_DIR	= $(PROJECT_DIR)
CPP_SOURCE_DIR	= $(PROJECT_DIR)

JAVA_ROOT=/usr/lib/jvm/java-6-sun
#OPENCV_ROOT	= /home/palimpseste/Library/opencv/opencv-2.1.0





# find platform (in lower case)
# -----------------------------
OSTYPE		= $(shell uname | tr '[A-Z]' '[a-z]')


# define build directories
# ------------------------
LIB_BUILD_ROOT	= $(BUILD_DIR)
LIB_BUILD_DIR	= $(LIB_BUILD_ROOT)


all : lib


# -> linux : Linux platform
# GCC compiler and linker options
ifeq ($(findstring linux, $(OSTYPE)),linux)


# -> to search for CV include path use the first line below
# -> to entenr manualy CV include path, use the second one
# comment/uncomment the appropiate line
# ---------------------------------------------------------
OPENCV_INC	= $(subst -I,,$(shell pkg-config --cflags opencv))
#OPENCV_INC	= $(OPENCV_ROOT)/include/openc


CC		= /usr/bin/g++
CC_FLAGS	= -fPIC
CC_INCLUDE	= $(CPP_SOURCE_DIR) \
		  $(JAVA_ROOT)/include/ \
		  $(JAVA_ROOT)/include/linux/ \
		  $(OPENCV_INC)
CC_OBJECT	= $(BUILD_DIR)/$(LIB_NAME).o
CC_LIBRARY	= $(LIB_BUILD_DIR)/lib$(LIB_NAME).so
LD		= $(CC)




# -> using CV from repository
# comment/uncomment line below to define LD_FLAGS
# -----------------------------------------------
#LD_FLAGS	= -shared -Wl,-soname $(shell pkg-config --libs opencv) -m32 -m64
LD_FLAGS	= -shared -Wl,-soname $(shell pkg-config --libs opencv)


# -> using your own CV compilation
# comment/uncomment lines below to define LD_FLAGS
# ------------------------------------------------
#GTK_LIBS	= $(shell pkg-config --libs gtk+-2.0)
#FFMPEG_LIBS	= -lavcodec -lavformat -lavutil -lswscale 
#GSTREAMER_LIBS	= $(shell pkg-config --libs gstreamer-0.10) -lgstvideo-0.10 -lgstapp-0.10 -lgstbase-0.10
#XINE_LIBS	= $(shell pkg-config --libs libxine)
#UNICAP_LIBS	= $(shell pkg-config --libs libunicap) -lucil
#V4L_LIBS	= -lv4l1 -lv4l2
#VIDEO_IO_LIBS	= $(FFMPEG_LIBS) $(GSTREAMER_LIBS) $(XINE_LIBS) $(UNICAP_LIBS) $(V4L_LIBS) -ldc1394 -lraw1394
#IMAGE_IO_LIBS	= -ljasper -ljpeg -lpng -ltiff -lzlib
#LD_FLAGS	= -shared -s -z defs \
#		  -Wl,-whole-archive -L$(OPENCV_ROOT)/lib -lcv -lcvaux -lcxcore -lhighgui -lml \
#		  -L$(OPENCV_ROOT)/3rdparty/lib -lflann -lopencv_lapack \
#		  $(VIDEO_IO_LIBS) $(IMAGE_IO_LIBS) $(GTK_LIBS) \
#		  -Wl,-no-whole-archive \
#		  -m32 -m64
endif


# ! object will automatically be removed on success
lib :	
	@mkdir -p $(LIB_BUILD_DIR)
	@echo
	@echo "compiling native library..."
	@$(CC) $(CC_FLAGS) $(patsubst %,-I%,$(CC_INCLUDE)) -c $(CPP_SOURCE_DIR)/*.cpp
	@echo "linking..."
	@rm -f $(CC_LIBRARY)
	@$(CC) $(LD_FLAGS) -o $(CC_LIBRARY) *.o
	@rm -f *.o
	@echo "done"
	@echo



