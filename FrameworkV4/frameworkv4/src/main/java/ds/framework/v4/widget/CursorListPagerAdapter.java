package ds.framework.v4.widget;

import android.database.Cursor;
import android.view.View;

import ds.framework.v4.app.DSActivity;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.CursorData;
import ds.framework.v4.db.TableQuery;

public abstract class CursorListPagerAdapter extends AbsTemplatePagerAdapter {
	
	public CursorListPagerAdapter(DSActivity activity, int pageLayoutID) {
		super(activity, pageLayoutID);

        // just use an empty data as default
		mPagerData = new CursorData() {

			@Override
			protected TableQuery getLoaderQuery() {
				return null;
			}
		};
	}

    @Override
    public void onDataLoaded(AbsAsyncData data, int loadId) {
        notifyDataSetChanged();
    }

	/**
	 * 
	 * @param position
	 * @return
	 */
	public CursorData getItem(int position) {
		final Cursor cursor = ((CursorData) mPagerData).getCursor();
		if (cursor != null) {
			cursor.moveToPosition(position);
		}
		
		return (CursorData) mPagerData;
	}

	@Override
	public void fillItem(View item, int position) {
		fillItem(getItem(position), position);
	}

    /**
     *
     * @return
     */
    public int getCount() {
        return mPagerData == null ? 0 : ((CursorData) mPagerData).getCount();
    }
	
	abstract protected void fillItem(CursorData data, int position);

}
