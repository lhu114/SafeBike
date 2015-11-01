package com.safering.safebike.navigation;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.safering.safebike.R;

public class ParentRctFvActivity extends AppCompatActivity {
    FragmentTabHost tabHost;

    View view;
    EditText keywordView;
    ListView listView;

    ArrayAdapter<POI> mAdapter;

    private static final String TAG_TAB_RECENT = "RECENT";
    private static final String TAG_TAB_FAVORITE = "FAVORITE";
    private static final String TAG_TAB_RECENT_NAME = "최근이용";
    private static final String TAG_TAB_FAVORITE_NAME = "즐겨찾기";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_rct_fv);

        tabHost = (FragmentTabHost)findViewById(R.id.tabHost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        tabHost.addTab(tabHost.newTabSpec(TAG_TAB_RECENT).setIndicator(TAG_TAB_RECENT_NAME), RecentFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAG_TAB_FAVORITE).setIndicator(TAG_TAB_FAVORITE_NAME), FavoriteFragment.class, null);

        listView = (ListView) findViewById(R.id.listView_destination_search);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Toast.makeText(ParentRctFvActivity.this, "onCreateOptionsMenu", Toast.LENGTH_SHORT).show();
        getMenuInflater().inflate(R.menu.menu_parent_rctfv, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        View menuView = MenuItemCompat.getActionView(item);

        keywordView = (EditText) menuView.findViewById(R.id.edit_keyword);
        keywordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPOI(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button btn = (Button) menuView.findViewById(R.id.btn_menu_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ParentRctFvActivity.this, "search", Toast.LENGTH_SHORT).show();
            }
        });

        btn = (Button) menuView.findViewById(R.id.btn_menu_cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        return true;
    }

    private void searchPOI(final String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            tabHost.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            NavigationNetworkManager.getInstance().searchPOI(ParentRctFvActivity.this, keyword, new NavigationNetworkManager.OnResultListener<SearchPOIInfo>() {
                @Override
                public void onSuccess(SearchPOIInfo result) {
                    clearAll();

                    for (POI poi : result.pois.poiList) {
                        mAdapter.add(poi);
//                        addMarker(poi);
                    }
                    if (result.pois.poiList.size() > 0) {
//                        moveMap(result.pois.poiList.get(0).getLatitude(), result.pois.poiList.get(0).getLongitude());
                    }
                }

                @Override
                public void onFail(int code) {

                }
            });
        } else {
            tabHost.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    private void clearAll() {
        mAdapter.clear();
    }
}
