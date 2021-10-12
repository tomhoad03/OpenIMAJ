import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

import java.net.URL;

public class ProcessingImages {
    public static void main( String[] args ) {
        try {
            // Use ImageUtilities to get image and show colour space
            MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));
            System.out.println(image.colourSpace);

            // Display image
            DisplayUtilities.displayName(image, "Chapter 2");
            DisplayUtilities.displayName(image.getBand(0), "Chapter 2");

            // Set all green and blue pixels to black
            MBFImage clone = image.clone();
            for (int y=0; y<image.getHeight(); y++) {
                for(int x=0; x<image.getWidth(); x++) {
                    clone.getBand(1).pixels[y][x] = 0;
                    clone.getBand(2).pixels[y][x] = 0;
                }
            }
            DisplayUtilities.displayName(clone, "Chapter 2");

            // Code above simplified
            clone.getBand(1).fill(0f);
            clone.getBand(2).fill(0f);
            DisplayUtilities.displayName(clone, "Chapter 2");

            // Processor interfaces: ImageProcessors, KernelProcessors, PixelProcessors and GridProcessors

            // Edge detection
            image.processInplace(new CannyEdgeDetector());
            DisplayUtilities.displayName(image, "Chapter 2");

            // Drawing on the image
            image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
            image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
            image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
            image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);

            image.drawShape(new Ellipse(700f, 450f, 20f, 10f, 0f), 2, RGBColour.RED);
            image.drawShape(new Ellipse(650f, 425f, 25f, 12f, 0f), 2, RGBColour.RED);
            image.drawShape(new Ellipse(600f, 380f, 30f, 15f, 0f), 2, RGBColour.RED);
            image.drawShape(new Ellipse(500f, 300f, 100f, 70f, 0f), 2, RGBColour.RED);

            image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
            image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
            DisplayUtilities.displayName(image, "Chapter 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}