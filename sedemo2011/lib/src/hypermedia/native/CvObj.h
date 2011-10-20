



#include "hypermedia_video_OpenCV.h"
#include "cv.h"
#include "highgui.h"
#include <map>
#include <string>
#include <iostream>
#include <algorithm>


#define MAX_NUMOF_BLOBS 256

#define CAPTURE			4
#define SOURCE			hypermedia_video_OpenCV_SOURCE
#define BUFFER			hypermedia_video_OpenCV_BUFFER
#define MEMORY			hypermedia_video_OpenCV_MEMORY
#define ROI				hypermedia_video_OpenCV_ROI

#define GRAY			hypermedia_video_OpenCV_GRAY
#define RGB				hypermedia_video_OpenCV_RGB

#define FLIP_VERTICAL	hypermedia_video_OpenCV_FLIP_VERTICAL


using namespace std;

class CvObj {
public :
	
	
	
	CvObj();
	~CvObj();
	
	
	void allocate ( JNIEnv * env, jobject obj, int width, int height );
	void deallocate( int type=-1 );
	
	jintArray pixels( JNIEnv * jenv, jobject jobj, int kind );
	jintArray RGBpixels( JNIEnv * jenv, jobject jobj, int kind );
	
	// --
	
	void capture( JNIEnv * env, jobject obj, int w, int h, int index=0 );
	void loadMovie( JNIEnv * env, jobject obj, jstring file, int w, int h );
	void loadImage( JNIEnv * env, jobject obj, jstring file, int w, int h );
	void read();
	void jump( double value, int type=CV_CAP_PROP_POS_AVI_RATIO );
	float property( int which );
	void flip( int mode );
	
	// --
	
	void remember( int kind, int mode );
	void restore( int type );
	void absDiff();
	void threshold( float value, float max, int type );
	void convert( int type );
	void copy( IplImage * src, int srcx, int srcy, int srcw, int srch, int destx, int desty, int destw, int desth );
	void copy( JNIEnv * env, jobject obj, jintArray pixels, int step, int srcx, int srcy, int srcw, int srch, int destx, int desty, int destw, int desth);
	void copy( JNIEnv * env, jobject obj, jstring file, int srcx, int srcy, int srcw, int srch, int destx, int desty, int destw, int desth );
	
	// -- 
	
	jobjectArray blobs( JNIEnv * env, jobject obj, int minArea, int maxArea, int maxBlobs, bool findHoles, int maxVertices );
	
	// --
	
	void ROI( int x, int y , int width, int height );
	void invert();
	void blur( int type, int param1, int param2, float param3, float param4 );
	void brightnessContrast( int brightness, int contrast );
	
	// -- detection 
	
	void cascade( JNIEnv * env, jobject obj, jstring file );
	jobjectArray detect( JNIEnv*, jobject, double, int, int, int, int );
	
	
	// --
	
	static int qsort_blobarea_comparator( const void * elem1, const void * elem2 );
	
	
	
	int width;
	int height;
	int interpolation;
	
	// the buffer color space
	// could be RGB, GRAY
	int color_space;
	
	IplImage * buffer;
	IplImage * source;
	IplImage * memory;
	
	
	CvCapture * cam;
	CvHaarClassifierCascade * haarcascade;
	
};
