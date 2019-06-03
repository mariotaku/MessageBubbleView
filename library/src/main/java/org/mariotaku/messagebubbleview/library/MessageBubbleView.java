package org.mariotaku.messagebubbleview.library;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.Dimension;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

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
    public static final int HORIZONTAL = Gravity.CLIP_HORIZONTAL;
    public static final int VERTICAL = Gravity.CLIP_VERTICAL;

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
        setBackgroundDrawable(new BackgroundDrawable());
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageBubbleView);
        setCornerRadius(a.getDimensionPixelSize(R.styleable.MessageBubbleView_cornerRadius, 0));
        setBubbleColor(a.getColorStateList(R.styleable.MessageBubbleView_bubbleColor));
        setCaretPosition(a.getInt(R.styleable.MessageBubbleView_caretPosition, NONE));
        setWrapContentMaxWidthPercent(a.getFraction(R.styleable.MessageBubbleView_wrapContentMaxWidthPercent, 1, 1, 0));
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (a.hasValue(R.styleable.MessageBubbleView_caretWidth) && a.hasValue(R.styleable.MessageBubbleView_caretHeight)) {
            int caretWidth, caretHeight;
            TypedValue tv = new TypedValue();
            a.getValue(R.styleable.MessageBubbleView_caretWidth, tv);
            if (tv.type == TypedValue.TYPE_DIMENSION) {
                caretWidth = TypedValue.complexToDimensionPixelSize(tv.data, dm);
            } else {
                caretWidth = tv.data;
            }
            a.getValue(R.styleable.MessageBubbleView_caretHeight, tv);
            if (tv.type == TypedValue.TYPE_DIMENSION) {
                caretHeight = TypedValue.complexToDimensionPixelSize(tv.data, dm);
            } else {
                caretHeight = tv.data;
            }
            setCaretSize(caretWidth, caretHeight);
        } else {
            final float w = dm.density * 12;
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
        final BackgroundDrawable background = getBackgroundDrawable();
        final int absPosition = GravityCompat.getAbsoluteGravity(position, ViewCompat.getLayoutDirection(this));
        background.setCaretPosition(position, absPosition);
        resetBackground();
    }

    @SuppressWarnings("unused")
    public float getCenterCaretPosition() {
        BackgroundDrawable drawable = getBackgroundDrawable();
        return drawable.getCenterCaretOffset();
    }

    @SuppressWarnings("unused")
    public void setCenterCaretOffset(float offset) {
        final BackgroundDrawable background = getBackgroundDrawable();
        background.setCenterCaretOffset(offset);
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
        private float mCenterCaretOffset = 0.5f;
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
            int paddingGravity;
            if ((mAbsoluteCaretPosition & VERTICAL) != 0) {
                paddingGravity = mAbsoluteCaretPosition & Gravity.VERTICAL_GRAVITY_MASK;
            } else {
                paddingGravity = mAbsoluteCaretPosition & Gravity.HORIZONTAL_GRAVITY_MASK;
            }
            switch (paddingGravity) {
                case LEFT:
                    padding.set(Math.round(mCaretWidth), 0, 0, 0);
                    break;
                case RIGHT:
                    padding.set(0, 0, Math.round(mCaretWidth), 0);
                    break;
                case BOTTOM:
                    padding.set(0, 0, 0, Math.round(mCaretHeight));
                    break;
                case TOP:
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

        void setCenterCaretOffset(float offset) {
            mCenterCaretOffset = offset;
            updatePath();
        }

        float getCenterCaretOffset() {
            return mCenterCaretOffset;
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
            float caretWidth = mCaretWidth, caretHeight = mCaretHeight;
            if (caretWidth < 0) {
                caretWidth = bounds.width();
            }
            if (caretHeight < 0) {
                caretHeight = bounds.height();
            }
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
                case BOTTOM | VERTICAL: {
                    updateBottomBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case TOP | VERTICAL: {
                    updateTopBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case LEFT | HORIZONTAL: {
                    updateLeftHBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case RIGHT | HORIZONTAL: {
                    updateRightHBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case TOP | LEFT | VERTICAL: {
                    updateTopLeftVBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
                    break;
                }
                case TOP | RIGHT | VERTICAL: {
                    updateTopRightVBubble(mBubblePath, bounds, radius, caretWidth, caretHeight);
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
            float caretCenter = bounds.left + radius + (bounds.width() - radius * 2 - caretWidth) * mCenterCaretOffset + caretWidth / 2;
            path.moveTo(caretCenter, bounds.bottom);
            path.lineTo(caretCenter - caretWidth / 2, bounds.bottom - caretHeight);
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
            path.lineTo(caretCenter + caretWidth / 2, bounds.bottom - caretHeight);
            path.close();
        }

        private void updateTopBubble(Path path, Rect bounds, float radius, float caretWidth,
                                     float caretHeight) {
            float caretCenter = bounds.left + radius + (bounds.width() - radius * 2 - caretWidth) * mCenterCaretOffset + caretWidth / 2;
            path.moveTo(caretCenter, bounds.top);
            path.lineTo(caretCenter - caretWidth / 2, bounds.top + caretHeight);
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
            path.lineTo(caretCenter + caretWidth / 2, bounds.top + caretHeight);
            path.close();
        }

        private void updateLeftHBubble(Path path, Rect bounds, float radius, float caretWidth,
                                       float caretHeight) {
            float caretCenter = bounds.top + radius + (bounds.height() - radius * 2 - caretWidth) * mCenterCaretOffset + caretWidth / 2;
            path.moveTo(bounds.left, caretCenter);
            path.lineTo(bounds.left + caretHeight, caretCenter - caretHeight / 2);
            path.lineTo(bounds.left + caretHeight, bounds.top + radius);
            path.cubicTo(bounds.left + caretHeight, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.left + caretHeight + radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.left + caretHeight + radius, bounds.top);
            path.lineTo(bounds.right - radius, bounds.top);
            path.cubicTo(bounds.right - radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.right, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.right, bounds.top + radius);
            path.lineTo(bounds.right, bounds.bottom - radius);
            path.cubicTo(bounds.right, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.right - radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.right - radius, bounds.bottom);
            path.lineTo(bounds.left + caretHeight + radius, bounds.bottom);
            path.cubicTo(bounds.left + caretHeight + radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.left + caretHeight, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.left + caretHeight, bounds.bottom - radius);
            path.lineTo(bounds.left + caretHeight, caretCenter + caretWidth / 2);
            path.close();
        }

        private void updateRightHBubble(Path path, Rect bounds, float radius, float caretWidth,
                                        float caretHeight) {
            float caretCenter = bounds.top + radius + (bounds.height() - radius * 2 - caretWidth) * mCenterCaretOffset + caretWidth / 2;
            path.moveTo(bounds.right, caretCenter);
            path.lineTo(bounds.right - caretHeight, caretCenter - caretHeight / 2);
            path.lineTo(bounds.right - caretHeight, bounds.top + radius);
            path.cubicTo(bounds.right - caretHeight, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.right - caretHeight - radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.right - caretHeight - radius, bounds.top);
            path.lineTo(bounds.left + radius, bounds.top);
            path.cubicTo(bounds.left + radius * CONTROL_POINT_RATIO, bounds.top,
                    bounds.left, bounds.top + radius * CONTROL_POINT_RATIO,
                    bounds.left, bounds.top + radius);
            path.lineTo(bounds.left, bounds.bottom - radius);
            path.cubicTo(bounds.left, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.left + radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.left + radius, bounds.bottom);
            path.lineTo(bounds.right - caretHeight - radius, bounds.bottom);
            path.cubicTo(bounds.right - caretHeight - radius * CONTROL_POINT_RATIO, bounds.bottom,
                    bounds.right - caretHeight, bounds.bottom - radius * CONTROL_POINT_RATIO,
                    bounds.right - caretHeight, bounds.bottom - radius);
            path.lineTo(bounds.right - caretHeight, caretCenter + caretWidth / 2);
            path.close();
        }

        private void updateTopLeftVBubble(Path path, Rect bounds, float radius, float caretWidth,
                                          float caretHeight) {
            path.moveTo(bounds.left, bounds.top);
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
            path.lineTo(bounds.left + caretWidth, bounds.top + caretHeight);
            path.lineTo(bounds.left, bounds.top);
            path.close();
        }

        private void updateTopRightVBubble(Path path, Rect bounds, float radius, float caretWidth,
                                           float caretHeight) {
            path.moveTo(bounds.right, bounds.top);
            path.lineTo(bounds.right - caretWidth, bounds.top + caretHeight);
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
            path.moveTo(bounds.right, bounds.top);
            path.close();
        }

    }

}
