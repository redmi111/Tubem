package com.online.garam.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class ProgressDrawable extends Drawable {
    private int mBackgroundColor;
    private int mForegroundColor;
    private float mProgress;

    public ProgressDrawable(Context context, int background, int foreground) {
        this(context.getResources().getColor(background), context.getResources().getColor(foreground));
    }

    public ProgressDrawable(int background, int foreground) {
        this.mBackgroundColor = background;
        this.mForegroundColor = foreground;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidateSelf();
    }

    public void draw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Paint paint = new Paint();
        paint.setColor(this.mBackgroundColor);
        canvas.drawRect(0.0f, 0.0f, (float) width, (float) height, paint);
        paint.setColor(this.mForegroundColor);
        canvas.drawRect(0.0f, 0.0f, (float) ((int) (this.mProgress * ((float) width))), (float) height, paint);
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter filter) {
    }

    public int getOpacity() {
        return -1;
    }
}
