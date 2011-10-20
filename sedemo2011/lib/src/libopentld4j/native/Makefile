libopentld4j.so: libopentld4j_OpenTLD4j.c libopentld4j_OpenTLD4j.c libopentld4j.exports
	mbuild $^ `pkg-config opencv --cflags --libs` -I$(JAVA_INCLUDE_DIR1) -I$(JAVA_INCLUDE_DIR2) -I$(LIBOPENTLD_DIR) -L$(LIBOPENTLD_DIR) -lopentld -lang cpp
