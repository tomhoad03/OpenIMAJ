package uk.ac.soton.ecs.tdh1g19.ch3;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.PixelProcessor;
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
            MBFImage input = ImageUtilities.readMBF(new URL("https://cdn.mos.cms.futurecdn.net/WDWXYGSNBAWinHh2YvDtob-1024-80.jpg"));
            input = ColourSpace.convert(input, ColourSpace.CIE_Lab);

            /*
            clustering - groups similar items together
            using k-means clustering
            converts the colour from RGB to another using the Euclidean distance to retain image
             */
            FloatKMeans cluster = FloatKMeans.createExact(2);
            float[][] imageData = input.getPixelVectorNative(new float[input.getWidth() * input.getHeight()][3]);
            FloatCentroidsResult result = cluster.cluster(imageData);
            float[][] centroids = result.centroids;

            // prints the coordinates of the classes
            for (float[] fs : centroids) {
                System.out.println(Arrays.toString(fs));
            }

            // assigns pixels to classes by classification
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

            // a set of pixels of the same class become a connected component
            GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();
            List<ConnectedComponent> components = labeler.findComponents(input.flatten());

            // displays the different class areas
            int i = 0;
            for (ConnectedComponent comp : components) {
                if (comp.calculateArea() < 50)
                    continue;
                input.drawText("Point:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
            }

            input = ColourSpace.convert(input, ColourSpace.RGB);
            DisplayUtilities.display(input);

            // FelzenszwalbHuttenlocherSegmenter
            MBFImage input2 = ImageUtilities.readMBF(new URL("https://cdn.mos.cms.futurecdn.net/WDWXYGSNBAWinHh2YvDtob-1024-80.jpg"));
            //input2 = ColourSpace.convert(input2, ColourSpace.CIE_Lab);

            GreyscaleConnectedComponentLabeler labeler2 = new GreyscaleConnectedComponentLabeler();
            List<ConnectedComponent> components2 = labeler.findComponents(input2.flatten());
            SegmentationUtilities.renderSegments(input2, components2);

            int j = 0;
            for (ConnectedComponent comp : components2) {
                if (comp.calculateArea() < 50)
                    continue;
                input.drawText("Point:" + (j++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
            }

            //input2 = ColourSpace.convert(input2, ColourSpace.RGB);
            DisplayUtilities.display(input2);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}