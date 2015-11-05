package com.safering.safebike.navigation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.safering.safebike.property.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by lhu on 2015-11-03.
 */
public class RecentDataManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "recentpoi";
    private static final int DB_VERSION = 1;

    private static RecentDataManager instance;

    private RecentDataManager() {
        super(MyApplication.getContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized RecentDataManager getInstance() {
        if (instance == null) {
            instance = new RecentDataManager();
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("safebike", "RecentDataManager.onCreate");
        String sql = "CREATE TABLE " + RecentDB.RecentTable.TABLE_NAME + "(" +
                RecentDB.RecentTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecentDB.RecentTable.COLUMN_POI_NAME + " TEXT NOT NULL, " +
                RecentDB.RecentTable.COLUMN_SEARCH_DATE + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertRecent(RecentItem item) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.clear();

            long currentTime = System.currentTimeMillis();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currentTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String searchDate = sdf.format(c.getTime());

            values.put(RecentDB.RecentTable.COLUMN_POI_NAME, item.rctPOIName);
            values.put(RecentDB.RecentTable.COLUMN_SEARCH_DATE, searchDate);

            item._id = db.insert(RecentDB.RecentTable.TABLE_NAME, null, values);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void updateRecent(RecentItem item) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.clear();

            long currentTime = System.currentTimeMillis();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currentTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String searchDate = sdf.format(c.getTime());

            values.put(RecentDB.RecentTable.COLUMN_POI_NAME, item.rctPOIName);
            values.put(RecentDB.RecentTable.COLUMN_SEARCH_DATE, searchDate);

            String selection = RecentDB.RecentTable._ID + " = ?";
            String[] args = new String[] { "" + item._id };

            db.update(RecentDB.RecentTable.TABLE_NAME, values, selection, args);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void deleteRecent(RecentItem item) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = RecentDB.RecentTable._ID + " = ?";
        String[] whereArgs = new String[] { "" + item._id };

        db.delete(RecentDB.RecentTable.TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    public void deleteRecentAll() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(RecentDB.RecentTable.TABLE_NAME, null, null);
        db.close();
    }

//    public List<RecentItem> getRecentList() {
//
//    }

    public Cursor getRecentCursor(String keyword) {
        SQLiteDatabase db  = getReadableDatabase();
        String[] columns = {RecentDB.RecentTable.TABLE_NAME + "." + RecentDB.RecentTable._ID,
                RecentDB.RecentTable.COLUMN_POI_NAME,
                RecentDB.RecentTable.COLUMN_SEARCH_DATE};

        String selection = null;
        String[] args = null;

        if(!TextUtils.isEmpty(keyword)) {
            selection = RecentDB.RecentTable.COLUMN_POI_NAME + " LIKE ?";
            args = new String[] {"%" + keyword + "%"};
        }

        String orderBy = RecentDB.RecentTable._ID + " DESC";
        Cursor c = db.query(RecentDB.RecentTable.TABLE_NAME, columns, selection, args, null, null, orderBy);

        return c;
    }
}
