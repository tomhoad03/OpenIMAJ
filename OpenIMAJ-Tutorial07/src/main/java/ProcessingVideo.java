import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

import java.net.URL;

public class ProcessingVideo {
    public static void main(String[] args) {
        try {
            // gets a video using Xuggle library
            Video<MBFImage> video = new XuggleVideo(new URL("http://static.openimaj.org/media/tutorial/keyboardcat.flv"));
            VideoDisplay<MBFImage> display1 = VideoDisplay.createVideoDisplay(video);

            // can get video from the webcam
            video = new VideoCapture(320, 240);

            // displays the video as a series of images and processes the images
            for (MBFImage mbfImage : video) {
                DisplayUtilities.displayName(mbfImage.process(new CannyEdgeDetector()), "videoFrames");
            }

            // an alternate approach using an event driven technique
            VideoDisplay<MBFImage> display2 = VideoDisplay.createVideoDisplay(video);
            display2.addVideoListener(
                    new VideoDisplayListener<>() {
                        public void beforeUpdate(MBFImage frame) {
                            frame.processInplace(new CannyEdgeDetector());
                        }

                        public void afterUpdate(VideoDisplay<MBFImage> display) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
