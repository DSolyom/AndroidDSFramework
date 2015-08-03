package ds.framework.v4.db;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.DatabaseUtils.InsertHelper;
import ds.framework.v4.common.Debug;
import ds.framework.v4.db.Table.Column;

public class JSONToDbHelper {

	int[] mColumnsPosition;
	Column[] mColumns; 
	
	public void extractColumns(InsertHelper ih, Table table) {
		
		Column[] columns = table.getColumns();
		
		mColumns = new Column[columns.length];
		mColumnsPosition = new int[columns.length];
		
		int sC = 0;
		
		for(Column column : columns) {
			if ((column.type & Table.JOINED_TABLE) > 0) {
				continue;
			}

			try {
				mColumnsPosition[sC] = ih.getColumnIndex(column.name);
				mColumns[sC] = column;
				++sC;
			} catch(IllegalArgumentException e) {
				Debug.logD("JSONToDbHelper", "illegal column: " + column.name);
			}
		}
	}
	
	public int bind(InsertHelper ih, Table table, JSONObject row, 
			JSONToDbValueInterface lJSONToDbValueInterface) {
		
		final int cc = mColumns.length;
		
		int id = -1;
		
		Object value = null;
		int type;
		
		// set values for columns
		for(int i = 0; i < cc; ++i) {
			try {
				if (mColumns[i] == null) {
					continue;
				}
				if (!row.has(mColumns[i].name) && (mColumns[i].type & Table.NULL) > 0) {
					ih.bindNull(mColumnsPosition[i]);
//Debug.logD("JSONToDb", "binding null for " + mColumns[i].name);						
					continue;
				}
				if (row.isNull(mColumns[i].name)) {
					ih.bindNull(mColumnsPosition[i]);
//Debug.logD("JSONToDb", "binding null for " + mColumns[i].name);						
					continue;
				}
				
				if ((mColumns[i].type & Table.INTEGER) > 0) {
					int val;
					try {
						value = val = row.getInt(mColumns[i].name);
					} catch(JSONException e) {
						value = row.getString(mColumns[i].name);
						if (((String) value).length() == 0) {
							value = val = 0;
						} else {
							val = Integer.parseInt((String) value);
						}
					}
					if (mColumns[i].name.equals("id")) {
						
						// id for joined tables
						id = val;
					}
					type = Table.INTEGER;
				} else if ((mColumns[i].type & Table.BOOLEAN) > 0) {
					int val = 0;
					try {
						val = row.getBoolean(mColumns[i].name) ? 1 : 0;
					} catch(JSONException e) {
						val = row.getInt(mColumns[i].name);
					}
					value = val;
					
					type = Table.BOOLEAN;
				} else if ((mColumns[i].type & Table.REAL) > 0) {
					try {
						final double d = row.getDouble(mColumns[i].name);
						value = d;
						
						type = Table.REAL;
					} catch(JSONException e) {
						if (row.getString(mColumns[i].name).length() == 0) {
							value = 0.0;
							ih.bind(mColumnsPosition[i], 0);
//Debug.logD("JSONToDb", "binding 0 for " + mColumns[i].name);		
						} else {
							throw(e);
						}
						
						type = Table.REAL;
					}
				} else {
					final String val = row.getString(mColumns[i].name);
					value = val;
					
					type = Table.TEXT;
				}
				
				if (lJSONToDbValueInterface != null) {
					try {
						value = lJSONToDbValueInterface.beforeInsert(table.getName(), mColumns[i].name, value);
					} catch(Exception  e) {
						android.util.Log.e("OnValueInsertListener", "Bad value! (" + e.getMessage() + ")");
						if (type == Table.TEXT) {
							
							// minor ? problem
							value = "";
						} else {
							throw(e);
						}
					}
				}
				
				switch(type) {
					case Table.INTEGER:
						ih.bind(mColumnsPosition[i], (Integer) value);
//Debug.logD("JSONToDb", "binding " + value + " for " + mColumns[i].name);		
						break;
						
					case Table.BOOLEAN:
						ih.bind(mColumnsPosition[i], (Integer) value != 0);
//Debug.logD("JSONToDb", "binding " + ((Integer) value != 0) + " for " + mColumns[i].name);
						break;
						
					case Table.REAL:
						ih.bind(mColumnsPosition[i], (Double) value);
//Debug.logD("JSONToDb", "binding " + value + " for " + mColumns[i].name);
						break;
				
					case Table.TEXT:	// no break intended
					default:
						ih.bind(mColumnsPosition[i], (String) value);
//Debug.logD("JSONToDb", "binding " + value + " for " + mColumns[i].name);
						break;
				}
			} catch (Exception e) {
				if ((mColumns[i].type & Table.NULL) > 0) {
					ih.bindNull(mColumnsPosition[i]);
//Debug.logD("JSONToDb", "binding null for " + mColumns[i].name);
					continue;
				}
				
				String valStr;
				
				try {
					valStr = String.valueOf(value);
				} catch(Exception e2) {
					valStr = "unknown";
				}
				
				android.util.Log.e("Bad data format", "table: " + table.getName() + " / column: " + mColumns[i].name + " / value: " + valStr);
				android.util.Log.e("Bad data format - row verson", row.toString());
			}
		}
		
		return id;
	}
	
	/**
	 * 
	 * @param db
	 * @param tableName
	 * @param ids
	 * @param lJSONtoDbValueInterface
	 */
	public void deleteById(Db db, String tableName, int ids[], JSONToDbValueInterface lJSONtoDbValueInterface) {
		
		// delete from main table
		if (lJSONtoDbValueInterface != null) {
			ids = lJSONtoDbValueInterface.beforeDelete(tableName, ids);
		}
		db.delete(tableName, new Condition("id", ids).toString());
		
		// delete from joined tables
		for(Column column : db.getTableByName(tableName).getColumns()) {
			if ((column.type & Table.JOINED_TABLE) == 0) {
				continue;
			}
			db.delete(column.name, new Condition(tableName + "_id", ids).toString());
		}
	}
	
	public interface JSONToDbValueInterface {
		public Object beforeInsert(String tableName, String column, Object value);
		public int[] beforeDelete(String tableName, int[] ids);
	}
}
