package uk.ac.soton.ecs.tdh1g19.ch14;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.RangePartitioner;

import java.util.ArrayList;
import java.util.List;

public class ParallelProcessing {
    public static void main(String[] args) {
        try {
            Parallel.forIndex(0, 10, 1, System.out::println);

            // Get the Caltech database
            VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);

            // Get a sample of the dataset
            GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);

            // Image processing
            List<MBFImage> output = new ArrayList<>();
            ResizeProcessor resize = new ResizeProcessor(200);
            Timer t1 = Timer.timer();

            // Parallel processing - exercise 1
            Parallel.forEach(images.values(), i -> {
                final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

                Parallel.forEachPartitioned(new RangePartitioner<>(i), it -> {
                    MBFImage tmpAccum = new MBFImage(200, 200, 3);
                    MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);

                    while (it.hasNext()) {
                        final MBFImage j = it.next();
                        tmp.fill(RGBColour.WHITE);

                        final MBFImage small = j.process(resize).normalise();
                        final int x = (200 - small.getWidth()) / 2;
                        final int y = (200 - small.getHeight()) / 2;
                        tmp.drawImage(small, x, y);
                        tmpAccum.addInplace(tmp);
                    }
                    synchronized (current) {
                        current.addInplace(tmpAccum);
                    }
                });
                current.divideInplace((float) i.size());
                output.add(current);
            });

            DisplayUtilities.display("Images", output);
            System.out.println("Time: " + t1.duration() + "ms");

            // 15409ms - no parallelization
            // 4545ms - parallelization of the inner loop
            // 4400ms - improved parallelization of the inner loop

            // 8536ms - parallelization of outer loop (with original inner loop)
            // 4046ms - parallelization of outer loop (with first parallelized inner loop)
            // 4010ms - parallelization of outer loop (with second parallelized inner loop)

            // not a noticeable performance difference (sometimes slower, sometimes faster) with both parallelized, with a fair amount of variance
            // overall, using parallelization made a massive performance difference when used on the inner loop, and a decent amount on the inner loop
            // very dependent on the hardware of the actual machine, puts a greater demand on the resources of the machine
            // when used on both the outer and inner loop, this increased the demand without a valuable enough improve in speed compared to just making the inner loop run in parallel
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
