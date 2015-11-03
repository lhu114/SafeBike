package com.safering.safebike.navigation;

import android.provider.BaseColumns;

/**
 * Created by lhu on 2015-11-02.
 */
public class RecentDB {

    public interface RecentTable extends BaseColumns {
        public static final String TABLE_NAME = "recentTable";
        public static final String COLUMN_POI_NAME = "name";
        public static final String COLUMN_SEARCH_DATE = "date";
    }
}
