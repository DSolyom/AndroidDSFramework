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

import android.database.Cursor;

public class TableQuery extends DbQuery {
	
	String mAlias;
	String mJoin = "";
	
	public TableQuery(String table, Db db) {
		super(db);
		mTable = table;
		mAlias = null;
	}
	
	public TableQuery(String table, String alias, Db db) {
		super(db);
		mAlias = alias;
		if (mAlias != null) {
			mTable = table + " AS " + mAlias;
		} else {
			mTable = table;
		}
	}
	
	/**
	 * return first row's first column's value as integer
	 * 
	 * @return
	 */
	public Integer loadFirstInteger() {
		if (getLimit() == null) {
			setLimit(1);
		}
		Cursor c = null;
		try {
			c = load();
			if (c.moveToFirst()) {
				Integer ret = c.getInt(0);
				c.close();
				return ret;
			} else {
				c.close();
				return null;
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
	}
	
	/**
	 * return first row's first column's value as long
	 * 
	 * @return
	 */
	public Long loadFirstLong() {
		if (getLimit() == null) {
			setLimit(1);
		}
		Cursor c = null;
		try {
			c = load();
			if (c.moveToFirst()) {
				Long ret = c.getLong(0);
				c.close();
				return ret;
			} else {
				c.close();
				return null;
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
	}

	/**
	 * return first row's first column's value as string 
	 * 
	 * @return
	 */
	public String loadFirstString() {
		if (getLimit() == null) {
			setLimit(1);
		}
		Cursor c = null;
		try {
			c = load();
			if (c.moveToFirst()) {
				String ret = c.getString(0);
				c.close();
				return ret;
			} else {
				c.close();
				return null;
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
	}
	
	@Override
	public String[] getSelect() {
		if (mSelect.length == 0) {
			mSelect = new String[] { defaultOrderBy() };
		}
		return mSelect;
	}
	
	public void join(String tableName, String alias, String type, Condition condition) {
		join(tableName, alias, type, new ConditionTree(condition));
	}
	
	public void join(String tableName, String alias, String type, ConditionTree on) {
		if (alias != null) {
			tableName += " AS " + alias;
		}
		if (type != null && type.length() > 0) {
			type += " JOIN ";
		} else {
			type = "JOIN ";
		}
		if (on == null) {
			mJoin += " " + type + tableName;
		} else {
			mJoin += " " + type + tableName + " ON " + on.toString();
		}
	}
	
	@Override
	public String getTable() {
		return mTable + mJoin;
	}
	
	/**
	 */
	public String getMainTable() {
		return mAlias == null ? mTable : mAlias;
	}
	
	@Override
	public String defaultOrderBy() {
		final String dob = super.defaultOrderBy();
		
		if (dob == null) {
			if (mAlias != null) {
				return mAlias + ".id";
			}
			return mTable + ".id";
		} else {
			return dob;
		}
	}

	/**
	 * filter by 'id' = id
	 * 
	 * @param id
	 * @return
	 */
	public void filterById(int id) {
		reset();
		if (mAlias != null) {
			filter(new Condition(mAlias + ".id", id));
		} else {
			filter(new Condition("id", id));
		}
	}
	
	/**
	 * filter by 'id' in ids
	 * 
	 * @param ids
	 * @return
	 */
	public void filterByIds(int[] ids) {
		reset();
		if (mAlias != null) {
			filter(new Condition(mAlias + ".id", ids));
		} else {
			filter(new Condition("id", ids));
		}
	}
}
