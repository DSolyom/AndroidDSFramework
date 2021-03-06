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

import android.database.DatabaseUtils;
import ds.framework.v4.datatypes.Interval;

public class Query {	
	protected String mTable;

	protected String[] mSelect;
	protected ConditionTree mWhere;
	protected ConditionTree mHaving;
	protected String mGroupBy;
	protected String mOrderBy;
	protected Interval mLimit;
	protected boolean mDistinct;
	
	public void reset() {
		mWhere = null;
		mHaving = null;
		mGroupBy = null;
		mOrderBy = null;
		mLimit = null;
		mDistinct = false;
	}

    /**
     *
     * @param table
     * @return
     */
	public Query table(String table) {
		mTable = table;
        return this;
	}
	
	/**
	 * set columns to select
	 * 
	 * @param columns
     * @return
	 */
	public Query select(String... columns) {
		mSelect = columns;
		return this;
	}
	
	/**
	 * add columns to select to existing ones
	 * 
	 * @param columns
     * @return
	 */
	public Query addSelect(String... columns) {
		if (mSelect == null) {
			select(columns);
			return this;
		}
		final String[] oldSelect = mSelect;
		mSelect = new String[oldSelect.length + columns.length];
		System.arraycopy(oldSelect, 0, mSelect, 0, oldSelect.length);
		System.arraycopy(columns, 0, mSelect, oldSelect.length, columns.length);
        
        return this;
	}
	
	/**
	 * filter
	 * 
	 * @param condition
     * @return
    */
	public Query filter(Condition condition) {
		filter(new ConditionTree(ConditionTree.AND, condition));
        return this;
	}
	
	/**
	 * filter
	 * 
	 * @param relation
	 * @param condition
     * @return
	 */
	public Query filter(int relation, Condition condition) {
		filter(new ConditionTree(relation, condition));
        return this;
	}
	
	/**
	 * filter
	 * 
	 * @param filter
     * @return
	 */
	public Query filter(ConditionTree filter) {
		if (mWhere == null) {
			mWhere = filter;
		} else {
			mWhere = mWhere.merge(filter);
		}
        return this;
	}
	
	/**
	 * having
	 * 
	 * @param having
     * @return
	 */
	public Query having(ConditionTree having) {
		if (mHaving == null) {
			mHaving = having;
		} else {
			mHaving = mHaving.merge(having);
		}
        return this;
	}
	
	/**
	 * groupBy
	 * 
	 * @param by
     * @return
	 */
	public Query groupBy(String by) {
		if (mGroupBy == null) {
			mGroupBy = by;
			return this;
		}
		mGroupBy += ", " + by;
        return this;
	}
	
	/**
	 * group by
	 * 
	 * @param by
	 * @param add - false => reset
	 */
	public Query groupBy(String by, boolean add) {
		if (!add) {
			mGroupBy = null;
		}
		groupBy(by);
        return this;
	}
	
	/**
	 * order by 'by' in ascending order
	 * 
	 * @param by
     * @return
	 */
	public Query orderBy(String by) {
		orderBy(by, true);
        return this;
	}
	
	/**
	 * order by
	 * 
	 * @param by
	 * @param asc - is ascending
     * @return
	 */
	public Query orderBy(String by, boolean asc) {
		if (mOrderBy == null) {
			mOrderBy = by + " COLLATE UNICODE " + (asc ? "ASC" : "DESC");
			return this;
		}
		mOrderBy += ", " + by + " COLLATE UNICODE " + (asc ? "ASC" : "DESC");
        return this;
	}
	
	public Query orderBy(String by, boolean asc, String collate) {
		if (mOrderBy == null) {
			mOrderBy = by + " COLLATE " + collate + " " + (asc ? "ASC" : "DESC");
			return this;
		}
		mOrderBy += ", " + by + " COLLATE " + collate + " " + (asc ? "ASC" : "DESC");
        return this;
	}
	
	/**
	 * order by 
	 * 
	 * @param by
	 * @param asc - is ascending
	 * @param add - false => reset
     * @return
	 */
	public Query orderBy(String by, boolean asc, boolean add) {
		if (!add) {
			mOrderBy = null;
		}
		orderBy(by, asc);
        return this;
	}
	
	/**
	 * set limit
	 * 
	 * @param limit
     * @return
	 */
	public Query setLimit(int limit) {
		mLimit = new Interval(0, limit);
        return this;
	}

	/**
	 * set limit 
	 * 
	 * @param offset
	 * @param limit
     * @return
	 */
	public Query setLimit(int offset, int limit) {
		mLimit = new Interval(offset, limit + offset);
        return this;
	}
	
	/**
	 * distinct select?
	 * 
	 * @param distinct
     * @return
	 */
	public Query distinct(boolean distinct) {
		mDistinct = distinct;
        return this;
	}

	/**
	 * quote column or table name
	 * 
	 * @param name
	 * @return
	 */
	public static String quoteName(String name) {
		String[] parts = name.split(" AS ");
		parts[0] = parts[0].trim();

		if (parts[0].charAt(0) != '`') {
			String[] subParts = parts[0].split("\\.");
			parts[0] = "`" + subParts[0] + "`";
			if (subParts.length == 2) {
				parts[0] += ".`" + subParts[1] + "`";
			}
		}
		if (parts.length == 2) {
			parts[1] = "`" + parts[1].trim() + "`";
			return parts[0] + " AS " + parts[1];
		}
		return parts[0];
	}
	
	/**
	 * quote integer value
	 * 
	 * @param value
	 * @return
	 */
	public static String quoteValue(int value) {
		return String.valueOf(value);
	}
	
	/**
	 * quote value
	 * 
	 * @param value
	 * @return
	 */
	public static String quoteValue(String value) {
		value = value.trim();
		return DatabaseUtils.sqlEscapeString(value);
	}

	/**
	 * quote value
	 * 
	 * @param values
	 * @return
	 */
	public static String quoteValue(int[] values) {
		String ret = "";
		
		for(int value : values) {
			if (ret.length() != 0) {
				ret += ", ";
			}
			ret += value;
		}
		return "(" + ret + ")";
	}
	
	/**
	 * quote value
	 * 
	 * @param values
	 * @return
	 */
	public static String quoteValue(Object[] values) {
		String ret = "";
		
		for(Object value : values) {
			if (ret.length() != 0) {
				ret += ", ";
			}
			ret += quoteValue(value.toString());
		}
		return "(" + ret + ")";
	}
	
	/**
	 * decide if given string is name or value and quote accordingly
	 * 
	 * @param value
	 * @return
	 */
	public static String quoteNameOrValue(String value) {
		value = value.trim();
		if (value.charAt(0) == '\'') {
			
			if (value.length() == 2) {
				return value;
			}
			// value
			return quoteValue(value.substring(1, value.length() - 1));
		}
		
		// name
		return quoteName(value);
	}
	
	/**
	 * column expression with table alias 
	 * 
	 * @param column
	 * @param tableAlias
	 * @return
	 */
	public static String columnExp(String column, String tableAlias) {
		if (column.charAt(0) == '\'') {
			return column;
		}
		int iC = column.indexOf('(');
		if (iC == -1) {
			return tableAlias + "." + column;
		}
		int length = column.length();
		if (length - 2 == iC) {
			return column;
		}
		String[] columns = column.substring(iC + 1, length - 1).split(",");
		column = column.substring(0, iC + 1);
		
		boolean first = true;
		for(String col : columns) {
			if (first) {
				first = false;
			} else {
				column += ", ";
			}
			column += tableAlias + "." + col;
		}
		
		return column + ")";
	}
	
	public static void conditionExp(ConditionTree tree, String tableAlias) {
		if (tree.isEmpty()) {
			return;
		}
		if (tree.mCondition != null) {
			tree.mCondition.mField = tableAlias + "." + tree.mCondition.mField;
			if (tree.mCondition.mStrValue != null) {
				tree.mCondition.mStrValue = columnExp(tree.mCondition.mStrValue, tableAlias);
			}
		} else {
			for(ConditionTree child : tree.mChildren) {
				conditionExp(child, tableAlias);
			}
		}
	}
}
