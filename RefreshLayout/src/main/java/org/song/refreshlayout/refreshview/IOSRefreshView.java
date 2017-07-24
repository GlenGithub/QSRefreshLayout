package org.song.refreshlayout.refreshview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.song.refreshlayout.IRefreshView;
import org.song.refreshlayout.QSBaseRefreshLayout;
import org.song.refreshlayout.QSRefreshLayout;
import org.song.refreshlayout.R;

/**
 * Created by song on 2017/7/21.
 */

public class IOSRefreshView extends FrameLayout implements IRefreshView {

    private float density;
    private Paint mPaint, mPaint1, mPaint2;
    private int circleRadius;
    private int subCircleRadius;
    private int offset;
    private int height;

    private ImageView imageView;
    private int status;

    public IOSRefreshView(Context context) {
        this(context, null);
    }

    public IOSRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xffb2b2b2);

        mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint1.setColor(Color.WHITE);
        mPaint1.setAntiAlias(true);                      //设置画笔颜色
        mPaint1.setStrokeWidth(2.5f * density);              //线宽
        mPaint1.setStyle(Paint.Style.STROKE);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setColor(Color.WHITE);
        mPaint2.setAntiAlias(true);                      //设置画笔颜色
        mPaint2.setStyle(Paint.Style.FILL);

        circleRadius = (int) (density * 16);

        setLayoutParams(new ViewGroup.LayoutParams(-1, height = circleRadius * 9));

        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.loading);
        FrameLayout.LayoutParams l = new LayoutParams((int) (30 * density), (int) (30 * density));
        l.gravity = Gravity.CENTER_HORIZONTAL;
        l.topMargin = circleRadius / 2;
        imageView.setLayoutParams(l);
        imageView.setVisibility(GONE);
        addView(imageView);
    }

    public void setCircleColor(int c) {
        mPaint.setColor(c);
    }

    public void setRefreshColor(int c) {
        mPaint1.setColor(c);
        mPaint2.setColor(c);
    }

    @Override//确定view大小
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        if (specMode != MeasureSpec.EXACTLY)//高度不确定时(xml设置为wrap_content时) 自己设置默认值
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    RectF oval = new RectF();
    int rotate = 30;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (status == QSRefreshLayout.STATUS_DRAGGING | status == QSRefreshLayout.STATUS_DRAGGING_REACH) {
            int w = getWidth();
            float y = circleRadius * 1.5f;
            subCircleRadius = (int) (circleRadius * (0.9f - .9f * offset / triggerDistance()));
            canvas.drawCircle(w / 2, y, circleRadius, mPaint);//大圆
            canvas.drawCircle(w / 2, y + offset, subCircleRadius, mPaint);//小圆
            drawBezierCurve(canvas, y);//贝赛尔曲线

            //刷新图标
            int rotatelen = 290;
            float radius = circleRadius / 2f;
            oval.left = w / 2 - radius;
            oval.top = y - radius;
            oval.right = w / 2 + radius;
            oval.bottom = y + radius;
            canvas.drawArc(oval, rotate, rotatelen, false, mPaint1);
            //三角形
            float len = 10 * density;
            mArrow.reset();
            mArrow.moveTo(0, 0);
            mArrow.lineTo(len, 0);
            mArrow.lineTo(len / 2, len / 2);
            mArrow.lineTo(0, 0);
            mArrow.offset(w / 2 + radius - len / 2, y);
            mArrow.close();
            canvas.rotate(-(360 - rotatelen - rotate), w / 2, y);
            canvas.drawPath(mArrow, mPaint2);
        }
    }

    private Path mArrow = new Path();
    private Path mPath = new Path();

    /**
     * 绘制贝塞尔曲线
     * 计算两圆切点...
     */
    private void drawBezierCurve(Canvas canvas, float y) {
        int w = getWidth();
        mPath.reset();
        mPath.moveTo(w / 2 - circleRadius, y);
        mPath.lineTo(w / 2 + circleRadius, y);
        mPath.quadTo(w / 2 + subCircleRadius, y + offset / 2, w / 2 + subCircleRadius, y + offset);
        mPath.lineTo(w / 2 - subCircleRadius, y + offset);
        mPath.quadTo(w / 2 - subCircleRadius, y + offset / 2, w / 2 - circleRadius, y);
        mPath.close();  // 闭合
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void updateStatus(int status) {
        this.status = status;
        if (status == QSRefreshLayout.STATUS_DRAGGING_REACH) {
            ((QSBaseRefreshLayout) getParent()).forcedIntoRefresh(isHead ? (int) (circleRadius * 3f) : -(int) (circleRadius * 3f));
        } else if (status == QSRefreshLayout.STATUS_REFRESHING) {
            ((AnimationDrawable) imageView.getDrawable()).start();
            imageView.setVisibility(VISIBLE);
        } else if (status == QSRefreshLayout.STATUS_NORMAL) {
            ((AnimationDrawable) imageView.getDrawable()).stop();
            imageView.setVisibility(GONE);
            offset = 0;
        }

    }

    @Override
    public void updateProgress(float progress) {
        rotate = (int) (30 + 120 * progress);
        invalidate();
    }

    @Override
    public boolean isBringToFront() {
        return false;
    }

    @Override
    public float dragRate() {
        return .6f;
    }

    @Override
    public int triggerDistance() {
        return getMeasuredHeight();
    }

    @Override
    public int maxDistance() {
        return 0;
    }

    @Override
    public int getThisViewOffset(int offset) {
        if (Math.abs(offset) > circleRadius * 3f) {
            this.offset = (int) (offset - circleRadius * 3f);
            return isHead ? getMeasuredHeight() : -getMeasuredHeight();
        } else {
            this.offset = 0;
            int i = (int) (getMeasuredHeight() - circleRadius * 3f + offset);
            return isHead ? i : -i;
        }
    }

    @Override
    public int getTargetOffset(int offset) {
        return offset;
    }

    @Override
    public int completeAnimaDuration() {
        return 0;
    }

    private boolean isHead;

    @Override
    public void isHeadView(boolean isHead) {
        this.isHead = isHead;
        if (!isHead)
            throw new IllegalArgumentException("foot not support");
    }
}
