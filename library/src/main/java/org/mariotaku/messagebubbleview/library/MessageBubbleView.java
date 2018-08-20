package org.mariotaku.messagebubbleview.library;

import android.annotation.SuppressLint;
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
import android.support.annotation.Dimension;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Display Content like message bubble
 * Created by mariotaku on 14/11/24.
 */
public class MessageBubbleView extends RelativeLayout {

    public static final int NONE = Gravity.NO_GRAVITY;
    public static final int TOP = Gravity.TOP;
    public static final int BOTTOM = Gravity.BOTTOM;
    @SuppressLint("RtlHardcoded")
    public static final int LEFT = Gravity.LEFT;
    @SuppressLint("RtlHardcoded")
    public static final int RIGHT = Gravity.RIGHT;
    public static final int START = Gravity.START;
    public static final int END = Gravity.END;
    public static final int CENTER_HORIZONTAL = Gravity.CENTER_HORIZONTAL;

    @Deprecated
    @SuppressLint("RtlHardcoded")
    public static final int TOP_LEFT = Gravity.TOP | Gravity.LEFT;
    @Deprecated
    @SuppressLint("RtlHardcoded")
    public static final int TOP_RIGHT = Gravity.TOP | Gravity.RIGHT;
    @Deprecated
    @SuppressLint("RtlHardcoded")
    public static final int BOTTOM_LEFT = Gravity.BOTTOM | Gravity.LEFT;
    @Deprecated
    @SuppressLint("RtlHardcoded")
    public static final int BOTTOM_RIGHT = Gravity.BOTTOM | Gravity.RIGHT;
    @Deprecated
    public static final int TOP_START = Gravity.TOP | Gravity.START;
    @Deprecated
    public static final int TOP_END = Gravity.TOP | Gravity.END;
    @Deprecated
    public static final int BOTTOM_START = Gravity.BOTTOM | Gravity.START;
    @Deprecated
    public static final int BOTTOM_END = Gravity.BOTTOM | Gravity.END;

    private float mWrapContentMaxWidthPercent;

    public MessageBubbleView(Context context) {
        this(context, null);
    }

    public MessageBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //noinspection deprecation
        setBackgroundDrawable(new BackgroundDrawable());
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageBubbleView);
        setCornerRadius(a.getDimensionPixelSize(R.styleable.MessageBubbleView_cornerRadius, 0));
        setBubbleColor(a.getColorStateList(R.styleable.MessageBubbleView_bubbleColor));
        setCaretPosition(a.getInt(R.styleable.MessageBubbleView_caretPosition, NONE));
        setWrapContentMaxWidthPercent(a.getFraction(R.styleable.MessageBubbleView_wrapContentMaxWidthPercent, 1, 1, 0));
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT
                && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST
                && mWrapContentMaxWidthPercent > 0) {
            int width = (int) Math.max(getSuggestedMinimumWidth(), MeasureSpec.getSize(widthMeasureSpec) * mWrapContentMaxWidthPercent);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressWarnings("unused")
    public void setBubbleColorFilter(ColorFilter cf) {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.setColorFilter(cf);
    }

    @SuppressWarnings("unused")
    public ColorFilter getBubbleColorFilter() {
        final BackgroundDrawable background = getBackgroundDrawable();
        return background.getPaintColorFilter();
    }

    @SuppressWarnings("unused")
    public void clearBubbleColorFilter() {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.clearColorFilter();
    }

    @SuppressWarnings("unused")
    public void setBubbleColor(@Nullable ColorStateList color) {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.setColor(color);
    }

    @Nullable
    @SuppressWarnings("unused")
    public ColorStateList getBubbleColor() {
        final BackgroundDrawable background = getBackgroundDrawable();
        return background.getColor();
    }

    @CaretPosition
    @SuppressWarnings("unused")
    public int getCaretPosition() {
        final BackgroundDrawable background = getBackgroundDrawable();
        return background.getCaretPosition();
    }

    @SuppressWarnings("unused")
    public void setCaretPosition(@CaretPosition int position) {
        if (position != NONE && !Gravity.isHorizontal(position)) {
            throw new IllegalArgumentException("Horizontal position mask not present");
        }
        if (position != NONE && !Gravity.isVertical(position)) {
            throw new IllegalArgumentException("Vertical position mask not present");
        }
        final BackgroundDrawable background = getBackgroundDrawable();
        final int absPosition = GravityCompat.getAbsoluteGravity(position, ViewCompat.getLayoutDirection(this));
        background.setCaretPosition(position, absPosition);
        resetBackground();
    }

    @SuppressWarnings("unused")
    public void setCaretSize(@Px int width, @Px int height) {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.setCaretSize(width, height);
        resetBackground();
    }

    @SuppressWarnings("unused")
    public void setCornerRadius(@Dimension float radius) {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.setCornerRadius(radius);
        resetBackground();
    }

    @Dimension
    @SuppressWarnings("unused")
    public float getCornerRadius() {
        final BackgroundDrawable background = getBackgroundDrawable();
        return background.getCornerRadius();
    }

    @SuppressWarnings("unused")
    public void setOutlineEnabled(boolean enabled) {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.setOutlineEnabled(enabled);
    }

    @SuppressWarnings("unused")
    public boolean isOutlineEnabled() {
        final BackgroundDrawable background = getBackgroundDrawable();
        return background.isOutlineEnabled();
    }

    public void setWrapContentMaxWidthPercent(@FloatRange(from = 0, to = 1) float percent) {
        mWrapContentMaxWidthPercent = percent;
        requestLayout();
    }

    public float getWrapContentMaxWidthPercent() {
        return mWrapContentMaxWidthPercent;
    }

    @NonNull
    private BackgroundDrawable getBackgroundDrawable() {
        final Drawable background = getBackground();
        if (!(background instanceof BackgroundDrawable)) {
            throw new IllegalArgumentException("You can't set custom background for MessageBubbleView");
        }
        return (BackgroundDrawable) background;
    }

    private void resetBackground() {
        BackgroundDrawable drawable = getBackgroundDrawable();
        ViewCompat.setBackground(this, null);
        ViewCompat.setBackground(this, drawable);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {NONE, TOP, BOTTOM, LEFT, RIGHT, START, END}, flag = true)
    @interface CaretPosition {

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
        private int mCaretPosition, mAbsoluteCaretPosition;
        private float mCornerRadius;
        private ColorStateList mColor;
        private boolean mOutlineEnabled;

        BackgroundDrawable() {
            mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBubblePath = new Path();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawPath(mBubblePath, mBubblePaint);
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

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            updatePath();
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

        @Override
        public boolean getPadding(@NonNull Rect padding) {
            switch (mAbsoluteCaretPosition) {
                case TOP | LEFT:
                case BOTTOM | LEFT:
                    padding.set(Math.round(mCaretWidth), 0, 0, 0);
                    break;
                case TOP | RIGHT:
                case BOTTOM | RIGHT:
                    padding.set(0, 0, Math.round(mCaretWidth), 0);
                    break;
                case BOTTOM | CENTER_HORIZONTAL:
                    padding.set(0, 0, 0, Math.round(mCaretHeight));
                    break;
                case TOP | CENTER_HORIZONTAL:
                    padding.set(0, Math.round(mCaretHeight), 0, 0);
                    break;
                default:
                    padding.set(0, 0, 0, 0);
                    break;
            }
            return true;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(@NonNull Outline outline) {
            if (!mOutlineEnabled || mCaretPosition != NONE) {
                outline.setRect(getBounds());
                return;
            }
            outline.setRoundRect(getBounds(), mCornerRadius);
        }

        @Nullable
        ColorStateList getColor() {
            return mColor;
        }

        void setColor(@Nullable ColorStateList color) {
            mColor = color;
            updateColor();
        }

        void setCaretSize(int width, int height) {
            mCaretWidth = width;
            mCaretHeight = height;
            updatePath();
        }

        void setCaretPosition(int position, int absolute) {
            mCaretPosition = position;
            mAbsoluteCaretPosition = absolute;
            updatePath();
        }

        int getCaretPosition() {
            return mCaretPosition;
        }

        ColorFilter getPaintColorFilter() {
            return mBubblePaint.getColorFilter();
        }

        void setCornerRadius(float radius) {
            mCornerRadius = radius;
            updatePath();
        }

        float getCornerRadius() {
            return mCornerRadius;
        }

        void setOutlineEnabled(boolean outlineEnabled) {
            this.mOutlineEnabled = outlineEnabled;
            invalidateSelf();
        }

        boolean isOutlineEnabled() {
            return mOutlineEnabled;
        }

        private void updateColor() {
            if (mColor == null) return;
            final int color = mColor.getColorForState(getState(), mColor.getDefaultColor());
            mBubblePaint.setColor(color);
            invalidateSelf();
        }

        private void updatePath() {
            final Rect bounds = getBounds();
            final float radius = mCornerRadius;
            final float caretWidth = mCaretWidth, caretHeight = mCaretHeight;
            mBubblePath.reset();
            switch (mAbsoluteCaretPosition) {
                case TOP | LEFT: {
                    updateTopLeftBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case TOP | RIGHT: {
                    updateTopRightBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case BOTTOM | LEFT: {
                    updateBottomLeftBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case BOTTOM | RIGHT: {
                    updateBottomRightBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case BOTTOM | CENTER_HORIZONTAL: {
                    updateBottomBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case TOP | CENTER_HORIZONTAL: {
                    updateTopBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
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

        private void updateBottomBubble(Path path, Rect bounds, float radius, float caretWidth,
                                        float caretHeight) {
            path.moveTo(bounds.centerX(), bounds.bottom);
            path.lineTo(bounds.centerX() - caretWidth / 2, bounds.bottom - caretHeight);
            path.lineTo(bounds.left + radius, bounds.bottom - caretHeight);
            path.cubicTo(bounds.left + radius * CONTROL_POINT_RATIO, bounds.bottom - caretHeight,
                    bounds.left, bounds.bottom - caretHeight - radius * CONTROL_POINT_RATIO,
                    bounds.left, bounds.bottom - caretHeight - radius);
            path.lineTo(bounds.left, bounds.top + radius);
            path.cubicTo(bounds.left, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.left + radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.left + radius, bounds.top);
            path.lineTo(bounds.right - radius, bounds.top);
            path.cubicTo(bounds.right - radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.right, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.right, bounds.top + radius);
            path.lineTo(bounds.right, bounds.bottom - caretHeight - radius);
            path.cubicTo(bounds.right, bounds.bottom - caretHeight - radius * CONTROL_POINT_RATIO,
                    bounds.right - radius * CONTROL_POINT_RATIO, bounds.bottom - caretHeight,
                    bounds.right - radius, bounds.bottom - caretHeight);
            path.lineTo(bounds.centerX() + caretWidth / 2, bounds.bottom - caretHeight);
            path.close();
        }

        private void updateTopBubble(Path path, Rect bounds, float radius, float caretWidth,
                                     float caretHeight) {
            path.moveTo(bounds.centerX(), bounds.top);
            path.lineTo(bounds.centerX() - caretWidth / 2, bounds.top + caretHeight);
            path.lineTo(bounds.left + radius, bounds.top + caretHeight);
            path.cubicTo(bounds.left + radius * CONTROL_POINT_RATIO, bounds.top + caretHeight,
                    bounds.left, bounds.top + caretHeight + radius * CONTROL_POINT_RATIO,
                    bounds.left, bounds.top + caretHeight + radius);
            path.lineTo(bounds.left, bounds.bottom - radius);
            path.cubicTo(bounds.left, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.left + radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.left + radius, bounds.bottom);
            path.lineTo(bounds.right - radius, bounds.bottom);
            path.cubicTo(bounds.right - radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.right, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.right, bounds.bottom - radius);
            path.lineTo(bounds.right, bounds.top + caretHeight + radius);
            path.cubicTo(bounds.right, bounds.top + caretHeight + radius * CONTROL_POINT_RATIO,
                    bounds.right - radius * CONTROL_POINT_RATIO, bounds.top + caretHeight,
                    bounds.right - radius, bounds.top + caretHeight);
            path.lineTo(bounds.centerX() + caretWidth / 2, bounds.top + caretHeight);
            path.close();
        }
    }

}
