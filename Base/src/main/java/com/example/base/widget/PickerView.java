package com.example.base.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.ColorInt;
import java.util.ArrayList;
import java.util.List;

// TODO: 2024/1/13 后面在上传
public class PickerView extends View {
    private static final String TAG = "PickerView";
    private static final boolean DEBUG = false;
    /**
     * 是否纵向展示
     */
    private Boolean verticalDisplay = false;
    /**
     * todo 待完善 一次触摸周期内，只移动一格
     * todo 待完善 快速滑动显示下一屏
     * todo 置灰态
     */
    private Boolean singleMove = true;

    /**
     * 自动回滚到中间的速度
     */
    private static final int minRollSpan = 10;
    private static final byte PERIOD = 10;
    private static final byte CODE = 0X10;
    private List<DataContent> contentList;
    /**
     * 选中的位置，这个位置是mDataList的中心位置，一直不变
     */
    private int currentSelected;
    private void setCurrentSelected(int currentSelected) {
        Log.d(TAG, "setCurrentSelected: " + currentSelected);
        if (selectListener != null) {
            selectListener.onSelect(contentList.get(currentSelected));
        }
        this.currentSelected = currentSelected;
    }
    private Paint mPaint;

    /**
     * （字size + 间距）： 字size 的比例（用于控件字间距）
     */
    private static final float MARGIN_ALPHA = 2f;

    private static final float TEXT_SIZE = 50;
    private static final float INTERVAL_SPACE = 70;
    private int defaultTextColor = 0x000000;
    private int selectTextColor = 0xff0000;

    private int viewHeight;
    private int viewWidth;
    private float lastDownPlace;
    /**
     * 滑动的距离
     */
    private float moveLen = 0;
    private boolean isInit = false;
    private IOnSelectListener selectListener;
    private volatile boolean isInAnimate;
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != CODE) {
                return;
            }
            if (Math.abs(moveLen) < minRollSpan) {
                moveLen = 0;
            } else {
                /**
                 * 为了实现滚动手机离开屏幕后，回到文字居中为准的功能
                 * 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                 */
                moveLen = moveLen - moveLen / Math.abs(moveLen) * minRollSpan;
                sendEmptyMessageDelayed(CODE, PERIOD);
            }
            invalidate();
        }
    };

    public PickerView(Context context) {
        this(context, null);
    }

    public PickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        contentList = new ArrayList<>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);
        mPaint.setTextAlign(Align.CENTER);
    }

    public void setOnSelectListener(IOnSelectListener listener) {
        selectListener = listener;
    }

    // TODO: 2023/12/28
    public void setPickerContent(List<DataContent> data) {
        if (data == null) {
            throw new NullPointerException("data can not be null!!");
        }
        configPaint(mPaint, TEXT_SIZE, defaultTextColor);
        contentList.addAll(data);
        for (int i = 0; i < contentList.size(); i++) {
            contentList.get(i).setTextWidth(mPaint.measureText(contentList.get(i).title));
            Log.d(TAG,
                    "setPickerContent: title:" + contentList.get(i).title + " Width:"  + contentList.get(i).getTextWidth());
        }
        setCurrentSelected(Math.max(0, contentList.size() % 2 == 0 ? contentList.size() / 2 - 1 : contentList.size() / 2));
        updateHandler.removeMessages(CODE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewHeight = getMeasuredHeight();
        viewWidth = getMeasuredWidth();
        isInit = true;
        updateHandler.removeMessages(CODE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                doDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                doMove(event);
                break;
            case MotionEvent.ACTION_UP:
                doUp();
                break;
            default:
                break;
        }
        return true;
    }

    private void doDown(MotionEvent event) {
        updateHandler.removeMessages(CODE);
        lastDownPlace = Boolean.TRUE.equals(verticalDisplay) ? event.getY() : event.getX();
    }

    private void doMove(MotionEvent event) {
        float eventPlace = Boolean.TRUE.equals(verticalDisplay) ? event.getY() : event.getX();
        Log.d(TAG, "doMove1 moveLen: " + moveLen + " move2: " + (eventPlace - lastDownPlace));

        // 限制在滑动到内容 边界时 的滑动范围
        if (currentSelected == 0 && (eventPlace - lastDownPlace) > 0){
            moveLen = Math.min(moveLen + (eventPlace - lastDownPlace), (INTERVAL_SPACE + TEXT_SIZE) / 2);
        } else if (currentSelected == contentList.size() - 1 && eventPlace - lastDownPlace < 0){
            moveLen = Math.max(moveLen + (eventPlace - lastDownPlace), -(INTERVAL_SPACE + TEXT_SIZE) / 2);
        } else {
            moveLen += (eventPlace - lastDownPlace);
        }

        // TODO: 2023/12/28 横向滑动选中的阈值自定义
        // 当滑动超过阈值时，切换选中目标
        float curTextSpace = Boolean.TRUE.equals(verticalDisplay) ?
                (TEXT_SIZE + INTERVAL_SPACE) / 2 :
                (contentList.get(currentSelected).getTextWidth() + INTERVAL_SPACE) / 2;

        Log.d(TAG, "doMove2 moveLen: " + moveLen + " " + " curTextSpace:" + curTextSpace);

        if (moveLen > curTextSpace) {
            // 往下滑超过离开距离
            float nextTextSize = Boolean.TRUE.equals(verticalDisplay) ?
                    TEXT_SIZE : contentList.get(Math.max(currentSelected - 1, 0)).getTextWidth();
            float nextTextSpace = (nextTextSize + INTERVAL_SPACE) / 2;
            moveLen = moveLen - (curTextSpace + nextTextSpace);
            Log.d(TAG, "doMove4: down nextTextSpace:" + nextTextSpace + " moveLen:" + moveLen);
            moveLen = moveLen < -nextTextSpace ? -nextTextSize : moveLen;
            // moveLen = moveLen < 0 ? Math.max(moveLen, -nextTextSize) : Math.min(moveLen, nextTextSize);
            setCurrentSelected(Math.max(0, currentSelected - 1));
        } else if (moveLen < -curTextSpace) {
            // 往上滑超过离开距离
            float nextTextSize = Boolean.TRUE.equals(verticalDisplay) ?
                    TEXT_SIZE : contentList.get(Math.min(currentSelected + 1, contentList.size() - 1)).getTextWidth();
            float nextTextSpace = (nextTextSize + INTERVAL_SPACE) / 2;
            moveLen = moveLen + (curTextSpace + nextTextSpace);
            Log.d(TAG, "doMove5: up nextTextSpace:" + nextTextSpace + " moveLen:" + moveLen);
            moveLen = moveLen > nextTextSpace ? nextTextSize : moveLen;
            // moveLen = moveLen < 0 ? Math.max(moveLen, -nextTextSize) : Math.min(moveLen, nextTextSize);
            setCurrentSelected(Math.min(contentList.size() - 1, currentSelected + 1));
        }

        lastDownPlace = eventPlace;
        Log.d(TAG, "doMove3 moveLen: " + moveLen);
        invalidate();
    }

    // TODO: 2023/12/29
    private void doUp() {
        Log.d(TAG, "doUp moveLen: " + moveLen);
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(moveLen) < 0.0001) {
            moveLen = 0;
            return;
        }
        //这段是为了修正瞬滑导致回滚较多现象，屏蔽亦可
//        if (Math.abs(moveLen) > INTERVAL_SPACE) {
//            int m = (int) (Math.abs(moveLen) / INTERVAL_SPACE);
//            for (int i = 0; i < m; i++) {
//                if (moveLen > 0) {
//                    setCurrentSelected(Math.max(0, currentSelected - 1));
//                } else {
//                    setCurrentSelected(Math.min(contentList.size() - 1, currentSelected + 1));
//                }
//            }
//            if (moveLen > 0) {
//                moveLen -= INTERVAL_SPACE * m;
//            } else {
//                moveLen += INTERVAL_SPACE * m;
//            }
//        }
        updateHandler.removeMessages(CODE);
        updateHandler.sendEmptyMessage(CODE);
    }

    /**
     * @param up 是否向上滑动
     */
    public void autoScroll(boolean up) {
        if (isInAnimate) {
            return;
        }
        isInAnimate = true;
        final float[] values = up ? new float[]{INTERVAL_SPACE * 2 + 1, 0} : new float[]{0, INTERVAL_SPACE * 2 + 1};
        ValueAnimator va = ValueAnimator.ofFloat(values);
        va.setDuration(100);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(animation -> {
            float v = (float) animation.getAnimatedValue();
            doMove(MotionEvent.obtain(System.currentTimeMillis(), 0, 0, 0f, v, 0));
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                doUp();
                isInAnimate = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                doDown(MotionEvent.obtain(System.currentTimeMillis(), 0, 0, 0f, values[0], 0));
            }
        });
        va.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInit || contentList.isEmpty()) {
            return;
        }
        drawData(canvas);
    }

    private void drawData(Canvas canvas) {
        // 先绘制选中的text再往上往下绘制其余的text
        configPaint(mPaint, TEXT_SIZE, selectTextColor);
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        float x = Boolean.TRUE.equals(verticalDisplay) ? (float) (viewWidth / 2.0) : (float) (viewWidth / 2.0 + moveLen);
        float y = Boolean.TRUE.equals(verticalDisplay) ? (float) (viewHeight / 2.0 + moveLen) : (float) (viewHeight / 2.0);
        FontMetricsInt fmi = mPaint.getFontMetricsInt();
        //这里是经过简化后的公式
        float baseline = Boolean.TRUE.equals(verticalDisplay) ? (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0)) : y;
        // Log.d(TAG, "drawData: x:" + x + " baseline:" + baseline);
        canvas.drawText(contentList.get(currentSelected).title, x, baseline, mPaint);

        if (DEBUG) {
            configPaint(mPaint, TEXT_SIZE, defaultTextColor);
            canvas.drawCircle(x, baseline, 10f, mPaint);
            canvas.drawCircle((float) viewWidth / 2,(float) viewHeight / 2, 10f, mPaint);
        }

        // 绘制上方data
         for (int i = 1; (currentSelected - i) >= 0; i++) {
            drawOtherText(canvas, i, -1);
        }
        // 绘制下方data
         for (int i = 1; (currentSelected + i) < contentList.size(); i++) {
            drawOtherText(canvas, i, 1);
        }
    }

    /**
     * @param canvas Canvas
     * @param position 距离mCurrentSelected的差值
     * @param type     1表示向下绘制，-1表示向上绘制
     */
    // TODO: 2023/12/29
    private void drawOtherText(Canvas canvas, int position, int type) {
        configPaint(mPaint, TEXT_SIZE, defaultTextColor);

        int index = currentSelected + type * position;
        String text = contentList.get(index).title;

        float distance = Boolean.TRUE.equals(verticalDisplay) ?
                (TEXT_SIZE + INTERVAL_SPACE) * position + type * moveLen :
                computeStringsWidth(contentList, index, currentSelected) + type * moveLen;

        float itemStart = Boolean.TRUE.equals(verticalDisplay) ?
                (float) (viewHeight / 2.0 + type * distance) :
                (float) (viewWidth / 2.0 + type * distance);
//        Log.d(TAG, "drawOtherText: index：" + index + " selected:" + currentSelected + " type：" + type + " " +
//                "moveLen：" + moveLen + " text：" + text + " viewWidth/2：" + (viewWidth / 2.0) + " distance：" + distance);
        FontMetricsInt fmi = mPaint.getFontMetricsInt();

        float baseline;
        if (Boolean.TRUE.equals(verticalDisplay)) {
            baseline = (float) (itemStart - (fmi.bottom / 2.0 + fmi.top / 2.0));
        } else {
            float textWidth = mPaint.measureText(text);
             baseline = (itemStart + type * contentList.get(currentSelected).getTextWidth() / 2);
//            Log.d(TAG, "drawOtherText3: itemStart：" + itemStart + " textWidth：" + textWidth + " baseline：" + baseline);
        }
//        Log.d(TAG, "drawOtherText2: " + baseline);
        canvas.drawText(
                text,
                Boolean.TRUE.equals(verticalDisplay) ? (float) (viewWidth / 2.0) : baseline,
                Boolean.TRUE.equals(verticalDisplay) ? baseline : (float) (viewHeight / 2.0),
                mPaint
        );

        if (DEBUG) {
            canvas.drawCircle(baseline, (float) (viewHeight / 2.0), 10f, mPaint);
        }
    }

    private float computeStringsWidth(List<DataContent> data, int index, int selectedIndex) {
        if (index == selectedIndex) {
            return 0;
        }
        if (index < 0 || index > data.size() - 1 || selectedIndex < 0 || selectedIndex > data.size() - 1) {
            throw new ArrayIndexOutOfBoundsException("startItem or endItem out of data");
        }
        float width = 0;
        int variable = (index > selectedIndex) ? 1 : 0;
        for (int i = Math.min(index, selectedIndex) + variable; i < Math.max(index, selectedIndex) + variable; ++i) {
            // 边界线上的item 只加一半 width
            if (i == index) {
                width += data.get(i).getTextWidth() / 2 + INTERVAL_SPACE;
            } else {
                width += data.get(i).getTextWidth() + INTERVAL_SPACE;
            }
        }
        return width;
    }

    private void configPaint(Paint paint, Float size, @ColorInt int color) {
        paint.setTextSize(size);
        paint.setColor(color);
        paint.setAlpha(255);
    }

    public void setVerticalDisplay(Boolean verticalDisplay) {
        this.verticalDisplay = verticalDisplay;
    }

    public Boolean getVerticalDisplay() {
        return verticalDisplay;
    }

    public interface IOnSelectListener {
        void onSelect(DataContent item);
    }

    public static class DataContent {
        private final String title;

        public float getTextWidth() {
            return textWidth;
        }

        public void setTextWidth(float textWidth) {
            this.textWidth = textWidth;
        }

        private float textWidth;

        public DataContent(String title) {
            this.title = title;
            this.textWidth = textWidth;
        }
    }
}

