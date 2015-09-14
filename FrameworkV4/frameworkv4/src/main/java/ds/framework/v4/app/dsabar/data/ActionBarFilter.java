package ds.framework.v4.app.dsabar.data;

import android.view.MenuItem;
import ds.framework.v4.app.DSActivity;
import ds.framework.v4.data.DbEntryFilter;

public class ActionBarFilter extends DbEntryFilter {

	private static final long serialVersionUID = 3987733000333968581L;
	
	private int mBarItemId;
	private int mBarIconRes;
	private String mOverflowTitle;

	public ActionBarFilter(int id, int iconRes, String title, 
			String filterBy, String filtered, FilterItem[] extraItems) {
		super(filterBy, filtered, extraItems);
		
		mBarItemId = id;
		mBarIconRes = iconRes;
		mOverflowTitle = title;
	}
	
	public ActionBarFilter(int id, int iconRes, String title,
			String filterBy, String filtered, 
			String titleColumn, String orderColumn, 
			FilterItem[] extraItems) {
		super(filterBy, filtered, titleColumn, orderColumn, extraItems);
		
		mBarItemId = id;
		mBarIconRes = iconRes;
		mOverflowTitle = title;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getId() {
		return mBarItemId;
	}
	
	/**
	 * put the filter on the bar/overflow menu<br/>
	 * !note: position is only taken into account if the filter is not already on the bar
	 * 
	 * @param activity
	 * @param position
	 */
	public void show(DSActivity activity, Integer position) {
		final MenuItem item = activity.findMenuItem(mBarItemId);
		if (item != null) {
			
			// already added - just turn it visible
			item.setVisible(true);
			return;
		}
		activity.addMenuItem(mBarItemId, mOverflowTitle, mBarIconRes, position, true);
	}
	
	/**
	 * hide the menu item for the filter
	 * 
	 * @param activity
	 */
	public void hide(DSActivity activity) {
		final MenuItem item = activity.findMenuItem(mBarItemId);
		if (item != null) {
			item.setVisible(false);
			return;
		}
	}
	
	/**
	 * remove the filter for good
	 * 
	 * @param activity
	 */
	public void remove(DSActivity activity) {
		activity.removeMenuItem(mBarItemId);
	}
	
	/**
	 * 
	 * @param activity
	 * @return
	 */
	public boolean isShown(DSActivity activity) {
		final MenuItem item = activity.findMenuItem(mBarItemId);
		return item != null && item.isVisible();
	}
}
