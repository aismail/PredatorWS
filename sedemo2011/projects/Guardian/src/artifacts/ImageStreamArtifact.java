package artifacts;

import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;

import cartago.Artifact;
import cartago.OPERATION;

/*
 * Image stream artifact.
 * 
 * Takes an image and displays it in a frame.
 */
public class ImageStreamArtifact extends Artifact {
	
	private ImageStreamFrame frame;
	
	/*
	 * Public constructor.
	 */
	public ImageStreamArtifact() {
		frame = new ImageStreamFrame();
		frame.setVisible(true);
	}
	
	/*
	 * Display a given image.
	 */
	@OPERATION void displayImage(MemoryImageSource image, Rectangle face, String name) {
		frame.setNewFrameForDisplay(image, face, name);
	}
}
