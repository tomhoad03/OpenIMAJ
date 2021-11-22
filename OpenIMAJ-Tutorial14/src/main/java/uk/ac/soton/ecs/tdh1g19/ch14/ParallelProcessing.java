package uk.ac.soton.ecs.tdh1g19.ch14;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.util.parallel.Parallel;

public class ParallelProcessing {
    public static void main(String[] args) {
        try {
            Parallel.forIndex(0, 10, 1, System.out::println);

            VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
