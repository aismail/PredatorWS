

#include "hypermedia_video_OpenCV.h"
#include "CvObj.h"
#include <map>
#include <string>
#include <iostream>


using namespace std;



/**
 * A list of all objects instantiate :
 *
 *    - key <string>	-> associated java object instance
 *	  - value <CvObj*>	-> pointer to the asociated C++ object
 */
map<string,CvObj*> instances;





/////////////////// FUNCTION DECLARATION (except Java native methods) //////////////////////////////


string javaObjectInstance( JNIEnv *env, jobject obj, bool fromjava );


