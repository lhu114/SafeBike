package com.safering.safebike.navigation;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.safering.safebike.R;

public class ParentRctFvActivity extends AppCompatActivity {
    FragmentTabHost tabHost;
    View view;

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


    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        view = inflater.inflate(R.layout.fragment_parent_rct_fv, container, false);
//
//        tabHost = (FragmentTabHost) view.findViewById(R.id.tabHost);
//        tabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
//        tabHost.addTab(tabHost.newTabSpec(TAG_TAB_RECENT).setIndicator(TAG_TAB_RECENT_NAME), RecentFragment.class, null);
//        tabHost.addTab(tabHost.newTabSpec(TAG_TAB_FAVORITE).setIndicator(TAG_TAB_FAVORITE_NAME), FavoriteFragment.class, null);
//
//        return view;
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_navigation, menu);
//
//        MenuItem item = menu.findItem(R.id.menu_search);
//        View menuView = MenuItemCompat.getActionView(item);
//
//        keywordView = (EditText) menuView.findViewById(R.id.edit_keyword);
//        keywordView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                searchDestination(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        Button btn = (Button) menuView.findViewById(R.id.btn_menu_search);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), "search", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        btn = (Button) menuView.findViewById(R.id.btn_menu_cancel);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), "cancel", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void searchDestination(final String keyword) {
//        mListAdapter = new ArrayAdapter<POI>(getContext(), android.R.layout.simple_list_item_1);
//        listView.setAdapter(mListAdapter);
//
////        mListAdapter.add(poi);
//    }
}
