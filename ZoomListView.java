package v;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ListView;

import java.util.Date;

public class ZoomListView extends ListView {
    private static final int INVALID_POINTER_ID = -1;
    static int DOUBLE_TAP_DURATION = 300,//ms
            CLICK_DURATION = 300; //ms

    static float MAX_ZOOM = 4;

    private int activePointerId = INVALID_POINTER_ID;
    private ScaleGestureDetector scaleGestureDetector;

    private float scale = 1.f,
            maxWidth = 0.0f,
            maxHeight = 0.0f,
            previousX,
            previousY,
            mPosX,
            mPosY,
            width,
            height;

    long prevTime = 0;


    public ZoomListView(Context context) {
        super(context);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public ZoomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public ZoomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        final int action = ev.getAction();
        scaleGestureDetector.onTouchEvent(ev);
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                long t = (new Date()).getTime();
                if(t-prevTime<DOUBLE_TAP_DURATION && Math.abs(ev.getX()- previousX)<20){
                    mPosX = -(MAX_ZOOM/scale -1)*(ev.getRawX()-getX());
                    mPosY = -(MAX_ZOOM/scale -1)*(ev.getRawY()-getY());
                    scale = scale ==MAX_ZOOM?1:MAX_ZOOM;
                    maxWidth = width - (width * scale);
                    maxHeight = height - (height * scale);
                    prevTime = 0;
                    invalidate();
                } else prevTime = t;

                previousX = ev.getX();
                previousY = ev.getY();

                activePointerId = ev.getPointerId(0);
                break;

            case MotionEvent.ACTION_MOVE:
                int pointerIndex = ev.findPointerIndex(activePointerId);
                final float dx = ev.getX(pointerIndex) - previousX;
                final float dy = ev.getY(pointerIndex) - previousY;

                mPosX += dx;
                mPosY += dy;

                if (mPosX > 0.0f)
                    mPosX = 0.0f;
                else if (mPosX < maxWidth)
                    mPosX = maxWidth;

                if (mPosY > 0.0f)
                    mPosY = 0.0f;
                else if (mPosY < maxHeight)
                    mPosY = maxHeight;

                previousX = ev.getX(pointerIndex);
                previousY = ev.getY(pointerIndex);

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                if((new Date()).getTime()-prevTime<CLICK_DURATION)
                    this.performClick();
                activePointerId = INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    previousX = ev.getX(newPointerIndex);
                    previousY = ev.getY(newPointerIndex);
                    activePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(scale, scale);
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        if (scale == 1.0f) {
            mPosX = 0.0f;
            mPosY = 0.0f;
        }
        canvas.translate(mPosX, mPosY);
        canvas.scale(scale, scale);
        super.dispatchDraw(canvas);
        canvas.restore();
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(1.0f, Math.min(scale, MAX_ZOOM));
            maxWidth = width - (width * scale);
            maxHeight = height - (height * scale);
            invalidate();
            return true;
        }
    }
}

