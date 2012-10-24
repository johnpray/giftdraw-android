package com.johnpray.giftdraw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
	public static final String KEY_PERSON_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_GROUP_ID = "group_id";
	public static final String KEY_GIFTEE_1_ID = "drawn_1_id";
	public static final String KEY_GIFTEE_2_ID = "drawn_2_id";
	public static final String KEY_NUM_TIMES_SELECTED = "num_times_selected";

	public static final String DATABASE_NAME = "GiftWrap";
	public static final String DATABASE_TABLE = "people";
	public static final int DATABASE_VERSION = 1;

	public static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE + " (" + KEY_PERSON_ID
			+ " integer primary key autoincrement, " + KEY_NAME
			+ " text not null, " + KEY_GROUP_ID + " integer, "
			+ KEY_GIFTEE_1_ID + " integer, " + KEY_GIFTEE_2_ID + " integer, "
			+ KEY_NUM_TIMES_SELECTED + " integer);";

	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO: If database structure changes in a new version, migrate it
			// gracefully here. Don't just drop and recreate the table. That's
			// rude.
		}
	}

	/** Opens the database. */
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/** Closes the databse. */
	public void close() {
		DBHelper.close();
	}

	/**
	 * Insert a new person into the database. This variation would only be used
	 * when restoring from a backup.
	 */
	public long insertPerson(String name, String photoLocation, int groupId,
			int drawn1Id, int drawn2Id, int numTimesSelected) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_GROUP_ID, groupId);
		initialValues.put(KEY_GIFTEE_1_ID, drawn1Id);
		initialValues.put(KEY_GIFTEE_2_ID, drawn2Id);
		initialValues.put(KEY_NUM_TIMES_SELECTED, numTimesSelected);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/** Insert a new person into the database. */
	public long insertPerson(String name, int groupId) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_GROUP_ID, groupId);
		initialValues.put(KEY_GIFTEE_1_ID, -1);
		initialValues.put(KEY_GIFTEE_2_ID, -1);
		initialValues.put(KEY_NUM_TIMES_SELECTED, 0);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/** Insert a new person into the database. */
	public long insertPerson(String name) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_GROUP_ID, -1);
		initialValues.put(KEY_GIFTEE_1_ID, -1);
		initialValues.put(KEY_GIFTEE_2_ID, -1);
		initialValues.put(KEY_NUM_TIMES_SELECTED, 0);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/** Update a particular person. */
	public boolean updatePerson(int personId, String name,
			String photoLocation, int groupId, int drawn1Id, int drawn2Id,
			int numTimesSelected) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_GROUP_ID, groupId);
		args.put(KEY_GIFTEE_1_ID, drawn1Id);
		args.put(KEY_GIFTEE_2_ID, drawn2Id);
		args.put(KEY_NUM_TIMES_SELECTED, numTimesSelected);
		return db.update(DATABASE_TABLE, args, KEY_PERSON_ID + "=" + personId,
				null) > 0;
	}

	/** Update a particular person. */
	public boolean updatePerson(int personId, String name, int groupId) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_GROUP_ID, groupId);
		return db.update(DATABASE_TABLE, args, KEY_PERSON_ID + "=" + personId,
				null) > 0;
	}

	/** Set the group of a particular person. */
	public boolean setGroup(int personId, int groupId) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_GROUP_ID, groupId);
		return db.update(DATABASE_TABLE, args, KEY_PERSON_ID + "=" + personId,
				null) > 0;
	}

	/** Set the first giftee of a particular person. */
	public boolean setGiftee1(int personId, long gifteeId) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_GIFTEE_1_ID, gifteeId);
		return db.update(DATABASE_TABLE, args, KEY_PERSON_ID + "=" + personId,
				null) > 0;
	}

	/** Set the second giftee of a particular person. */
	public boolean setGiftee2(int personId, long gifteeId) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_GIFTEE_2_ID, gifteeId);
		return db.update(DATABASE_TABLE, args, KEY_PERSON_ID + "=" + personId,
				null) > 0;
	}

	/** Set the number of times this person has been selected as a giftee. */
	public boolean setNumTimesSelected(int personId, int numTimesSelected) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_NUM_TIMES_SELECTED, numTimesSelected);
		return db.update(DATABASE_TABLE, args, KEY_PERSON_ID + "=" + personId,
				null) > 0;
	}

	/** Reset giftees and numTimesSelected on all people. */
	public int resetDrawing() throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_GIFTEE_1_ID, -1);
		args.put(KEY_GIFTEE_2_ID, -1);
		args.put(KEY_NUM_TIMES_SELECTED, 0);
		return db.update(DATABASE_TABLE, args, null, null);
	}

	/** Retrieve all people. */
	public Cursor getAllPeople() throws SQLException {
		return db.query(DATABASE_TABLE, new String[] { KEY_PERSON_ID, KEY_NAME,
				KEY_GROUP_ID, KEY_GIFTEE_1_ID, KEY_GIFTEE_2_ID,
				KEY_NUM_TIMES_SELECTED }, null, null, null, null, null);
	}

	/** Retrieve all people, sorted by group. */
	public Cursor getAllPeopleSortedByGroup() throws SQLException {
		return db.query(DATABASE_TABLE, new String[] { KEY_PERSON_ID, KEY_NAME,
				KEY_GROUP_ID, KEY_GIFTEE_1_ID, KEY_GIFTEE_2_ID,
				KEY_NUM_TIMES_SELECTED }, null, null, null, null, KEY_GROUP_ID);
	}

	/** Retrieve a particular person. */
	public Cursor getPersonById(int personId) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_PERSON_ID, KEY_NAME, KEY_GROUP_ID, KEY_GIFTEE_1_ID,
				KEY_GIFTEE_2_ID, KEY_NUM_TIMES_SELECTED }, KEY_PERSON_ID + "="
				+ personId, null, null, null, null, null);
		return mCursor;
	}

	/** Retrieve all people in a particular group. */
	public Cursor getPeopleByGroupId(int groupId) throws SQLException {
		return db.query(DATABASE_TABLE, new String[] { KEY_PERSON_ID, KEY_NAME,
				KEY_GROUP_ID, KEY_GIFTEE_1_ID, KEY_GIFTEE_2_ID,
				KEY_NUM_TIMES_SELECTED }, KEY_GROUP_ID + "=" + groupId, null,
				null, null, null, null);
	}

	/** Retrieve all people NOT in a particular group. */
	public Cursor getPeopleNotInGroupId(int groupId) throws SQLException {
		return db.query(DATABASE_TABLE, new String[] { KEY_PERSON_ID, KEY_NAME,
				KEY_GROUP_ID, KEY_GIFTEE_1_ID, KEY_GIFTEE_2_ID,
				KEY_NUM_TIMES_SELECTED }, KEY_GROUP_ID + "!=" + groupId, null,
				null, null, null, null);
	}

	/** Retrieve the number of times a person has been selected as a giftee. */
	public int getNumTimesSelected(int personId) throws SQLException {
		Cursor cursor = db.query(DATABASE_TABLE,
				new String[] { KEY_NUM_TIMES_SELECTED }, KEY_PERSON_ID + "="
						+ personId, null, null, null, null, null);
		cursor.moveToFirst();
		int n = cursor.getInt(cursor
				.getColumnIndex(DBAdapter.KEY_NUM_TIMES_SELECTED));
		cursor.close();
		return n;
	}

	/**
	 * Delete a particular person. WARNING: Not reversible unless data is backed
	 * up.
	 */
	public boolean deletePerson(int personId) throws SQLException {
		return db.delete(DATABASE_TABLE, KEY_PERSON_ID + "=" + personId, null) > 0;
	}

	/**
	 * Delete all people in a particular group. WARNING: Not reversible unless
	 * data is backed up.
	 */
	public boolean deletePeopleInGroup(int groupId) throws SQLException {
		return db.delete(DATABASE_TABLE, KEY_GROUP_ID + "=" + groupId, null) > 0;
	}

	/** Delete all people. WARNING: Not reversible unless data is backed up. */
	public boolean deleteAllPeople() throws SQLException {
		return db.delete(DATABASE_TABLE, null, null) > 0;
	}
}
