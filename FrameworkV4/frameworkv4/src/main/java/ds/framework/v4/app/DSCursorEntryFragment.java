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
import ds.framework.v4.data.CursorEntry;
import ds.framework.v4.db.Condition;
import ds.framework.v4.db.TableQuery;
import android.os.Bundle;

abstract public class DSCursorEntryFragment extends DSFragment {

	private int mEntryId;
	protected DSCursorEntryFragmentEntry mEntry;
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
    public void loadData() {
    	if (mEntry == null) {
    		mEntry = getEntry();
    	}
    	mEntry.load();
    	
    	super.loadData();
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		
		mEntry.invalidate();
    }
    
    @Override
    public void invalidateData() {
    	if (mEntry != null) {
    		mEntry.invalidate();
    	}
    	super.invalidateData();
    }
    
    @Override
    public void reset() {
    	super.reset();
    	mEntry = null;
    }

	protected DSCursorEntryFragmentEntry getEntry() {
		return new DSCursorEntryFragmentEntry();
	}
	
	/**
	 * DSCursorEntryFragmentEntry
	 */
	protected class DSCursorEntryFragmentEntry extends CursorEntry {

		@Override
		public TableQuery newEntryLoaderQuery() {
			final TableQuery q = new TableQuery(mMainTable, "entry", Global.getOpenDb());
			q.filter(new Condition("entry.id", mEntryId));
			q.select("*");
			q.setLimit(1);
			return q;
		}
		
	}
}
