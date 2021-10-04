package org.openimaj;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

import java.io.IOException;
import java.net.URL;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        try {
            // Use ImageUtilities to get image and show colour space
            MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));
            System.out.println(image.colourSpace);

            // Display image
            DisplayUtilities.display(image);
            DisplayUtilities.display(image.getBand(0), "Red Channel");

            // Set all green and blue pixels to black
            MBFImage clone = image.clone();
            for (int y=0; y<image.getHeight(); y++) {
                for(int x=0; x<image.getWidth(); x++) {
                    clone.getBand(1).pixels[y][x] = 0;
                    clone.getBand(2).pixels[y][x] = 0;
                }
            }
            DisplayUtilities.display(clone);

            // Code above simplified
            clone.getBand(1).fill(0f);
            clone.getBand(2).fill(0f);
            DisplayUtilities.display(clone);

            // Processor interfaces: ImageProcessors, KernelProcessors, PixelProcessors and GridProcessors

            // Edge detection
            image.processInplace(new CannyEdgeDetector());
            DisplayUtilities.display(image);

            // Drawing on the image
            image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
            image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
            image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
            image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
            image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
            image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
            DisplayUtilities.display(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
