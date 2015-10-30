package com.safering.safebike.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.safering.safebike.R;



public class NavigationFragment extends Fragment {
    private static final String DEBUG_TAG = "safebike";

    View view;
    EditText keywordView;
    ListView listView;
    ArrayAdapter<POI> mListAdapter;

    public NavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

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

        MenuItem item = menu.findItem(R.id.menu_search);
        View menuView = MenuItemCompat.getActionView(item);

        keywordView = (EditText) menuView.findViewById(R.id.edit_keyword);
        keywordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchDestination(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button btn = (Button) menuView.findViewById(R.id.btn_menu_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "search", Toast.LENGTH_SHORT).show();
            }
        });

        btn = (Button) menuView.findViewById(R.id.btn_menu_cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "cancel", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_search) {
            /*
             * 최근이용, 즐겨찾기 탭 활성화
             */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchDestination(final String keyword) {

    }
}
