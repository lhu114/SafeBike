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
    public static final int FRIEND_NO_SELECT = 0;
    public static final String FRIEND_INFORM = "friendInform";
    FriendAdapter fAdapter;
    ListView listView;
    FriendItem friendItem;

    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        fAdapter = new FriendAdapter(FRIEND_NO_SELECT);
        listView = (ListView)view.findViewById(R.id.listview_myfriend);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                friendItem = (FriendItem)fAdapter.getItem(position);

                PopupMenu popupMenu = new PopupMenu(getContext(),view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup_friend,popupMenu.getMenu());
                popupMenu.getMenu().getItem(0).setTitle(friendItem.friendId);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().equals(friendItem.friendId)) {
                            Intent intent = new Intent(getContext(), FriendProfileActivity.class);
                            intent.putExtra(FRIEND_INFORM, friendItem);
                            startActivity(intent);
                            return true;
                        }
                        else{
                            //친구삭제 -- 서버에 전송
                            fAdapter.remove(position);
                            return true;
                        }

                    }
                });
                popupMenu.show();
            }
        });
        listView.setAdapter(fAdapter);

        setFriendList();//네트워크로 친구리스트 받아서 뷰에 표시

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent((MainActivity)getActivity(),FriendAddActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //setFriendList(); 네트워크에서 친구리스트 받아오기
    }

    public void setFriendList(){
        //네트워크로 받아서 처리
        for (int i = 0; i < 20; i++) {
            FriendItem friendItem = new FriendItem();
            friendItem.friendId = "friend" + i;
            fAdapter.add(friendItem);
        }

    }




}
