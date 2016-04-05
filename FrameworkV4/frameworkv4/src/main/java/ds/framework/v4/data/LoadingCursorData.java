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

import java.io.IOException;

import ds.framework.v4.common.Debug;
import ds.framework.v4.db.TableQuery;
import ds.framework.v4.widget.AbsLoadingRecyclerViewAdapter;

abstract public class LoadingCursorData extends CursorData implements AbsLoadingRecyclerViewAdapter.LoadingRecyclerViewAdapterData{

    private boolean mWayForward = true;

    private int mOffset = 0;
    private int mCount = -1;
    protected boolean mFirstLoad = true;
    protected int mInMemoryLimit = 150; // change only in constructor
    protected int mPageLimit = 25;      // change only in constructor

    private boolean mLoadingFinished = false;
    private int mLoadingRowCount = 1;

    // indicate if we are trying to load more data
    private boolean mLoadingSome = false;
    private int mCurrentPosition = 0;

    private boolean mReachedDBEnd;

    /**
     *
     * @return
     */
    public int getPageLimit() {
        return mPageLimit;
    }

    /**
     *
     * @return
     */
    public int getInMemoryLimit() {
        return mInMemoryLimit;
    }


    /**
     *
     */
    public void setToUnlimited() {
        mPageLimit = -1;
        mInMemoryLimit = 500;
    }

    @Override
    synchronized public int getAllCount() {
        return (mCount == -1 ? 0 : mCount) + mLoadingRowCount;
    }

    @Override
    public boolean loadIfNeeded(OnDataLoadListener listener, int loadId) {
        if (!(mLoadingSome || mPageLimit == -1 || mFirstLoad) || isLoading()) {

            // do not load if not loading for the first time, unlimited loads or loading some new items
            return false;
        }
        if (!mLoadingSome) {
            mCurrentPosition = 0;
        }

        return super.loadIfNeeded(listener, loadId);
    }

    @Override
    public void loadSome(boolean wayForward, int positionAskingForLoad, OnDataLoadListener onDataLoadListener) {
        if (isLoading() || (wayForward && mLoadingFinished && mReachedDBEnd)) {
            return;
        }

        mLoadingSome = true;

        mCurrentPosition = positionAskingForLoad;
        mWayForward = wayForward;

        synchronized (this) {
            invalidate(true);
        }
        loadIfNeeded(onDataLoadListener, 0);

        mLoadingSome = false;
    }

    @Override
    public int getDataOffset() {
        return mOffset;
    }

    private void modifyOffset(int by) {
        mOffset += by;
        refreshQuery();
    }

    @Override
    protected TableQuery getLoaderQuery() {
        return getLoaderQuery(mOffset, mInMemoryLimit);
    }

    /**
     * !note: always call notifyDataSetChanged in adapter after this
     */
    synchronized public void reset() {
        stopLoading();

        mOffset = 0;
        mCount = -1;
        mLoadingFinished = false;
        mReachedDBEnd = false;
    }

    @Override
    public Cursor loadDataInThread(Thread in) {
        int currentCount = 0;
        boolean needForeign = true;

        synchronized (this) {
            if (!isLoading()) {

                // interrupted
                return null;
            }

            if (!mWayForward) {

                // going backwards - decrease offset and load data from db
                if (mOffset > 0) {
                    modifyOffset(-mPageLimit);

                    // need to load again next time when moving forward
                    mReachedDBEnd = false;
                }

                needForeign = false;
            } else {

                final int dbCount = getLoaderQuery().count(true, false);

                currentCount = dbCount - mOffset;
                if (dbCount - mOffset >= mInMemoryLimit) {

                    // going forward and current count after offset >= mInMemoryLimit
                    modifyOffset(mPageLimit);
                }
                if (mPageLimit != -1 && currentCount >= mCurrentPosition + mPageLimit) {

                    // still have enough data in db
                    needForeign = false;
                } else if (mLoadingFinished) {

                    // no more on server

                    // also reached end of our data - no need to load again while continuing moving forward
                    mReachedDBEnd = true;
                    needForeign = false;
                }
            }
        }

        if (needForeign) {
            boolean loadingFinished = false;
            try {
                loadingFinished = loadFromForeignSource(currentCount, mOffset, mPageLimit);
            } catch(Throwable e) {
                Debug.logException(e);
                return null;
            }

            synchronized (this) {
                if (!isLoading()) {

                    // interrupted
                    return null;
                }

                mLoadingFinished = loadingFinished;
            }
        }

        mFirstLoad = false;

        return super.loadDataInThread(in);
    }

    @Override
    protected void onDataLoaded(Object result, OnDataLoadListener listener) {
        mCount = getLoaderQuery().count(true, false);
        mLoadingRowCount = mLoadingFinished ? 0 : 1;

        super.onDataLoaded(result, listener);
    }

    /**
     * load from foreign source (backend ... )
     * !note: handle errors inside, existing data will still load
     */
    abstract protected boolean loadFromForeignSource(int currentCount, int offset, int pageLimit) throws Throwable;

    /**
     *
     * @param offset
     * @param limit
     * @return
     */
    protected abstract TableQuery getLoaderQuery(int offset, int limit);
}
