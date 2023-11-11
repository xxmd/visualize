package io.github.xxmd;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class TimeLine extends View {
    private long duration = (4 * 60 + 9) * 1000;
    private float gap = 100;
    private TextPaint textPaint;
    private Paint dotPaint;
    private int textHeight;
    private int textWidth;

    public TimeLine(@NonNull Context context) {
        super(context);
        init();
    }

    public TimeLine(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(textWidth / 2, 0);
        for (int i = 0; i < duration / 1000f; i++) {
            if (i % 2 == 0) {
                drawTimeText(canvas, i);
            } else {
                drawDot(canvas, i);
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = gap * duration / 1000;
        setMeasuredDimension((int) width, heightMeasureSpec);
    }

    private void drawTimeText(Canvas canvas, int position) {
        float centerX = gap * position - textWidth / 2;
        String durationText = DurationFormatUtils.formatDuration(position * 1000, "mm:ss");
        canvas.drawText(durationText, gap * position, textHeight, textPaint);
    }

    private void drawDot(Canvas canvas, int position) {
        canvas.drawCircle(gap * position, textHeight / 2, 3, dotPaint);
    }

    private void init() {
        initPaint();

        Rect rect = new Rect();
        String format = "00:00";
        textPaint.getTextBounds(format, 0, format.length(), rect);
        textHeight = rect.height();
        textWidth = rect.width();
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
