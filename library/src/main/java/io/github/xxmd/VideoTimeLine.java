package io.github.xxmd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.HashMap;
import java.util.Map;

public class VideoTimeLine extends View {
    private int frameWidth = 120;
    private int frameHeight;
    private TextPaint textPaint;
    private Paint dotPaint;
    private int textHeight;
    private int textWidth;

    private String videoFilePath = "";
    private float frameRatio;
    private MediaMetadataRetriever retriever;
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private float preX;
    private float preY;
    private int minimumFlingVelocity;
    private int scrollDistance;
    private long totalWidth;
    private boolean moving = false;
    private int scrollThreshold;
    private int startSecond;
    private int transferX;
    private long duration;
    private Map<Integer, Bitmap> videoFrameBuffer = new HashMap<>();

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
        initVideoInfo();
    }

    private void initVideoInfo() {
        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFilePath);
        duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        int frameRealWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int frameRealHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        frameRatio = frameRealWidth * 1.0f / frameRealHeight;
        frameHeight = (int) (frameWidth / frameRatio);
        totalWidth = (long) (frameWidth * duration / 1000f);

        invalidate();
    }

    public VideoTimeLine(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoTimeLine(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("", String.valueOf(scrollDistance));
        if (StringUtils.isNotEmpty(videoFilePath)) {
            int second = scrollDistance / frameWidth;
            int startX = 0;
            int halfWidth = canvas.getWidth() / 2;
            if (scrollDistance < halfWidth) {
                startX = halfWidth - scrollDistance;
            } else {
                startX = scrollDistance % frameWidth * -1;
            }
            for (int i = 0; i <= canvas.getWidth() / frameWidth * 2 && second <= duration / 1000; i++) {
                drawFrame(canvas, second, startX);
                second++;
                startX += frameWidth;
            }
        }
    }

    private void drawFrame(Canvas canvas, int second, float startX) {
        if (second % 2 == 0) {
            drawTimeText(canvas, second, startX);
        } else {
            drawDot(canvas, startX);
        }
//        canvas.save();
//        canvas.translate(0, textHeight * 2);
//        drawVideoFrame(canvas, second, startX);
//        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scroller.forceFinished(true);
                moving = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float xGap = event.getX() - preX;
                float yGap = event.getY() - preY;
//                boolean validMove = Math.abs(xGap) > Math.abs(yGap) && xGap > scrollThreshold;
//                if (!validMove) {
//                    break;
//                }
                scrollDistance -= xGap;
                if (scrollDistance < 0) {
                    scrollDistance = 0;
                }
                if (scrollDistance > totalWidth) {
                    scrollDistance = (int) totalWidth;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                int xVelocity = (int) velocityTracker.getXVelocity();
                scroller.fling(scrollDistance, 0, -xVelocity, 0, 0, (int) totalWidth, 0, 0);
                invalidate();
                break;
        }
        preX = event.getX();
        preY = event.getY();
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollDistance = scroller.getCurrX();
            if (scrollDistance < 0) {
                scrollDistance = 0;
            }
            if (scrollDistance > totalWidth) {
                scrollDistance = (int) totalWidth;
            }
            invalidate();
        }
    }

    private void drawVideoFrame(Canvas canvas, int second, float startX) {
        if (!videoFrameBuffer.containsKey(second)) {
            Bitmap videoFrame = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                videoFrame = retriever.getScaledFrameAtTime(second * 1000000, MediaMetadataRetriever.OPTION_CLOSEST, frameWidth, frameHeight);
            }
            videoFrameBuffer.put(second, videoFrame);
        }
        canvas.drawBitmap(videoFrameBuffer.get(second), startX, 0, new Paint());
    }


    private void drawTimeText(Canvas canvas, int second, float startX) {
        String durationText = DurationFormatUtils.formatDuration(second * 1000, "mm:ss");
        canvas.drawText(durationText, startX, textHeight, textPaint);
    }

    private void drawDot(Canvas canvas, float startX) {
        canvas.drawCircle(startX, textHeight / 2, 3, dotPaint);
    }

    private void init() {
        initPaint();

        Rect rect = new Rect();
        String format = "00:00";
        textPaint.getTextBounds(format, 0, format.length(), rect);
        textHeight = rect.height();
        textWidth = rect.width();

        minimumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        scrollThreshold = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        scroller = new Scroller(getContext());
    }

    private void initPaint() {
        int color = ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.6));

        textPaint = new TextPaint();
        textPaint.setColor(color);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);

        dotPaint = new Paint();
        dotPaint.setColor(color);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);
    }


}
