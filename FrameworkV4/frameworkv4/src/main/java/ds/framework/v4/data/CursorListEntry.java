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
package ds.framework.v4.data;

import android.database.Cursor;
import ds.framework.v4.db.TableQuery;

public class CursorListEntry extends CursorEntry {
	
	public CursorListEntry() {
		
	}
	
	public CursorListEntry(Cursor c) {
		mCursor = c;
	}

	@Override
	public TableQuery newEntryLoaderQuery() {
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getCount() {
		if (mCursor == null || mCursor.isClosed()) {
			return 0;
		}
		return mCursor.getCount();
	}
}
