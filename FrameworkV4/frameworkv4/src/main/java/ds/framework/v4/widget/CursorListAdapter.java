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

import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.CursorData;

public abstract class CursorListAdapter extends AbsTemplateViewHolderAdapter<CursorData> {

    public CursorListAdapter(ActivityInterface in, int rowLayoutId) {
        super(in, rowLayoutId);
    }

    @Override
    public CursorData getItem(int position) {
        if (mRecyclerViewData == null) {
            return null;
        }
        return ((CursorData) mRecyclerViewData).getItem(position);
    }

    @Override
    public void onDataLoaded(AbsAsyncData data, int loadId) {
        notifyDataSetChanged();
    }

    /**
     * @return
     */
    public int getCount() {
        return mRecyclerViewData == null ? 0 : ((CursorData) mRecyclerViewData).getCount();
    }
}
