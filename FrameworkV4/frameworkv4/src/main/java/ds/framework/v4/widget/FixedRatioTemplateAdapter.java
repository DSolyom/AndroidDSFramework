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
package ds.framework.v4.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import ds.framework.v4.app.DSActivity;

public abstract class FixedRatioTemplateAdapter<T> extends TemplateRecyclerViewAdapter<T> {
	
	private int mItemWidth;
	
	/**
	 * ratio of width / height
	 */
	private float sizeRatio = 1;
	
	public FixedRatioTemplateAdapter(DSActivity in, int itemWidth, int rowLayoutId) {
		super(in, rowLayoutId);

		mItemWidth = itemWidth;
	}
		
	@Override
	protected View inflateTemplateView(int rowRes, ViewGroup viewParent, int viewType) {
		final View view = super.inflateTemplateView(rowRes, viewParent, viewType);
		final LayoutParams lp = view.getLayoutParams();
		lp.width = mItemWidth;
		lp.height = (int) ((float) mItemWidth / sizeRatio);

		return view;
	}
}
