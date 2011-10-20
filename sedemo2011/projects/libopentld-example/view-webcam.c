#include <opencv/highgui.h>
#include <opencv/cv.h>
#include <stdlib.h>
#include <stdio.h>

#include "libopentld.h"
#include "mclmcrrt.h"

int run_main(int argc, const char** argv);

int main(int argc, const char **argv) {
	mclmcrInitialize();
	return mclRunMain((mclMainFcnType)run_main, argc, argv);
}

int run_main( int argc, const char** argv ) {
	cvNamedWindow( "Example2", CV_WINDOW_AUTOSIZE );
	CvCapture* capture;
	IplImage* frame;
	IplImage* grayscaleFrame;
	int i;
	size_t dimensions = 4;
	mxArray* bbox = NULL;
	mxArray* source;
	int *bbox_ptr;
	int a, b, c, d, ok;
	double aa, bb, cc, dd;

	if (!mclInitializeApplication(NULL, 0)) {
		fprintf(stderr, "Could not initialize the application\n");
		exit(-1);
	}

	if (!libopentldInitialize()) {
		fprintf(stderr, "Could not initialize libopentld\n");
		exit(-1);
	}

	capture = cvCaptureFromCAM(-1);
	if (capture == NULL) {
		printf("Oooops .. I'm screwed :-(\n");
		exit(-1);
	}

	frame = cvQueryFrame(capture);
	grayscaleFrame = cvCreateImage(cvSize(frame->width, frame->height), IPL_DEPTH_8U, 1);

	printf("frame width: %d, height: %d\n", frame->width, frame->height);

	mlfTldExampleInitDefault();

	while(1) {
		frame = cvQueryFrame( capture );
		if( !frame ) break;

		cvConvertImage(frame, grayscaleFrame, 0);
		cvSaveImage("temp/frame.png", grayscaleFrame);
		mlfTldProcessSingleFrame();


		a = -1, b = -1, c = -1, d = -1;
		aa = -1, bb = -1, cc = -1, dd = -1;
		FILE * f = fopen("bb.txt", "rt");
		if (f != NULL) {
			fscanf(f, "%lf", &aa); a = (int) aa;
			fscanf(f, "%lf", &bb); b = (int) bb;
			fscanf(f, "%lf", &cc); c = (int) cc;
			fscanf(f, "%lf", &dd); d = (int) dd;
			fclose(f);
		}
		ok = (a != -1) && (b != -1) && (c != -1) && (d != -1);
		
		if (ok) {
			printf("a = %d, b = %d, c = %d, d = %d\n", a, b, c,d);
			cvDrawRect(frame, cvPoint(a,b), cvPoint(c,d), cvScalar(255, 0, 0), 2);
		}

		cvShowImage( "Example2", frame );
		char c = cvWaitKey(33);
		if( c == 27 ) break;

	}

	cvReleaseImage(&frame);
	cvReleaseImage(&grayscaleFrame);
	cvReleaseCapture( &capture );
	cvDestroyWindow( "Example2" );

	libopentldTerminate();

	/* Destroy stuff related to Matlab */
	
	mclTerminateApplication();

	return 0;
}
