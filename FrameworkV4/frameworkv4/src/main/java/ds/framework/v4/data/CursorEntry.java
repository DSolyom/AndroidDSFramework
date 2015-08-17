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

import android.database.Cursor;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.TableQuery;

public abstract class CursorEntry extends BasicCursorEntry {
	
	/**
	 * has cursor with data?
	 */
	private boolean mValid = false;
	
	/**
	 * preset query - use this and load(false) if you want to set the query in a fixed place (ie. in the main thread)
	 */
	TableQuery mQuery;
	
	public void load() {
		load(true);
	}
	
	public void load(boolean newQuery) {
		try {
			TableQuery query = null;
			if (newQuery) {
				query = newEntryLoaderQuery();
			} else {
				query = mQuery;
			}
			
			setCursor(query.load());

			if (mValid) {
				mCursor.moveToFirst();
			}
		} catch(Throwable e) {
			Debug.logException(e);
		}
	}
	
	/**
	 * ensure query before using load(false)
	 */
	public void ensureQuery() {
		mQuery = newEntryLoaderQuery();
	}
	
	/**
	 * set a cursor - be sure it is active and pointing to the data of the entry
	 * 
	 * @param c
	 */
	public void setCursor(Cursor c) {
		if (mCursor != null) {
			if (!mCursor.isClosed()) {
				mCursor.close();
			}
			mCursor = null;
		}
		if (c != null && c.getCount() > 0) {
			mCursor = c;
			mValid = true;
		} else {
			if (c != null) {
				c.close();
			}
			mCursor = null;
			mValid = false;
		}
	}
	
	/**
	 * has cursor with data?
	 * 
	 * @return
	 */
	public boolean isValid() {
		return mValid;
	}
	
	public void invalidate() {
		if (mCursor != null) {
			if (!mCursor.isClosed()) {
				mCursor.close();
			}
			mCursor = null;
		}
		mValid = false;
	}
	
	/**
	 * return preset entry loader query
	 * 
	 * @return
	 */
    abstract public TableQuery newEntryLoaderQuery();
}
