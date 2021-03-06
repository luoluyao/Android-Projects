package com.mikepenz.fastadapter_extensions.scroll;

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private int mPreviousTotal = 0;
    private boolean mLoading = true;
    private int mVisibleThreshold = 30;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

    private int mCurrentPage = 1;

    private RecyclerView.LayoutManager mLayoutManager;

    public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager layoutManager, int visibleThreshold) {
        this.mLayoutManager = layoutManager;
        this.mVisibleThreshold = visibleThreshold;
    }

    private int findFirstVisibleItemPosition(RecyclerView recyclerView) {
        final View child = findOneVisibleChild(0, mLayoutManager.getChildCount(), false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    private View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                                     boolean acceptPartiallyVisible) {
        OrientationHelper helper;
        if (mLayoutManager.canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(mLayoutManager);
        } else {
            helper = OrientationHelper.createHorizontalHelper(mLayoutManager);
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = mLayoutManager.getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        mVisibleItemCount = recyclerView.getChildCount();
        mTotalItemCount = mLayoutManager.getItemCount();
        mFirstVisibleItem = findFirstVisibleItemPosition(recyclerView);

        if (mLoading) {
            if (mTotalItemCount > mPreviousTotal) {
                mLoading = false;
                mPreviousTotal = mTotalItemCount;
            }
        }
        if (!mLoading && (mTotalItemCount - mVisibleItemCount)
                <= (mFirstVisibleItem + mVisibleThreshold)) {

            mCurrentPage++;

            onLoadMore(mCurrentPage);

            mLoading = true;
        }
    }

    public int getTotalItemCount() {
        return mTotalItemCount;
    }

    public int getFirstVisibleItem() {
        return mFirstVisibleItem;
    }

    public int getVisibleItemCount() {
        return mVisibleItemCount;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public abstract void onLoadMore(int currentPage);
}