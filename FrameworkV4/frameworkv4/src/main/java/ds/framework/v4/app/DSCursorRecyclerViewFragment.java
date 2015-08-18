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

import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.CursorList;
import ds.framework.v4.widget.CursorListAdapter;
import ds.framework.v4.widget.RecyclerViewHeaderedAdapter;

/**
 * a list fragment which gets it's data from a cursor<br/>
 * this needs a database set in the container activity to be able to work
 */
abstract public class DSCursorRecyclerViewFragment extends AbsDSRecyclerViewFragment {

	public DSCursorRecyclerViewFragment() {
		super();
	}

	public DSCursorRecyclerViewFragment(boolean isDialog) {
		super(isDialog);
	}
	
	@Override
	protected void setAdapterData(RecyclerViewHeaderedAdapter adapter, AbsRecyclerViewData data, int loadId) {
		((CursorListAdapter) adapter).setData((CursorList) data, loadId);
	}
	
	@Override
	protected void invalidateAdapter(RecyclerViewHeaderedAdapter adapter) {
		super.invalidateAdapter(adapter);

		// maybe we are just loaded new data and the adapter haven't got the chance to get it
		if (mData != null)
        for(AbsAsyncData data : mData) {
            if (data instanceof CursorList) {
                ((CursorList) data).closeCursor();
            }
        }
	}
    
}
