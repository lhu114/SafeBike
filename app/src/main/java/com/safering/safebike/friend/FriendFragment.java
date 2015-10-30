package com.safering.safebike.friend;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.adapter.FriendItemView;
import com.safering.safebike.login.LoginActivity;
import com.safering.safebike.setting.SettingFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {

    FriendAdapter adapter;
    ListView listView;
    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
         adapter = new FriendAdapter(0);
        listView = (ListView)view.findViewById(R.id.listview_myfriend);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //Toast.makeText(getContext(),"item click",Toast.LENGTH_SHORT).show();

                PopupMenu popupMenu = new PopupMenu(getContext(),view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup_friend,popupMenu.getMenu());
                popupMenu.getMenu().getItem(0).setTitle("friend");
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //item.getItemId()에 따라 다르게 처리
                        FriendItem friend = (FriendItem)adapter.getItem(position);
                        Toast.makeText(getContext(),"id : " + friend.friendId,Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(),FriendProfileActivity.class);
                        intent.putExtra("friendInform",friend);
                        startActivity(intent);
                        return true;

                    }
                });
                popupMenu.show();
                //팝업 클릭시 FriendProfileActivity
            }
        });
        listView.setAdapter(adapter);

        setAdapter();//네트워크로 친구리스트 받아서 뷰에 표시

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //새로운 액티비티 띄우기
                Intent intent = new Intent((MainActivity)getActivity(),FriendAddActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


    public void setAdapter(){
        for (int i = 0; i < 20; i++) {
            FriendItem friendItem = new FriendItem();
            friendItem.friendId = "friend" + i;
            adapter.add(friendItem);
        }

    }




}
