package uk.ac.soton.ecs.tdh1g19.ch5;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.*;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
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

            // Feature matching - exercise 1
            LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<>(80);
            LocalFeatureMatcher<Keypoint> matcher1 = new BasicTwoWayMatcher<>();
            LocalFeatureMatcher<Keypoint> matcher2 = new FastLimitedEuclideanKeypointMatcher<>(80);
            LocalFeatureMatcher<Keypoint> matcher3 = new VotingKeypointMatcher<>(80);
            matcher.setModelFeatures(queryKeypoints);
            matcher.findMatches(targetKeypoints);

            /*
            BasicTwoWayMatcher - uses Euclidean distances,
                                    produces fewer matches than BasicMatcher
            FastLimitedEuclideanKeypointMatcher - uses a ByteKDTree to find nearest neighbours more efficiently,
                                                    produces a lot fewer matches before the transform but a lot more after the transform
            VotingKeypointMatcher - rejects matches with no local support,
                                        similar results to BasicMatcher
             */

            // Draw the matches
            MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
            DisplayUtilities.display(basicMatches);

            // Applies an affine transform using RANSAC to learn from the initial set of matches - exercise 2
            RobustAffineTransformEstimator modelFitter2 = new RobustAffineTransformEstimator(50.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5));
            RobustHomographyEstimator modelFitter1 = new RobustHomographyEstimator(50.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5), HomographyRefinement.SYMMETRIC_TRANSFER);
            RobustHomographyEstimator modelFitter = new RobustHomographyEstimator(0.2, HomographyRefinement.NONE);

            /*
            NONE - seemed to improve the bounding box accuracy, some matches were incorrect
            SYMMETRIC_TRANSFER - reduced the number of incorrect matches
            SINGLE_IMAGE_TRANSFER - increased the bounding box to make up for the incorrect matches

            LMedS - was possible to reduce outliers by tweaking outlier proportion
             */

            matcher = new ConsistentLocalFeatureMatcher2d<>(new FastBasicKeypointMatcher<>(8), modelFitter);
            matcher.setModelFeatures(queryKeypoints);
            matcher.findMatches(targetKeypoints);

            MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
            DisplayUtilities.display(consistentMatches);

            // Draws a box around area of matches
            target.drawShape(query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
            DisplayUtilities.display(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
