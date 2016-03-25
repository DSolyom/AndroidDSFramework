/*
	Copyright 2013 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package ds.framework.v4.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import ds.framework.v4.Global;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.Condition;
import ds.framework.v4.db.TableQuery;

/**
 * to load and show a list of filter items<br/>
 * if filtered table is given only items used in that table will be loaded
 */
public class DbEntryFilter implements Serializable {

	private static final long serialVersionUID = -2848699718595232264L;

	public static final int FILTER_ALL = -1;
	
	private String mFilterBy;
	private String mTitleColumn;
	private String mOrderColumn;
	private String mFiltered;
	private int[] mIds;
	private String[] mTitles;
	private FilterItem[] mExtraItems;
    private ArrayList<HashMap<String, String>> mDialogItems;

	private boolean mIsLoaded = false;
	
	protected FilterItem mSelectedItem;
	
	public DbEntryFilter(String filterBy, String filtered, FilterItem[] extraItems) {
		this(filterBy, filtered, "title", "title", extraItems);
	}

	public DbEntryFilter(String filterBy, String filtered, 
			String titleColumn, String orderColumn, FilterItem[] extraItems) {
		mFilterBy = filterBy;
		mTitleColumn = titleColumn;
		mOrderColumn = orderColumn;
		mFiltered = filtered;
		mExtraItems = extraItems == null ? new FilterItem[] {} : extraItems;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFilterBy() {
		return mFilterBy;
	}
	
	/**
	 * 
	 * @return
	 */
	public FilterItem[] getExtraItems() {
		return mExtraItems;
	}
	
	/**
	 * load filter item list<br/>
	 * also make sure that the item with the same id as the previously selected item gets selected
	 */
	public void load() {		
		final TableQuery query = getQuery();
		Cursor c = null;
		try {
			c = query.load();
			
			mIds = new int[c.getCount() + mExtraItems.length];
			mTitles = new String[c.getCount() + mExtraItems.length];
			
			int at = 0;
			for(FilterItem extra : mExtraItems) {
				mIds[at] = extra.id;
				mTitles[at++] = extra.title;
			}
			
			final Integer oldSelectedId = mSelectedItem == null ? 0 : mSelectedItem.id;
			
			if (c.moveToFirst()) {
				do {
					mIds[at] = c.getInt(0);
					mTitles[at] = c.getString(1);
					
					if (oldSelectedId == mIds[at]) {
						
						// if we are here it is certain that mSelectedItem is not null
						// set its title to the newly loaded one
						mSelectedItem.title = mTitles[at];
					}
					
					++at;
				} while(c.moveToNext());
			}
			c.close();

            mDialogItems = createDialogItems(mTitles);

			mIsLoaded  = true;
		} catch(Throwable e) {
			Debug.logException(e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
	}
	
	/**
	 * select a filter item silently
	 * 
	 * @param item
	 */
	public void selectItem(FilterItem item) {
		if (item != null && item.title == null) {
			
			// did not know the title, only the id? find the title
			mSelectedItem = findFilterItemById(item.id);
			
		} else {
			mSelectedItem = item;
		}
	}
	
	/**
	 * select a filter item silently by id
	 * 
	 * @param id
	 */
	public void selectItem(int id) {
		mSelectedItem = findFilterItemById(id);
	}
	
	/**
	 * return selected item position<br/>
	 * returns -1 if there is no selected item
	 * 
	 * @return
	 */
	public int getSelectedItemPosition() {
		if (mSelectedItem == null) {
			return -1;
		}
		int at = 0;
		for(int id : mIds) {
			if (mSelectedItem.id == id) {
				return at;
			}
			++at;
		}
		return -1;
	}
	
	/**
	 * get query to load filter items
	 * 
	 * @return
	 */
	protected TableQuery getQuery() {
		final TableQuery query = new TableQuery(mFilterBy, "filter", Global.getOpenDb());
		query.select("filter.id", "filter." + mTitleColumn);
		if (mFiltered != null) {
			query.join(mFiltered, mFiltered, "", 
					new Condition(mFiltered + "." + mFilterBy + "_id", "filter.id"));
		}
		query.orderBy("filter." + mOrderColumn);
		query.distinct(true);
		return query;
	}
	
	public boolean isLoaded() {
		return mIsLoaded;
	}
	
	/**
	 * valid filter items count
	 * 
	 * @return
	 */
	public int getCount() {
		return mIds == null ? 0 : mIds.length;
	}
	
	/**
	 * get a filter item by position<br/>
	 * the item is constructed here<br/>
	 * returns null if position is invalid
	 * 
	 * @param position
	 * @return
	 */
	public FilterItem getFilterItem(int position) {
		if (mIds == null || mIds.length <= position || position < 0) {
			return null;
		}
		return new FilterItem(mIds[position], mTitles[position]);
	}
	
	public FilterItem findFilterItemById(int id) {
		if (mIds == null) {
			return null;
		}
		final int sI = mIds.length;
		for(int i = 0; i < sI; ++i) {
			if (mIds[i] == id) {
				return new FilterItem(mIds[i], mTitles[i]);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public FilterItem getSelectedFilterItem() {
		return mSelectedItem;
	}
	
	/**
	 * show single choice dialog
	 *  
	 * @param listener
	 */
	public void show(final OnFilterItemSelectedListener listener) {
        Context context = Global.getCurrentActivity();
		ListAdapter adapter = createFilterAdapter(context, mDialogItems);
		Global.registerDialog(new AlertDialog.Builder(context).setSingleChoiceItems(adapter, -1,
				new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							mSelectedItem = new FilterItem(mIds[which], mTitles[which]);
							listener.onFilterItemSelected(mSelectedItem);
						} catch(Throwable e) {
							Debug.logException(e);
						}
						dialog.dismiss();
					}
		
				}
		).show());
	}

    /**
     *
     * @param titles
     * @return
     */
    public ArrayList<HashMap<String, String>> createDialogItems(String[] titles) {
        final ArrayList<HashMap<String, String>> dialogItems = new ArrayList<>();

        for(int i = 0; i < titles.length; ++i) {
            final HashMap<String, String> item = new HashMap<>();
            item.put("title", titles[i]);
            dialogItems.add(item);
        }

        return dialogItems;
    }

    /**
     *
     * @param context
     * @param dialogItems
     * @return
     */
    public ListAdapter createFilterAdapter(Context context, ArrayList<HashMap<String, String>> dialogItems) {
        return new SimpleAdapter(context, dialogItems, android.R.layout.simple_dropdown_item_1line, new String[] { "title" }, new int[] { android.R.id.text1 } );
    }
	
	/**
	 * show multi choice dialog
	 * 
	 * @param listener
	 */
	public void show(OnFilterItemsSelectedListener listener) {
		
	}
	
	public static class FilterItem implements Serializable {

		private static final long serialVersionUID = -5073508220488399604L;

		public int id;
		public String title;
		
		public FilterItem(int id, String title) {
			this.id = id;
			this.title = title;
		}
	}
	
	/**
	 * listener for single choice mode
	 */
	public interface OnFilterItemSelectedListener {
		public void onFilterItemSelected(FilterItem selected);
	}
	
	/**
	 * listener for multi choice mode
	 */
	public interface OnFilterItemsSelectedListener {
		public void onFilterItemsSelected(ArrayList<FilterItem> selected);
	}
}
