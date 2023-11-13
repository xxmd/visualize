package io.github.xxmd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoTimeLine extends View {
    private int frameWidth = 200;
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
    private TreeMap<Integer, Bitmap> videoFrameBuffer = new TreeMap<>();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 50, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
    private Stack<Runnable> frameLoadStack = new Stack<>();
    private Bitmap loadPicture;

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
        totalWidth = (long) (frameWidth * duration / 1000f) + frameWidth;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
        loadPicture = Bitmap.createScaledBitmap(bitmap, frameWidth, frameHeight, false);
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
        if (StringUtils.isNotEmpty(videoFilePath)) {
            int halfWidth = canvas.getWidth() / 2;
            int second, startX;
            if (scrollDistance < halfWidth) {
                startX = halfWidth - scrollDistance;
                second = 0;
            } else {
                startX = scrollDistance % frameWidth * -1;
                second = (scrollDistance - halfWidth) / frameWidth;
            }
            for (int i = 0; i <= canvas.getWidth() / frameWidth * 4 && second <= duration / 1000; i++) {
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
        canvas.save();
        canvas.translate(0, textHeight * 2);
        drawVideoFrame(canvas, second, startX);
        canvas.restore();
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
            int tempSecond = second - 1;
            while (tempSecond >= 0) {
                if (videoFrameBuffer.get(tempSecond) != null) {
                    canvas.drawBitmap(videoFrameBuffer.get(tempSecond), startX, 0, new Paint());
                }
                tempSecond--;
            }
            threadPoolExecutor.execute(() -> {
                Bitmap videoFrame = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        MediaMetadataRetriever.BitmapParams bitmapParams = new MediaMetadataRetriever.BitmapParams();
                        bitmapParams.setPreferredConfig(Bitmap.Config.ALPHA_8);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            videoFrame = retriever.getScaledFrameAtTime(second * 1000000, MediaMetadataRetriever.OPTION_CLOSEST, frameWidth, frameHeight, bitmapParams);
                        }
                    }

                }
                videoFrameBuffer.put(second, videoFrame);
                invalidate();
            });
        } else {
            canvas.drawBitmap(videoFrameBuffer.get(second), startX, 0, new Paint());
        }
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

        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private void initPaint() {
        int color = ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.6));

        textPaint = new TextPaint();
        textPaint.setColor(color);
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Paint.Align.CENTER);

        dotPaint = new Paint();
        dotPaint.setColor(color);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);
    }


}
