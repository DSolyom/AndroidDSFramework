package ds.framework.v4.db;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;

import ds.framework.v4.common.Debug;
import ds.framework.v4.db.Table.Column;

public class JSONToDbHelper {

    public static final long ERROR = -2;
    public static final long NO_ID = -1;
    
    private Column[] mColumns;
    private int mColumnCount;
    private SQLiteStatement mInsertStatement;
    private SQLiteStatement mUpdateStatement;

    /**
     * currently only supporting insert or update
     * TODO: add type parameter to control if insert/update/replace
     *
     * @param table
     * @param row
     */
	public void createStatement(Table table, JSONObject row, Db db) {
Debug.logD("JSONToDbHelper", "createStatement for table: " + table);
		Column[] columns = table.getColumns();

		mColumnCount = 0;

        String columnNames = "";
        String insertUniqueNames = "";
        String insertQmarks = "";
        String updateWhere = "";
        String updateSet = "";
        boolean hasUnique = false;

		for(Column column : columns) {
            if ((column.type & Table.JOINED_TABLE) > 0 || !row.has(column.name)) {
                continue;
            }

            ++mColumnCount;
        }

        mColumns = new Column[mColumnCount];

        int next = 0;
        int last = mColumnCount;

        for(Column column : columns) {
            if ((column.type & Table.JOINED_TABLE) > 0 || !row.has(column.name)) {
                continue;
            }

            insertQmarks += ",?";

            if ((column.type & (Table.UNIQUE | Table.PRIMARY)) == 0) {
                columnNames += "," + column.name;
                updateSet += ", " + column.name + " = ? ";
                mColumns[next++] = column;
            } else {
                hasUnique = true;
                insertUniqueNames = ", " + column.name + insertUniqueNames;
                updateWhere = ", " + column.name + " = ? " + updateWhere;

                // update where columns go to the end for easier bind
                mColumns[--last] = column;
            }
		}

        if (columnNames.length() > 0) {
            columnNames += insertUniqueNames;
        } else {
            columnNames = insertUniqueNames;
        }
        
        mInsertStatement = db.getSQLiteStatement("INSERT OR ABORT INTO " + table.getName()
                + " (" + columnNames.substring(1) + ") VALUES (" + insertQmarks.substring(1) + ")");

        if (hasUnique) {
            mUpdateStatement = db.getSQLiteStatement("UPDATE " + table.getName() + " SET " +
                    updateSet.substring(1) + " WHERE " + updateWhere.substring(1));
        } else {
            mUpdateStatement = null;
        }
	}

    /**
     *
     * @param table
     * @param row
     * @param lJSONToDbValueInterface
     * @return
     */
	public long bind(Table table, JSONObject row,
			JSONToDbValueInterface lJSONToDbValueInterface) {

        return bind(table, row, lJSONToDbValueInterface, false);
    }

    /**
     *
     * @param table
     * @param row
     * @param lJSONToDbValueInterface
     * @param implicitOverrideNull
     * @return
     */
    public long bind(Table table, JSONObject row,
                    JSONToDbValueInterface lJSONToDbValueInterface, boolean implicitOverrideNull) {
        
        // in case previous one was simple insert
        if (mUpdateStatement != null) {
            mUpdateStatement.clearBindings();
        }

		long id = NO_ID;
		
		Object value = null;
		int type;
		
		// set values for columns
		for(int i = 0; i < mColumnCount; ++i) {
			try {
				if (mColumns[i] == null) {
					continue;
				}
				if (((!row.has(mColumns[i].name) && implicitOverrideNull) || row.isNull(mColumns[i].name)) && (mColumns[i].type & Table.NULL) > 0) {
					mInsertStatement.bindNull(i + 1);
                    if (mUpdateStatement != null) {
                        mUpdateStatement.bindNull(i + 1);
                    }
Debug.logD("JSONToDb", "binding null for " + mColumns[i].name);
					continue;
				}
                if (!row.has(mColumns[i].name)) {
                    continue;
                }

				type = mColumns[i].type & Table.TYPE_MASK;

                value = row.get(mColumns[i].name);

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
                        if (value instanceof String) {
                            value = Long.parseLong((String) value);
                        } else if (value instanceof Integer) {
                            value = new Long((Integer) value);
                        }
                        if (mColumns[i].name.equals("id")) {

                            // id for joined tables
                            id = (long) value;
                        }
                        mInsertStatement.bindLong(i + 1, (long) value);
                        if (mUpdateStatement != null) {
                            mUpdateStatement.bindLong(i + 1, (long) value);
                        }
                        break;

                    case Table.BOOLEAN:
                        value = (value instanceof Boolean) ? ((Boolean) value ? 1l : 0l) : (Long) value;
                        mInsertStatement.bindLong(i + 1, (long) value);
                        if (mUpdateStatement != null) {
                            mUpdateStatement.bindLong(i + 1, (long) value);
                        }
                        break;

                    case Table.REAL:
                        try {
                            mInsertStatement.bindDouble(i + 1, (double) value);
                            if (mUpdateStatement != null) {
                                mUpdateStatement.bindDouble(i + 1, (double) value);
                            }
                        } catch(ClassCastException e) {
                            if (value instanceof String && ((String) value).length() == 0) {
                                value = 0.0d;
                                mInsertStatement.bindDouble(i + 1, (double) value);
                                if (mUpdateStatement != null) {
                                    mUpdateStatement.bindDouble(i + 1, (double) value);
                                }
                            } else {
                                mInsertStatement.bindDouble(i + 1, Double.valueOf((String) value));
                                if (mUpdateStatement != null) {
                                    mUpdateStatement.bindDouble(i + 1, Double.valueOf((String) value));
                                }
                            }
                        }
                        break;

                    default:

                        // this way it can either be a string, a jsonobject, etc...
                        mInsertStatement.bindString(i + 1, value.toString());
                        if (mUpdateStatement != null) {
                            mUpdateStatement.bindString(i + 1, value.toString());
                        }
                        break;
				}

Debug.logD("JSONToDb", "binded " + value + " for " + mColumns[i].name);

			} catch (Exception e) {
				if ((mColumns[i].type & Table.NULL) > 0) {
                    mInsertStatement.bindNull(i + 1);
                    if (mUpdateStatement != null) {
                        mUpdateStatement.bindNull(i + 1);
                    }
Debug.logD("JSONToDb", "binding null for " + mColumns[i].name);
					continue;
				}

                Debug.logException(e);
				
				String valStr;
				
				try {
					valStr = String.valueOf(value);
				} catch(Exception e2) {
					valStr = "unknown";
				}
				
				android.util.Log.e("Bad data format", "table: " + table.getName() + " / column: " + mColumns[i].name + " / value: " + valStr);
				android.util.Log.e("Bad data format (raw)", row.toString());
			}
		}
		
		return id;
	}

    /**
     * execute insert or update
     * TODO: add type parameter to control if insert/update/replace
     *
     * @return last inserted id for insert, 0 for update, ERROR (-1) for error
     */
    public long execute() throws SQLiteException {
        long ret = NO_ID;
        try {
            ret = mInsertStatement.executeInsert();
        } catch (SQLiteException e) {
            if (mUpdateStatement != null) {
                ret = mUpdateStatement.executeUpdateDelete() == 0 ? ERROR : 0;
            } else {
                throw e;
            }
        }
        return ret;
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
