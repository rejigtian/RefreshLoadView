package com.rejig.refreshloadview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.RequiresApi;


public class MassageRelativeLayout extends RelativeLayout {

    //显示消息的控件
    private LinearLayout mHeaderMessageView;
    private TextView mHeaderMessageText;


    private int mHeaderMessageViewHeight;
    //滚动类
    private Scroller mScroller;

    public MassageRelativeLayout(Context context) {
        this(context, null);
    }

    public MassageRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MassageRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        init(context);
    }

    private void init(Context context) {
        //滚动类
        mScroller = new Scroller(context, new DecelerateInterpolator());
        //初始化一个显示消息的textView
        mHeaderMessageView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pullrefresh_header_message, (ViewGroup) getParent(), false);
        mHeaderMessageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

        mHeaderMessageText = (TextView) mHeaderMessageView.findViewById(R.id.pullRefresh_message);

        // 初始化 头部高度
        mHeaderMessageText.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        mHeaderMessageViewHeight = mHeaderMessageText.getHeight();//57

                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //确保添加到后面
        addView(mHeaderMessageView, 1);
    }

    public void showMessage() {
        mScroller.startScroll(0, getHeaderMessageViewHeight(), 0, 0, PullRefreshRecyclerView.SCROLL_DURATION);
        invalidate();
    }

    public void hideMessage() {
        mScroller.startScroll(0, getVisibleHeight(), 0, -getVisibleHeight(), PullRefreshRecyclerView.SCROLL_DURATION);
        invalidate();
    }


    /**
     * 设置消息
     */
    public void setMessage(String message) {
        mHeaderMessageText.setText(message);

    }


    /**
     * 获取消息总高度
     *
     * @return
     */
    public int getHeaderMessageViewHeight() {
        return mHeaderMessageViewHeight;
    }

    /**
     * 设置隐藏高度
     *
     * @param height
     */
    private void setVisibleHeight(int height) {
        if (height < 0) {
            height = 0;
        }

        LayoutParams lp = (LayoutParams) mHeaderMessageView.getLayoutParams();
        lp.height = height;
        mHeaderMessageView.setLayoutParams(lp);
    }

    /**
     * 获取隐藏的高度
     *
     * @return
     */
    public int getVisibleHeight() {
        return mHeaderMessageView.getLayoutParams().height;
    }
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {

            setVisibleHeight(mScroller.getCurrY());

            postInvalidate();
        }
        super.computeScroll();
    }
}
