package artifacts;

import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import libopentld4j.OpenTLD4j;

import utils.Constants;
import utils.ImageUtils;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class FaceTrackerArtifact extends Artifact {
	
	/*
	 * Starts tracking a given face.
	 * 
	 * Input: initial image, and initial bounding box.
	 */
	@OPERATION public void startTrackingFace(MemoryImageSource image, Rectangle face) {
		ImageUtils.saveMemoryImageSourceToGrayscaleFile(image, "png", "temp/frame.png", "temp/frame2.png");
		try {
			FileOutputStream os = new FileOutputStream("temp/init.txt");
			OutputStreamWriter osw = new OutputStreamWriter(os);
			int x1 = face.x;
			int y1 = face.y;
			int x2 = face.x + face.width;
			int y2 = face.y + face.height;
			String boundingBox = x1 + "," + y1 + "," + x2 + "," + y2;
			osw.write(boundingBox, 0, boundingBox.length());
			osw.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		OpenTLD4j.init();
	}
	
	/*
	 * Continue tracking a given face.
	 * 
	 * Input: current image
	 * Output: current bounding box
	 */
	@OPERATION public void continueTrackingFace(MemoryImageSource image, OpFeedbackParam<Rectangle> face) {
		double a = 0.0, b = 0.0, c = 0.0, d = 0.0;
        int aa, bb, cc, dd;
        String line;
		
		ImageUtils.saveMemoryImageSourceToGrayscaleFile(image, "png", "temp/frame.png", "temp/frame2.png");
		OpenTLD4j.trackOneFrame();
		
		try {
            File f = new File("bb.txt");
            if (f.length() == 16) {
                face.set(Constants.NULL_RECTANGLE);
                return;
            }
            
            FileInputStream fis = new FileInputStream("bb.txt");
            DataInputStream dis = new DataInputStream(fis);
            InputStreamReader iis = new InputStreamReader(dis);
            BufferedReader bis = new BufferedReader(iis);

            line = bis.readLine();
            if (!line.equals("NaN")) {
                a = new Double(line);
            }

            line = bis.readLine();
            if (!line.equals("NaN")) {
                b = new Double(line);
            }

            line = bis.readLine();
            if (!line.equals("NaN")) {
                c = new Double(line);
            }

            line = bis.readLine();
            if (!line.equals("NaN")) {
                d = new Double(line);
            }

            dis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            face.set(Constants.NULL_RECTANGLE);
        }

        aa = (int) a;
        bb = (int) b;
        cc = (int) c;
        dd = (int) d;

        face.set(new Rectangle(aa, bb, cc - aa, dd - bb));
	}
}