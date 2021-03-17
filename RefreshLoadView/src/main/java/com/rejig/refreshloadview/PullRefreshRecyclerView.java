package com.rejig.refreshloadview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PullRefreshRecyclerView extends RecyclerView {
    private int state;
    /**
     * 记录定点与尾节点
     */
    private int a,b;
    private float mLastY = -1; // save event y
    /**
     * 滚动需要的时间
     */
    public final static int SCROLL_DURATION = 200;

    /**
     * 提示消息显示时间
     */
    public final static int MESSAGE_SHOW_DURATION = 2000;
    /**
     * 阻尼效果
     */
    private final static float OFFSET_RADIO = 1.5f;
    /**
     * 上拉加载的距离,默认50px
     */
    public static final int PULL_LOAD_MORE_DELTA = 50;
    /**
     * 下拉刷新的距离,默认150px
     */
    private static final int PULL_REFRESH_DELTA = 150;

    /**
     * 是否设置为自动加载更多,目前没实现
     */
    private boolean mEnableAutoLoading = false;
    /**
     * 是否可以上拉  默认可以
     */
    public boolean mEnablePullLoad = true;
    /**
     * 是否可以下拉   默认可以
     */
    public boolean mEnablePullRefresh = true;
    /**
     * 是否正在加载
     */
    private boolean mPullLoading = false;
    /**
     * 是否正在刷新
     */
    private boolean mPullRefreshing = false;
    /**
     * 区分上拉和下拉
     */
    private int mScrollBack;
    private final static int SCROLLBACK_HEADER = 0;
    private final static int SCROLLBACK_FOOTER = 1;
    //滚动类
    private Scroller mScroller;

    //头布局控件
    public RecyclerViewHeader mHeaderView;
    //尾控件
    public RecyclerViewFooter mFooterView;
    //消息提示类
    private MassageRelativeLayout mParent;
    //adapter的装饰类
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;


    public PullRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public PullRefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        //滚动类
        mScroller = new Scroller(context, new DecelerateInterpolator());
        //获取到头布局
        mHeaderView = new RecyclerViewHeader(context);
        mHeaderView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //获取尾布局
        mFooterView = new RecyclerViewFooter(context);
        mFooterView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mFooterView.setState(RecyclerViewFooter.STATE_INVISIBAL);
    }


    private Adapter adapter;

    @Override
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(adapter);


        //添加头,确保是第一个
        mHeaderAndFooterWrapper.addHeaderView(mHeaderView);
        //添加尾,确保是第最后一个
        mHeaderAndFooterWrapper.addFootView(mFooterView);
        //获取到它的父容器
        if (getParent() instanceof MassageRelativeLayout) {
            mParent = (MassageRelativeLayout) getParent();
        }
        super.setAdapter(mHeaderAndFooterWrapper);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mLastY == -1) {
            mLastY = e.getRawY();
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHeaderView.setVisibility(GONE);
                //按下的时候记录值
                mLastY = e.getRawY();
                if (mEnablePullRefresh&& (!canScrollVertically(-1)) && (mHeaderView.getVisibleHeight() > 0 )) {
                    // 更新头部高度
                    a=mHeaderView.getRealityHeight();
                    Log.e("6666666", "onTouchEvent: "+a );
                }
                Log.e("6666666", "onTouchEvent: "+a );
//                else if (isSlideToBottom())
//                {
//                    b=mFooterView.getBottomMargin();
//                }
                break;
            case MotionEvent.ACTION_MOVE:


                float moveY = e.getRawY();
                //手指滑动的差值
                float distanceY = moveY - mLastY;
                mLastY = moveY;

                //第一个条目完全显示   //头部高度大于0   deltaY大于0  向下移动
                Log.e("PullRefreshRecyclerView",canScrollVertically(-1)+"==="+canScrollVertically(1)+mFooterView.getBottomMargin() +"000"+distanceY+mEnablePullLoad);
                if (mEnablePullRefresh&&((!canScrollVertically(-1))|| mHeaderView.getVisibleHeight() >= 0) && distanceY > 0) {
                    // 更新头部高度
                    updateHeaderHeight(distanceY / OFFSET_RADIO);
                }
                else if (mEnablePullLoad&&isSlideToBottom() && (mFooterView.getBottomMargin() > 0 || distanceY < 0)) {
                    Log.e("PullRefreshRecyclerView",mFooterView.getBottomMargin()+"-------111------"+distanceY);
                    //已经到达底部,改变状态或者自动加载
                    updateFooterHeight(-distanceY / OFFSET_RADIO);
                    if (mEnablePullLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA && !mPullLoading){
                        mPullLoading = true;
                        if (mOnRefreshListener != null&&mEnablePullLoad) {
                            mOnRefreshListener.onLoadMore();
                        }
                    }
                }else if (distanceY > 0){
                    updateFooterHeight(-distanceY / OFFSET_RADIO);
                }

                break;
            default:
                mLastY = -1; // 复位
                if (mEnablePullRefresh&&(!canScrollVertically(-1))){
                    // 松手的时候  高度大于  一定值  调用刷新
                    if (mEnablePullRefresh && mHeaderView.getVisibleHeight() > a+PULL_REFRESH_DELTA) {
                        Log.e("6666666", "onTouchEvent: "+a +mHeaderView.getVisibleHeight());
                        //变为刷新状态
                        mPullRefreshing = true;
                        mHeaderView.setState(RecyclerViewHeader.STATE_REFRESHING);
                        //回调事件
                        if (mOnRefreshListener != null) {
                            mOnRefreshListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                }
                else if (isSlideToBottom()&&mEnablePullLoad) {
//                    if (mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA && !mPullLoading) {
//                        mPullLoading = true;
//                        mFooterView.setState(RecyclerViewFooter.STATE_READY);
//                        if (mOnRefreshListener != null) {
//                            mOnRefreshListener.onLoadMore();
//                        }
//
//                    }
                    resetFooterHeight();
                }
                else {
                    resetFooterHeight();
                    resetHeaderHeight();
                }

                break;
        }


        return super.onTouchEvent(e);
    }

    /**
     * 更新尾部加载
     *
     * @param distance 上拉距离
     */
    private void updateFooterHeight(float distance) {
        int height = mFooterView.getBottomMargin() + (int) distance;
        Log.e("PullRefreshRecyclerView","-------------"+height);
        if (mEnablePullLoad && !mPullLoading) {
            if (height > PULL_LOAD_MORE_DELTA) {
                //改变状态
                mFooterView.setState(RecyclerViewFooter.STATE_READY);
            }
        }
        mFooterView.setBottomMargin(height);

    }

    /**
     * 更新头部刷新
     *
     * @param distance 下拉距离
     */
    private void updateHeaderHeight(float distance) {
        mHeaderView.setVisibility(VISIBLE);
        // 设置头部高度,原先的高度加上
        mHeaderView.setVisibleHeight((int) distance + mHeaderView.getVisibleHeight());
        // 未处于刷新状态，更新箭头
        if (mEnablePullRefresh && !mPullRefreshing) {
            //下拉高度到达可以刷新的位置
            if (mHeaderView.getVisibleHeight() - a>PULL_REFRESH_DELTA) {
                mHeaderView.setState(RecyclerViewHeader.STATE_READY);
            } else {
                mHeaderView.setState(RecyclerViewHeader.STATE_NORMAL);
            }
        }
        //移动到顶部
        smoothScrollBy(0, 0);
    }

    /**
     * 重置头部高度
     */
    private void resetHeaderHeight() {
        int height = mHeaderView.getVisibleHeight();
        if (height == 0) // 如果=0  是不可见的 直接返回
            return;

        if (mPullRefreshing && height <= mHeaderView.getRealityHeight()) {
            return;
        }

        int finalHeight = 0;

        if (mPullRefreshing && height > mHeaderView.getRealityHeight()) {
            finalHeight = mHeaderView.getRealityHeight();
        }
        if (mParent != null) {
            if (mHeaderView.getVisibleHeight() == mParent.getHeaderMessageViewHeight()) {
                finalHeight = mParent.getHeaderMessageViewHeight();
            }
        }

        mScrollBack = SCROLLBACK_HEADER;//设置标识
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        // 触发计算滚动
        invalidate();
    }

    /**
     * 重置尾部高度
     */
    private void resetFooterHeight() {
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 0) {
            mScrollBack = SCROLLBACK_FOOTER;//设置标识
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }

    public void scrollToHead(){
        mScroller.startScroll(mScroller.getCurrX(),mScroller.getStartY(),mScroller.getCurrX(),mScroller.getCurrY(),500);
        invalidate();
    }
    /**
     * 停止刷新
     */
    public void stopRefresh() {
        mScrollBack = SCROLLBACK_HEADER;//设置标识
        int obligateHeight;
        if (mParent != null) {
            obligateHeight = mParent.getHeaderMessageViewHeight();
        } else {
            obligateHeight = 0;
        }
        int height = mHeaderView.getVisibleHeight();
        if (mPullRefreshing) {
            //是否复位

            mPullRefreshing = false;

            //显示更新了多少条消息
            if (mParent != null) {
                mParent.showMessage();
            }
            mScroller.startScroll(0, height, 0, obligateHeight - height, SCROLL_DURATION);
            // 触发计算滚动
            invalidate();

            //延时执行复位移动
            if (mParent != null) {
                handler.removeCallbacksAndMessages(null);
                handler.sendEmptyMessageDelayed(1, MESSAGE_SHOW_DURATION);
            }
        }

    }

    /**
     * 停止加载
     */
    public void stopLoadMore() {
            mPullLoading=false;
    }

    /**
     * 消息
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mHeaderView.getVisibleHeight() == mParent.getHeaderMessageViewHeight()) {
//                resetHeaderHeight();
                mScroller.startScroll(0, mHeaderView.getVisibleHeight(), 0, -mHeaderView.getVisibleHeight(), SCROLL_DURATION);
                postInvalidate();
            }

            mParent.hideMessage();
        }
    };

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLLBACK_HEADER) {
                mHeaderView.setVisibleHeight(mScroller.getCurrY());
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
            }
            postInvalidate();
        }
        super.computeScroll();
    }

    public OnRefreshListener mOnRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    /**
     * 刷新接口,
     */
    public interface OnRefreshListener {
        void onRefresh();
        void onLoadMore();
    }

    /**
     * 判断是否到底
     *
     * @return boolean
     */
    private boolean isSlideToBottom() {
        return (!canScrollVertically(1));
    }
    public void setState(int state){
        this.state=state;
    }

}