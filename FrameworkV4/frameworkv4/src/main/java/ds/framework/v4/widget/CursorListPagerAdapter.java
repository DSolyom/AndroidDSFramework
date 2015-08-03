package ds.framework.v4.widget;

import android.database.Cursor;
import android.view.View;
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.data.CursorList;
import ds.framework.v4.data.CursorListEntry;

public abstract class CursorListPagerAdapter extends AbsTemplatePagerAdapter {

	private CursorListEntry mEntry;
	
	public CursorListPagerAdapter(ActivityInterface activity, int pageLayoutID) {
		super(activity, pageLayoutID);
		
		mEntry = new CursorListEntry();
	}

	/**
	 * 
	 * @param data
	 * @param loadId
	 */
	public void setData(CursorList data, int loadId) {
		setCursor(data.getCursor());
	}

	/**
	 * 
	 * @param c
	 */
	public void setCursor(Cursor c) {
		mEntry.setCursor(c);
	}

	/**
	 * 
	 * @param position
	 * @return
	 */
	public CursorListEntry getItemData(int position) {
		final Cursor cursor = mEntry.getCursor();
		if (cursor != null) {
			cursor.moveToPosition(position);
		}
		
		return mEntry;
	}

	@Override
	public void fillItem(View item, int position) {
		fillItem(getItemData(position), position);
	}

	/**
	 * 
	 * @return
	 */
	public int getCount() {
		return mEntry.getCount();
	}

	public Integer getInt(String field) {
		return mEntry.getInt(field);
	}

	public String getString(String field) {
		return mEntry.getString(field);
	}
	
	public boolean getBoolean(String field) {
		return mEntry.getBoolean(field);
	}
	
	public double getDouble(String field) {
		return mEntry.getDouble(field);
	}
	
	abstract protected void fillItem(CursorListEntry data, int position);

}
