

#include "CvJNILib.h"






/**
 * Return the required object Java instance name.
 * Optionaly, if only one element exits in objects list, this name could be retrieve by the first 
 * map key. This option is enable by default to optimize the process.
 *
 * @param env		pointer to native method interface
 * @param obj		the Java object
 * @param fromjava	force to call Java to get the instance name
 *
 * @return the Java object instance name
 */
string javaObjectInstance( JNIEnv *env, jobject obj, bool fromjava=false )
{
	
	// retrieve the first map element key value
	// -> exit
	if ( !fromjava && instances.size()==1 ) {
		map<string,CvObj*>::iterator itr = instances.begin();
		return itr->first;
	}
	
	// call java to retrieve the object instance 
	jclass cls = env->GetObjectClass( obj );
	jmethodID mid = env->GetMethodID( cls, "toString", "()Ljava/lang/String;" );
	jstring key = (jstring) env->CallObjectMethod( obj, mid );
	
	return env->GetStringUTFChars( key, JNI_FALSE );
}






/////////////////////////////// JAVA NATIVE METHOD /////////////////////////////////////////////////


JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_instantiate
( JNIEnv * env, jobject obj )
{
	string key = javaObjectInstance( env, obj, true );
	instances[ key ] = new CvObj();
	//std::cout << "instantiate "<< key << " -> map size "<< instances.size() << std::endl;
}

JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_dispose
( JNIEnv * env, jobject obj )
{
	
	// Processing PApplet call automatically this method while exiting the program
	// if all objects are already be deleted by user for some miscalleneous reason
	// -> exit
	if ( instances.empty() ) return;
	
	string key = javaObjectInstance( env, obj, true );
	
	//std::cout << "dispose " << key << std::endl;
	if ( instances.count(key)>0 ) {
		
		//std::cout << "deallocate ... ";
		instances[ key ]->deallocate();
		
		delete instances[ key ];
		instances.erase( key );
		
		//std::cout << " -> map size "<< instances.size() << std::endl;
	}
}


JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_allocate
( JNIEnv * env, jobject obj, jint width, jint height )
{
	string key = javaObjectInstance( env, obj, false );
	instances[ key ]->allocate( env, obj, (int)width, (int)height );
}




////////////////////////////////////////// BUFFER //////////////////////////////////////////////////

// method : remember
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_remember
( JNIEnv * env, jobject obj, jint kind, jint mode )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->remember( (int)kind, (int)mode );
}

// method : absdiff
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_absDiff
( JNIEnv * env, jobject  obj )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->absDiff();
}

// method : threshold
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_threshold
( JNIEnv * env, jobject obj, jfloat value, jfloat max, jint type ) 
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->threshold( (float)value, (float)max, (int)type );
}

// method : convert
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_convert
( JNIEnv * env, jobject obj, jint type )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->convert( (int)type );
}


// method : flip
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_flip
( JNIEnv * env, jobject obj, jint mode ) 
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->flip( (int)mode );
}


/////////////////////////////////////// BLOBs DETECTION ////////////////////////////////////////////

// method : blobs detect
JNIEXPORT jobjectArray JNICALL Java_hypermedia_video_OpenCV_blobs
( JNIEnv * env, jobject obj, jint minArea, jint maxArea, jint maxBlobs, jboolean findHoles, jint maxVertices )
{
	string key = javaObjectInstance( env, obj );
	return instances[ key ]->blobs( env, obj, (int)minArea, (int)maxArea, (int)maxBlobs, (bool)findHoles, (int)maxVertices );
}



//////////////////////////////////////// DATA MANIPULATION /////////////////////////////////////////


// method : pixels
JNIEXPORT jintArray JNICALL Java_hypermedia_video_OpenCV_pixels
( JNIEnv * env, jobject obj, jint type )
{	
	string key = javaObjectInstance( env, obj );
	return instances[ key ]->pixels( env, obj, (int)type );
}

// method : RGBpixels
JNIEXPORT jintArray JNICALL Java_hypermedia_video_OpenCV_RGBpixels
( JNIEnv * env, jobject obj, jint type )
{	
	string key = javaObjectInstance( env, obj );
	return instances[ key ]->RGBpixels( env, obj, (int)type );
}


// method : copy
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_copy__Lhypermedia_video_OpenCV_2II
( JNIEnv * env, jobject obj, jobject source, jint srctype, jint desttype )
{	
	
	string srcId  = javaObjectInstance( env, source );
	string destId = javaObjectInstance( env, obj );
	
	IplImage * srcImg;
	IplImage * destImg;
	
	switch( srctype ) {
		case MEMORY	: srcImg = cvCloneImage( instances[ srcId ]->memory ); break;
		case SOURCE	: srcImg = cvCloneImage( instances[ srcId ]->source ); break;
		default		: srcImg = cvCloneImage( instances[ srcId ]->buffer ); break;
	}
	switch( desttype ) {
		case MEMORY	: destImg = cvCloneImage( instances[ destId ]->memory ); break;
		case SOURCE	: destImg = cvCloneImage( instances[ destId ]->source ); break;
		default		: destImg = cvCloneImage( instances[ destId ]->buffer ); break;
	}
	
	instances[ destId ]->copy( srcImg, 0, 0, srcImg->width, srcImg->height, 0, 0, destImg->width, destImg->height );
	
	// free memory
	cvReleaseImage( &srcImg );
	cvReleaseImage( &destImg );
}

// method : copy
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_copy___3IIIIIIIIII
(
 JNIEnv * env, jobject obj, 
 jintArray pixels, jint step, 
 jint srcx, jint srcy, jint srcw, jint srch, 
 jint destx, jint desty, jint destw, jint desth
)
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->copy( env, obj, pixels, (int)step, (int)srcx, (int)srcy, (int)srcw, (int)srch, (int)destx, (int)desty, (int)destw, (int)desth );
}

// method : copy
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_copy__Ljava_lang_String_2IIIIIIII
(
 JNIEnv * env, jobject obj, 
 jstring file, 
 jint srcx, jint srcy, jint srcw, jint srch, 
 jint destx, jint desty, jint destw, jint desth
)
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->copy( env, obj, file, (int)srcx, (int)srcy, (int)srcw, (int)srch, (int)destx, (int)desty, (int)destw, (int)desth );
}

// method : interpolation
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_interpolation
( JNIEnv * env, jobject obj, jint method )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->interpolation = (int)method;
}


JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_restore
( JNIEnv * env, jobject obj, jint type ) 
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->restore( (int)type );
}

/////////////////////////////////// CAPTURE AND MOVIE FILE /////////////////////////////////////////


// method : (CvPlayer) capture
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_capture
( JNIEnv * env, jobject obj, jint width, jint height, jint index )
{	
	string key = javaObjectInstance( env, obj );
	instances[ key ]->capture( env, obj, (int)width, (int)height, (int)index );
}

// method : loadMovie
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_movie
( JNIEnv * env, jobject obj, jstring file, jint w, jint h )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->loadMovie( env, obj, file, (int)w, (int)h );
}

// method : loadImage
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_loadImage
( JNIEnv * env, jobject obj, jstring file, jint w, jint h )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->loadImage( env, obj, file, (int)w, (int)h );
}


// method : (CvPlayer) read
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_read
( JNIEnv * env, jobject obj )
{	
	string key = javaObjectInstance( env, obj );
	instances[ key ]->read();
}

// method : (CvPlayer) jump
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_jump
( JNIEnv * env, jobject obj, jfloat value, jint type )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->jump( (double)value, (int)type );
}

// method : (CvPlayer) property
JNIEXPORT jfloat JNICALL Java_hypermedia_video_OpenCV_property
( JNIEnv * env, jobject obj, jint which )
{
	string key = javaObjectInstance( env, obj );
	return (jfloat)instances[ key ]->property( (int)which );
}


///////////////////////////// Region of interest ///////////////////////////////////////////////////

// -- method : ROI
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_ROI
( JNIEnv * env, jobject obj, jint x, jint y , jint w, jint h )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->ROI( (int)x, (int)y , (int)w, (int)h );
}


// -- method : invert
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_invert
( JNIEnv * env, jobject obj )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->invert();
}

// -- method : blur
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_blur
( JNIEnv * env, jobject obj, jint type, jint param1, jint param2, jfloat param3, jfloat param4
)
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->blur( (int)type, (int)param1, (int)param2, (float)param3, (float)param4 );
}

// -- method : brightness ~ contrast
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_brightnessContrast
( JNIEnv * env, jobject obj, jint brightness, jint contrast )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->brightnessContrast( (int)brightness, (int)contrast );
}



////////////////////////////////////// OBJECT DETECTION ////////////////////////////////////////////


// method : cascade
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_cascade
( JNIEnv * env, jobject obj, jstring file )
{
	string key = javaObjectInstance( env, obj );
	instances[ key ]->cascade( env, obj, file );
}

// -- method : detect
JNIEXPORT jobjectArray JNICALL Java_hypermedia_video_OpenCV_detect
( JNIEnv * env, jobject obj, jfloat scale, jint neighbors, jint flags, jint w, jint h )
{
	string key = javaObjectInstance( env, obj );
	return instances[ key ]->detect( env, obj, (double)scale, (int)neighbors, (int)flags, (int)w, (int)h );
}





///////////////////////////////////////////// TODO /////////////////////////////////////////////////

/*JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_perspective( JNIEnv * env, jobject jobj ) {
 
 CvMat* matrix = cvCreateMatHeader( 3, 3, CV_32FC1 );
 cvCreateData( matrix );
 
 cvWarpPerspective( buffer, buffer, matrix, CV_WARP_INVERSE_MAP+CV_WARP_FILL_OUTLIERS, cvScalarAll(0) );
 
 // deallocate matrix
 cvReleaseMat( &matrix );
 }*/

