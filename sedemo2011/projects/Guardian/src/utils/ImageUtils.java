package utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {
	public static void saveMemoryImageSourceToGrayscaleFile(MemoryImageSource image, 
													 		String fileType,
													 		String pathToFile,
													 		String pathToTempFile) {
		if (image == null)
			return;
		
		Image imageToBeWritten = Toolkit.getDefaultToolkit().createImage(image);
        BufferedImage buffer = new BufferedImage(imageToBeWritten.getWidth(null), 
        										 imageToBeWritten.getHeight(null), 
        										 BufferedImage.TYPE_BYTE_GRAY);
        buffer.getGraphics().drawImage(imageToBeWritten, 0, 0, null);
                
        try {
                FileOutputStream os = new FileOutputStream(pathToTempFile);
                FileDescriptor fd = os.getFD();
                ImageIO.write(buffer, fileType, os);
                os.flush();
                fd.sync();
                os.close();
        } catch(IOException ex) {
                ex.printStackTrace();
        }
        
        File file = new File(pathToFile);
        File file2 = new File(pathToTempFile);
        file2.renameTo(file);
	}
	
}