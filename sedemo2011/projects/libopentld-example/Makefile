view-webcam: view-webcam.c
	mbuild $^ `pkg-config opencv --cflags --libs` -L$(LIBOPENTLD_DIR) -I$(LIBOPENTLD_DIR) -lopentld -lang cpp

run: view-webcam
	./run_view_webcam.sh /usr/local/MATLAB/R2011a

.PHONY:
clean:
	rm -f view-webcam
