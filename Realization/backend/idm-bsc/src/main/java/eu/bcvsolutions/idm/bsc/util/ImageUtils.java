package eu.bcvsolutions.idm.bsc.util;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

public class ImageUtils {

	/**
	 * Prebrano ze stack overflow: 
	 * http://stackoverflow.com/questions/7603400/how-to-make-a-rounded-corner-image-in-java
	 * 
	 */
	public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();

	    // This is what we want, but it only does hard-clipping, i.e. aliasing
	    // g2.setClip(new RoundRectangle2D ...)

	    // so instead fake soft-clipping by first drawing the desired clip shape
	    // in fully opaque white with antialiasing enabled...
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

	    // ... then compositing the image on top,
	    // using the white shape from above as alpha source
	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(image, 0, 0, null);

	    g2.dispose();

	    return output;
	}
	
	public static BufferedImage readFromFile(String fileName) throws IOException {
		return ImageIO.read(new File(fileName));
	}
	
	public static void writeToFile(BufferedImage bi, String type, String fileName) throws IOException {
		ImageIO.write(bi, type, new File(fileName));
	}
	
	public static String createBCBackground(Color mainBody, Color cornerTop, 
			Color cornerBottom, boolean transparent) throws IOException {
		
		String fileName = "/tmp/" + UUID.randomUUID().toString() + ".png";
		createBCBackground(fileName, mainBody, cornerTop, cornerBottom, transparent);
		return fileName;
	}
	
	public static void createBCBackground(String fileName, Color mainBody, 
			Color cornerTop, Color cornerBottom, boolean transparent) throws IOException {
		
		BufferedImage bi = new BufferedImage(850, 540, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		
		g.setColor(cornerTop);
		g.fillRect(800, 0, 50, 540 / 2);
		g.setColor(cornerBottom);
		g.fillRect(800, 540 / 2, 50, 540 / 2);
		if (!transparent) {
			g.setColor(mainBody);
			g.fillRect(0, 0, 800, 540);
		}
		g.dispose();
		
		int w = bi.getWidth();
	    int h = bi.getHeight();
		BufferedImage output = new BufferedImage(850, 540, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = output.createGraphics();
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 75, 75));
	    
	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(bi, 0, 0, null);
		
		if (transparent) {
			g2.setComposite(AlphaComposite.Src);
			g2.setColor(mainBody);
			g2.fillRect(0, 0, 800, 540);
		}
	   g2.dispose();

		writeToFile(output, "png", fileName);
	}
	

}
