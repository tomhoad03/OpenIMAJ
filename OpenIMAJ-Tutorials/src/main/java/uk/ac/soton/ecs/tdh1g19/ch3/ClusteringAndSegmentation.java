package uk.ac.soton.ecs.tdh1g19.ch3;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ClusteringAndSegmentation {
    public static void main(String[] args) {
        try {
            // Take an image and put it into LAB colour space
            MBFImage input = ImageUtilities.readMBF(new URL("https://cdn.mos.cms.futurecdn.net/WDWXYGSNBAWinHh2YvDtob-1024-80.jpg"));
            input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
            MBFImage clonedInput = input.clone();

            // K-Means clustering algorithm using two classes
            FloatKMeans cluster = FloatKMeans.createExact(2);

            // Flattens the image
            float[][] imageData = input.getPixelVectorNative(new float[input.getWidth() * input.getHeight()][3]);

            // Groups the pixels into their classes
            FloatCentroidsResult result = cluster.cluster(imageData);

            // Prints the coordinates of the centre of the classes/the location of the centroid
            float[][] centroids = result.centroids;
            for (float[] fs : centroids) {
                System.out.println(Arrays.toString(fs));
            }

            // Assigns pixels to classes by classification - exercise 1
            input.processInplace((PixelProcessor<Float[]>) pixel -> {
                HardAssigner<float[],?,?> assigner = result.defaultHardAssigner();

                float[] floats1 = new float[3];
                for (int i = 0; i < 3; i++){
                    floats1[i] = pixel[i];
                }
                float[] centroided = centroids[assigner.assign(floats1)];

                Float[] floats2 = new Float[3];
                for (int i = 0; i < 3; i++){
                    floats2[i] = centroided[i];
                }
                return floats2;
            });

            /*
            Pixel Processor:
            Pros: Easy to read, understand, code for as it feels like a for each loop
            Cons: Felt a bit slower than using the loops
             */

            // A set of pixels of the same class become a connected component
            GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();
            List<ConnectedComponent> components = labeler.findComponents(input.flatten());

            // Finds a cluster of touching >50 pixels to form a connected component by segmentation
            int i = 0;
            for (ConnectedComponent comp : components) {
                if (comp.calculateArea() < 50)
                    continue;
                input.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
            }

            input = ColourSpace.convert(input, ColourSpace.RGB);
            // DisplayUtilities.display(input);

            // FelzenszwalbHuttenlocherSegmenter - exercise 2
            FelzenszwalbHuttenlocherSegmenter<MBFImage> segmenter = new FelzenszwalbHuttenlocherSegmenter<>(0, 250, 50);
            SegmentationUtilities.renderSegments(clonedInput, segmenter.segment(clonedInput));

            DisplayUtilities.display(clonedInput);

            /*
            FelzenszwalbHuttenlocherSegmenter:
            Pros: Reduced the noise created before and hence provided a better segmentation
            Cons: Seemed to run much slower than the adaptive thresholding, hard to balance weighting
             */
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}