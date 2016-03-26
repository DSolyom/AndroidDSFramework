/*
	Copyright 2016 Dániel Sólyom

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

/**
 * data object which gets it's values from a cursor
 */
public class BasicCursorEntry extends AbsAsyncData implements BasicEntryInterface {

	protected Cursor mCursor;
	
	
	public BasicCursorEntry() {
		super();
	}

    public BasicCursorEntry(String loaderTag) {
        super(loaderTag);
    }

	public BasicCursorEntry(Cursor c) {
        super();

		mCursor = c;

        // leave the cursor's position as it was as this is an entry
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return mCursor == null || mCursor.getCount() == 0;
	}

	/**
	 *
	 * @param field
	 * @return
	 */
	public Integer getInt(String field) {
		return getInt(mCursor, field);
	}

    /**
     *
     * @param c
     * @param field
     * @return
     */
	static public Integer getInt(Cursor c, String field) {
		try {
			return c.getInt(c.getColumnIndex(field));
		} catch(Throwable e) {
			return null;
		}
	}

    /**
     *
     * @param field
     * @return
     */
    public Long getLong(String field) {
        return getLong(mCursor, field);
    }

    /**
     *
     * @param c
     * @param field
     * @return
     */
    static public Long getLong(Cursor c, String field) {
        try {
            return c.getLong(c.getColumnIndex(field));
        } catch(Throwable e) {
            return null;
        }
    }

	/**
	 *
	 * @param field
	 * @return
	 */
	public String getString(String field) {
		return getString(mCursor, field);
	}

	/**
	 *
	 * @param c
	 * @param field
	 * @return
	 */
	static public String getString(Cursor c, String field) {
		try {
			final int index = c.getColumnIndex(field);
			if (index != -1 && !c.isNull(index)) {
				return c.getString(index);
			} else {
				return "";
			}
		} catch(Throwable e) {
			return "";
		}
	}

    /**
     *
     * @param field
     * @return
     */
	public boolean getBoolean(String field) {
		return getBoolean(mCursor, field);
	}

    /**
     *
     * @param c
     * @param field
     * @return
     */
	static public boolean getBoolean(Cursor c, String field) {
		try {
			return c.getInt(c.getColumnIndex(field)) > 0;
		} catch(Throwable e) {
			return false;
		}
	}
	
	public double getDouble(String field) {
		return getDouble(mCursor, field);
	}

    /**
     *
     * @param c
     * @param field
     * @return
     */
	static public double getDouble(Cursor c, String field) {
		try {
			return c.getDouble(c.getColumnIndex(field));
		} catch(Throwable e) {
			return 0.0d;
		}
	}
	
	/**
	 * set the cursor for this entry<br/>
	 * !you must handle closing the old cursor if there was any
	 * 
	 * @param c
	 */
	public void setCursor(Cursor c) {
		mCursor = c;
	}
	
	/**
	 * 
	 * @return
	 */
	public Cursor getCursor() {
		return mCursor;
	}

	@Override
	protected LoaderThread createLoader() {
		return null;
	}
}
