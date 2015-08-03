/*
	Copyright 2012 Dániel Sólyom

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

import java.util.ArrayList;

import android.os.Bundle;
import ds.framework.v4.app.DSActivity.OnConnectionChangeListener;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.AbsAsyncData.OnDataLoadListener;
import ds.framework.v4.template.Template;

abstract public class AbsDSAsyncDataFragment extends DSFormFragment 
		implements OnDataLoadListener, OnConnectionChangeListener {

	protected AbsAsyncData[] mData;
	protected ArrayList<AbsAsyncData> mDynamicData = new ArrayList<AbsAsyncData>();
	private int mAsyncDataLoadState;
	private boolean mInLoadData = false;
	
	private int mLoadingViewID;
	
	public AbsDSAsyncDataFragment() {
		super();
	}
	
	public AbsDSAsyncDataFragment(boolean isDialog) {
		super(isDialog);
	}

	@Override
	public void onResume() {
		
		// async data may need to recover loaders
		boolean needToLoadAsync = mDataState == DATA_LOADING;
		
		super.onResume();
		
		if (mData != null) {
			for(AbsAsyncData data : mData) {
				data.setOnDataLoadListener(this);
			}
		}

		for(AbsAsyncData data : mDynamicData) {
			data.setOnDataLoadListener(this);
		}
		
		if (needToLoadAsync) {
			loadAsyncData();
		}
	}
	
	@Override
	public void onPause() {
		if (mData != null) {
			for(AbsAsyncData data : mData) {
				data.setOnDataLoadListener(null);
			}
		}

		for(AbsAsyncData data : mDynamicData) {
			data.setOnDataLoadListener(null);
		}
		
		super.onPause();
	}
	
	@Override
	public void loadData() {
		loadAsyncData();
	}
	
	/**
	 * 
	 * @param data
	 */
	public void load(AbsAsyncData data) {
		if (!mDynamicData.contains(data)) {
			mDynamicData.add(data);
		}
		data.loadIfNeeded(this, -1);
	}
	
	/**
	 * 
	 */
	private void loadAsyncData() {
		if (mData == null) {
			mData = getAsyncDataObjects();
		}
		if (mData != null) {
			int at = 0;
			mInLoadData = true;
			for(AbsAsyncData data : mData) {
				if (data.isValid() || !data.loadIfNeeded(this, at)) {
					mAsyncDataLoadState &= ~(1 << at);
				}

				at++;
			}
			mInLoadData = false;
			
			if (mAsyncDataLoadState == 0) {
				onDataLoaded();
			}
		} else {
			onDataLoaded();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isLoading() {
		if (mData == null) {
			return false;
		}
		for(AbsAsyncData data : mData) {
			if (data.isLoading()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void invalidateData() {
		if (mData != null) {
			for(AbsAsyncData data : mData) {
				data.invalidate();
			}
		}
		mAsyncDataLoadState = 0;
		
		super.invalidateData();
	}
	
	/**
	 * invalidate only one data - still it sets mDataState to DATA_INVALID
	 * 
	 * @param which
	 */
	public void invalidateData(int which) {
		mData[which].invalidate();
		
		mAsyncDataLoadState &= ~(1 << mData[which].getLoadId());
		
		super.invalidateData();
	}
	
	@Override
	public void reset() {
		super.reset();
		invalidateData();
		mData = null;
		mAsyncDataLoadState = 0;
	}
	
	@Override
	public void display() {
		super.display();
		
		if (mLoadingViewID == 0) {
			mLoadingViewID = getLoadingViewID();
		}
		
		if (mLoadingViewID != 0) {
			setLoadingVisibility();
		}
	}
	
	/**
	 * set loading view visibility 
	 */
	protected void setLoadingVisibility() {
		mTemplate.fill(mLoadingViewID, shouldShowLoading(), Template.VISIBLE);
	}

// implementing OnDataLoadListener
	
	@Override
	public void onDataLoadStart(AbsAsyncData data, int loadId) {
		mAsyncDataLoadState |= (1 << loadId);
		mDataState = DATA_LOADING;	
	}
	
	@Override
	public void onDataLoaded(AbsAsyncData data, int loadId) {
		if (mDynamicData.contains(data)) {
			mDynamicData.remove(data);
			return;
		}
		
		mAsyncDataLoadState &= ~(1 << loadId);
		if (!mInLoadData && mAsyncDataLoadState == 0) {
			if (isActive() && mActionBarItemsCreated) {
				handleActionBarItems(true);
			}
			onDataLoaded();
		}
	}


	@Override
	public void onDataLoadFailed(AbsAsyncData data, int loadId) {
		mAsyncDataLoadState &= ~(1 << loadId);
		
		if (shouldShowLoadErrorFor(data, loadId)) {
			showLoadErrorFor(data, loadId);
		}
	}

	@Override
	public void onDataLoadInterrupted(AbsAsyncData data, int loadId) {
		mAsyncDataLoadState &= ~(1 << loadId);
	}
	
//
	
	/**
	 * 
	 */
	public void stopLoading() {
		if (mData != null) {
			for(AbsAsyncData data : mData) {
				data.setOnDataLoadListener(null);
				data.stopLoading();
			}
		}
		mAsyncDataLoadState = 0;
		mDataState = DATA_INVALID;
	}
	
	/**
	 * override this if you have a view shown only when loading the date
	 * 
	 * @return
	 */
	protected int getLoadingViewID() {
		return 0;
	}
	
	/**
	 * override if you only need to show loading for a specific data (or for something else)
	 * 
	 * @return
	 */
	protected boolean shouldShowLoading() {
		if (mData != null)
		for(AbsAsyncData data : mData) {
			if (shouldShowLoadErrorFor(data, data.getLoadId())) {
				return false;
			}
		}
		return isLoading();
	}
	
	/**
	 * override to define when to show loading error message
	 * 
	 * @param data
	 * @param loadId
	 * @return
	 */
	protected boolean shouldShowLoadErrorFor(AbsAsyncData data, int loadId) {
		return false;
	}
	
	/**
	 * 
	 * @param data
	 * @param loadId
	 */
	protected void showLoadErrorFor(AbsAsyncData data, int loadId) {
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		invalidateData();
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		mDynamicData = (ArrayList<AbsAsyncData>) savedInstanceState.getSerializable(mFragmentId + "other-data-TAG");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (mDynamicData != null) {
			for(AbsAsyncData data : mDynamicData) {
				if (data.getLoaderTag() == null) {
					data.stopLoading();
				} else {
					
					// need to set loader to null before serializing
					data.nullLoader();
				}
			}
			
			outState.putSerializable(mFragmentId + "other-data-TAG", mDynamicData);
			
			for(AbsAsyncData data : mDynamicData) {
				if (data.getLoaderTag() != null) {
					
					// recover loader
					data.recoverLoader();
				}
			}
		}
	}
	
// implement OnConnectionChangeListener

	@Override
	public void onNoConnection() {
		;
	}

	@Override
	public void onConnectionEstablished() {
		loadData();
	}
	
	abstract protected AbsAsyncData[] getAsyncDataObjects();
}
