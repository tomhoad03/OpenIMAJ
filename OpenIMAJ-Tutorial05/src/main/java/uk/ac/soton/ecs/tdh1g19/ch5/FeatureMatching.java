package uk.ac.soton.ecs.tdh1g19.ch5;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;

import java.net.URL;

public class FeatureMatching {
    public static void main(String[] args) {
        try {
            MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
            MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

            // Feature extraction using a SIFT descriptor
            DoGSIFTEngine engine = new DoGSIFTEngine();
            LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
            LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

            // Feature matching
            LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<>(80);
            matcher.setModelFeatures(queryKeypoints);
            matcher.findMatches(targetKeypoints);

            // Draw the matches
            MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
            DisplayUtilities.display(basicMatches);

            // Applies an affine transform using RANSAC to learn from the initial set of matches
            RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(50.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5));
            matcher = new ConsistentLocalFeatureMatcher2d<>(new FastBasicKeypointMatcher<>(8), modelFitter);

            matcher.setModelFeatures(queryKeypoints);
            matcher.findMatches(targetKeypoints);

            MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(),
                    RGBColour.RED);

            DisplayUtilities.display(consistentMatches);

            // Draws a box around area of matches
            target.drawShape(query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
            DisplayUtilities.display(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
