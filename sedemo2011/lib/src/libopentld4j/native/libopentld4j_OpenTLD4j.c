#include "libopentld4j_OpenTLD4j.h"
#include "libopentld.h"
#include "mclmcrrt.h"
#include <stdio.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

/*
 * Class:     libopentld4j_OpenTLD4j
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_libopentld4j_OpenTLD4j_init(JNIEnv *, jclass) {
	printf("OpenTLD4j init start ..\n");

	mclmcrInitialize();

	if (!mclInitializeApplication(NULL, 0)) {
		fprintf(stderr, "Could not initialize the application\n");
		return;
	}
	printf("Application initialized successfully\n");

        if (!libopentldInitialize()) {
		fprintf(stderr, "Could not initialize libopentld\n");
		return;
        }
	printf("OpentTLD library initialized successfully\n");

	mlfTldExampleInitDefault();
	printf("TLD engine initialized successfully\n");

	printf("OpenTLD4j init done\n");

}

/*
 * Class:     libopentld4j_OpenTLD4j
 * Method:    trackOneFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_libopentld4j_OpenTLD4j_trackOneFrame (JNIEnv *, jclass) {
	mlfTldProcessSingleFrame();
}

/*
 * Class:     libopentld4j_OpenTLD4j
 * Method:    done
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_libopentld4j_OpenTLD4j_done(JNIEnv *, jclass) {
	libopentldTerminate();
	mclTerminateApplication();
}
