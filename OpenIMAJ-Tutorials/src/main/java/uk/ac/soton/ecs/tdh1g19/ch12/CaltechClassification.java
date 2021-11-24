package uk.ac.soton.ecs.tdh1g19.ch12;

import de.bwaldvogel.liblinear.SolverType;
import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.kernel.HomogeneousKernelMap;
import org.openimaj.time.Timer;
import org.openimaj.util.pair.IntFloatPair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Using the Caltech database to classify images
public class CaltechClassification {
    public static void main(String[] args) {
        try {
            Timer t1 = Timer.timer();

            // Get the Caltech dataset
            GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101.getData(ImageUtilities.FIMAGE_READER);

            // Take a subset of this database
            GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = GroupSampler.sample(allData, 5, false);

            // Split the subset into training and testing sets
            GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<>(data, 15, 0, 15);

            // Create SIFT extractor
            DenseSIFT dsift = new DenseSIFT(5, 7);
            PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<>(dsift, 6f, 7);

            // Clusterer and feature extractors - exercises 1 and 2
            HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);
            HomogeneousKernelMap mapper = new HomogeneousKernelMap(HomogeneousKernelMap.KernelType.Chi2, HomogeneousKernelMap.WindowType.Rectangular);

            FeatureExtractor<DoubleFV, Record<FImage>> extractor = new PHOWExtractor(pdsift, assigner);
            FeatureExtractor<DoubleFV, Record<FImage>> extractor1 = mapper.createWrappedExtractor(new PHOWExtractor(pdsift, assigner));
            FeatureExtractor<DoubleFV, Record<FImage>> extractor2 = new DiskCachingFeatureExtractor<>(new File("\\cache"), new PHOWExtractor(pdsift, assigner));

            // Construct and train a classifiers
            LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<>(extractor, LiblinearAnnotator.Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
            ann.train(splits.getTrainingDataset());

            // Evaluate the classification accuracy
            ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = new ClassificationEvaluator<>(ann, splits.getTestDataset(), new CMAnalyser<>(CMAnalyser.Strategy.SINGLE));
            Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
            CMResult<String> result = eval.analyse(guesses);

            System.out.println(result);
            System.out.println("Time: " + t1.duration() + "ms");

            /*
            Default Classification
            Accuracy: 0.653
            Error Rate: 0.347
            Time: 169,230ms

            Homogenous Kernel Map - slightly slower but noticeably improves the overall accuracy
            Accuracy: 0.787
            Error Rate: 0.213
            Time: 175,611ms

            Feature caching - much faster
            Accuracy: 0.680
            Error Rate: 0.320
            Time: 144318ms
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // K-means clustering
    static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift) {
        List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<>();

        for (Record<FImage> rec : sample) {
            pdsift.analyseImage(rec.getImage());
            allkeys.add(pdsift.getByteKeypoints(0.005f));
        }

        if (allkeys.size() > 10000) allkeys = allkeys.subList(0, 10000);

        ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
        DataSource<byte[]> datasource = new LocalFeatureListDataSource<>(allkeys);
        ByteCentroidsResult result = km.cluster(datasource);

        return result.defaultHardAssigner();
    }

    // Feature extraction
    static class PHOWExtractor implements FeatureExtractor<DoubleFV, Record<FImage>> {
        PyramidDenseSIFT<FImage> pdsift;
        HardAssigner<byte[], float[], IntFloatPair> assigner;

        public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner)
        {
            this.pdsift = pdsift;
            this.assigner = assigner;
        }

        public DoubleFV extractFeature(Record<FImage> object) {
            FImage image = object.getImage();
            pdsift.analyseImage(image);

            BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<>(assigner);

            BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<>(bovw, 2, 2);

            return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
        }
    }
}
