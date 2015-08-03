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

import android.content.ContentValues;
import android.view.View;
import android.view.ViewGroup;
import ds.framework.v4.R;
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.common.Common;
import ds.framework.v4.template.Template;

/**
 * A TemplateListAdapter with an option to show a loading image (animation) 
 * when an item is either null or (visually) below count
 *
 * @param <T>
 */
public abstract class LoadingListAdapter extends TemplateListAdapter<ContentValues> {

	private int mLoadingRowResourceId;

	public LoadingListAdapter(ActivityInterface in, int rowResourceId, int loadingRowResourceId) {
		super(in, rowResourceId);
		
		mLoadingRowResourceId = loadingRowResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewParent) {
		if (position >= getCount() || getItem(position) == null) {
			
			if (convertView instanceof ViewGroup) {
				Common.removeAllViewsRec((ViewGroup) convertView);
				convertView.setTag(null);
			}
			
			// when loading
			final View view = mIn.inflate(mLoadingRowResourceId, viewParent);

			return view;
		}
		
		return super.getView(position, convertView, viewParent);
	}
}