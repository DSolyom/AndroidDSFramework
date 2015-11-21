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

package ds.framework.v4.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import ds.framework.v4.datatypes.Interval;

public class DbQuery extends Query {

	private final Db mDb;
	
	protected String mDefaultOrderBy;

	public DbQuery(Db db) {
		mDb = db;
	}
	
	/**
	 * execute query as select
	 * 
	 * @return
	 */
	public Cursor load() throws NullPointerException, SQLException {
		return mDb.query(this);
	}

	/**
	 * execute a count query
	 * 
	 * @return - number of rows matching the query
	 */
	public int count() {
		return count(false);
	}

	/**
	 * execute a count query
	 * 
	 * @param withoutLimitAndFilter
	 * @return - number of rows matching the query
	 */
	public int count(boolean withoutLimitAndFilter) throws NullPointerException, SQLException {
		return this.count(withoutLimitAndFilter, withoutLimitAndFilter);
	}

	public int count(boolean withoutLimit, boolean withoutFilter) throws NullPointerException, SQLException {
		return mDb.count(this, withoutLimit, withoutFilter);
	}
	
	/**
	 * execute delete query
	 * 
	 * @return
	 */
	public int delete() throws NullPointerException, SQLException {
		return mDb.delete(getTable(), getWhere());
	}
	
	/**
	 * execute insert query
	 * 
	 * @param cv
	 */
	public void insert(ContentValues cv) throws NullPointerException, SQLException {
		mDb.insert(getTable(), cv);
	}
	
	/**
	 * execute insert or update query
	 * 
	 * @param
	 */
	public void insertOrUpdate(ContentValues cv) throws NullPointerException, SQLException {
		mDb.insertOrUpdate(getTable(), cv);
	}
	
	/**
	 * execute update query
	 * 
	 * @param cv
	 * @param where
	 */
	public void update(ContentValues cv, String where) throws NullPointerException, SQLException {
		mDb.update(getTable(), cv, where);
	}

	/**
	 * delete first count entry by column
	 * 
	 * @param column - values in this column must be of type int and best to be unique
	 * @param count - how many rows to delete
	 * @return
	 */
	public int deleteFirstBy(String column, int count) throws NullPointerException, SQLException {
		
		// first load the required column values (deleteBy)
		final String[] saveSelect = mSelect;
		final Interval saveLimit = mLimit;
		mSelect = new String[] { column };

		Cursor cursor = load();
		final ArrayList<Integer> deleteBy = new ArrayList<Integer>();
		if (cursor.moveToFirst()) {
			do {
				deleteBy.add(cursor.getInt(0));
			} while(cursor.moveToNext());
		}

		mLimit = saveLimit;
		mSelect = saveSelect;
		
		return mDb.delete(getTable(), new Condition(column, deleteBy).toString());
	}
	
	public String getTable() {
		return mTable;
	}
	
	public String[] getSelect() {
		return mSelect;
	}
	
	public String getWhere() {
		if (mWhere == null) {
			return null;
		}
		return mWhere.toString();
	}
	
	public String getGroupBy() {
		return mGroupBy;
	}
	
	public String getHaving() {
		if (mHaving == null) {
			return null;
		}
		return mHaving.toString();
	}
	
	public String getOrderBy() {
		return mOrderBy;
	}
	
	public String getLimit() {
		return mLimit == null ? null : mLimit.start + ", " + (mLimit.end - mLimit.start);
	}
	
	/**
	 * default order by column - needed if limit is set but order by is not<br/>
	 * also used for count
	 * 
	 * @return
	 */
	public String defaultOrderBy() {
		return mDefaultOrderBy;
	}
	
	/**
	 * default order by column - needed if limit is set but order by is not<br/>
	 * also used for count
	 * 
	 * @return
	 */
	public void setDefaultOrderBy(String by) {
		mDefaultOrderBy = by;
	}

	public boolean isDistinct() {
		return mDistinct;
	}
}
