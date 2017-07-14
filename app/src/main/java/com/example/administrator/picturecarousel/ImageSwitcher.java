package com.example.administrator.picturecarousel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * Created on 2017/7/14
 * Author 郑少鹏
 */

public class ImageSwitcher extends ViewGroup {
    private String TAG = ImageSwitcher.class.getSimpleName();
    private static final int SNAP_VELOCITY = 300;

    private Scroller scroller;

    private VelocityTracker mVelocityTracker;

    private int mTouchSlop;

    private float mMotionX;

    private int mImageWidth;

    private int imageCount;

    private int mIndex;

    private int mImageHeight;

    private int[] imageItems;

    private boolean forceToRelayout;

    private int mTouchState = TOUCH_STATE_REST;

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;

    private static final int AUTO_MSG = 0;

    private static final int START_MSG = 2;

    private static final int HANDLE_MSG = 1;

    private static final long PHOTO_CHANGE_TIME = 4000;

    private Handler mHandler = new Handler() {// 处理图片自动或者手动滚动操作

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AUTO_MSG:
                    scrollToNext();
                    mHandler.sendEmptyMessageDelayed(AUTO_MSG, PHOTO_CHANGE_TIME);
                    break;
                case START_MSG:
                    mHandler.sendEmptyMessageDelayed(AUTO_MSG, PHOTO_CHANGE_TIME);
                    break;
                case HANDLE_MSG:
                    mHandler.removeMessages(AUTO_MSG);
                    mHandler.sendEmptyMessageDelayed(AUTO_MSG, PHOTO_CHANGE_TIME);
                default:
                    break;
            }
        }
    };

    /**
     * 表示滚动到下一张图片这个动作
     */
    private static final int SCROLL_NEXT = 0;
    /**
     * 表示滚动到上一张图片这个动作
     */
    private static final int SCROLL_PREVIOUS = 1;

    private static final int SCROLL_BACK = 2;

    public ImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * 当View被添加到Window容器的时候才开始执行:生命周期依次先后 onMeasure > onLayout > onDraw >onAttachedToWindow
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.sendEmptyMessage(START_MSG); // 发送消息让图片自动开始滚动
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed || forceToRelayout) {
            imageCount = getChildCount();
            mImageWidth = getMeasuredWidth();
            mImageHeight = getMeasuredHeight();
            int marginLeft = 0;
            scroller.abortAnimation(); // 设置scroller为滚动状态
            this.scrollTo(0, 0); // 每次重新布局时候，重置滚动初始位置
            int[] items = {getIndexForItem(1), getIndexForItem(2),
                    getIndexForItem(3), getIndexForItem(4),
                    getIndexForItem(5)};
            imageItems = items;
            for (int i = 0; i < items.length; i++) {
                ImageView childView = (ImageView) getChildAt(items[i]);
                childView.layout(marginLeft, 0, marginLeft
                        + mImageWidth, mImageHeight);
                marginLeft = marginLeft + mImageWidth;
            }
            refreshImageView();
            forceToRelayout = false;
        }
    }

    private void refreshImageView() {
        for (int i = 0; i < imageItems.length; i++) {
            ImageView childView = (ImageView) getChildAt(imageItems[i]);
            childView.invalidate();
        }
    }

    private int getIndexForItem(int item) {
        int index = -1;
        index = mIndex + item - 3;
        while (index < 0) {
            index = index + imageCount;
        }
        while (index > imageCount - 1) {
            index = index - imageCount;
        }
        return index;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        float xLoc = ev.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMotionX = xLoc;
                mTouchState = TOUCH_STATE_REST;
                Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE");
                int xDif = (int) Math.abs(mMotionX - xLoc);
                if (xDif > mTouchSlop) {  // 当我们的水平距离滚动达到我们滚动的最小距离,开始拦截ViewGroup的事件给子控件分发
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onInterceptTouchEvent ACTION_UP");
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "onInterceptTouchEvent ACTION_CANCEL");
                mTouchState = TOUCH_STATE_REST;
                break;
            default:
                Log.e(TAG, "onInterceptTouchEvent DEFAULT");
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scroller.isFinished()) { // scroller还没有开始或者已经完成，以下代码在手指滑动的时候才开始执行
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            int action = event.getAction();
            float x = event.getX();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // 记录按下时的横坐标
                    mMotionX = x;
                case MotionEvent.ACTION_MOVE:
                    int disX = (int) (mMotionX - x);
                    mMotionX = x;
                    scrollBy(disX, 0);
                    break;
                case MotionEvent.ACTION_UP:
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    if (judeScrollToNext(velocityX)) {
                        // 下一张图
                        scrollToNext();
                    } else if (judeScrollToPrevious(velocityX)) {
                        // 上一张图
                        scrollToPrevious();
                    } else {
                        // 当前图片
                        scrollBack();
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    mHandler.sendEmptyMessageDelayed(HANDLE_MSG, PHOTO_CHANGE_TIME);
                    return true;
            }
        }
        return false;
    }

    private void scrollBack() {
        if (scroller.isFinished()) {
            beginScroll(getScrollX(), 0, -getScrollX(), 0, SCROLL_BACK);
        }
    }

    private void scrollToPrevious() {
        if (scroller.isFinished()) {
            setImageSwitchIndex(SCROLL_PREVIOUS);
            int disX = -mImageWidth - getScrollX();
            beginScroll(getScrollX(), 0, disX, 0, SCROLL_PREVIOUS);
        }
    }

    private void scrollToNext() {
        if (scroller.isFinished()) {
            setImageSwitchIndex(SCROLL_NEXT);
            int disX = mImageWidth - getScrollX();
            beginScroll(getScrollX(), 0, disX, 0, SCROLL_NEXT);
        }
    }

    /**
     * 图片开始滑动
     */
    private void beginScroll(int startX, int startY, int dx, int dy, final int action) {
        int duration = (int) (700f / mImageWidth * Math.abs(dx));
        scroller.startScroll(startX, startY, dx, dy, duration);
        invalidate();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (action == SCROLL_NEXT || action == SCROLL_PREVIOUS) {
                    forceToRelayout = true;
                    requestLayout();
                }
            }
        }, duration);
    }

    private void setImageSwitchIndex(int action) {
        if (action == SCROLL_NEXT) {
            if (mIndex < imageCount) {
                mIndex++;
            } else {
                mIndex = 0;
            }
        } else if (action == SCROLL_PREVIOUS) {
            if (mIndex > 0) {
                mIndex--;
            } else {
                mIndex = imageCount - 1;
            }
        }

    }

    /**
     * 判断时候滑向前一个
     *
     * @param velocityX
     * @return
     */
    private boolean judeScrollToPrevious(int velocityX) {
        return velocityX > SNAP_VELOCITY || getScrollX() < -mImageWidth / 2;
    }

    /**
     * 判断时候滑向后一个
     *
     * @param velocityX
     * @return
     */
    private boolean judeScrollToNext(int velocityX) {
        return velocityX < -SNAP_VELOCITY || getScrollX() > mImageWidth / 2;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            // 刷新View 否则效果可能有误差
            postInvalidate();
        }
    }
}
