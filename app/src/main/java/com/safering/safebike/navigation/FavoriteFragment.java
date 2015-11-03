package com.safering.safebike.navigation;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {
    ListView listView;
    FavoriteAdapter mAdapter;
    TextView messageView;
    Button deleteBtn;

    public FavoriteFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        messageView = (TextView) view.findViewById(R.id.text_message_favorite);
        deleteBtn = (Button) view.findViewById(R.id.btn_delete_favorite);
        listView = (ListView) view.findViewById(R.id.listView_favorite);
        mAdapter = new FavoriteAdapter();
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.getItemAtPosition(position);
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT);
            }
        });

        initData();

        if (mAdapter.getCount() > 0) {
            messageView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        } else {

        }

        return view;
    }

    private void initData() {
        /*
         * 네트워크 요청해서 즐겨찾기 데이터 가져오기
         */

        for (int i = 0; i < 10; i++) {
            FavoriteItem itemData = new FavoriteItem();
            itemData.fvPOIName = "즐겨찾기 목적지";
            mAdapter.add(itemData);
        }
    }
}
