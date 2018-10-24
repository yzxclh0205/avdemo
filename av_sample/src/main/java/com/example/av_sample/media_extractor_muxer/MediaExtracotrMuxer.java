package com.example.av_sample.media_extractor_muxer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 从MP4中提取视频并生成新的视频文件
 * 1.找到视频文件索引 并读取 2.将视频信息写入文件
 */

public class MediaExtracotrMuxer extends AppCompatActivity implements View.OnClickListener {


    private MediaMuxer mediaMuxer;
    private int frameRate;
    private int mVideoTrackIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startExtractorMuxer();
    }

    private String inputPath = Environment.getExternalStorageDirectory()+ File.separator+"3.mp4";
    private String outputPath = Environment.getExternalStorageDirectory()+ File.separator+"4.mp4";
    private void startExtractorMuxer() {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(inputPath);
            int trackCount = extractor.getTrackCount();
            for(int i=0;i<trackCount;i++){
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                if(!mine.startsWith("video/")){
                    continue;
                }
                frameRate = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                extractor.selectTrack(i);
                mediaMuxer = new MediaMuxer(outputPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mVideoTrackIndex = mediaMuxer.addTrack(trackFormat);
                mediaMuxer.start();
            }

            if(mediaMuxer == null){
                return;
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1000);
            int sampleSize = 0;
            while((sampleSize = extractor.readSampleData(buffer,0))>0){
                bufferInfo.size = sampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                bufferInfo.presentationTimeUs += 1000 * 1000/frameRate;
                mediaMuxer.writeSampleData(mVideoTrackIndex,buffer,bufferInfo);
                extractor.advance();
            }
            extractor.release();
            mediaMuxer.stop();
            mediaMuxer.release();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }
}
