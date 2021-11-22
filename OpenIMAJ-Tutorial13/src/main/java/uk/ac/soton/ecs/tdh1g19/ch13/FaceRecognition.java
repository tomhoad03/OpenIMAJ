package uk.ac.soton.ecs.tdh1g19.ch13;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

import java.util.*;

public class FaceRecognition {
    public static void main(String[] args) {
        try {
            // Collect a dataset of faces
            VFSGroupDataset<FImage> dataset = new VFSGroupDataset<>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

            // Splitting the dataset into a training set and a testing set
            int nTraining = 5;
            int nTesting = 5;
            GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<>(dataset, nTraining, 0, nTesting);
            GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
            GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

            // Eigenfaces face recognition algorithm

            // Learns the PCA basis
            List<FImage> basisImages = DatasetAdaptors.asList(training);
            int nEigenvectors = 100;
            EigenImages eigen = new EigenImages(nEigenvectors);
            eigen.train(basisImages);

            // Draws the first 12 (change to 50) basis vectors - exercise 2
            List<FImage> eigenFaces = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                eigenFaces.add(eigen.visualisePC(i));
            }

            // Creates a database of features
            Map<String, DoubleFV[]> features = new HashMap<>();
            for (final String person : training.getGroups()) {
                final DoubleFV[] fvs = new DoubleFV[nTraining];

                for (int i = 0; i < nTraining; i++) {
                    final FImage face = training.get(person).get(i);
                    fvs[i] = eigen.extractFeature(face);
                }
                features.put(person, fvs);
            }

            // Performs feature extraction on each face
            double correct = 0, incorrect = 0;
            for (String truePerson : testing.getGroups()) {
                for (FImage face : testing.get(truePerson)) {
                    DoubleFV testFeature = eigen.extractFeature(face);

                    String bestPerson = null;
                    double minDistance = Double.MAX_VALUE;
                    for (final String person : features.keySet()) {
                        for (final DoubleFV fv : features.get(person)) {
                            double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);
                            double threshold = 12;

                            // Adding a threshold - exercise three
                            if (distance < minDistance) {
                                if (distance <= threshold) {
                                    minDistance = distance;
                                    bestPerson = person;
                                }
                            }
                            /*
                            The distance was often in the range of just of 20 to just under 10.
                            I chose to set the threshold to 10 for the first test.
                            This significantly decreased the overall accuracy to about 84%.
                            Decreasing this threshold would really decrease the accuracy.
                            Setting the threshold to twelve gave a ~92% accuracy which may be a good threshold.
                             */
                        }
                    }

                    System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

                    if (truePerson.equals(bestPerson))
                        correct++;
                    else
                        incorrect++;
                }
            }
            System.out.println("Accuracy: " + (correct / (correct + incorrect)));

            DisplayUtilities.display("EigenFaces", eigenFaces);

            // Reconstructing faces - exercise 1
            DoubleFV testFeature = eigen.extractFeature(testing.getRandomInstance());
            DisplayUtilities.display(eigen.reconstruct(testFeature).normalise());

            /*
            Using fewer images for the training set greatly increased the variance of the accuracy for the facial recognition.
            Sometimes it would give me a really accurate result and other times it was worse than before.
            Increasing the number of images gave me the same ~93% very consistently.
            Using PCA for facial recognition is unlikely to give an accuracy of higher than 93% using a sensible training set size.
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
