package ds.framework.v4.app;

import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.CursorList;
import ds.framework.v4.widget.CursorListPagerAdapter;

abstract public class DSCursorListPagerFragment extends AbsViewPagerFragment {

	public DSCursorListPagerFragment() {
		super();
	}
	
	public DSCursorListPagerFragment(boolean isDialog) {
		super(isDialog);
	}
	
	@Override
	protected void setAdapterData(AbsAsyncData data, int loadId) {
		if (data instanceof CursorList) {
			((CursorListPagerAdapter) mAdapter).setData((CursorList) data, loadId);
		}
	}

	@Override
	protected void invalidateAdapter() {
		if (mAdapter != null && (mAdapter instanceof CursorListPagerAdapter)) {
			((CursorListPagerAdapter) mAdapter).setCursor(null);
		}
		
		if (mData != null && mData.length > 0 && mData[0] instanceof CursorList) {
			((CursorList) mData[0]).closeCursor();
		}
	}
}
