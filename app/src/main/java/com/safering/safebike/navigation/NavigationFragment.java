package com.safering.safebike.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.safering.safebike.R;



public class NavigationFragment extends Fragment {
    private static final String DEBUG_TAG = "safebike";

    View view;
    EditText keywordView;
    ListView listView;
    ArrayAdapter<POI> mListAdapter;

    public NavigationFragment() {
        // Required empty public constructor
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(DEBUG_TAG, "NavigationFragment.onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "NavigationFragment.onCreateView");
        // Inflate the layout for this fragment
        try {
            view = inflater.inflate(R.layout.fragment_navigation, container, false);
        } catch (InflateException e) {
            /*
             * 구글맵 View가 이미 inflate되어 있는 상태이므로, 에러를 무시합니다.
             */
        }
        return view;
    }

    /*
     * 프래그먼트가 화면에서 사라질 때 프래그먼트의 뷰를 컨테이너 뷰에서 제거
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(DEBUG_TAG, "NavigationFragment.onDestroyView");

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();

            if(parent != null) {
                parent.removeView(view);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_navigation, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_search) {
            /*
             * 최근이용, 즐겨찾기 탭 활성화
             */
            Intent intent = new Intent(getContext(), ParentRctFvActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchDestination(final String keyword) {
        mListAdapter = new ArrayAdapter<POI>(getContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(mListAdapter);

//        mListAdapter.add(poi);
    }
}
