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

import ds.framework.v4.app.AbsDSAsyncDataFragment;
import ds.framework.v4.io.BackgroundThread;
import ds.framework.v4.io.BackgroundThread.Result;

abstract public class AbsAsyncData implements Serializable {

	private String mLoaderTag;
	
	/**
	 * data loaded and valid?
	 */
	protected boolean mValid = false;
	
	/**
	 * !note: invalidate has no effect on the data validness if it starts as valid<br/>
	 * you have to call invalidate(true) for that
	 */
	private boolean mStartsAsValid = false;
	
	/**
	 * 
	 */
	private int mLoadId;
	
	/**
	 * the loader thread - only used when no loaderTag added to retrieve it<br/>
	 * !note: as a side effect AbsAsyncData is only 'Serializable' if it has a loaderTag 
	 */
	private LoaderThread mLoader;
	
	/**
	 * use this constructor if you don't want the data and its loader to be persistent
	 * when the activity is recreated
	 */
	public AbsAsyncData() {
	}
	
	/**
	 * use this constructor if you want the data to be persistent when the activity is recreated
	 * 
	 * @param loaderTag
	 */
	public AbsAsyncData(String loaderTag) {
		mLoaderTag = loaderTag;
	}
	
	public String getLoaderTag() {
		return mLoaderTag;
	}
	
	public void setLoaderTag(String loaderTag) {
		stopLoading();
		mLoaderTag = loaderTag;
	}
	
	/**
	 * call this if the data should only load upon certain curcimstances you want to controll
	 */
	public void startValid() {
		mValid = true;
		mStartsAsValid = true;
	}
	
	/**
	 * 
	 * @param listener
	 */
	synchronized public void setOnDataLoadListener(OnDataLoadListener listener) {
		LoaderThread loader = recoverLoader();
		if (loader != null) {
			loader.mOwner = listener != null ? this : null;
			loader.mListener = listener;
		}
		if (mLoaderTag == null) {
			mLoader = loader;
		}
	}
	
	/**
	 * returns true if need loading<br/>
	 * !note: if you want to keep track of multiply data you need to be aware that this might call onDataLoaded/onDataLoadFailed
	 * 
	 * @param listener
	 * @param loadId
	 * @return
	 */
	public boolean loadIfNeeded(final OnDataLoadListener listener, final int loadId) {

		// make sure the listener gets renewed
		setOnDataLoadListener(listener);
		
		// retrieve LoaderThread if we had it previously
		LoaderThread loader = recoverLoader();
		boolean loaderRecovered = loader != null;
		
		// try to retrieve result already created
		final Result result = BackgroundThread.getResult(mLoaderTag, true);
		if (result != null) {
			mValid = false;
			
			if (loader != null) {
				
				// no need to do anything in the loader now as we have our result
				loader.mOwner = null;
				loader.requestInterrupt();
			}
			
			mLoadId = loadId;
			
			if (listener != null && loaderRecovered) {
				listener.onDataLoadStart(this, loadId);
			}
			
			if (result.success) {
				onDataLoaded(result.data, listener);
			} else {
				onDataLoadFailed(result.data, listener);
			}
			
			return false;
		}

		// do nothing if already loading
		// except for calling onDataLoadStart if we just recovered our loader
		if (isLoading() && loadId == mLoadId) {
			
			mValid = false;
			if (listener != null && loaderRecovered) {
				listener.onDataLoadStart(this, loadId);
			}
			return true;
		}
	
		if (isValid()) {
			return false;
		}
		
		if (loader != null && loader.getState() == BackgroundThread.FINISHED) {
		
			// we have loader and it's finished but we still got here?
			// this means the loader's onFinish could not do it's job but
			// we had no mLoaderTag to save the results with
			// need to reload
			invalidate();
		}
		
		if (listener != null) {
			listener.onDataLoadStart(this, loadId);
		}
		load(listener, loadId);
		return true;
	}
	
	/**
	 * start loading data
	 * 
	 * @param listener
	 */
	synchronized protected void load(final OnDataLoadListener listener, final int loadId) {
		mLoadId = loadId;
		
		// interrupt if already loading but the load id has changed
		LoaderThread loader = recoverLoader();
		if (loader != null) {
			loader.requestInterrupt();
		}
		
		// get loader and start loading
		loader = createLoader();
		if (mLoaderTag == null) {
			mLoader = loader;
		}
		if (loader != null) {
            loader.mOwner = this;
            loader.mListener = listener;
            loader.start(mLoaderTag);
		} else {

            // no loader - just pretend we have finished loading
            listener.onDataLoaded(this, loadId);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	synchronized public boolean isLoading() {
		LoaderThread loader = recoverLoader();

		// has loader and it's loading
		return loader != null && (loader.getState() == BackgroundThread.RUNNING || loader.getState() == BackgroundThread.FINISHING);
	}
	
	/**
	 * get the id identifying this data object
	 * 
	 * @see AbsDSAsyncDataFragment
	 * 
	 * @return
	 */
	public int getLoadId() {
		return mLoadId;
	}
	
	/**
	 * data loaded and valid? 
	 * 
	 * @return
	 */
	synchronized public boolean isValid() {
		return mValid;
	}
	
	/**
	 * invalidate data
	 */
	public void invalidate() {
		invalidate(false);
	}
	
	/**
	 * invalidate data
	 * 
	 * @param forced
	 */
	public void invalidate(boolean forced) {
		if (forced || !mStartsAsValid) {
			invalidateInner();
		}
	}
	
	/**
	 * 
	 */
	synchronized protected void invalidateInner() {
		stopLoading();
		mValid = false;
	}
	
	/**
	 * stop loading
	 */
	synchronized public void stopLoading() {
		LoaderThread loader = recoverLoader();
		
		if (loader != null) {
			loader.requestInterrupt();
			mLoader = null;
		}
	}
	
	/**
	 * called when the data is loaded and it needs to be 'saved'
	 * 
	 * @param result
	 */
	protected void onDataLoaded(Object result, OnDataLoadListener listener) {
		mValid = true;
		listener.onDataLoaded(this, mLoadId);
		mLoader = null;
	}
	
	/**
	 * called when the data load is failed
	 * 
	 * @param result
	 */
	protected void onDataLoadFailed(Object result, OnDataLoadListener listener) {
		mValid = mStartsAsValid;
		listener.onDataLoadFailed(this, mLoadId);
        mLoader = null;
	}
	
	protected void onInterrupt(OnDataLoadListener listener) {
		mValid = mStartsAsValid;
		listener.onDataLoadInterrupted(this, mLoadId);
        mLoader = null;
	}
	
	/**
	 * set loader to null - always call recoverLoader soon after 
	 */
	public void nullLoader() {
		mLoader = null;
	}
	
	/**
	 * 
	 */
	public LoaderThread recoverLoader() {
		if (mLoader != null) {
			return mLoader;
		}
		LoaderThread loader = (LoaderThread) BackgroundThread.getThreadByTag(mLoaderTag);
		if (mLoaderTag == null) {
			mLoader = loader;
		}
		return loader;
	}
	
	protected abstract LoaderThread createLoader();
	
	static public abstract class LoaderThread extends BackgroundThread {
		
		/**
		 * owner data object
		 */
		AbsAsyncData mOwner;
		
		/**
		 * listener for data loading events
		 */
		protected OnDataLoadListener mListener;
		
		private int mLoadId;
		
		public LoaderThread() {
			;
		}
		
		public LoaderThread(boolean continuous) {
			super(continuous);
		}

		@Override
		protected void onFinished() {
			super.onFinished();
			
			if (mOwner == null || mListener == null) {
				return;
			}
			
			final Object data;
			Result result = null;
			if (mOwner.mLoaderTag == null) {
				result = getResult();
			} else {
				result = getResult(mOwner.mLoaderTag, true);				
			}

			if (result != null) {
				data = result.data;
			} else {
				data = null;
			}
			mOwner.onDataLoaded(data, mListener);
			mListener = null;
		}
		
		@Override
		protected void onFailure() {
			super.onFailure();
			
			if (mOwner == null || mListener == null) {
				return;
			}
			
			final Object data;
			Result result = getResult(mOwner.mLoaderTag, true);
			if (result != null) {
				data = result.data;
			} else {
				data = null;
			}
			mOwner.onDataLoadFailed(data, mListener);
			mListener = null;
		}
		
		@Override
		protected void onInterrupt() {
			super.onInterrupt();
			
			if (mOwner == null || mListener == null) {
				return;
			}
			mOwner.onInterrupt(mListener);			
			mListener = null;
		}
	}
	
	public interface OnDataLoadListener {
		public void onDataLoadStart(AbsAsyncData data, int loadId);
		public void onDataLoaded(AbsAsyncData data, int loadId);
		public void onDataLoadFailed(AbsAsyncData data, int loadId);
		public void onDataLoadInterrupted(AbsAsyncData data, int loadId);
	}
}
