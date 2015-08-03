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
package ds.framework.v4.app;

import ds.framework.v4.data.MultiCursorList;
import ds.framework.v4.widget.MultiCursorListAdapter;

/**
 * a list fragment which gets it's data from a cursor<br/>
 * this needs a database set in the container activity to be able to work
 */
abstract public class DSCursorListFragment extends AbsDSListFragment {
	
	public DSCursorListFragment() {
		super();
	}
	
	public DSCursorListFragment(boolean isDialog) {
		super(isDialog);
	}
	
	@Override
	protected void setAdapterData(AbsListData data, int loadId) {
		((MultiCursorListAdapter) mAdapter).setData((MultiCursorList) data, loadId);
	}
	
	@Override
	protected void invalidateAdapter() {
			
		// close cursors
		if (mAdapter != null && mAdapter instanceof MultiCursorListAdapter) {
			((MultiCursorListAdapter) mAdapter).setCursors(null);
		}
	
		// maybe we are just loaded new data and the adapter haven't got the chance to get it
		if (mData != null && mData.length > 0 && mData[0] instanceof MultiCursorList) {
			((MultiCursorList) mData[0]).closeCursors();
		}
	}
    
}
