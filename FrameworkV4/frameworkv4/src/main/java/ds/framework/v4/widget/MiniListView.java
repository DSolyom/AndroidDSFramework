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

import ds.framework.v4.common.Common;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MiniListView extends LinearLayout {

	private RecyclerViewHeaderedAdapter mAdapter;
	private MiniListDataSetObserver mDataSetObserver = new MiniListDataSetObserver();

	public MiniListView(Context context) {
		super(context);
		init();
	}
	
	public MiniListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	protected void init() {
	}

	public RecyclerViewHeaderedAdapter getAdapter() {
		return mAdapter;
	}
	
	public int getCount() {
		return mAdapter.getCount();
	}

	public void setAdapter(RecyclerViewHeaderedAdapter adapter) {
		if (adapter != null && adapter == mAdapter) {
			mAdapter.notifyDataSetChanged();
			return;
		}

		if (mAdapter != null) {
			mAdapter.unregisterAdapterDataObserver(mDataSetObserver);
			mAdapter = null;
		}

		if (adapter == null) {
			return;
		}
		
		mAdapter = adapter;

		mDataSetObserver = new MiniListDataSetObserver();
		mAdapter.registerAdapterDataObserver(mDataSetObserver);
		mAdapter.notifyDataSetChanged();
	}

    /**
     * @class MiniListDataSetObserver
     */
	class MiniListDataSetObserver extends RecyclerView.AdapterDataObserver {
		
		ViewListFiller vlf;
		
		@Override
		public void onChanged() {
			if (vlf != null) {
				vlf.invalidate();
				vlf = null;
			}
			final int oldCount = getChildCount();
			final int count = mAdapter.getCount();

			final View[] newViews = new View[count];
			
			for(int i = 0; i < count; ++i) {

				// get to reuse the view
				newViews[i] = getChildAt(i);
			}
			
			// remove rest of the old views from the list and every subview they have
			for(int i = count; i < oldCount; ++i) {
				final View child = getChildAt(count);
				if (child instanceof ViewGroup) {
					Common.removeAllViewsRec((ViewGroup) child);
				}
				removeView(child);
			}

			requestLayout();
			invalidate();
			
			// add the new views
			vlf = new ViewListFiller(newViews);
			vlf.run();
		}
		
		class ViewListFiller implements Runnable {
			
			private int count;
			private View[] views;
			
			public ViewListFiller(View[] views) {
				this.views = views;
				this.count = this.views.length;
			}
			
			synchronized public void invalidate() {
				count = 0;
				views = null;
			}
			
			@Override
			synchronized public void run() {
				if (count == 0) {
					return;
				}
				new ViewAdder(0).run();
			}
			
			class ViewAdder implements Runnable {

				private int at;
				
				public ViewAdder(int at) {
					this.at = at;
				}
				
				@Override
				public void run() {
					synchronized(ViewListFiller.this) {
						if (count == 0) {
							return;
						}
						try {
							for(int i = 0; i < 15 && at < count; ++i) {
								if (views[at] != null) {
									removeViewAt(at);
								}
								addView(getView(at, views[at]), at);
								++at;
							}
						} catch(IndexOutOfBoundsException e) {
							
							// just clearing this view while it's still working on showing
							invalidate();
						}
						if (at < count) {
							post(new ViewAdder(at));
						} else {
							invalidate();
						}
					}
				}		
			}
		}
	}

    /**
     *
     * @param position
     * @param convertView
     * @return
     */
	public View getView(int position, View convertView) {
		LayoutParams lp;
        if (convertView == null) {
            RecyclerView.ViewHolder vh = mAdapter.onCreateViewHolder(MiniListView.this, RecyclerViewHeaderedAdapter.VIEWTYPE_DEFAULT);
            convertView = vh.itemView;
            convertView.setTag(vh);
        }

		lp = new LayoutParams(convertView.getLayoutParams());
		convertView.setLayoutParams(lp);

        mAdapter.onBindViewHolder((RecyclerView.ViewHolder) convertView.getTag(), position);
		
		return convertView;
	}
}
