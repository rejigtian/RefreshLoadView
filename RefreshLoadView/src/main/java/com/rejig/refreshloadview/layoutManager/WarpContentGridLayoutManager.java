package com.rejig.refreshloadview.layoutManager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WarpContentGridLayoutManager extends GridLayoutManager {
    public WarpContentGridLayoutManager(Context context, int spanCount,
                                        @RecyclerView.Orientation int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation,reverseLayout);
    }
    public WarpContentGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public WarpContentGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
