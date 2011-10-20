package artifacts;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.Timer;
import java.util.TimerTask;

import utils.Constants;

public class ImageStreamFrame extends Frame {
	/**
	 * Generated serial UID.
	 */
	private static final long serialVersionUID = 8946240696138423011L;
	private MemoryImageSource image;
	private Rectangle face;
	private String name;
	private Timer timer;
	private Object lock;
	private Image buffer;
	
	/*
	 * Build a new frame.
	 */
	public ImageStreamFrame() {
		super("Image Stream");
		lock = new Object();
		setBounds( 100, 100, 640, 480);
		setBackground( Color.BLACK );
		
		// Start a task which forces update 10 times per second.
		timer = new Timer();
	    timer.schedule(new TimerTask() {
	    	public void run() {
	    		update(getGraphics());
	            getToolkit().sync();
	    	}
	    }, 1000, //initial delay
	       50); //subsequent rate
	}
	
	/*
	 * Deliver a new frame to the image stream displaying frame.
	 */
	public void setNewFrameForDisplay(MemoryImageSource image, Rectangle face, String name) {
		synchronized(lock) {
			this.image = image;
			this.face = face;
			this.name = name;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Window#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		MemoryImageSource imageToDraw = null;
		Rectangle faceToDraw = null;
		String nameToDraw = null;
		
		synchronized(lock) {
			imageToDraw = image;
			faceToDraw = face;
			nameToDraw = name;
		}
		
		if (imageToDraw == null)
			return;
		
		Image currentImage = this.createImage(imageToDraw);
		if (buffer == null) {
			buffer = new BufferedImage(currentImage.getWidth(null), currentImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		}
		
		Graphics bufferGraphics = buffer.getGraphics();
		bufferGraphics.drawImage(currentImage, 0, 0, null);
		bufferGraphics.setColor( Color.RED );
		if (faceToDraw != null && faceToDraw != Constants.NULL_RECTANGLE) {
			bufferGraphics.drawRect(faceToDraw.x, faceToDraw.y, faceToDraw.width, faceToDraw.height);
			if (nameToDraw != null && nameToDraw.length() > 0)
				bufferGraphics.drawChars(nameToDraw.toCharArray(), 0, nameToDraw.length(), faceToDraw.x, faceToDraw.y - 15);
		}
		
		g.drawImage(buffer, 0, 0, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Container#update(java.awt.Graphics)
	 */
	public void update(Graphics g) {
		paint(g);
	}
}