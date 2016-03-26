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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import ds.framework.v4.common.Debug;

abstract public class Db {

	private static final String DATABASE_NAME = "ds_framework_";
	
	private static MYSQLiteOpenHelper sDbHelper;

	private static SQLiteDatabase sDb;
	private static int openCount;
	
	private static final HashMap<String, Table> sTables = new HashMap<String, Table>();

	/**
	 * call ensureDB(Context context, String dbName, int version, Table... tables) inside
	 */
	abstract public void ensureDB(Context context) throws SQLiteException;

	public void ensureDB(Context context, String dbName, int version, Table... tables) throws SQLiteException {
		ensureDB(context, dbName, version, Arrays.asList(tables));
	}

	synchronized public void ensureDB(Context context, String dbName, int version, List<Table> tables) throws SQLiteException {
		if (sTables.isEmpty()) {
			for(Table table : tables) {
				sTables.put(table.getName(), table);
			}
		}
		
		synchronized(Db.class) {

			if (sDb != null && sDb.isOpen()) {
				return;
			}
			
			// need a new db reference
			sDb = createDB(context, dbName, version);
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param dbName
	 * @param version
	 * @return
	 */
	protected SQLiteDatabase createDB(Context context, String dbName, int version) {
		sDbHelper = 
				new MYSQLiteOpenHelper(context.getApplicationContext(), DATABASE_NAME + dbName, null, version);

		openCount = 0; // not opened yet
		return sDbHelper.getWritableDatabase();
	}
	
	/**
	 * open database or increase open count if already opened<br/>
	 * it increases the count even if database could not be opened as it is expected to
	 * have close() called in this case too
	 * 
	 * @param context
	 */
	synchronized public void open(Context context) {
		try {
			ensureDB(context);
		} catch(SQLiteException e) {
			Debug.logException(e);
			
			// no return - it is expected to have a close() call event if open() failed
		}
		
		++openCount;
	}

	/**
	 *
	 */
	synchronized public boolean isClosed() {
		return openCount == 0;
	}
	
	/**
	 * decrease close count and close the database if it is reached zero
	 */
	synchronized public void close() {
		if (sDb == null) {
			openCount = 0;
			return;
		}

		if (sDb.inTransaction()) {
			endTransaction();
		}
		
		if (!sDb.isOpen()) {
			openCount = 0;
			sDb = null;
			return;
		}
		--openCount;
		if (openCount == 0) {
			sDbHelper.close();
			sDb = null;
		}
	}

	/**
	 * get tables
	 * 
	 * @return
	 */
	public HashMap<String, Table> getTables() {
		return sTables;
	}
	
	/**
	 * get table by name
	 * 
	 * @param tableName
	 * @return
	 */
	public Table getTableByName(String tableName) {
		return sTables.get(tableName);
	}

	/**
	 * execute query 
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public Cursor query(DbQuery query) throws SQLException {
		try {
			return sDb.query(query.isDistinct(), query.getTable(), query.getSelect(), 
					query.getWhere(), null, query.getGroupBy(), 
					query.getHaving(), query.getOrderBy(), query.getLimit());
		} catch (SQLException e) {
			Debug.logException(e);
			throw(e);
		} catch (Exception e) {
			Debug.logException(e);
			throw(new SQLException(e.getMessage()));
		}
	}
	
	/**
	 * count rows
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public int count(DbQuery query) throws SQLException {
		return count(query, false, false);
	}

    /**
     * count rows
     *
     * @param query
     * @param withoutLimit
     * @param withoutFilter
     * @return
     * @throws SQLException
     */
	public int count(DbQuery query, boolean withoutLimit, boolean withoutFilter) throws SQLException {
		try {
			String limit = null;
			String where = null;
			if (!withoutLimit) {
				limit = query.getLimit();
			}
			if (!withoutFilter) {
				where = query.getWhere();
			}
			
			Cursor result = sDb.query(
					query.getTable(), 
					new String[] { "COUNT(DISTINCT " + query.defaultOrderBy() + ")" }, 
					where, 
					null, 
					query.getGroupBy(),
					query.getHaving(),
					null,
					limit);
			result.moveToFirst();
			int count = result.getCount() == 0 ? 0 : result.getInt(0);
			result.close();
			return count;
		} catch (SQLException e) {
			Log.e("sql query", Log.getStackTraceString(e));
			throw(e);
		} catch (Exception e) {
			Log.e("sql query - uncaught", Log.getStackTraceString(e));
			throw(new SQLException(e.getMessage()));
		}
	}
	
	/**
	 * insert
	 * 
	 * @param table
	 * @param cv
	 */
	public void insert(String table, ContentValues cv) {
		try {
			sDb.insertOrThrow(table, null, cv);
		} catch (SQLException e) {
			Log.e("sql insert", Log.getStackTraceString(e));

			throw(e);
		} catch (Exception e) {
			Log.e("sql insert - uncaught", Log.getStackTraceString(e));
			throw(new SQLException(e.getMessage()));
		}
	}
	
	/**
	 * update
	 * 
	 * @param table
	 * @param cv
	 * @param where
	 */
	public void update(String table, ContentValues cv, String where) {
		try {
			sDb.update(table, cv, where, null);
		} catch (SQLException e) {
			Log.e("sql update", Log.getStackTraceString(e));
			throw(e);
		} catch (Exception e) {
			Log.e("sql update - uncaught", Log.getStackTraceString(e));
			throw(new SQLException(e.getMessage()));
		}
	}
	
	/**
	 * insert a row or replace on conflict<br/>
	 *
	 * @param table
	 * @param cv
	 * @return
	 */
	public long insertOrReplace(String table, ContentValues cv) {
		long id;
		try {
			id = sDb.insertWithOnConflict(table, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
		} catch (SQLException e) {
			Log.e("sql update", Log.getStackTraceString(e));
			throw(e);
		} catch (Exception e) {
			Log.e("sql update - uncaught", Log.getStackTraceString(e));
			throw(new SQLException(e.getMessage()));
		}
		if (id == -1) {
			throw(new SQLException("insert failed"));
		}
		return id;
	}
	
	/**
	 * delete
	 * 
	 * @param table
	 * @param where
	 */
	public int delete(String table, String where) {
		int deleted;
		try {
			deleted = sDb.delete(table, where, null);
			if (!sDb.inTransaction() && (where == null || Math.random() < 0.001 * deleted)) {
				sDb.execSQL("VACUUM;");
			}
		} catch (SQLException e) {
			Log.e("sql delete", Log.getStackTraceString(e));
			throw(e);
		} catch (Exception e) {
			Log.e("sql delete - uncaught", Log.getStackTraceString(e));
			throw(new SQLException(e.getMessage()));
		}
		return deleted;
	}

	/**
	 * truncate a table by dropping and recreating
	 * 
	 * @param table
	 * @param vacuum - need to execute vacuum after delete?
	 */
	public void truncate(String table, boolean vacuum) {
		truncate(sTables.get(table), vacuum);
	}
	
	/**
	 * truncate a table by dropping and recreating
	 * 
	 * @param table
	 * @param vacuum - need to execute vacuum after delete?
	 */
	public void truncate(Table table, boolean vacuum) {
		if (vacuum) {
			try {
				dropTable(table.getName(), false);
			} catch(Exception e) {
			
				// maybe the table is not exists
				Debug.logException(e);
			}
			table.create(sDb);

			sDb.execSQL("VACUUM;");
		} else {
			try {
				sDb.execSQL("DELETE FROM " + table.getName() + " WHERE 1");
			} catch(Exception e) {
				
				// maybe the table does not exists
				Debug.logException(e);
			}	
		}
	}
	
	/**
	 * remove table from database
	 * 
	 * @param table
	 * @param vacuum
	 */
	public void dropTable(String table, boolean vacuum) {
		try {
			sDb.execSQL("DROP TABLE " + Query.quoteName(table) + ";");
			if (vacuum) {
				vacuum();
			}
		} catch (SQLException e) {
			Log.e("sql drop", Log.getStackTraceString(e));
			throw(e);
		} catch (Exception e) {
			Log.e("sql drop - uncaught", Log.getStackTraceString(e));
			throw(new SQLException(e.getMessage()));
		}
	}
	
	/**
	 * drops all known tables from the database
	 * @throws Exception 
	 * 
	 */
	public void dropAll() throws Exception {
		beginTransaction();
		for(String name : sTables.keySet()) {
			try {
				dropTable(name, false);
			} catch(SQLException e) {
				endTransaction();
				throw(e);
			} catch(Exception e) {
				endTransaction();
				throw(e);
			}
		}
		setTransactionSuccessful();
		endTransaction();
		vacuum();
	}
	
	/**
	 * vacuum database
	 */
	public void vacuum() {
		sDb.execSQL("VACUUM;");
	}

	/**
	 * last inserted row id
	 * 
	 * @return
	 */
	public int lastId() {
		Cursor cur = sDb.rawQuery("select last_insert_rowid();", null);
		cur.moveToFirst();
		return cur.getInt(0);
	}
	
	public Cursor rawQuery(String query) {
		return sDb.rawQuery(query, null);
	}
	
	public void execSQL(String sql) {
		sDb.execSQL(sql);
	}
	
	synchronized public void beginTransaction() throws IllegalStateException {
		sDb.beginTransaction();
	}
	
	synchronized public void setTransactionSuccessful() {
		sDb.setTransactionSuccessful();
	}
	
	synchronized public void endTransaction() {
		try {
			sDb.endTransaction();
		} catch(Throwable e) {
			Debug.logException(e);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean inTransaction() {
		try {
			return sDb.inTransaction();
		} catch(Throwable e) {
			Debug.logD("Db", "sDb null");
			return false;
		}
	}
	
	public void truncateTables(boolean localToo) {
		beginTransaction();
		try {
			for(Table table : sTables.values()) {
				if (!localToo && table.isLocal()) {
					continue;
				}
				truncate(table, false);
			}
			setTransactionSuccessful();
		} catch(SQLException e) {
			throw(e);
		} finally {
			endTransaction();
		}

		try {
			vacuum();
		} catch(SQLException e) {
			throw(e);
		}
	}
	
	/**
	 * get an insert helper
	 * 
	 * @param tableName
	 * @return
	 */
	public InsertHelper getInsertHelper(String tableName) {
		return new InsertHelper(sDb, tableName);
	}

    /**
     *
     * @param sql
     * @return
     */
    public SQLiteStatement getSQLiteStatement(String sql) {
        return sDb.compileStatement(sql);
    }

    /**
     *
     * @return
     */
    public SQLiteDatabase getSQLiteDatabase() {
        return sDb;
    }
	
	public void onCreateDB(SQLiteDatabase db) {
		sDb = db;
		
		beginTransaction();
		try {
			for(Table table : sTables.values()) {
				table.create(sDb);
			}
			setTransactionSuccessful();
		} catch(SQLException e) {
			throw(e);
		} finally {
			endTransaction();
		}
	}

	public void onUpgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
		;
	}
	
	class MYSQLiteOpenHelper extends SQLiteOpenHelper {

		public MYSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateDB(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onUpgradeDB(db, oldVersion, newVersion);
		}
		
	}
}
