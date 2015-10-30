package com.safering.safebike.friend;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendAddressFragment extends Fragment {
    ListView listView;
    FriendAdapter adapter;
    public FriendAddressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_address, container, false);
        listView = (ListView)view.findViewById(R.id.listview_address_friend);
        adapter = new FriendAdapter(1);
        listView.setAdapter(adapter);
        Button btn = (Button)view.findViewById(R.id.btn_search_friend_address);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //네트워크로 결과 받아오기
                for(int i = 0; i < 20; i++){
                    FriendItem friend = new FriendItem();
                    friend.friendId = "friend address" + i;
                    adapter.add(friend);
                }
            }
        });
        return view;
    }


}
