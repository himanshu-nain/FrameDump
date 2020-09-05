package com.example.framedump;

import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FrameExtractionJob implements Runnable {

    private static final String TAG = FrameExtractionJob.class.getSimpleName();

    private final Integer videoIndex;
    private final String absoluteVideoPath;
    private final String videoName;
    private ExtractionCompleteListener extractionCompleteListener;

    public FrameExtractionJob(Integer videoIndex, String absoluteVideoPath, String fileName, @Nullable ExtractionCompleteListener listener) {
        this.videoIndex = videoIndex;
        this.absoluteVideoPath = absoluteVideoPath;
        this.videoName = fileName;
        this.extractionCompleteListener = listener;
    }

    @Override
    public void run() {

        int frameRate = getFrameRate();

        loadAndSaveFrames(frameRate);

//        List<Bitmap> frameList = getFrameList(frameRate);
//
//        try {
//            saveToDisk(frameList);
//        } catch (IOException e) {
//            Log.e("FRAME EXTRACTION JOB", "Error", e);
//        }

    }

    /*
    private void saveToDisk(List<Bitmap> frameList) throws IOException {

        if(frameList.size() == 0)
            return;


        for(int i=0; i<frameList.size(); ++i) {

            saveToDisk(frameList.get(i), i);

        }


    }
    */

    private void saveToDisk(Bitmap bmp, int index) throws IOException {

        if(bmp == null)
            return;


        File mediaFile = getOutputMediaFile(index);
        if(!mediaFile.exists())
            mediaFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(mediaFile);
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();


    }

    private File getOutputMediaFile(int index) {

        File rootDir = new File(Environment.getExternalStorageDirectory(), "FRAME_EXTRACTOR");
        if(!rootDir.exists() || !rootDir.isDirectory()){
            rootDir.mkdirs();
        }

        File videoFramesFolder = new File(rootDir, this.videoName);
        if(!videoFramesFolder.exists() || !videoFramesFolder.isDirectory()){
            videoFramesFolder.mkdirs();
        }

        String mCurrentPath = videoFramesFolder.getAbsolutePath() + "/" + index + ".jpg";
        return new File(mCurrentPath);
    }

    //private List<Bitmap> getFrameList(int frameRate) {
    private void loadAndSaveFrames(int frameRate) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //List<Bitmap> frameList = new ArrayList<>();

        try {
            retriever.setDataSource(absoluteVideoPath);

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            assert duration != null;
            int durationSec = Integer.parseInt(duration) / 1000;

            int no_of_frames = frameRate * durationSec;

            for(int i=0; i < no_of_frames; i++) {

                // TEmp
                saveToDisk(retriever.getFrameAtIndex(i), i);

                //frameList.add(retriever.getFrameAtIndex(i));
            }

            this.extractionCompleteListener.onExtractionComplete(this.videoIndex);

        }
        catch (Exception e) {
            Log.e(TAG, "Some error occurred at path " + absoluteVideoPath, e);
        } finally {
            retriever.release();
        }

        //return frameList;
    }

    private int getFrameRate() {
        int frameRate = 20;

        MediaExtractor extractor = new MediaExtractor();

        File file = new File(this.absoluteVideoPath);
        FileInputStream fileInputStream;

        try{

            fileInputStream = new FileInputStream(file);
            FileDescriptor fd = fileInputStream.getFD();

            extractor.setDataSource(fd);
            int numTracks = extractor.getTrackCount();
            for(int i=0; i<numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                if(format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Some error occurred at path " + this.absoluteVideoPath, e);
        } finally {
            extractor.release();
        }

        return frameRate;

    }

}
