build: view-webcam view-webcam-edges

view-webcam: view-webcam.c
	mbuild $^ `pkg-config opencv --cflags --libs` -lang cpp

view-webcam-edges: view-webcam-edges.c
	mbuild $^ `pkg-config opencv --cflags --libs` -lang cpp

.PHONY:
clean:
	rm -rf view-webcam view-webcam-edges
