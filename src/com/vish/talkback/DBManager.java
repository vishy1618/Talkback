package com.vish.talkback;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager {
	// the Activity or Application that is creating an object from this class.
	Context context;
 
	// a reference to the database used by this application/object
	private SQLiteDatabase db;
 
	// These constants are specific to the database.  They should be 
	// changed to suit your needs.
		
	private String DB_NAME;
	private final int DB_VERSION = 1;
 
	// These constants are specific to the database table.  They should be
	// changed to suit your needs.
	private String GTALK_TABLE_NAME="gtalk_cfg";
	private String FBOOK_TABLE_NAME="fbook_cfg";
	private String BLUET_TABLE_NAME="bluetooth_cfg";
	private String CHATH_TABLE_NAME="chat_history";
	
	private String COL_USERNAME="username";
	private String COL_PASSWORD="passwd";
	//Chat history columns
	private String COL_DATETIME="date_time";
	private String COL_CURR_USER="curr_user";
	private String COL_MESSAGE="message";
	
	
	public DBManager(Context context)
	{
		this.context = context;
 
		// create or open the database
		DB_NAME = (String) context.getResources().getText(R.string.db_name);
		CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	/**********************************************************************
	 * Close the connection to the database
	 */
	public void closeConnection() {
		try {
			db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("SQL Close Error:",e.toString());
			e.printStackTrace();
		}
	}
 
 
 
	/**********************************************************************
	 * ADDING A ROW TO THE DATABASE TABLE
	 * 
	 */
	public void addRow(String tableName, String[] columns)
	{
		// this is a key value pair holder used by android's SQLite functions
		ContentValues values = new ContentValues();
		
		if(tableName==GTALK_TABLE_NAME || tableName==FBOOK_TABLE_NAME) {
			values.put(COL_USERNAME, columns[0]);
			values.put(COL_PASSWORD, columns[1]);
		}
		if(tableName==BLUET_TABLE_NAME) {
			values.put(COL_USERNAME, columns[0]);
		}
		if(tableName==CHATH_TABLE_NAME) {
			values.put(COL_USERNAME, columns[0]);
			values.put(COL_DATETIME, columns[1]);
			values.put(COL_CURR_USER, columns[2]);
			values.put(COL_MESSAGE, columns[3]);
		}
		
		// ask the database object to insert the new data 
		try{db.insert(tableName, null, values);}
		catch(Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}
 
 
 
	/**********************************************************************
	 * DELETING A ROW FROM THE DATABASE TABLE
	 * 
	 */
	public void deleteRow(String tableName, String userName)
	{
		// ask the database manager to delete the row of given id
		try {db.delete(tableName, COL_USERNAME + "=" + userName, null);}
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
		
	}
	
	/**********************************************************************
	 * DELETING ALL ROWS FROM A DATABASE TABLE
	 * 
	 */
	public void deleteAllRows(String tableName)
	{
		// ask the database manager to delete the row of given id
		try {db.delete(tableName, null, null);}
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
		
	}
	
	/**********************************************************************
	 * Check if all the tables are empty or not
	 */
	public boolean isEmpty() {
		Cursor cursor;
		
		cursor = db.query(
				GTALK_TABLE_NAME,
				new String[]{COL_USERNAME,COL_PASSWORD},
				null, null, null, null, null
		);
		cursor.moveToFirst();
		if(cursor.isAfterLast())
			return true;
		cursor = db.query(
				FBOOK_TABLE_NAME,
				new String[]{COL_USERNAME,COL_PASSWORD},
				null, null, null, null, null
		);
		if(cursor.isAfterLast())
			return true;
		cursor = db.query(
				BLUET_TABLE_NAME,
				new String[]{COL_USERNAME},
				null, null, null, null, null
		);
		if(cursor.isAfterLast())
			return true;
		cursor = db.query(
				CHATH_TABLE_NAME,
				new String[]{COL_USERNAME,COL_DATETIME,COL_CURR_USER,COL_MESSAGE},
				null, null, null, null, null
		);
		if(cursor.isAfterLast())
			return true;
		
		//if no tables are empty then return false
		return false;
	}
 
	/**********************************************************************
	 * UPDATING A ROW IN THE DATABASE TABLE
	 * 
	 */ 
	public void updateRow(String tableName, String userName, String[] columns)
	{
		// this is a key value pair holder used by android's SQLite functions
		ContentValues values = new ContentValues();
		if(tableName==GTALK_TABLE_NAME || tableName==FBOOK_TABLE_NAME) {
			values.put(COL_USERNAME, columns[0]);
			values.put(COL_PASSWORD, columns[1]);
		}
		if(tableName==BLUET_TABLE_NAME) {
			values.put(COL_USERNAME, columns[0]);
		}
		if(tableName==CHATH_TABLE_NAME) {
			values.put(COL_USERNAME, columns[0]);
			values.put(COL_DATETIME, columns[1]);
			values.put(COL_CURR_USER, columns[2]);
			values.put(COL_MESSAGE, columns[3]);
		}
 
		// ask the database object to update the database row of given rowID
		try {db.update(tableName, values, COL_USERNAME + "=" + userName, null);}
		catch (Exception e)
		{
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
	}
 
	/**********************************************************************
	 * RETRIEVING A ROW FROM THE DATABASE TABLE
	 * 
	 */
	public ArrayList<Object> getRowAsArray(String tableName, String userName)
	{
		// create an array list to store data from the database row.
		ArrayList<Object> rowArray = new ArrayList<Object>();
		Cursor cursor;

		try
		{
			// this is a database call that creates a "cursor" object.
			// the cursor object store the information collected from the
			// database and is used to iterate through the data.
			String[] columns = null;
			if(tableName==GTALK_TABLE_NAME || tableName==FBOOK_TABLE_NAME) {
				columns = new String[]{COL_USERNAME,COL_PASSWORD};
			}
			if(tableName==BLUET_TABLE_NAME) {
				columns = new String[]{COL_USERNAME};
			}
			if(tableName==CHATH_TABLE_NAME) {
				columns = new String[]{COL_USERNAME,
						COL_DATETIME,
						COL_CURR_USER,
						COL_MESSAGE};
			}
			cursor = db.query
			(
					tableName,
					columns,
					COL_USERNAME + "=" + userName,
					null, null, null, null, null
			);
 
			// move the pointer to position zero in the cursor.
			cursor.moveToFirst();
 
			// if there is data available after the cursor's pointer, add
			// it to the ArrayList that will be returned by the method.
			if (!cursor.isAfterLast())
			{
				int i = 0;
				for(i=0;i<cursor.getColumnCount();i++) {
					rowArray.add(cursor.getString(i));
				}
			}
 
			// let java know that you are through with the cursor.
			cursor.close();
		}
		catch (SQLException e) 
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
 
		// return the ArrayList containing the given row from the database.
		return rowArray;
	}
 
 
 
 
	/**********************************************************************
	 * RETRIEVING ALL ROWS FROM THE DATABASE TABLE
	 * the key is automatically assigned by the database
	 */
 
	public ArrayList<ArrayList<Object>> getAllRowsAsArrays(String tableName)
	{
		// create an ArrayList that will hold all of the data collected from
		// the database.
		ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();
 
		// this is a database call that creates a "cursor" object.
		// the cursor object store the information collected from the
		// database and is used to iterate through the data.
		Cursor cursor = null;
 
		try
		{
			String[] columns = null;
			if(tableName==GTALK_TABLE_NAME || tableName==FBOOK_TABLE_NAME) {
				columns = new String[]{COL_USERNAME,COL_PASSWORD};
			}
			if(tableName==BLUET_TABLE_NAME) {
				columns = new String[]{COL_USERNAME};
			}
			if(tableName==CHATH_TABLE_NAME) {
				columns = new String[]{COL_USERNAME,
						COL_DATETIME,
						COL_CURR_USER,
						COL_MESSAGE};
			}
			// ask the database object to create the cursor.
			cursor = db.query(
					tableName,
					columns,
					null, null, null, null, null
			);
 
			// move the cursor's pointer to position zero.
			cursor.moveToFirst();
 
			// if there is data after the current cursor position, add it
			// to the ArrayList.
			if (!cursor.isAfterLast())
			{
				do
				{
					ArrayList<Object> dataList = new ArrayList<Object>();
 
					for(int j=0;j<cursor.getColumnCount();j++) {
						dataList.add(cursor.getString(j));
					}
 
					dataArrays.add(dataList);
				}
				// move the cursor's pointer up one position.
				while (cursor.moveToNext());
			}
		}
		catch (SQLException e)
		{
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
		
		cursor.close();
		// return the ArrayList that holds the data collected from
		// the database.
		return dataArrays;
	}
 
 
 
 
	/**********************************************************************
	 * THIS IS THE BEGINNING OF THE INTERNAL SQLiteOpenHelper SUBCLASS.
	 * 
	 * I MADE THIS CLASS INTERNAL SO I CAN COPY A SINGLE FILE TO NEW APPS 
	 * AND MODIFYING IT - ACHIEVING DATABASE FUNCTIONALITY.  ALSO, THIS WAY 
	 * I DO NOT HAVE TO SHARE CONSTANTS BETWEEN TWO FILES AND CAN
	 * INSTEAD MAKE THEM PRIVATE AND/OR NON-STATIC.  HOWEVER, I THINK THE
	 * INDUSTRY STANDARD IS TO KEEP THIS CLASS IN A SEPARATE FILE.
	 *********************************************************************/
 
	/**
	 * This class is designed to check if there is a database that currently
	 * exists for the given program.  If the database does not exist, it creates
	 * one.  After the class ensures that the database exists, this class
	 * will open the database for use.  Most of this functionality will be
	 * handled by the SQLiteOpenHelper parent class.  The purpose of extending
	 * this class is to tell the class how to create (or update) the database.
	 * 
	 * @author Randall Mitchell
	 *
	 */
	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper
	{
		public CustomSQLiteOpenHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}
 
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// This string is used to create the database.  It should
			// be changed to suit your needs.
			String gtalkCreateQuery = "create table " +
										GTALK_TABLE_NAME +
										" (" + COL_USERNAME
										 + " varchar primary key not null," +
										COL_PASSWORD + " varchar" +
										");";
			String fbookCreateQuery = "create table " +
			FBOOK_TABLE_NAME +
			" (" + COL_USERNAME
			 + " varchar primary key not null," +
			COL_PASSWORD + " varchar" +
			");";
			
			String blueCreateQuery = "create table " +
			BLUET_TABLE_NAME +
			" (" + COL_USERNAME
			 + " varchar primary key not null" +
			");";
			
			String chathCreateQuery = "create table " +
			CHATH_TABLE_NAME +
			" (" + COL_USERNAME
			 + " varchar," +
			COL_DATETIME + " date," +
			COL_CURR_USER + " varchar," +
			COL_MESSAGE + " varchar" +
			");";
			
			// create the tables...
			
			try {
				db.execSQL(gtalkCreateQuery);
				db.execSQL(fbookCreateQuery);
				db.execSQL(blueCreateQuery);
				db.execSQL(chathCreateQuery);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Log.e("SQL Error:",e.toString());
			}
		}
 
 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
			// OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE.
		}
	}
}
