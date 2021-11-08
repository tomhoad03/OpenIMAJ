package uk.ac.soton.ecs.tdh1g19.ch4;

import org.openimaj.feature.DoubleFVComparison;
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
            MBFImage image = ImageUtilities.readMBF(new URL("https://cdn.mos.cms.futurecdn.net/WDWXYGSNBAWinHh2YvDtob-1024-80.jpg"));

            URL[] imageURLs = new URL[] {
                    new URL( "http://openimaj.org/tutorial/figs/hist1.jpg" ),
                    new URL( "http://openimaj.org/tutorial/figs/hist2.jpg" ),
                    new URL( "http://openimaj.org/tutorial/figs/hist3.jpg" )
            };

            List<MultidimensionalHistogram> histograms = new ArrayList<>();
            HistogramModel model = new HistogramModel(4, 4, 4);

            for(URL u : imageURLs) {
                model.estimateModel(ImageUtilities.readMBF(u));
                histograms.add(model.histogram.clone());
            }
            System.out.println(histograms.get(0));

            double distanceScore = histograms.get(0).compare(histograms.get(1), DoubleFVComparison.EUCLIDEAN);
            System.out.println("Distance between 1 and 2: " + distanceScore);

            for(int i = 0; i < histograms.size(); i++) {
                for(int j = i; j < histograms.size(); j++) {
                    double distance = histograms.get(i).compare(histograms.get(j), DoubleFVComparison.EUCLIDEAN);
                    System.out.println("Distance between " + (i + 1) + " and " + (j + 1) + ": " + distance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
