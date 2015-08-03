/*
	Copyright 2015 Dániel Sólyom

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
package ds.framework.v4.widget;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.data.CursorListEntry;
import ds.framework.v4.data.MultiCursorList;

@SuppressLint("UseSparseArrays")
public abstract class MultiCursorListAdapter extends AbsTemplateAdapter<CursorListEntry> {
	
	protected HashMap<Integer, Cursor> mCursors = new HashMap<Integer, Cursor>();
	private CursorListEntry mCursorEntry;
	protected int mColumnId;
	
	private int mCount;
	protected int mNextCursorPosition = 0;
	
	public MultiCursorListAdapter(ActivityInterface in, int rowLayoutId) {
		super(in, rowLayoutId);
	}
		
	/**
	 * 
	 * @param data
	 */
	public void setData(MultiCursorList data, int loadId) {
		
		// assume only have one data per adapter
		// !override otherwise
		setCursors(data.getCursors());
	}

	/**
	 * 
	 * @param cursors
	 */
	synchronized public void setCursors(ArrayList<Cursor> cursors) {
		final HashMap<Integer, Cursor> oldCursors = mCursors;
		mCursors = new HashMap<Integer, Cursor>();
		mCount = 0;

		mNextCursorPosition = 0;
		if (cursors != null)
		for(Cursor c : cursors) {
			mCursors.put(mNextCursorPosition++, c);
			if (c == null) {
				continue;
			}
			mCount += c.getCount();
		}
		mCursorEntry = null;
		if (!mCursors.isEmpty()) {
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
		for(Cursor c : oldCursors.values()) {
			if (c == null) {
				continue;
			}
			if (!mCursors.containsValue(c)) {
				c.close();
			}
		}
	}
	
	/**
	 * 
	 */
	public void reset() {
		setCursors(null);
		super.reset();
	}
	
	/**
	 * put a cursor to the adapter at the given position<br/>
	 * !note: this will remove and close the current cursor at this position unless it's the same cursor
	 * 
	 * @param cursor
	 * @param position
	 */
	synchronized public void putCursor(Cursor cursor, int position) {
		if (cursor != null && mCursors.containsValue(cursor)) {
			if (mCursors.get(position) == cursor) {
				return;
			}
			
			for(int i = 0; i < mNextCursorPosition; ++i) {
				if (cursor == mCursors.get(i)) {
					mCursors.remove(i);
					mCount -= cursor.getCount();
					break;
				}
			}			
		}
		
		final Cursor removed = mCursors.remove(position);
		if (removed != null) {
			mCount -= removed.getCount();
			removed.close();
		}
		
		mCursors.put(position, cursor);
		if (cursor != null) {
			mCount += cursor.getCount();
		}
		
		if (position >= mNextCursorPosition) {
			mNextCursorPosition = position + 1;
		}
		
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mCount;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * sets which cursor the item is belong to and its position inside that cursor
	 * 
	 * @return
	 */
	synchronized public void getItemCursorPosition(int position, int[] result) {
		int count = 0;
		int oldCount = 0;
		for(int i = 0; i < mNextCursorPosition; ++i) {
			final Cursor c = mCursors.get(i);
			if (c == null) {
				continue;
			}
			count += c.getCount();
			if (count > position) {
				result[0] = i;
				result[1] = position - oldCount;
				return;
			}
			oldCount = count;
		}
		throw new IndexOutOfBoundsException("illegal position " + position);
	}

	@Override
	public Object getItem(int position) {
		return null;	// not used
	}
	
	/**
	 * 
	 * @param cursorPosition
	 * @param position
	 * @return
	 */
	synchronized public CursorListEntry getItem(int cursorPosition, int position) {
		final Cursor c = mCursors.get(cursorPosition);
		if (c == null) {
			throw new IndexOutOfBoundsException("illegal position " + position + " for cursor " + cursorPosition);
		}
		
		c.moveToPosition(position);
		
		mCursorEntry = new CursorListEntry(c);
		
		return mCursorEntry;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewParent) {
		convertView = ensureConvertView(position, convertView, viewParent);
		
		mTemplate.setRoot(convertView);
		
		fillRow(position);

		return convertView;
	}
	
	/**
	 * 
	 * @param position
	 */
	synchronized protected void fillRow(int position) {
		int count = 0;
		int cursorPosition = 0;
		while(cursorPosition < mNextCursorPosition) {
			final Cursor c = mCursors.get(cursorPosition);
			if (c == null) {
				++cursorPosition;
				continue;
			}
			count += c.getCount();
			if (position < count) {
				break;
			}
			position -= count;
			
			++cursorPosition;
		}
		final CursorListEntry e = getItem(cursorPosition, position);
		fillRow(e, cursorPosition, position);
	}
	
	@Override
	protected void fillRow(CursorListEntry data, int position) {
		;
	}
	
	/**
	 * 
	 * @param data
	 * @param cursorPosition
	 * @param position
	 */
	abstract protected void fillRow(CursorListEntry data, int cursorPosition, int position);
}
