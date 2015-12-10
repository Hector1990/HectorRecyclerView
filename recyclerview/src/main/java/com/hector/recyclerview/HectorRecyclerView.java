package com.hector.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class HectorRecyclerView extends RecyclerView {

    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_HEADER = 1;
    private final static int TYPE_FOOTER = 2;

    private RecyclerViewHeader mHeader;
    private RecyclerViewFooter mFooter;

    private float mLastY;
    private boolean mCanRefresh = false;
    private boolean mLoadMoreEnabled = true;
    private boolean mRefreshEnabled = true;

    private Adapter mWrapperAdapter;

    private RefreshListener listener;

    public HectorRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public HectorRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HectorRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mHeader = new RecyclerViewHeader(context);
        mFooter = new RecyclerViewFooter(context);
        mFooter.setVisibility(GONE);
    }

    public void setListener(RefreshListener listener) {
        this.listener = listener;
    }

    public void setRefreshEnabled(boolean mRefreshEnabled) {
        this.mRefreshEnabled = mRefreshEnabled;
    }

    public void setLoadMoreEnabled(boolean mLoadMoreEnabled) {
        this.mLoadMoreEnabled = mLoadMoreEnabled;
    }

    public void autoRefresh() {
        mHeader.autoDisplay();
        mHeader.release();
        if (listener != null) {
            listener.refresh();
        }
    }

    public void refreshComplete() {
        if (mRefreshEnabled) {
            mHeader.complete();
        }
    }

    public void loadComplete() {
        if (mLoadMoreEnabled) {
            mFooter.complete();
        }
    }

    public void noMoreData() {
        if (!mLoadMoreEnabled) {
            return;
        }
        if (getLayoutManager().getItemCount() <= getLayoutManager().getChildCount()){
            mFooter.setVisibility(GONE);
        }
        else {
            mFooter.setVisibility(VISIBLE);
            mFooter.noMore();
        }
    }

    private int findMaxPos(int[] pos) {
        int max = -1;
        for (int item : pos) {
            if (item > max) {
                max = item;
            }
        }
        return max;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && listener != null && mLoadMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisiblePos = 0;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisiblePos = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            else if (layoutManager instanceof LinearLayoutManager) {
                lastVisiblePos = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] pos = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(pos);
                lastVisiblePos = findMaxPos(pos);
            }
            if (lastVisiblePos >= layoutManager.getItemCount() - 1 && layoutManager.getItemCount() > layoutManager.getChildCount()
                    && layoutManager.getChildCount() > 0 && mLoadMoreEnabled) {
                mFooter.setVisibility(VISIBLE);
                mFooter.loading();
                listener.loadMore();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mLastY == -1) {
            mLastY = e.getRawY();
        }
        int action = e.getAction();
        if (mRefreshEnabled) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mLastY = e.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaY = e.getRawY() - mLastY;
                    mLastY = e.getRawY();
                    mCanRefresh = mHeader.onMove(deltaY / 3);
                    break;
                default:
                    mLastY = -1;
                    if (mCanRefresh) {
                        if (listener != null) {
                            mHeader.release();
                            listener.refresh();
                        }
                    } else {
                        mHeader.resetHeader();
                    }
                    break;
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapperAdapter = new WrapperAdapter(adapter, mHeader, mFooter);
        super.setAdapter(mWrapperAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
    }

    private AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mWrapperAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapperAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapperAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapperAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapperAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapperAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    protected class WrapperAdapter extends Adapter {

        private Adapter mMainAdapter;
        private RecyclerViewHeader mHeaderView;
        private RecyclerViewFooter mFooterView;

        public WrapperAdapter(Adapter mMainAdapter, RecyclerViewHeader mHeaderView, RecyclerViewFooter mFooterView) {
            this.mMainAdapter = mMainAdapter;
            this.mHeaderView = mHeaderView;
            this.mFooterView = mFooterView;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new SimpleViewHolder(mHeaderView);
            }
            if (viewType == TYPE_FOOTER) {
                return new SimpleViewHolder(mFooterView);
            }
            return mMainAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == 0) {
                return;
            }
            int realPos = position - 1;
            if (mMainAdapter != null) {
                if (realPos < mMainAdapter.getItemCount()) {
                    mMainAdapter.onBindViewHolder(holder, realPos);
                    return;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mMainAdapter == null) {
                return 1;
            }
            return 1 + mMainAdapter.getItemCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            else if (position == getItemCount() - 1) {
                return TYPE_FOOTER;
            }
            if (mMainAdapter != null) {
                int realPos = position - 1;
                if (realPos < mMainAdapter.getItemCount()) {
                    return mMainAdapter.getItemViewType(realPos);
                }
            }
            return TYPE_NORMAL;
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public interface RefreshListener {
        public void refresh();
        public void loadMore();
    }

}
