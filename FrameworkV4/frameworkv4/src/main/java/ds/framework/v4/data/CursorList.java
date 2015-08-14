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
package ds.framework.v4.data;

import android.database.Cursor;
import ds.framework.v4.db.TableQuery;

public abstract class CursorList extends MultiCursorList {
	
	public CursorList() {
		super(1);
	}

	public CursorList(String loaderTag) {
		super(loaderTag, 1);
	}

	public Cursor getCursor() {
		return mCursors.get(0);
	}
	
	public void closeCursor() {
		closeCursors();
	}

	/**
	 * 
	 * @return
	 */
	protected Cursor loadDataInThread(Thread in) {
		
		// to be able to override single loading
		return super.loadDataInThread(in, 0);
	}
	
	@Override
	protected Cursor loadDataInThread(Thread in, int position) {

		// to be able to override single loading
		return loadDataInThread(in);
	}
	
	@Override
	protected TableQuery getListLoaderQuery(int position) {
		
		// to be able to override single loading
		return getListLoaderQuery();
	}
	
	/**
	 * override this to return a preset list loader query
	 * 
	 * @return
	 */
    protected TableQuery getListLoaderQuery() {
    	return null;
    }
    
}
