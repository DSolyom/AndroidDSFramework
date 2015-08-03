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
package ds.framework.v4.data;

import java.util.ArrayList;

import android.database.Cursor;
import ds.framework.v4.app.AbsDSListFragment.AbsListData;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.TableQuery;

public abstract class MultiCursorList extends AbsListData {
	
	protected ArrayList<Cursor> mCursors = new ArrayList<Cursor>();
	final private ArrayList<TableQuery> mLoaderQueries = new ArrayList<TableQuery>();
	private int mCursorCount;

	public MultiCursorList(int cursorCount) {
		mCursorCount = cursorCount;
	}
	
	public MultiCursorList(String loaderTag, int cursorCount) {
		super(loaderTag);
		mCursorCount = cursorCount;
	}
	
	public ArrayList<Cursor> getCursors() {
		return mCursors;
	}
	
	public void closeCursors() {
		for(Cursor c : mCursors) {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		mCursors.clear();
		
		// data is not valid anymore
		// did not called invalidate because we may not want loading to stop
		mValid = false;
	}
	
	/**
	 * 
	 */
	synchronized protected void load(final OnDataLoadListener listener, final int loadId) {
		synchronized(mLoaderQueries) {
			mLoaderQueries.clear();
			
			for(int i = 0; i < mCursorCount; ++i) {
				mLoaderQueries.add(getListLoaderQuery(i));
			}
		}
		
		super.load(listener, loadId);
	}
	
	/**
	 * 
	 * @param in
	 * @param results
	 */
	protected void loadDataInThread(Thread in, ArrayList<Cursor> results) {
		for(int i = 0; i < mCursorCount; ++i) {
			
			// load cursor data one bye one
			results.add(loadDataInThread(in, i));
		}
	}
	
	/**
	 * 
	 * @param in
	 * @param position
	 * @return
	 */
	protected Cursor loadDataInThread(Thread in, int position) {
		final TableQuery loaderQuery = mLoaderQueries.get(position);
		if (loaderQuery == null) {
			return null;
		}
		return loaderQuery.load();
	}
	
	@Override
	protected LoaderThread createLoader() {
		return new CursorListLoaderThread();
	}
	
	/**
	 * override this to return a preset list loader query for the given cursor position
	 * 
	 * @return
	 */
    abstract protected TableQuery getListLoaderQuery(int position);
    
    protected class CursorListLoaderThread extends LoaderThread {
		
		protected ArrayList<Cursor> mResults = new ArrayList<Cursor>();

		@Override
		protected boolean runCycle(Thread in) {
			try {
				synchronized(mLoaderQueries) {
					loadDataInThread(in, mResults);
				}
			} catch(Throwable e) {
				Debug.logException(e);
				return false;
			}
			return true;
		}
		
		@Override
		protected boolean onCycleFinished() {
			mContinuous = mResults.size() < mCursorCount;
			return true;
		}
		
		@Override
		protected boolean onCycleFailure() {
			mContinuous = false;
			return false;
		}
		
		@Override
		protected void onFinished() {
			
			// close old cursors
			for(Cursor c : mCursors) {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			
			// copy new ones (...)
			mCursors = mResults;

			mResults = new ArrayList<Cursor>();
			super.onFinished();
		}
		
		@Override
		protected void onFailure() {
			for(Cursor c : mResults) {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			mResults.clear();
			
			super.onFailure();
		}
		
		@Override
		protected void onInterrupt() {
			for(Cursor c : mResults) {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			mResults.clear();
			mContinuous = false;
			super.onInterrupt();
		}
	}
}
