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
import ds.framework.v4.data.CursorListEntry;

public abstract class CursorRecyclerViewAdapter extends MultiCursorRecyclerViewAdapter {

	public CursorRecyclerViewAdapter(ActivityInterface in, int rowLayoutId) {
		super(in, rowLayoutId);
	}
	
	/**
	 * 
	 * @param c
	 */
	public void setCursor(Cursor c) {
		putCursor(c, 0);
	}

	@Override
	public CursorListEntry getItem(int position) {
		return super.getItem(0, position);
	}
	

	@Override
	protected void fillRow(CursorListEntry data, int cursorPosition,
			int position) {
		fillRow(data, position);
	}
}
