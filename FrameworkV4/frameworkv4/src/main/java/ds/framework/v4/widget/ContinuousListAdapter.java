/*
	Copyright 2011 Dániel Sólyom

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

package ds.framework.v4.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import ds.framework.v4.R;
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.common.AnimationStarter;

public abstract class ContinuousListAdapter<T> extends TemplateListAdapter<T> {

	private int mLoaderLayout;
	private Integer mLoaderAnimation;
	
	private int mListOffset;
	private boolean mHasMoreItems;
	private boolean mDownloadingBackward;
	private boolean mDownloadingForward;
	
	protected int mLoadThreshold = 0;
	protected int mLoadStep;
	protected int mStepLimit;
	
	public ContinuousListAdapter(ActivityInterface in, int rowResourceId, int loaderLayout) {
		this(in, rowResourceId, loaderLayout, null);
	}
	
	public ContinuousListAdapter(ActivityInterface in, int rowResourceId, int loaderLayout, Integer loaderImage) {
		super(in, rowResourceId);
		
		mLoaderLayout = loaderLayout;
		mLoaderAnimation = loaderImage;

		reset();
		
		mLoadStep = 20;
		mStepLimit = 0;
	}
	
	public void reset() {
		mListOffset = 0;
		mHasMoreItems = true;
		mDownloadingForward = false;
		mDownloadingBackward = false;

		stopLoading();
		
		super.reset();
	}
	
	public int getLoadStep() {
		return mLoadStep;
	}
	
	public void setLoadStep(int step) {
		mLoadStep = step;
	}
	
	public void setStepLimit(int limit) {
		assert(limit > 5 || limit == 0);
		mStepLimit = limit;
	}
	
	@Override
	public int getCount() {
		return super.getCount() + (mHasMoreItems ? 1 : 0) + (mListOffset > 0 ? 1 : 0);
	}

	@Override
	public boolean isEmpty() {
		return super.getCount() == 0;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRealCount() {
		return super.getCount();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getListOffset() {
		return mListOffset;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasTopLoadingRow() {
		return mListOffset > 0;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasBottomLoadingRow() {
		return mHasMoreItems;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int count = super.getCount();

		if (position + mLoadThreshold >= count && mHasMoreItems || position <= mLoadThreshold && mListOffset > 0) {
			final boolean forward = position > mLoadThreshold || mListOffset == 0;
			if (forward || !forward && mAdapterView.getFirstVisiblePosition() <= position + 1 + mLoadThreshold) {

				loadFrom(mListOffset + (forward ? count : 0), forward);
			}
		}

		if (position == count && mHasMoreItems) { 
			return getLoadingRow(parent, position, true);
		} else if (position == 0 && mListOffset > 0) {
			return getLoadingRow(parent, position, false);
		}

		if (convertView != null && convertView.getTag() == null) {
			convertView = null;
		}

		try {
			return super.getView(position - (hasTopLoadingRow() ? 1 : 0), convertView, parent);
		} catch(IndexOutOfBoundsException e) {	
			// be sure
			return new View(mIn.getContext());
		}
	}
	
	/**
	 * get loading row
	 * 
	 * @param parent
	 * @param position - includes loading rows ofc
	 * @param forward
	 * @return
	 */
	protected View getLoadingRow(ViewGroup parent, int position, boolean forward) {
		final View view = mIn.inflate(mLoaderLayout, parent, false);
		if (mLoaderAnimation != null) {
			final AnimationDrawable animation = (AnimationDrawable) 
					((ImageView) view.findViewById(mLoaderAnimation)).getDrawable();
			AnimationStarter.start(animation);
		}

		view.setTag(null);
		return view;
	}
	
	/**
	 * load items from offset - either forward or backward
	 * !note! offset is the first (well last) item not needed to load when loading backward
	 * 
	 * @param offset
	 * @param forward
	 */
	protected void loadFrom(int offset, boolean forward) {
		int start = offset;
		int count = mLoadStep;
		
		if (forward) {
			if (mDownloadingForward) {
				return;
			}
			mDownloadingForward = true;
		} else {
			if (mDownloadingBackward) {
				return;
			}
			mDownloadingBackward = true;
			
			start -= mLoadStep;
			
			if (start < 0) {
				count += start;
				start = 0;
			}
		}
		
		final int rstart = start;
		final int rcount = count;
		final boolean rforward = forward;

		mAdapterView.post(new Runnable() {
			
			@Override
			public void run() {
				load(rstart, rcount, rforward);
			}
		});
	}
	
	/**
	 * load count items from start
	 * 
	 * @param start
	 * @param count
	 * @param forward
	 */
	abstract protected void load(int start, int count, boolean forward);
	
	/**
	 * stop loading
	 */
	abstract protected void stopLoading();
	
	/**
	 * call after load is finished
	 * 
	 * @param items
	 * @param forward
	 */
	protected void showResults(T[] items, boolean forward) {
		showResults(Arrays.asList(items), forward);
	}
	
	/**
	 * 
	 * @param forward
	 */
	protected void onLoadFailure(boolean forward) {
		if (forward) {
			mDownloadingForward = false;
		} else {
			mDownloadingBackward = false;
		}
	}
	
	/**
	 * call after load is finished
	 * 
	 * @param items
	 * @param forward
	 */
	protected void showResults(List<T> items, boolean forward) {
		addItems(items, forward);

		if (forward) {
			mDownloadingForward = false;
			
			if (items.size() < mLoadStep) {
				mHasMoreItems = false;
			}
		} else {
			mDownloadingBackward = false;
		}
	}
	
	public boolean isValidPosition(int position) {
		return position >= (mListOffset > 0 ? 1 : 0) && position < super.getCount() + (mHasMoreItems ? 0 : 1);
	}
	
	synchronized public void addItems(T[] items, boolean toFront) {
		addItems(Arrays.asList(items), toFront);
	}
	
	synchronized public void addItems(List<T> items, boolean toFront) {
		final int addedSize = items.size();
		
		if (addedSize == 0) {
			notifyDataSetChanged();
			return;
		}
		
		assert(addedSize <= mLoadStep);
		
		int newPosition = -9999;
		int newOffset = 0;

		if (mStepLimit != 0 && addedSize + mItems.size() > mStepLimit * mLoadStep) {
			
			// overflow of items - remove 2 loading steps
			int removeCount = 2 * mLoadStep;
			final int start;
			
			if (!toFront) {
				start = mItems.size() - removeCount;
				mHasMoreItems = true;
			} else {
				start = 0;
				mListOffset += removeCount;
			}
			
			if (toFront) {
				newPosition = mAdapterView.getFirstVisiblePosition() - removeCount;
				newOffset = mAdapterView.getChildAt(0).getTop();
			}
			
			while(--removeCount >= 0) {
				mItems.remove(start);
			}
		}
		if (toFront) {
			mItems.addAll(items);
		} else {
			mListOffset -= addedSize;
			
			final ArrayList<T> save = new ArrayList<T>();
			save.addAll(mItems);
			mItems.clear();
			mItems.addAll(items);
			mItems.addAll(save);
			
			newPosition = mAdapterView.getFirstVisiblePosition() + addedSize + 1;
			newOffset = mAdapterView.getChildAt(1).getTop();
		}
		
		notifyDataSetChanged();
		
		if (newPosition != -9999 && mAdapterView instanceof ListView) {
			((ListView) mAdapterView).setSelectionFromTop(newPosition, newOffset);
		}

	}
}
