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

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.common.Common;
import ds.framework.v4.template.Template;

abstract public class AbsTemplateAdapter<T> extends BaseAdapter implements DSAdapterInterface {

	protected ActivityInterface mIn;
	private Integer mRowLayoutRes;
	protected Template mTemplate;
	
	public AbsTemplateAdapter(ActivityInterface in, int rowLayoutId) {
		mIn = in;
		mRowLayoutRes = rowLayoutId;
		mTemplate = new Template(mIn, null);
	}
	
	public ActivityInterface getIn() {
		return mIn;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void reset() {
		notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup viewParent) {
		convertView = ensureConvertView(position, convertView, viewParent);
		
		mTemplate.setRoot(convertView);
		
		fillRow((T) getItem(position), position);

		return convertView;
	}
	
	/**
	 * 
	 * @param convertView
	 * @return
	 */
	protected View ensureConvertView(int position, View convertView, ViewGroup viewParent) {
		if (convertView != null) {
			if (!mRowLayoutRes.equals(convertView.getTag())) {
				if (convertView instanceof ViewGroup) {
					Common.removeAllViewsRec((ViewGroup) convertView);
				}
				convertView = null;
			}
		}
		if (convertView == null) {
			convertView = inflateConvertView(mRowLayoutRes, viewParent);
		}
		
		return convertView;
	}
	
	/**
	 * override to do something with the just inflated view
	 * 
	 * @return
	 */
	protected View inflateConvertView(int rowRes, ViewGroup viewParent) {
		final View convertView = mIn.inflate(rowRes, viewParent, false);
		convertView.setTag(rowRes);
		
		return convertView;
	}
	
	/**
	 * @param row - holding "row" data
	 */
	abstract protected void fillRow(T data, int position);
}
