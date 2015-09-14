package ds.framework.v4.data;

import android.database.Cursor;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.TableQuery;

@SuppressWarnings("serial")
abstract public class AbsAsyncCursorEntry extends AbsAsyncData {

	protected CursorListEntry mEntry;
	private TableQuery mQuery;
	
	public AbsAsyncCursorEntry() {
		super();
		
		init();
	}
	
	public AbsAsyncCursorEntry(String loaderTag) {
		super(loaderTag);

		init();
	}
	
	/**
	 * 
	 */
	public void init() {
		mEntry = new CursorListEntry();
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
	
	@Override
	public boolean isValid() {
		return mEntry.isValid(); 
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return mEntry.isEmpty();
	}
	
	@Override
	public void invalidate(boolean forced) {
		super.invalidate(forced);
		
		mEntry.invalidate();
	}

	/**
	 * 
	 * @return
	 */
	public CursorListEntry getCursorEntry() {
		return mEntry;
	}
	
	@Override
	protected LoaderThread createLoader() {
		return new CursorEntryLoaderThread();
	}
	
	/**
	 * 
	 * @param position
	 */
	public void moveToPosition(int position) {
		mEntry.mCursor.moveToPosition(position);
	}
	
	/**
	 * alias for getListLoaderQuery()
	 * 
	 * @return
	 */
	abstract protected TableQuery getEntryLoaderQuery();
	
	@Override
	public void load(OnDataLoadListener listener, int loadId) {
		synchronized(mEntry) {
			mQuery = getEntryLoaderQuery();
		}
		
		super.load(listener, loadId);
	}
	
	/**
	 * @class CursorEntryLoaderThread
	 */
	protected class CursorEntryLoaderThread extends LoaderThread {

		protected Cursor mResult;
		
		@Override
		protected boolean runCycle(Thread in) {
			try {
				synchronized(mEntry) {
					mResult = mQuery.load();
				}
			} catch(Throwable e) {
				Debug.logException(e);
				return false;
			}
			return true;
		}
		
		@Override
		protected void onFinished() {
			mResult.moveToFirst();
			mEntry.setCursor(mResult);
			mResult = null;
			super.onFinished();
		}
	}
}
