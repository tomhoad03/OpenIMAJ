package uk.ac.soton.ecs.tdh1g19.ch12;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;

public class CaltechClassification {
    public static void main(String[] args) {
        try {
            GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101.getData(ImageUtilities.FIMAGE_READER);

            GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = GroupSampler.sample(allData, 5, false);

            GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<>(data, 15, 0, 15);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
