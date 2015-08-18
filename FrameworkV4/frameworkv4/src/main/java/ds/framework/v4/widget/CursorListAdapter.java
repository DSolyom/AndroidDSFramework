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

import android.database.Cursor;

import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.data.CursorList;
import ds.framework.v4.data.CursorListEntry;

public abstract class CursorListAdapter extends AbsTemplateViewHolderAdapter<CursorListEntry> {

    protected CursorList mData;
    private CursorListEntry mCLE;

	public CursorListAdapter(ActivityInterface in, int rowLayoutId) {
		super(in, rowLayoutId);
	}

    /**
     *
     * @param data
     * @param loadId
     */
    public void setData(CursorList data, int loadId) {
        setCursor(data.getCursor());
        mData = data;
    }

	/**
	 * 
	 * @param c
	 */
	public void setCursor(Cursor c) {
        if (mCLE == null) {
            mCLE = new CursorListEntry(c);
            return;
        } else if (c == mCLE.getCursor()) {
            return;
        }
        mCLE.setCursor(c);

        mData = null;
	}

	@Override
	public CursorListEntry getItem(int position) {
        mCLE.getCursor().moveToPosition(position);
		return mCLE;
	}

	/**
	 *
	 * @return
	 */
	public int getCount() {
		return mCLE == null ? 0 : mCLE.getCount();
	}
}
