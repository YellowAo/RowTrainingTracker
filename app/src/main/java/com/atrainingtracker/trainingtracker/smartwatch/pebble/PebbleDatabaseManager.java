

package com.atrainingtracker.trainingtracker.smartwatch.pebble;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.ActivityType;
import com.atrainingtracker.banalservice.sensor.SensorType;
import com.atrainingtracker.trainingtracker.TrainingApplication;

import java.util.LinkedList;


public class PebbleDatabaseManager {
    private static final String TAG = PebbleDatabaseManager.class.getName();
    private static final boolean DEBUG = TrainingApplication.DEBUG && true;
    private static PebbleDatabaseManager cInstance;
    private static PebbleDbHelper cPebbleDbHelper;
    private int mOpenCounter;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(PebbleDbHelper pebbleDbHelper) {
        if (cInstance == null) {
            cInstance = new PebbleDatabaseManager();
            cPebbleDbHelper = pebbleDbHelper;
        }
    }

    public static synchronized PebbleDatabaseManager getInstance() {
        if (cInstance == null) {
            throw new IllegalStateException(PebbleDatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return cInstance;
    }

    private static Cursor getViewsCursor(SQLiteDatabase db, long viewId) {
        return db.query(PebbleDbHelper.VIEWS_TABLE,
                null,
                PebbleDbHelper.C_ID + "=?",
                new String[]{viewId + ""},
                null,
                null,
                null);
    }

    public static ActivityType getActivityType(long viewId) {

        ActivityType activityType = ActivityType.getDefaultActivityType();

        SQLiteDatabase db = getInstance().getOpenDatabase();
        Cursor cursor = getViewsCursor(db, viewId);

        if (cursor.moveToFirst()) {
            activityType = ActivityType.valueOf(cursor.getString(cursor.getColumnIndex(PebbleDbHelper.ACTIVITY_TYPE)));
        }

        cursor.close();
        getInstance().closeDatabase();

        return activityType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // some helper methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getName(long viewId) {
        String name = null;

        SQLiteDatabase db = getInstance().getOpenDatabase();
        Cursor cursor = getViewsCursor(db, viewId);

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(PebbleDbHelper.NAME));
        }

        getInstance().closeDatabase();

        return name;
    }

    public static void updateSensorType(long viewId, int rowNr, SensorType sensorType) {
        ContentValues values = new ContentValues();
        values.put(PebbleDbHelper.SENSOR_TYPE, sensorType.name());

        SQLiteDatabase db = getInstance().getOpenDatabase();
        db.update(PebbleDbHelper.ROWS_TABLE,
                values,
                PebbleDbHelper.ROW_NR + "=? AND " + PebbleDbHelper.VIEW_ID + "=?",
                new String[]{rowNr + "", viewId + ""});
        getInstance().closeDatabase();
    }

    public static void updateNameOfView(long viewId, String name) {

        ContentValues values = new ContentValues();
        values.put(PebbleDbHelper.NAME, name);

        SQLiteDatabase db = getInstance().getOpenDatabase();
        db.update(PebbleDbHelper.VIEWS_TABLE,
                values,
                PebbleDbHelper.C_ID + "=?",
                new String[]{viewId + ""});
        getInstance().closeDatabase();
    }

    public static long getFirstViewId(ActivityType activityType) // the first view is the one with a negative value for PREV_VIEW_ID
    {
        long viewId = -1;

        SQLiteDatabase db = getInstance().getOpenDatabase();
        Cursor cursor = db.query(PebbleDbHelper.VIEWS_TABLE,
                null,
                PebbleDbHelper.ACTIVITY_TYPE + "=? AND " + PebbleDbHelper.PREV_VIEW_ID + "<0",
                new String[]{activityType.name()},
                null, null, null);

        if (cursor.moveToFirst()) {
            viewId = cursor.getInt(cursor.getColumnIndex(PebbleDbHelper.C_ID));
        }
        cursor.close();
        getInstance().closeDatabase();

        return viewId;
    }

    public static long getNextViewId(long viewId) {
        long nextViewId = -1;

        SQLiteDatabase db = getInstance().getOpenDatabase();
        Cursor cursor = getViewsCursor(db, viewId);

        if (cursor.moveToFirst()) {
            nextViewId = cursor.getInt(cursor.getColumnIndex(PebbleDbHelper.NEXT_VIEW_ID));
        }

        cursor.close();
        getInstance().closeDatabase(); // db.close();

        return nextViewId;
    }

    public static long getPrevViewId(long viewId) {
        long nextViewId = -1;

        SQLiteDatabase db = getInstance().getOpenDatabase();
        Cursor cursor = getViewsCursor(db, viewId);

        if (cursor.moveToFirst()) {
            nextViewId = cursor.getInt(cursor.getColumnIndex(PebbleDbHelper.PREV_VIEW_ID));
        }

        cursor.close();
        getInstance().closeDatabase(); // db.close();

        return nextViewId;
    }

    public static void ensureEntryForActivityTypeExists(Context context, ActivityType activityType) {
        if (getFirstViewId(activityType) == -1) {  // entry does not exist
            PebbleDatabaseManager databaseManager = getInstance();
            SQLiteDatabase db = databaseManager.getOpenDatabase();
            PebbleDbHelper.insertDefaultViewToDb(context, db, activityType, -1, -1);
            databaseManager.closeDatabase();
        }
    }

    public static LinkedList<Long> getViewIdList(ActivityType activityType) {
        LinkedList<Long> result = new LinkedList<>();

        long viewId = getFirstViewId(activityType);
        while (viewId >= 0) {
            result.add(viewId);
            viewId = getNextViewId(viewId);
        }

        return result;
    }

    public static LinkedList<String> getTitleList(ActivityType activityType) {
        LinkedList<String> result = new LinkedList<>();

        for (long viewId : getViewIdList(activityType)) {
            result.add(getName(viewId));
        }

        return result;
    }

    public static LinkedList<SensorType> getSensorTypeList(long viewId) {
        LinkedList<SensorType> result = new LinkedList<>();

        SQLiteDatabase db = getInstance().getOpenDatabase();
        Cursor cursor = db.query(PebbleDbHelper.ROWS_TABLE,
                null,
                PebbleDbHelper.VIEW_ID + "=?",
                new String[]{viewId + ""},
                null,
                null,
                PebbleDbHelper.ROW_NR + " ASC");

        while (cursor.moveToNext()) {
            result.add(SensorType.valueOf(cursor.getString(cursor.getColumnIndex(PebbleDbHelper.SENSOR_TYPE))));
        }

        cursor.close();
        getInstance().closeDatabase();

        return result;
    }

    public static void deleteView(long viewId) {

        long prevViewId = getPrevViewId(viewId);
        long nextViewId = getNextViewId(viewId);

        SQLiteDatabase db = getInstance().getOpenDatabase();
        // delete from both tables
        db.delete(PebbleDbHelper.VIEWS_TABLE, PebbleDbHelper.C_ID + "=?", new String[]{viewId + ""});
        db.delete(PebbleDbHelper.ROWS_TABLE, PebbleDbHelper.VIEW_ID + "=?", new String[]{viewId + ""});

        // update the next and prev viewId of the prev and next view
        if (prevViewId >= 0) {
            ContentValues values = new ContentValues();
            values.put(PebbleDbHelper.NEXT_VIEW_ID, nextViewId);
            db.update(PebbleDbHelper.VIEWS_TABLE, values,
                    PebbleDbHelper.C_ID + "=?", new String[]{prevViewId + ""});
        }
        if (nextViewId >= 0) {
            ContentValues values = new ContentValues();
            values.put(PebbleDbHelper.PREV_VIEW_ID, prevViewId);
            db.update(PebbleDbHelper.VIEWS_TABLE, values,
                    PebbleDbHelper.C_ID + "=?", new String[]{nextViewId + ""});
        }

        getInstance().closeDatabase();
    }

    public static long addDefaultView(Context context, long viewId, boolean addAfterCurrentLayout) {
        long newViewId = -1;

        SQLiteDatabase db = getInstance().getOpenDatabase();

        if (addAfterCurrentLayout) {
            long nextViewId = getNextViewId(viewId);
            newViewId = PebbleDbHelper.insertDefaultViewToDb(context, db, getActivityType(viewId), viewId, nextViewId);

            // update next of current
            ContentValues values = new ContentValues();
            values.put(PebbleDbHelper.NEXT_VIEW_ID, newViewId);
            db.update(PebbleDbHelper.VIEWS_TABLE, values,
                    PebbleDbHelper.C_ID + "=?", new String[]{viewId + ""});

            // update prev of old next
            values.clear();
            values.put(PebbleDbHelper.PREV_VIEW_ID, newViewId);
            db.update(PebbleDbHelper.VIEWS_TABLE, values,
                    PebbleDbHelper.C_ID + "=?", new String[]{nextViewId + ""});

        } else {
            long prevViewId = getPrevViewId(viewId);
            newViewId = PebbleDbHelper.insertDefaultViewToDb(context, db, getActivityType(viewId), prevViewId, viewId);

            // update prev of current
            ContentValues values = new ContentValues();
            values.put(PebbleDbHelper.PREV_VIEW_ID, newViewId);
            db.update(PebbleDbHelper.VIEWS_TABLE, values,
                    PebbleDbHelper.C_ID + "=?", new String[]{viewId + ""});

            // update next of old prev
            values.clear();
            values.put(PebbleDbHelper.NEXT_VIEW_ID, newViewId);
            db.update(PebbleDbHelper.VIEWS_TABLE, values,
                    PebbleDbHelper.C_ID + "=?", new String[]{prevViewId + ""});

        }

        getInstance().closeDatabase();

        return newViewId;
    }

    public static void deleteRow(long viewId, int rowNr) {
        if (DEBUG) Log.i(TAG, "deleteRow viewId=" + viewId + ", rowNr=" + rowNr);

        SQLiteDatabase db = getInstance().getOpenDatabase();

        db.delete(PebbleDbHelper.ROWS_TABLE,
                PebbleDbHelper.VIEW_ID + "=? AND " + PebbleDbHelper.ROW_NR + "=?",
                new String[]{viewId + "", rowNr + ""});

        getInstance().closeDatabase();
    }

    public static void addRow(long viewId, int rowNr, SensorType sensorType) {
        if (DEBUG)
            Log.i(TAG, "addRow viewId=" + viewId + ", rowNr=" + rowNr + ", sensorType=" + sensorType.name());
        ContentValues values = new ContentValues();
        values.put(PebbleDbHelper.ROW_NR, rowNr + "");
        values.put(PebbleDbHelper.VIEW_ID, viewId);
        values.put(PebbleDbHelper.SENSOR_TYPE, sensorType.name());

        SQLiteDatabase db = getInstance().getOpenDatabase();

        // first delete the corresponding rowNr
        db.delete(PebbleDbHelper.ROWS_TABLE,
                PebbleDbHelper.VIEW_ID + "=? AND " + PebbleDbHelper.ROW_NR + "=?",
                new String[]{viewId + "", rowNr + ""});
        // then insert the new values
        db.insert(PebbleDbHelper.ROWS_TABLE, null, values);
        getInstance().closeDatabase();
    }

    public synchronized SQLiteDatabase getOpenDatabase() {
        mOpenCounter++;
        if (mOpenCounter == 1) {
            // Opening new database
            mDatabase = cPebbleDbHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        if (mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // The database Helper
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static class PebbleDbHelper extends SQLiteOpenHelper {
        public static final String DB_NAME = "PebbleView.db";
        // public static final int    DB_VERSION  = 2;
        public static final int DB_VERSION = 3; // upgraded to version 3 at 10.1.2016
        public static final String VIEWS_TABLE = "ViewsTable";
        public static final String ROWS_TABLE = "LayoutRowsTable";
        public static final String C_ID = BaseColumns._ID;
        // decided to keep this scheme here but the one with layout nr for TrackingViews (21.2.2016)
        public static final String VIEW_ID = "ViewId";
        public static final String ROW_NR = "RowNr";
        // public static final String SPORT_TYPE    = "ActivityType";
        // public static final String SENSOR_BITS   = "SensorBits";
        public static final String ACTIVITY_TYPE = "ActivityType";
        public static final String NAME = "Name";
        public static final String NEXT_VIEW_ID = "NextViewId";
        public static final String PREV_VIEW_ID = "PrevViewId";
        // public static final String VIEW_ID       = "ViewID";
        public static final String SENSOR_TYPE = "SensorType";
        @Deprecated
        protected static final String CREATE_LAYOUTS_TABLE_V2 = "create table " + ROWS_TABLE + " ("
                + ROW_NR + " int, "
                + VIEW_ID + " int, "   // corresponds to C_ID of VIEWS_TABLE
                + SENSOR_TYPE + " text)";
        protected static final String CREATE_VIEWS_TABLE_V3 = "create table " + VIEWS_TABLE + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                // + VIEW_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ACTIVITY_TYPE + " text, "
                + NAME + " text, "
//            + LAYOUT_NR     + " int, " // since layouts might be deleted, this makes no longer sense => this field is replaced by prev and next view id
                + PREV_VIEW_ID + " int, "
                + NEXT_VIEW_ID + " int)";
        protected static final String CREATE_LAYOUTS_TABLE_V3 = "create table " + ROWS_TABLE + " ("
                + ROW_NR + " int, "
                + VIEW_ID + " int, "   // corresponds to C_ID of VIEWS_TABLE
                + SENSOR_TYPE + " text)";
        @Deprecated
        static final String LAYOUT_NR = "LayoutNr";       // for each ActivityType, we might have different views/activities/layouts
        @Deprecated
        protected static final String CREATE_VIEWS_TABLE_V2 = "create table " + VIEWS_TABLE + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                // + VIEW_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ACTIVITY_TYPE + " text, "
                + NAME + " text, "
                + LAYOUT_NR + " int)";  // since layouts might be deleted, this makes no longer sense?
        private static final String TAG = PebbleDbHelper.class.getName();
        private static final boolean DEBUG = TrainingApplication.DEBUG & true;
        private Context mContext;


        // Constructor
        public PebbleDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        public static long insertDefaultViewToDb(Context context, SQLiteDatabase db, ActivityType activityType, long prevViewId, long nextViewId) {
            String name = context.getString(R.string.text_default);

            Cursor cursor = db.query(VIEWS_TABLE,
                    null,
                    ACTIVITY_TYPE + "=?",
                    new String[]{activityType.name()},
                    null,
                    null,
                    null);
            int numberOfDefaults = cursor.getCount();
            if (numberOfDefaults > 0) {
                name = context.getString(R.string.string_and_number_format, name, numberOfDefaults + 1);
            }

            ContentValues values = new ContentValues();
            values.clear();
            values.put(ACTIVITY_TYPE, activityType.name());
            values.put(NAME, name);
            values.put(PREV_VIEW_ID, prevViewId);
            values.put(NEXT_VIEW_ID, nextViewId);
            long viewID = db.insert(VIEWS_TABLE, null, values);

            int rowNr = 1;
            for (SensorType sensorType : getDefaultSensorList(activityType)) {
                values.clear();
                values.put(ROW_NR, rowNr);
                values.put(VIEW_ID, viewID);
                values.put(SENSOR_TYPE, sensorType.name());
                db.insert(ROWS_TABLE, null, values);
                rowNr++;
            }

            return viewID;
        }

        private static final void addColumn(SQLiteDatabase db, String table, String column, String type) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + ";");
        }

        public static LinkedList<SensorType> getDefaultSensorList(ActivityType activityType) {
            LinkedList<SensorType> result = new LinkedList<SensorType>();

            switch (activityType) {
                case ROWING_POWER:
                    result.add(SensorType.TIME_ACTIVE);  // 1
                    result.add(SensorType.HR);           // 2
                    result.add(SensorType.POWER);        // 3
                    result.add(SensorType.CADENCE);      // 4
                    result.add(SensorType.LAP_NR);       // 5
                    break;

                case ROWING_SPEED_AND_CADENCE:
                    result.add(SensorType.TIME_ACTIVE);  // 1
                    result.add(SensorType.HR);           // 2
                    result.add(SensorType.SPEED_mps);    // 3
                    result.add(SensorType.CADENCE);      // 4
                    result.add(SensorType.LAP_NR);       // 5
                    break;

                case ROWING_SPEED:
                    result.add(SensorType.TIME_ACTIVE);  // 1
                    result.add(SensorType.HR);           // 2
                    result.add(SensorType.SPEED_mps);    // 3
                    break;

                case GENERIC_HR:
                    result.add(SensorType.TIME_ACTIVE); // 1
                    result.add(SensorType.HR);          // 2
                    result.add(SensorType.SPEED_mps);   // 3
                    break;

                case RUN_SPEED_AND_CADENCE:
                    result.add(SensorType.TIME_ACTIVE); // 1
                    result.add(SensorType.HR);          // 2
                    result.add(SensorType.PACE_spm);    // 3
                    result.add(SensorType.CADENCE);     // 4
                    result.add(SensorType.LAP_NR);      // 5
                    break;

                // does not exist in reality
//    	case RUN_SPEED:
//            result.add(SensorType.TIME_ACTIVE); // 1
//            result.add(SensorType.HR);          // 2
//            result.add(SensorType.PACE_spm);    // 3
//            break;

                case GENERIC:
                default:
                    result.add(SensorType.TIME_ACTIVE); // 1
                    result.add(SensorType.SPEED_mps);   // 2
                    result.add(SensorType.DISTANCE_m);  // 3
                    break;

            }

            return result;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_VIEWS_TABLE_V3);
            db.execSQL(CREATE_LAYOUTS_TABLE_V3);

            if (DEBUG) Log.d(TAG, "onCreated sql: " + CREATE_VIEWS_TABLE_V3);
            if (DEBUG) Log.d(TAG, "onCreated sql: " + CREATE_LAYOUTS_TABLE_V3);

            if (DEBUG) Log.d(TAG, "filling db");

            for (ActivityType activityType : ActivityType.values()) {
                insertDefaultViewToDb(mContext, db, activityType, -1, -1);
            }
            if (DEBUG) Log.d(TAG, "filled db");
        }

        // Called whenever newVersion != oldVersion
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            if (oldVersion < 3) {
                // first, add new columns
                addColumn(db, VIEWS_TABLE, PREV_VIEW_ID, "int");
                addColumn(db, VIEWS_TABLE, NEXT_VIEW_ID, "int");

                Cursor cursor = null;
                for (ActivityType activityType : ActivityType.values()) {
                    int viewId, prevViewId = -1, nextViewId = -1;
                    ContentValues values = new ContentValues();

                    // reconstruct and insert prev view ids
                    cursor = db.query(VIEWS_TABLE, null,
                            ACTIVITY_TYPE + "=?", new String[]{activityType.name()},
                            null, null,
                            LAYOUT_NR + " ASC"); // order by

                    while (cursor.moveToNext()) {
                        viewId = cursor.getInt(cursor.getColumnIndex(C_ID));
                        values.clear();
                        values.put(PREV_VIEW_ID, prevViewId);
                        db.update(VIEWS_TABLE, values, C_ID + "=?", new String[]{viewId + ""});
                        prevViewId = viewId;
                    }

                    // reconstruct and insert next view ids
                    cursor = db.query(VIEWS_TABLE, null,
                            ACTIVITY_TYPE + "=?", new String[]{activityType.name()},
                            null, null,
                            LAYOUT_NR + " DESC"); // order by

                    while (cursor.moveToNext()) {
                        viewId = cursor.getInt(cursor.getColumnIndex(C_ID));
                        values.clear();
                        values.put(NEXT_VIEW_ID, nextViewId);
                        db.update(VIEWS_TABLE, values, C_ID + "=?", new String[]{viewId + ""});
                        nextViewId = viewId;
                    }
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            // db.execSQL("drop table if exists " + VIEWS_TABLE);
            // db.execSQL("drop table if exists " + ROWS_TABLE);
            //
            // if (DEBUG) Log.d(TAG, "onUpgraded");
            // onCreate(db);  // run onCreate to get new database
        }

    }
}
