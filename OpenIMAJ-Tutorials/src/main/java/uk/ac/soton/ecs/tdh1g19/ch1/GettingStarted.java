package uk.ac.soton.ecs.tdh1g19.ch1;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.hershey.HersheyFont;

import java.awt.*;

// A basic maven project created in cmd and integrated into an IDE (IntelliJ)
public class GettingStarted {
    public static void main( String[] args ) {
        // Create an image
        MBFImage image = new MBFImage(750,100, ColourSpace.RGB);

        // Fill the image with white
        image.fill(RGBColour.WHITE);

        // Render some test into the image - exercise 1
        // image.drawText("Hello World", 10, 60, HersheyFont.CURSIVE, 50, RGBColour.BLACK);
        image.drawText("Dreams are messages from the deep.", 10, 60, new GeneralFont("Times New Roman", Font.PLAIN), 50, RGBColour.ORANGE);

        // Apply a Gaussian blur
        image.processInplace(new FGaussianConvolve(2f));

        // Display the image
        DisplayUtilities.display(image);
    }
}