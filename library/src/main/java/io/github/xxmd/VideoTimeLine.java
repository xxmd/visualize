package io.github.xxmd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoTimeLine extends FrameLayout {
    private String videoFilePath;
    private List<Bitmap> bitmapList = new ArrayList<>();

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
        renderVideo();
    }

    private void renderVideo() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFilePath);
        long frameCount = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
        int videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        float ratio = videoWidth * 1.0f / videoHeight;

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bitmapList = new ArrayList<>();
        VideoFrameAdapter videoFrameAdapter = new VideoFrameAdapter(bitmapList);
        recyclerView.setAdapter(videoFrameAdapter);

        ImageView ivCover = findViewById(R.id.iv_cover);
        ViewGroup.LayoutParams layoutParams = ivCover.getLayoutParams();
        layoutParams.width = (int) (layoutParams.height / ratio);
        ivCover.setLayoutParams(layoutParams);
//        Bitmap frameAtTime = retriever.getFrameAtTime();
////        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        ivCover.setImageBitmap(frameAtTime);
    }



    public VideoTimeLine(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoTimeLine(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.video_time_line, this);
    }

}
