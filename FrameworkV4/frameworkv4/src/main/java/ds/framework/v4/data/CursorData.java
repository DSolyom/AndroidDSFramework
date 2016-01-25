/*
	Copyright 2016 Dániel Sólyom

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

public abstract class CursorData extends BasicCursorEntry {

    private TableQuery mLoaderQuery;

    private Object mLoaderQueryLock = new Object();

    public CursorData() {
        super();
    }

    public CursorData(String loaderTag) {
        super(loaderTag);
    }

    /**
     *
     * @return
     */
    public Cursor getCursor() {
        return mCursor;
    }

	/**
	 * set a cursor - be sure it is active and pointing to the data of the entry
	 * 
	 * @param c
	 */
	public void setCursor(Cursor c) {
		if (mCursor == c) {
			return;
		}
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

    @Override
    synchronized protected void load(final OnDataLoadListener listener, final int loadId) {
        synchronized(mLoaderQueryLock) {
            mLoaderQuery = getLoaderQuery();
        }

        super.load(listener, loadId);
    }

    /**
     * call when 'manual' query refresh needed
     * !note: make sure you're doing the right thing
     */
    protected void refreshQuery() {
        synchronized(mLoaderQueryLock) {
            mLoaderQuery = getLoaderQuery();
        }
    }

	public void invalidateInner() {
        super.invalidateInner();

        setCursor(null);
	}

    /**
     *
     * @param in
     * @return
     */
    protected Cursor loadDataInThread(Thread in) {
        if (mLoaderQuery == null) {
            return null;
        }
        return mLoaderQuery.load();
    }

    /**
     *
     * @return
     */
    public int getCount() {
        if (mCursor == null || mCursor.isClosed()) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     *
     * @param position
     * @return
     */
    public CursorData getItem(int position) {
        if (mCursor == null || mCursor.isClosed()) {
            return null;
        }

        mCursor.moveToPosition(position);

        return this;
    }

    @Override
    protected LoaderThread createLoader() {
        return new CursorLoaderThread();
    }

    /**
     * override this to return the loader query
     *
     * @return
     */
    abstract protected TableQuery getLoaderQuery();

    /**
     * @Class CursorLoaderThread
     */
    protected class CursorLoaderThread extends LoaderThread {

        protected Cursor mResult;

        @Override
        protected boolean runCycle(Thread in) {
            try {
                synchronized(mLoaderQueryLock) {
                    mResult = loadDataInThread(in);
                    mResult.moveToFirst();
                }
            } catch(Throwable e) {
                Debug.logException(e);
                return false;
            }
            return true;
        }

        @Override
        protected void onFinished() {
            setCursor(mResult);

            super.onFinished();
        }

        @Override
        protected void onFailure() {
            if (mResult != null && !mResult.isClosed()) {
                mResult.close();
            }

            setCursor(null);

            super.onFailure();
        }

        @Override
        protected void onInterrupt() {
            if (mResult != null && !mResult.isClosed()) {
                mResult.close();
            }

            super.onInterrupt();
        }
    }

}
