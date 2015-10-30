//package com.safering.safebike.navigation;
//
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTabHost;
//import android.support.v4.view.MenuItemCompat;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import com.safering.safebike.R;
//
///**
// * A simple {@link Fragment} subclass.
// */
//public class ParentRctFvFragment extends Fragment {
//    FragmentTabHost tabHost;
//    View view;
//
//    EditText keywordView;
//    ListView listView;
//    ArrayAdapter<POI> mListAdapter;
//
//    private static final String TAG_TAB_RECENT = "RECENT";
//    private static final String TAG_TAB_FAVORITE = "FAVORITE";
//    private static final String TAG_TAB_RECENT_NAME = "최근이용";
//    private static final String TAG_TAB_FAVORITE_NAME = "즐겨찾기";
//
//    public ParentRctFvFragment() {
//        // Required empty public constructor
//        this.setHasOptionsMenu(true);
//    }
//
//
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
//}
