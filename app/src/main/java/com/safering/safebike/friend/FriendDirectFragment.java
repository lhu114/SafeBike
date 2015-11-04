package com.safering.safebike.friend;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendDirectFragment extends Fragment {
    ListView listView;
    FriendAdapter fAdapter;

    public FriendDirectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_direct, container, false);
        Button btn = (Button) view.findViewById(R.id.btn_search_friend_direct);
        listView = (ListView) view.findViewById(R.id.listview_direct_friend);
        fAdapter = new FriendAdapter(1);
        listView.setAdapter(fAdapter);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 20; i++) {
                    //서버랑 비교해서 가져오기
                    FriendItem item = new FriendItem();
                    item.friendId = "friendNumber/" + i;
                    fAdapter.add(item);
                }

            }
        });
        return view;
    }
}
