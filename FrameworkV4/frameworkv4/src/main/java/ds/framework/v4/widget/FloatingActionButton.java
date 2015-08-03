package ds.framework.v4.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;

import ds.framework.v4.Global;
import ds.framework.v4.R;

public class FloatingActionButton extends ImageButton {

    Context context;
    Paint mButtonPaint;
    private boolean mHidden;
    private boolean mNoClickAnimation;

    public FloatingActionButton(Context context) {
        super(context);

        this.context = context;
        init(Color.WHITE);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyleAttr, 0);

        mNoClickAnimation = !a.getBoolean(R.styleable.DsView_noClickAnimation, false);

        init(a.getColor(R.styleable.DsView_foreground, Color.WHITE));

        a.recycle();

        setBackgroundResource(0);
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    public void init(int color) {
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setColor(color);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setShadowLayer(5.0f * Global.getDipMultiplier(), 0.0f, 1.75f * Global.getDipMultiplier(), Color.argb(100, 0, 0, 0));

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setClickable(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);

        super.onDraw(canvas);

          //  canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
            //        (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);

    }

    @Override
    public boolean performClick() {
        if (super.performClick()) {

            if (!mNoClickAnimation) {
                setAlpha(0.7f);

                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            setAlpha(1.0f);
                        } catch (Throwable e) {
                            ;
                        }
                    }
                }, 100);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setAlpha(1.0f);
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setAlpha(0.7f);
        }
        return super.onTouchEvent(event);
    }

    public void setColor(int color) {
        init(color);
    }

    public void hide() {
        if (!isHidden()) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, 0);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1, 0);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(new AccelerateInterpolator());
            animSetXY.setDuration(100);
            animSetXY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {}

                @Override
                public void onAnimationEnd(Animator animator) {
                    setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {}

                @Override
                public void onAnimationRepeat(Animator animator) {}
            });
            animSetXY.start();
            mHidden = true;
        }
    }

    public void show() {
        if (isHidden()) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0, 1);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(new OvershootInterpolator());
            animSetXY.setDuration(200);
            animSetXY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {}

                @Override
                public void onAnimationCancel(Animator animator) {}

                @Override
                public void onAnimationRepeat(Animator animator) {}
            });
            animSetXY.start();
            mHidden = false;
        }
    }

    public boolean isHidden() {
        return mHidden || getVisibility() == View.GONE || getVisibility() == View.INVISIBLE;
    }
}