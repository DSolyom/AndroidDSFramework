package ds.framework.v4.widget;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class SmartListPositionHeaderWidget implements OnScrollListener {

	private Integer mSavedFirstChildTop;
	private int mSavedFirstVisibleItem;
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (visibleItemCount == 0) {
			return;
		}
		final int fcTop = ((ListView) view).getChildAt(0).getTop();
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
	public void onScrollStateChanged(AbsListView view,
			int scrollState) {
		;
	}

	/**
	 * Override to show header in the way you want
	 */
	protected void showPositionHeader(int position) {
		
	}
}
