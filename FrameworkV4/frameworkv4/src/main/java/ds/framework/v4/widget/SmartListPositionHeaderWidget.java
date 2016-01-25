package ds.framework.v4.widget;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

public class SmartListPositionHeaderWidget extends RecyclerView.OnScrollListener {

	private Integer mSavedFirstChildTop;
	private int mSavedFirstVisibleItem;
	
	@Override
	public void onScrolled(RecyclerView view, int dx, int dy) {
		if (view.getChildCount() == 0) {
			return;
		}

        final RecyclerView.LayoutManager lm = view.getLayoutManager();
		int firstVisibleItem = 0;

        if (lm instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findFirstVisibleItemPosition();
        } else if (lm instanceof GridLayoutManager) {
            firstVisibleItem = ((GridLayoutManager) view.getLayoutManager()).findFirstVisibleItemPosition();
        } else {

            // TODO: ?
            return;
        }

		final int fcTop = view.getChildAt(0).getTop();
		if (mSavedFirstChildTop == null || 
				mSavedFirstChildTop == fcTop && mSavedFirstVisibleItem == firstVisibleItem) {
			mSavedFirstChildTop = fcTop;
			mSavedFirstVisibleItem = firstVisibleItem;
			return;
		}
		mSavedFirstChildTop = fcTop;
		mSavedFirstVisibleItem = firstVisibleItem;
		
		showPositionHeader(mSavedFirstVisibleItem);
	}

	@Override
	public void onScrollStateChanged(RecyclerView view, int newState) {
		;
	}

	/**
	 * Override to show header in the way you want
	 */
	protected void showPositionHeader(int position) {
		
	}
}
