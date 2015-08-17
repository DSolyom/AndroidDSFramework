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

import android.database.Cursor;

import java.util.ArrayList;

import ds.framework.v4.app.AbsDSRecyclerViewFragment;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.TableQuery;

public abstract class CursorList extends AbsDSRecyclerViewFragment.AbsRecyclerViewData {

    private Cursor mCursor;
    private TableQuery mLoaderQuery;

	public CursorList() {
		super();
	}

	public CursorList(String loaderTag) {
		super(loaderTag);
	}

	public Cursor getCursor() {
		return mCursor;
	}
	
	public void closeCursor() {
		if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

        // data is not valid anymore
        mValid = false;
	}

    @Override
    synchronized protected void load(final OnDataLoadListener listener, final int loadId) {
        synchronized(mLoaderQuery) {
            mLoaderQuery = getListLoaderQuery();
        }

        super.load(listener, loadId);

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
     * override this to return a preset list loader query
     *
     * @return
     */
    abstract protected TableQuery getListLoaderQuery();

    @Override
    protected LoaderThread createLoader() {
        return new CursorListLoaderThread();
    }

    /**
     * @Class CursorListLoaderThread
     */
    protected class CursorListLoaderThread extends LoaderThread {

        protected Cursor mResult;

        @Override
        protected boolean runCycle(Thread in) {
            try {
                synchronized(mLoaderQuery) {
                    mResult = loadDataInThread(in);
                }
            } catch(Throwable e) {
                Debug.logException(e);
                return false;
            }
            return true;
        }

        @Override
        protected void onFinished() {
            mCursor = mResult;

            super.onFinished();
        }

        @Override
        protected void onFailure() {
            if (mResult != null && !mResult.isClosed()) {
                mResult.close();
            }

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
