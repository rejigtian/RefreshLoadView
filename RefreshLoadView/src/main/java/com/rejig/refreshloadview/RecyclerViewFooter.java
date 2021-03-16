package com.rejig.refreshloadview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;


public class RecyclerViewFooter extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_RELOADING = 2;
    public final static int STATE_GONE = 3;
    public final static int STATE_NONE = 4;
    public final static int STATE_INVISIBAL = 100;
    private Context context;
    private View contentView;
    private View progressBar;
    private TextView hintView;


    public RecyclerViewFooter(Context context) {
        this(context, null);
    }

    public RecyclerViewFooter(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewFooter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();

    }

    private void initView() {
        LinearLayout moreView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pullrefrefh_recyclerview_footer, null);
        addView(moreView);
        moreView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        contentView = moreView.findViewById(R.id.pullrefrefh_footer_content);
        progressBar = moreView.findViewById(R.id.pullrefrefh_footer_ProgressBar);
        hintView = (TextView) moreView.findViewById(R.id.pullrefrefh_footer_hint_TextView);
    }

    /**
     * 设置状态
     *
     * @param state
     */
    public void setState(int state) {
        progressBar.setVisibility(View.GONE);
        hintView.setVisibility(VISIBLE);
        if (state == STATE_READY) {
            hintView.setVisibility(View.VISIBLE);
            hintView.setText("正在加载");
            progressBar.setVisibility(View.VISIBLE);
        }
        else if (state == STATE_RELOADING) {
            show();
            hintView.setVisibility(View.VISIBLE);
            hintView.setText("上拉加载更多");
        }
        else if(state==STATE_GONE)
        {
            hintView.setText("没有更多了");
            progressBar.setVisibility(View.GONE);
        }
        else  if (state==STATE_NORMAL){
            hintView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            hintView.setText("上拉加载更多");
        }else if (state==STATE_NONE){
            progressBar.setVisibility(View.GONE);
            hintView.setVisibility(GONE);
            hintView.setText("没有数据");
        }else if (state==STATE_INVISIBAL){
            hintView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
//设置提示文字
    public void setFooterText(String s){
        hintView.setText(s);
    }
    /**
     * 设置距离下边的BottomMargin
     *
     * @param height
     */
    public void setBottomMargin(int height) {
        if (height < 0) return;
        LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
        lp.bottomMargin = height;
        contentView.setLayoutParams(lp);

    }

    /**
     * 获取BottomMargin
     *
     * @return
     */
    public int getBottomMargin() {
        LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
        return lp.bottomMargin;
    }


    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
        lp.height = 1;
        contentView.setLayoutParams(lp);
    }

    /**
     * show footer
     */
    public void show() {
        LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        contentView.setLayoutParams(lp);
    }
}