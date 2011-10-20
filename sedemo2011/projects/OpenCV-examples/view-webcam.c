#include "highgui.h"
#include "cv.h"
#include <stdlib.h>
#include <stdio.h>

int main( int argc, char** argv ) {
	cvNamedWindow( "Example2", CV_WINDOW_AUTOSIZE );
	CvCapture* capture = cvCaptureFromCAM(-1);
	IplImage* frame, *simplifiedFrame = NULL, *simplifiedFrame2 = NULL, *frame2 = NULL;
	CvMemStorage* storage = cvCreateMemStorage(0);
	CvSeq* lines = NULL;
	int i;

	if (capture == NULL) {
		printf("Oooops .. I'm screwed :-(\n");
		exit(-1);
	}

	while(1) {
		frame = cvQueryFrame( capture );
		if( !frame ) break;

		if (simplifiedFrame == NULL) {
			simplifiedFrame = cvCreateImage(cvSize(frame->width, frame->height), IPL_DEPTH_8U, 1);
		}

		if (simplifiedFrame2 == NULL) {
			simplifiedFrame2 = cvCreateImage(cvSize(frame->width, frame->height), IPL_DEPTH_8U, 1);
		}

		if (frame2 == NULL) {
			frame2 = cvCreateImage(cvSize(frame->width, frame->height), frame->depth, frame->nChannels);
		}

		cvConvertImage(frame, simplifiedFrame, 0);
		cvCanny(simplifiedFrame, simplifiedFrame2, 300 * 7 * 7, 400 * 7 * 7, 7);
		cvConvertImage(simplifiedFrame2, frame2, 0);
		cvAdd(frame, frame2, frame);
		cvShowImage( "Example2", frame );
		char c = cvWaitKey(33);
		if( c == 27 ) break;

	}

	cvReleaseImage(&frame);
	cvReleaseImage(&frame2);
	cvReleaseImage(&simplifiedFrame);
	cvReleaseImage(&simplifiedFrame2);
	cvReleaseMemStorage( &storage );
	cvReleaseCapture( &capture );
	cvDestroyWindow( "Example2" );
}
