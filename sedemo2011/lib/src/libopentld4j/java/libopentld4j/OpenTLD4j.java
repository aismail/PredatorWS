package libopentld4j;

/*
 * Main class for OpenTLD for Java.
 */
public class OpenTLD4j {
	/*
	 * Load the native library.
	 */
	static {
		System.loadLibrary("opentld4j_OpenTLD4j");
	}
	
	/*
	 * Initializes the OpenTLD library.
	 */
	public static native void init();
	
	/*
	 * Tracks one frame.
	 * 
	 * The input file is temp/frame.png, in the current folder.
	 * The output file is bb.txt, in the current folder.
	 */
	public static native void trackOneFrame();
	
	/*
	 * Finish using the OpenTLD library.
	 */
	public static native void done();
	
}