package com.example.yizhan.pathmeasuredemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.telecom.Log;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yizhan on 2017/10/18.
 */

public class AirPlan extends View {

    private Paint mPaint;
    private int mCenterX;
    private int mCenterY;
    private float mAnimatedValue = 0;

    private final static float RADIUS = 150;
    private final static long DURATION = 3000;
    private Path mPath;
    private PathMeasure mPathMeasure;
    private float mLength;
    private Matrix mMatrix;
    private Bitmap mBitmap;

    private boolean isFirst = true;
    private ValueAnimator mValueAnimator;

    private final static int NONE = 0;
    private final static int RUNNING = 1;

    private int currentState = NONE;


    public AirPlan(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        initPaint();

        initAnimator();

        initPath();

        mMatrix = new Matrix();

        initBitmap();
    }

    private void initBitmap() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plane, options);
    }

    private void initPath() {
        mPath = new Path();
        mPath.addCircle(0, 0, RADIUS, Path.Direction.CW);

        mPathMeasure = new PathMeasure();
        mPathMeasure.setPath(mPath, false);
        mLength = mPathMeasure.getLength();
    }

    private void initAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                mAnimatedValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mCenterX, mCenterY);

//        Log.i("TAG", "mAnimatedValue == " + mAnimatedValue);
        float[] posArr = new float[2];
        float[] tanArr = new float[2];
        mPathMeasure.getPosTan(mAnimatedValue * mLength, posArr, tanArr);
//        Log.i("TAG", "tanArr[1] == " + tanArr[1]);
//        Log.i("TAG", "tanArr[0] == " + tanArr[0] + "。");


        float degree = (float) (Math.atan2(tanArr[1], tanArr[0]) * 180 / Math.PI);

        mMatrix.reset();
        mMatrix.postRotate(degree, mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
        mMatrix.postTranslate(posArr[0] - mBitmap.getWidth() / 2, posArr[1] - mBitmap.getHeight() / 2);

        canvas.drawPath(mPath, mPaint);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

//        Log.i("TAG", "currentState == " + currentState);
        if (currentState == NONE) {
            mValueAnimator.start();
            currentState = RUNNING;
        }

    }
}
