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

import android.database.sqlite.SQLiteDatabase;

public class Table {

	public static final int INTEGER = 1;
	public static final int TEXT = 2;
	public static final int DATETIME = 4;
	public static final int BOOLEAN = 8;
	public static final int REAL = 16;
	
	public static final int TYPE_MASK = 255;
	
	public static final int PRIMARY = 256;
	public static final int NULL = 512;
	public static final int INDEX = 1024;
	public static final int AUTOINCREMENT = 2048;
	public static final int UNIQUE = 4096;
	
	public static final int JOINED_TABLE = 65536;
	public static final int JOINED_ON_DELETE_TABLE = 131072;
	
	public static final int JOINED_MASK = JOINED_TABLE | JOINED_ON_DELETE_TABLE;
	
	public static final int UNICODE = 262144;
	public static final int NOCASE = 524288;
	
	public static final int COLLATE_MASK = UNICODE | NOCASE;
	
	private String mName;
	protected Column[] mColumns;
	
	private boolean mNotLocal;

	public Table(String name, Column... columns) {
		this(name, false, columns);
	}
	
	public Table(String name, boolean notLocal, Column... columns) {
		mName = name;
		mColumns = columns;
		mNotLocal = notLocal;
	}
	
	public boolean isLocal() {
		return !mNotLocal;
	}

	public void drop(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS '" + mName + "'");
	}
	
	public void create(SQLiteDatabase db) {
		String ret = "CREATE TABLE " + DbQuery.quoteName(mName) + " (";
		final ArrayList<String> indexes = new ArrayList<String>();
		String strColumns = "";
		
		boolean first = true;
		for(Column column : mColumns) {
			if ((column.type & TYPE_MASK) == 0) {
				continue;
			}
			
			if (first) {
				first = false;
			} else {
				strColumns += ", ";
			}
			strColumns += columnNameAndDef(column.name, column.type);
			
			if ((column.type & INDEX) == 0) {
				continue;
			}
			indexes.add("CREATE INDEX " + mName + column.name + "_index on " + DbQuery.quoteName(mName) + "(" + column.name + ");");
			
		}
		
		db.execSQL(ret + strColumns + ");");
		
		for(String strIndex : indexes) {
			db.execSQL(strIndex);
		}
	}
	
	/**
	 * 
	 * @param db
	 */
	public void clear(Db db) {
		db.truncate(DbQuery.quoteName(mName), false);
	}
	
	/**
	 * 
	 * @param db
	 */
	public void truncate(Db db) {
		db.truncate(DbQuery.quoteName(mName), true);
	}
	
	public static String columnNameAndDef(String name, int type) {
		return DbQuery.quoteName(name) + " " + columnDefString(type);
	}
	
	public static String columnDefString(int type) {
		String ret = "";
		
		switch(type & TYPE_MASK) {
			case INTEGER:
				ret = "INTEGER";
				break;
				
			case DATETIME:
				ret = "DATETIME";
				break;
				
			case BOOLEAN:
				ret = "BOOLEAN";
				break;
				
			case REAL:
				ret = "REAL";
				break;
				
			case TEXT:
			default:
				ret = "TEXT";
				break;
		}
		
		if ((type & COLLATE_MASK) > 0) {
			ret += " COLLATE";
		}
		if ((type & UNICODE) > 0) {
			ret += " UNICODE";
		}
		if ((type & NOCASE) > 0) {
			ret += " NOCASE";
		}
		
		if ((type & PRIMARY) > 0) {
			ret += " PRIMARY KEY";
		} else {
			if ((type & NULL) > 0) {
				ret += " NULL";
			} else {
				ret += " NOT NULL";
			}
		}
		if ((type & AUTOINCREMENT) > 0) {
			ret += " AUTOINCREMENT";
		}
		if ((type & UNIQUE) > 0) {
			ret += " UNIQUE";
		}

		return ret;
	}

	public static class Column {
		public String name;
		public int type;
		
		public Column(String name, int type) {
			this.name = name;
			this.type = type;
		}
	}

	public String getName() {
		return mName;
	}

	/**
	 * return column info by column's name
	 * 
	 * @param name
	 * @return
	 */
	public Column getColumnByName(String name) {
		for(Column column : mColumns) {
			if (column.name.equals(name)) {
				return column;
			}
		}
		return null;
	}

	/**
	 * return all columns' info
	 * 
	 * @return
	 */
	public Column[] getColumns() {
		return mColumns;
	}
}
