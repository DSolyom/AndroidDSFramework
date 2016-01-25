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
package ds.framework.v4.app;

import ds.framework.v4.Global;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.CursorData;
import ds.framework.v4.db.Condition;
import ds.framework.v4.db.TableQuery;
import android.os.Bundle;

abstract public class DSCursorEntryFragment extends AbsDSAsyncDataFragment {

	private int mEntryId;
	protected DSCursorEntryFragmentData mEntry;
	private String mMainTable;
	
	public DSCursorEntryFragment init(String mainTable) {
		mMainTable = mainTable;
		
		return this;
	}
	
	public void setEntryId(Integer id) {
		mEntryId = id;
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putInt(mFragmentId + "-entry-id", mEntryId);
    	outState.putString(mFragmentId + "-main-table", mMainTable);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mEntryId = savedInstanceState.getInt(mFragmentId + "-entry-id");
    	mMainTable = savedInstanceState.getString(mFragmentId + "-main-table");
    }
    
    @Override
    public AbsAsyncData[] getAsyncDataObjects() {
    	if (mEntry == null) {
    		mEntry = getEntry();
    	}

		return new AbsAsyncData[] { mEntry };
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		
		mEntry.invalidate();
    }

    @Override
    public void reset() {
    	super.reset();

    	mEntry = null;
    }

	protected DSCursorEntryFragmentData getEntry() {
		return new DSCursorEntryFragmentData();
	}
	
	/**
	 * DSCursorEntryFragmentEntry
	 */
	protected class DSCursorEntryFragmentData extends CursorData {

		@Override
		public TableQuery getLoaderQuery() {
			final TableQuery q = new TableQuery(mMainTable, "entry", Global.getOpenDb());
			q.filter(new Condition("entry.id", mEntryId));
			q.select("*");
			q.setLimit(1);
			return q;
		}
		
	}
}
