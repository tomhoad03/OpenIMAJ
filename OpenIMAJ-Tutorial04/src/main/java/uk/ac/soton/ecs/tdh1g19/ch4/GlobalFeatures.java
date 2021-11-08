package uk.ac.soton.ecs.tdh1g19.ch4;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GlobalFeatures {
    public static void main(String[] args) {
        try {
            // Get the three example images
            URL[] imageURLs = new URL[] {
                    new URL( "http://openimaj.org/tutorial/figs/hist1.jpg" ),
                    new URL( "http://openimaj.org/tutorial/figs/hist2.jpg" ),
                    new URL( "http://openimaj.org/tutorial/figs/hist3.jpg" )
            };

            // Gets the image histogram from each image
            List<MultidimensionalHistogram> histograms = new ArrayList<>();
            HistogramModel model = new HistogramModel(4, 4, 4);

            for(URL u : imageURLs) {
                model.estimateModel(ImageUtilities.readMBF(u));
                histograms.add(model.histogram.clone());
            }

            for (MultidimensionalHistogram histogram : histograms) {
                System.out.println(histogram);
            }

            // Determines the euclidean distance between each pair of image histograms.
            double minimumDistance = 0, maximumDistance = 0, minimumIntersection = 0;
            int x = 0, y = 0, m = 0, n = 0, p = 0, q = 0;

            for(int i = 0; i < histograms.size(); i++) {
                for(int j = i; j < histograms.size(); j++) {
                    if (i != j) {
                        double distance = histograms.get(i).compare(histograms.get(j), DoubleFVComparison.EUCLIDEAN);
                        System.out.println("Distance between " + (i + 1) + " and " + (j + 1) + ": " + distance);

                        // By minimum distance - exercise 1
                        if (distance < minimumDistance || minimumDistance == 0) {
                            minimumDistance = distance;
                            x = i;
                            y = j;
                        }

                        // By maximum distance - exercise 1
                        if (distance > maximumDistance || minimumDistance == 0) {
                            maximumDistance = distance;
                            m = i;
                            n = j;
                        }

                        // By intersection - exercise 2
                        double intersection = histograms.get(i).compare(histograms.get(j), DoubleFVComparison.INTERSECTION);

                        if (intersection < minimumIntersection || minimumIntersection == 0) {
                            minimumIntersection = intersection;
                            p = i;
                            q = j;
                        }
                    }
                }
            }

            DisplayUtilities.display("Global Features - Most similar by euclidean distance", new MBFImage[]{ImageUtilities.readMBF(imageURLs[x]), ImageUtilities.readMBF(imageURLs[y])});
            DisplayUtilities.display("Global Features - Most dissimilar by euclidean distance", new MBFImage[]{ImageUtilities.readMBF(imageURLs[m]), ImageUtilities.readMBF(imageURLs[n])});
            DisplayUtilities.display("Global Features - Most similar by intersection", new MBFImage[]{ImageUtilities.readMBF(imageURLs[p]), ImageUtilities.readMBF(imageURLs[q])});

            /*
            The most similar by euclidean distance was expected as both have very similar colouring compared to the moon picture
            The most dissimilar was also expected as the moon and the second sunset share very little colours whereas the first sunset has bits of blue and grey
            By intersection produced an interesting result producing the opposite pairings as by euclidean distance
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
