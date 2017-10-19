package com.example.yizhan.pathmeasuredemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yizhan on 2017/10/19.
 */

public class AniSearch extends View {

    private final static int SEARCH_RADIUS = 25;
    private final static int PROGRESS_RADIUS = 50;
    private final static int STROKE_WIDTH = 8;


    private Path mSearchPath;
    private Path mProgressPath;

    private static final int NONE = 0;
    private static final int START = 1;
    private static final int SEARCHING = 2;
    private static final int END = 3;

    //为什么要设置这个状态？主要是为了在不同状态下随着动画值的改变画出不同的内容
    private int currentState = NONE;

    //动画显示的时间
    private final static int Duration = 2000;
    private ValueAnimator mStartAnimator;
    private ValueAnimator mSearchingAnimator;
    private ValueAnimator mEndAnimator;
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;
    private float mAnimatedValue;
    private int mCenterX;
    private int mCenterY;
    private Paint mPaint;
    private PathMeasure mPathMeasure;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (currentState) {
                case NONE://重新开始动画显示
                    currentState = START;
                    mStartAnimator.start();
                    break;
                case START:
                    currentState = SEARCHING;
                    mSearchingAnimator.start();
                    break;
                case SEARCHING:
                    currentState = END;
                    mEndAnimator.start();
                    break;
                case END:
                    currentState = NONE;
                    mHandler.sendEmptyMessage(0);
                    break;
            }

        }
    };

    public AniSearch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //初始化画笔
        initPaint();

        initPath();

        initListener();

        initAnimator();

        currentState = START;
        mStartAnimator.start();
    }

    private void initAnimator() {

        mStartAnimator = ValueAnimator.ofFloat(0, 1).setDuration(Duration);
        mSearchingAnimator = ValueAnimator.ofFloat(0, 1).setDuration(Duration);
        mSearchingAnimator.setRepeatCount(5);//设置缓冲动画的次数
        mEndAnimator = ValueAnimator.ofFloat(1, 0).setDuration(Duration);

        mStartAnimator.addUpdateListener(mAnimatorUpdateListener);
        mSearchingAnimator.addUpdateListener(mAnimatorUpdateListener);
        mEndAnimator.addUpdateListener(mAnimatorUpdateListener);

        mStartAnimator.addListener(mAnimatorListener);
        mSearchingAnimator.addListener(mAnimatorListener);
        mEndAnimator.addListener(mAnimatorListener);
    }

    private void initListener() {

        mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };

        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationStart(Animator animation) {

            }
        };

    }

    private void initPath() {

        mSearchPath = new Path();
        mProgressPath = new Path();

        //画出一个半径为50，划过的角度将近为360度，方向为顺时针的圆弧
        RectF rectF = new RectF(-SEARCH_RADIUS, -SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
        mSearchPath.addArc(rectF, 45, 359.9f);

        ////画出一个半径为100，划过的角度将近为360度，方向为逆时针的圆弧
        RectF rectF1 = new RectF(-PROGRESS_RADIUS, -PROGRESS_RADIUS, PROGRESS_RADIUS, PROGRESS_RADIUS);
        mProgressPath.addArc(rectF1, 45, 359.9f);

        //取出半径为100的圆弧的第一个点的坐标
        mPathMeasure = new PathMeasure(mProgressPath, false);
        float[] posArr = new float[2];
        mPathMeasure.getPosTan(0, posArr, null);

        //半径为50的圆弧最后一个点与半径为100的圆弧第一个点进行连接
        //为什么要连接？因为半径为50的圆弧和这条连接线正好组成了一个搜索图标
        mSearchPath.lineTo(posArr[0], posArr[1]);
    }

    private void initPaint() {

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);//设置画笔模式为只画边界
        mPaint.setStrokeWidth(STROKE_WIDTH);//设置边界的宽度
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置边界的起始端或末端的样式，像个帽子
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取当前控件的中心
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //先画个背景色
        canvas.drawColor(Color.parseColor("#0082D7"));

        //画布平移到当前控件的中心
        canvas.translate(mCenterX, mCenterY);

        mPaint.setColor(Color.WHITE);
        Path dst;
        //根据当前状态画出不同的内容
        switch (currentState) {
            case NONE:
                canvas.drawPath(mSearchPath, mPaint);
                break;
            case START:
                dst = new Path();
                mPathMeasure.setPath(mSearchPath, false);
                mPathMeasure.getSegment(mAnimatedValue * mPathMeasure.getLength(), mPathMeasure.getLength(), dst, true);
                canvas.drawPath(dst, mPaint);
                break;
            case SEARCHING:
                dst = new Path();
                mPathMeasure.setPath(mProgressPath, false);
                float stop = mAnimatedValue * mPathMeasure.getLength();
                float start = (float) (stop - (0.5 - Math.abs(0.5 - mAnimatedValue)) * mPathMeasure.getLength());
                mPathMeasure.getSegment(start, stop, dst, true);
                canvas.drawPath(dst, mPaint);
                break;
            case END:
                dst = new Path();
                mPathMeasure.setPath(mSearchPath, false);
                mPathMeasure.getSegment(mAnimatedValue * mPathMeasure.getLength(), mPathMeasure.getLength(), dst, true);
                canvas.drawPath(dst, mPaint);
                break;
        }

    }
}
