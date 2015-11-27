package com.safering.safebike.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.property.PropertyManager;

public class ParentRctFvActivity extends AppCompatActivity {
    FragmentTabHost tabHost;

    View view, viewRecent, viewFavorite;
    EditText keywordView;
    ListView listView;
    TextView tvRecentTitle, tvFavoriteTitle;

    ArrayAdapter<POI> mAdapter;

    POI poi;

    private static final String TAG_TAB_RECENT = "RECENT";
    private static final String TAG_TAB_FAVORITE = "FAVORITE";
    private static final String TAG_TAB_RECENT_NAME = "최근이용";
    private static final String TAG_TAB_FAVORITE_NAME = "즐겨찾기";

    private static final String KEY_POI_OBJECT = "poiobject";
    private static final String KEY_DESTINATION_POI_NAME = "destinationpoiname";
//    private static final String KEY_POI_NAME = "poiName";
//    private static final String KEY_POI_LATITUDE = "poiLatitude";
//    private static final String KEY_POI_LONGITUDE = "poiLongitude";
//    private static final String KEY_POI_ADDRESS = "poiAddress";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_rct_fv);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar_parent_rctfv);
        getSupportActionBar().setElevation(0);
        tabHost = (FragmentTabHost)findViewById(R.id.tabHost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        tabHost.getTabWidget().setDividerDrawable(null);

        viewRecent = getLayoutInflater().inflate(R.layout.recent_fragment_view, null);
        viewFavorite = getLayoutInflater().inflate(R.layout.favorite_fragment_view, null);

        tvRecentTitle = (TextView) findViewById(R.id.text_tab_recent_title);
        tvFavoriteTitle = (TextView) findViewById(R.id.text_tab_favorite_title);

        tabHost.addTab(tabHost.newTabSpec(TAG_TAB_RECENT).setIndicator(viewRecent), RecentFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAG_TAB_FAVORITE).setIndicator(viewFavorite), FavoriteFragment.class, null);

        listView = (ListView) findViewById(R.id.listView_destination_search);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                poi = (POI) listView.getItemAtPosition(position);

                if (poi != null) {
                    //    Toast.makeText(ParentRctFvActivity.this, poiName, Toast.LENGTH_SHORT).show();

                    /*
                     *  검색어 RecentDb 에 저장 처리 필요
                     */
                    RecentItem item = new RecentItem();
                    item.rctPOIName = poi.name;

                    RecentDataManager.getInstance().insertRecent(item);

//                    String defineAddress = null;

                    Log.d("safebike", "poi.secondNo : " + poi.secondNo);

                    sendPOI(poi);
                }
            }
        });

        keywordView = (EditText) findViewById(R.id.edit_keyword);
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

        ImageButton btn = (ImageButton) findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchKeyword = keywordView.getText().toString();

                if (!TextUtils.isEmpty(searchKeyword)) {

//                    검색어 RecentDb 에 저장 처리 필요

                    RecentItem item = new RecentItem();
                    item.rctPOIName = searchKeyword;

                    RecentDataManager.getInstance().insertRecent(item);



//                    ParentRctFvActivity 에 있는 setResult 처리


                    Log.d("safebike", "rctPoiName : " + searchKeyword);
                    NavigationNetworkManager.getInstance().searchPOI(ParentRctFvActivity.this, searchKeyword, new NavigationNetworkManager.OnResultListener<SearchPOIInfo>() {
                        @Override
                        public void onSuccess(SearchPOIInfo result) {
                            POI poi = result.pois.poiList.get(0);

                            if (poi != null) {
//                                String defineAddress = null;

                                Log.d("safebike", "poi.secondNo : " + poi.secondNo);

                                sendPOI(poi);
                            }
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });
                }
            }
        });

        btn = (ImageButton) findViewById(R.id.btn_cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

        ImageButton btn = (ImageButton) menuView.findViewById(R.id.btn_menu_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchKeyword = keywordView.getText().toString();

                if (!TextUtils.isEmpty(searchKeyword)) {

                 *  검색어 RecentDb 에 저장 처리 필요

                    RecentItem item = new RecentItem();
                    item.rctPOIName = searchKeyword;

                    RecentDataManager.getInstance().insertRecent(item);



                 * ParentRctFvActivity 에 있는 setResult 처리


                    Log.d("safebike", "rctPoiName : " + searchKeyword);
                    NavigationNetworkManager.getInstance().searchPOI(ParentRctFvActivity.this, searchKeyword, new NavigationNetworkManager.OnResultListener<SearchPOIInfo>() {
                        @Override
                        public void onSuccess(SearchPOIInfo result) {
                            POI poi = result.pois.poiList.get(0);

                            if (poi != null) {
//                                String defineAddress = null;

                                Log.d("safebike", "poi.secondNo : " + poi.secondNo);

                                sendPOI(poi);
                            }
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });
                }
            }
        });

        btn = (ImageButton) menuView.findViewById(R.id.btn_menu_cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        return true;
    }*/
//                }

    private void searchPOI(final String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            tabHost.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            Log.d("safebike", keyword);
//            poi = new POI();
//            poi.name = keyword;
//
//            mAdapter.add(poi);


            NavigationNetworkManager.getInstance().searchPOI(ParentRctFvActivity.this, keyword, new NavigationNetworkManager.OnResultListener<SearchPOIInfo>() {
                @Override
                public void onSuccess(SearchPOIInfo result) {
                    clearAll();

                    for (POI poi : result.pois.poiList) {
                        mAdapter.add(poi);
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

    public void sendPOI(POI poi) {
//        String defineAddress = null;
//
//        if (!poi.detailAddrName.equals("") && !poi.firstNo.equals("") && !poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress() + " "+ poi.getDetailAddress();
//
//            Log.d("safebike", "defineAddress 1");
//        } else if (!poi.detailAddrName.equals("") && !poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress() + " " + poi.firstNo;
//
//            Log.d("safebike", "defineAddress 2");
//        } else if (!poi.detailAddrName.equals("") && poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress();
//
//            Log.d("safebike", "defineAddress 3");
//        } else if (poi.detailAddrName.equals("") && !poi.firstNo.equals("") && !poi.secondNo.equals("")) {
//            defineAddress = poi.middleAddrName + " " + poi.lowerAddrName + " " + poi.getDetailAddress();
//
//            Log.d("safebike", "defineAddress 4");
//        } else if (poi.detailAddrName.equals("") && !poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress() + " " + poi.firstNo;
//
//            Log.d("safebike", "defineAddress 5");
//        } else if (poi.detailAddrName.equals("") && poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.middleAddrName + " " + poi.lowerAddrName;
//
//            Log.d("safebike", "defineAddress 6");
//        } else {
//            defineAddress = poi.getAddress() + " " + poi.getDetailAddress();
//
//            Log.d("safebike", "defineAddress 7");
//        }

//        Intent intent = new Intent(ParentRctFvActivity.this, NavigationFragment.class);

        Intent intent = new Intent();
        intent.putExtra(KEY_POI_OBJECT, poi);
//        intent.putExtra(KEY_POI_LATITUDE, poi.getLatitude());
//        intent.putExtra(KEY_POI_LONGITUDE, poi.getLongitude());
//        intent.putExtra(KEY_POI_NAME, poi.name);
//        intent.putExtra(KEY_POI_ADDRESS, defineAddress);
        setResult(RESULT_OK, intent);

        finish();
    }

    public void sendFavoritePOI(FavoriteItem fvItem) {
        PropertyManager.getInstance().setDestinationLatitude(fvItem.fvPOILatitude);
        PropertyManager.getInstance().setDestinationLongitude(fvItem.fvPOILongitude);

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        intent = new Intent(ParentRctFvActivity.this, SelectRouteActivity.class);
        intent.putExtra(KEY_DESTINATION_POI_NAME , fvItem.fvPOIName);
        startActivity(intent);

        finish();
    }

    private void clearAll() {
        mAdapter.clear();
    }
}
