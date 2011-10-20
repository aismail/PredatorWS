package artifacts;

import utils.Constants;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import hypermedia.video.OpenCV;

import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;

/*
 * Webcam artifact. Uses OpenCV in order to take input from the webcam.
 */
public class WebcamArtifact extends Artifact {
	
	private Object lock;
	private boolean faceDetectionEnabled;
    private Rectangle[] faces;
    private MemoryImageSource image;
    private OpenCV cv;
	
    /*
     * Constructor.
     */
	public WebcamArtifact() {
		lock = new Object();
		faceDetectionEnabled = false;
		cv = new OpenCV();
        cv.capture(640, 480);
        cv.cascade(OpenCV.CASCADE_FRONTALFACE_ALT);
        Thread grabFrameThread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					// We don't need to lock the shared data structure
					// while communicating with the webcam. The actual
					// shared resource is the buffer.
					cv.read();
					MemoryImageSource frame = new MemoryImageSource( cv.width, cv.height, cv.pixels(), 0, cv.width );
					
					synchronized(lock) {
						image = frame; 
						// Also do face detection if it's enabled.
						if (faceDetectionEnabled) {
							faces = cv.detect( 1.2f, 2, OpenCV.HAAR_DO_CANNY_PRUNING, 20, 20 );
						} else {
							faces = null;
						}
					}
					
					// Just in case cv.read() doesn't block, we
					// force our way out of busy-waiting.
					try {
						Thread.sleep(10);
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		grabFrameThread.start();
	}

	/*
	 * Get a frame from the webcam.
	 */
	@OPERATION void getWebcamFrame(OpFeedbackParam<MemoryImageSource> mis, OpFeedbackParam<Rectangle> face) {
		synchronized(lock) {
			mis.set(image);
			if (faces == null || faces.length == 0)
				face.set(Constants.NULL_RECTANGLE);
			else
				face.set(faces[0]);
		}
	}
	
	/*
	 * Enable face detection.
	 */
	@OPERATION void enableFaceDetection() {
		synchronized(lock) {
			faceDetectionEnabled = true;
		}
	}
	
	/*
	 * Disables face detection.
	 */
	@OPERATION void disableFaceDetection() {
		synchronized(lock) {
			faceDetectionEnabled = false;
		}
	}
	
	/*
	 * Get the null face.
	 */
	@OPERATION void getNullFace(OpFeedbackParam<Rectangle> face) {
		face.set(Constants.NULL_RECTANGLE);
	}
	
}
