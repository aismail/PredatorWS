package artifacts;

import java.awt.image.MemoryImageSource;
import java.io.File;
import utils.ImageUtils;

import com.github.mhendred.face4j.DefaultFaceClient;
import com.github.mhendred.face4j.FaceClient;
import com.github.mhendred.face4j.model.Photo;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class FaceIdentifierArtifact extends Artifact {
	// Keys to connect to our account on face.com
    private static final String API_KEY = "648086a5c9b7f5da95ed6894fe906f34";
    private static final String API_SEC = "e50eef3c7197bf85498a27c9068472ca";
    // Namespace on face.com created for this project
    private static final String NAMESPACE = "opencv";
    // The list of user ids to search for in this queries
    private static final String USER_ID = "all@" + NAMESPACE;
    private FaceClient faceClient;
    
    /*
     * Constructor.
     */
    public FaceIdentifierArtifact() {
    	faceClient = new DefaultFaceClient(API_KEY, API_SEC);
    }
    
	/*
	 * Identify a face in a given picture.
	 */
	@OPERATION void identifyFace(MemoryImageSource image, OpFeedbackParam<String> name) {
		try {
		ImageUtils.saveMemoryImageSourceToGrayscaleFile(image, "png", "temp/face_identification.png", "temp/face_identification2.png");
		File img = new File("temp/face_identification.png");
		Photo photo = null;
		
		try {
			photo = faceClient.recognize(img, USER_ID);
        } catch (Exception ex) {
            ex.printStackTrace();
            photo = null;
        }
        
        if (photo == null) {
        	name.set("Unknown");
        	return;
        }
        
        if (photo.getFace() == null || 
        	photo.getFace().getGuess() == null) {
        	name.set("Unknown");
        } else {
        	String guess = photo.getFace().getGuess().first;
        	if (guess.equals("andrei@opencv"))
        		name.set("Andrei ISMAIL");
        	else if (guess.equals("tudor@opencv"))
        		name.set("Tudor BERARIU");
        	else 
        		name.set("Unknown");
        }
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
}