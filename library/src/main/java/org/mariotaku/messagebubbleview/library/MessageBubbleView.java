package org.mariotaku.messagebubbleview.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Display Content like message bubble
 * Created by mariotaku on 14/11/24.
 */
public class MessageBubbleView extends FrameLayout {

    public static final int NONE = 0x0;
    public static final int TOP_LEFT = 0x1;
    public static final int TOP_RIGHT = 0x2;
    public static final int BOTTOM_LEFT = 0x3;
    public static final int BOTTOM_RIGHT = 0x4;
    public static final int TOP_START = 0x11;
    public static final int TOP_END = 0x12;
    public static final int BOTTOM_START = 0x13;
    public static final int BOTTOM_END = 0x14;

    public MessageBubbleView(Context context) {
        this(context, null);
    }

    public MessageBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //noinspection deprecation
        setBackgroundDrawable(new BackgroundDrawable(getResources()));
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageBubbleView);
        setCornerRadius(a.getDimensionPixelSize(R.styleable.MessageBubbleView_cornerRadius, 0));
        setBubbleColor(a.getColorStateList(R.styleable.MessageBubbleView_bubbleColor));
        setCaretPosition(a.getInt(R.styleable.MessageBubbleView_caretPosition, NONE));
        if (a.hasValue(R.styleable.MessageBubbleView_caretWidth) && a.hasValue(R.styleable.MessageBubbleView_caretHeight)) {
            setCaretSize(a.getDimensionPixelSize(R.styleable.MessageBubbleView_caretWidth, 0),
                    a.getDimensionPixelSize(R.styleable.MessageBubbleView_caretHeight, 0));
        } else {
            final Resources resources = getResources();
            final float w = resources.getDisplayMetrics().density * 12;
            final float h = w * 0.75f;
            setCaretSize(Math.round(w), Math.round(h));
        }
        a.recycle();
    }

    public void setBubbleColorFilter(ColorFilter cf) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        background.setColorFilter(cf);
    }

    public ColorFilter getBubbleColorFilter() {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        return ((BackgroundDrawable) background).getPaintColorFilter();
    }

    public void clearBubbleColorFilter() {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        background.clearColorFilter();
    }

    public void setBubbleColor(ColorStateList color) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        ((BackgroundDrawable) background).setColor(color);
    }

    public ColorStateList getBubbleColor() {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        return ((BackgroundDrawable) background).getColor();
    }

    public void setCaretPosition(int position) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        final int rawPosition = resolveHardCodedPosition(position, ViewCompat.getLayoutDirection(this));
        ((BackgroundDrawable) background).setCaretPosition(rawPosition);
    }

    private static int resolveHardCodedPosition(int caretPosition, int layoutDirection) {
        switch (caretPosition) {
            case TOP_START:
                return layoutDirection == LAYOUT_DIRECTION_RTL ? TOP_RIGHT : TOP_LEFT;
            case TOP_END:
                return layoutDirection == LAYOUT_DIRECTION_RTL ? TOP_LEFT : TOP_RIGHT;
            case BOTTOM_START:
                return layoutDirection == LAYOUT_DIRECTION_RTL ? BOTTOM_RIGHT : BOTTOM_LEFT;
            case BOTTOM_END:
                return layoutDirection == LAYOUT_DIRECTION_RTL ? BOTTOM_LEFT : BOTTOM_RIGHT;
        }
        return caretPosition;
    }

    public void setCaretSize(int width, int height) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        ((BackgroundDrawable) background).setCaretSize(width, height);
    }

    public void setCornerRadius(float radius) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        ((BackgroundDrawable) background).setCornerRadius(radius);
    }

    public float getCornerRadius(float radius) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        return ((BackgroundDrawable) background).getCornerRadius();
    }

    public void setOutlineEnabled(boolean enabled) {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        ((BackgroundDrawable) background).setOutlineEnabled(enabled);
    }

    public boolean isOutlineEnabled() {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) throw new IllegalArgumentException();
        return ((BackgroundDrawable) background).isOutlineEnabled();
    }

    private static class BackgroundDrawable extends Drawable {

        /**
         * @see <a href='https://nacho4d-nacho4d.blogspot.com/2011/05/bezier-paths-rounded-corners-rectangles.html'>Bezier Paths : making rectangles with rounded corners</a>
         */
        private static final float CONTROL_POINT_RATIO = 0.447771526f;

        private final Paint mBubblePaint;
        private final Path mBubblePath;

        private final RectF mTempRectF = new RectF();

        private float mCaretWidth, mCaretHeight;
        private int mCaretPosition;
        private float mCornerRadius;
        private ColorStateList mColor;
        private boolean outlineEnabled;

        BackgroundDrawable(Resources resources) {
            mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBubblePath = new Path();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(mBubblePath, mBubblePaint);
        }

        private void setColor(ColorStateList color) {
            mColor = color;
            updateColor();
        }

        private ColorStateList getColor() {
            return mColor;
        }

        public void setCaretSize(int width, int height) {
            mCaretWidth = width;
            mCaretHeight = height;
            updatePath();
            updateViewPadding();
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        @Override
        protected boolean onStateChange(int[] state) {
            updateColor();
            return true;
        }

        private void updateColor() {
            if (mColor == null) return;
            final int color = mColor.getColorForState(getState(), mColor.getDefaultColor());
            mBubblePaint.setColor(color);
            invalidateSelf();
        }

        private void setCaretPosition(int position) {
            mCaretPosition = position;
            updatePath();
            updateViewPadding();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            updatePath();
            updateViewPadding();
        }

        private void updateViewPadding() {
            final Callback callback = getCallback();
            if (!(callback instanceof View)) return;
            final View view = (View) callback;
            switch (mCaretPosition) {
                case TOP_LEFT:
                case BOTTOM_LEFT:
                    view.setPadding(Math.round(mCaretWidth), 0, 0, 0);
                    break;
                case TOP_RIGHT:
                case BOTTOM_RIGHT:
                    view.setPadding(0, 0, Math.round(mCaretWidth), 0);
                    break;
                default:
                    view.setPadding(0, 0, 0, 0);
                    break;
            }
        }

        private void updatePath() {
            final Rect bounds = getBounds();
            final float radius = mCornerRadius;
            final float caretWidth = mCaretWidth, caretHeight = mCaretHeight;
            mBubblePath.reset();
            switch (mCaretPosition) {
                case TOP_LEFT: {
                    updateTopLeftBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case TOP_RIGHT: {
                    updateTopRightBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case BOTTOM_LEFT: {
                    updateBottomLeftBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case BOTTOM_RIGHT: {
                    updateBottomRightBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                default: {
                    updateRectBubble(mBubblePath, bounds, radius);
                    break;
                }
            }
            invalidateSelf();
        }

        private void updateRectBubble(Path path, Rect bounds, float radius) {
            mTempRectF.set(bounds);
            path.addRoundRect(mTempRectF, radius, radius, Direction.CW);
        }

        private void updateTopRightBubble(Path path, Rect bounds, float radius, float caretWidth,
                                          float caretHeight) {
            path.moveTo(bounds.right, bounds.top);
            path.lineTo(bounds.right - caretWidth, bounds.top + caretHeight);
            path.lineTo(bounds.right - caretWidth, bounds.bottom - radius);
            path.cubicTo(bounds.right - caretWidth, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.right - caretWidth - radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.right - caretWidth - radius, bounds.bottom);
            path.lineTo(bounds.left + radius, bounds.bottom);
            path.cubicTo(bounds.left + radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.left, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.left, bounds.bottom - radius);
            path.lineTo(bounds.left, bounds.top + radius);
            path.cubicTo(bounds.left, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.left + radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.left + radius, bounds.top);
            path.close();
        }


        private void updateTopLeftBubble(Path path, Rect bounds, float radius, float caretWidth,
                                         float caretHeight) {
            path.moveTo(bounds.left, bounds.top);
            path.lineTo(bounds.right - radius, bounds.top);
            path.cubicTo(bounds.right - radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.right, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.right, bounds.top + radius);
            path.lineTo(bounds.right, bounds.bottom - radius);
            path.cubicTo(bounds.right, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.right - radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.right - radius, bounds.bottom);
            path.lineTo(bounds.left + caretWidth + radius, bounds.bottom);
            path.cubicTo(bounds.left + caretWidth + radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.left + caretWidth, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.left + caretWidth, bounds.bottom - radius);
            path.lineTo(bounds.left + caretWidth, bounds.top + caretHeight);
            path.close();
        }

        private void updateBottomLeftBubble(Path path, Rect bounds, float radius, float caretWidth,
                                            float caretHeight) {
            path.moveTo(bounds.left, bounds.bottom);
            path.lineTo(bounds.right - radius, bounds.bottom);
            path.cubicTo(bounds.right - radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.right, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.right, bounds.bottom - radius);
            path.lineTo(bounds.right, bounds.top + radius);
            path.cubicTo(bounds.right, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.right - radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.right - radius, bounds.top);
            path.lineTo(bounds.left + caretWidth + radius, bounds.top);
            path.cubicTo(bounds.left + caretWidth + radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.left + caretWidth, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.left + caretWidth, bounds.top + radius);
            path.lineTo(bounds.left + caretWidth, bounds.bottom - caretHeight);
            path.close();
        }

        private void updateBottomRightBubble(Path path, Rect bounds, float radius, float caretWidth,
                                             float caretHeight) {
            path.moveTo(bounds.right, bounds.bottom);
            path.lineTo(bounds.left + radius, bounds.bottom);
            path.cubicTo(bounds.left + radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.left, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.left, bounds.bottom - radius);
            path.lineTo(bounds.left, bounds.top + radius);
            path.cubicTo(bounds.left, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.left + radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.left + radius, bounds.top);
            path.lineTo(bounds.right - caretWidth - radius, bounds.top);
            path.cubicTo(bounds.right - caretWidth - radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.right - caretWidth, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.right - caretWidth, bounds.top + radius);
            path.lineTo(bounds.right - caretWidth, bounds.bottom - caretHeight);
            path.close();
        }

        @Override
        public void setAlpha(int alpha) {
            mBubblePaint.setAlpha(alpha);
        }

        @Override
        public int getAlpha() {
            return mBubblePaint.getAlpha();
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mBubblePaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public ColorFilter getColorFilter() {
            return getPaintColorFilter();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(@NonNull Outline outline) {
            if (!outlineEnabled || !mBubblePath.isConvex()) return;
            outline.setConvexPath(mBubblePath);
        }

        private ColorFilter getPaintColorFilter() {
            return mBubblePaint.getColorFilter();
        }

        private void setCornerRadius(float radius) {
            mCornerRadius = radius;
            updatePath();
        }

        private float getCornerRadius() {
            return mCornerRadius;
        }

        public void setOutlineEnabled(boolean outlineEnabled) {
            this.outlineEnabled = outlineEnabled;
            invalidateSelf();
        }

        public boolean isOutlineEnabled() {
            return outlineEnabled;
        }
    }

}
