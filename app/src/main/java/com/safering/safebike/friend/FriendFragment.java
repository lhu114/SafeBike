package com.safering.safebike.friend;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {
    public static final int FRIEND_NO_SELECT = 0;
    public static final String FRIEND_INFORM = "friendInform";
    public static final String USER_FRIEND = "friendList";
    public static final String FRIEND_ADAPTER = "friendAdapter";
    public static final String FRIEND_POSITION = "friendPosition";
    FriendAdapter fAdapter;
    //ArrayList<String> fEmailList;
    ListView listView;
    TextView textInvite;
    TextView textSafeBikeMainTitle, textMainTitle;
    FriendItem friendItem;
    View listFooter;


    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        fAdapter = new FriendAdapter(FRIEND_NO_SELECT);
        //fEmailList = new ArrayList<String>();

        textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);

        listFooter = inflater.inflate(R.layout.custom_friend_view,null);
        textInvite = (TextView)listFooter.findViewById(R.id.text_invite_friend);

        listView = (ListView) view.findViewById(R.id.listview_myfriend);
        setFont();
        listView.addFooterView(listFooter);

        textSafeBikeMainTitle.setVisibility(View.GONE);
        textMainTitle.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                friendItem = (FriendItem) fAdapter.getItem(position);
                Intent intent = new Intent(getContext(), FriendProfileActivity.class);
                intent.putExtra(FRIEND_INFORM, friendItem);
                intent.putExtra(FRIEND_ADAPTER,fAdapter);
                intent.putExtra(FRIEND_POSITION,position);
                startActivity(intent);
/*
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup_friend, popupMenu.getMenu());
                popupMenu.getMenu().getItem(0).setTitle(friendItem.pname);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals(friendItem.pname)) {
                            Intent intent = new Intent(getContext(), FriendProfileActivity.class);
                            intent.putExtra(FRIEND_INFORM, friendItem);
                            startActivity(intent);
                            return true;
                        } else {
                            String uEmail = PropertyManager.getInstance().getUserEmail();
                            String fEmail = friendItem.pemail;

                            NetworkManager.getInstance().removeUserFriend(getContext(), uEmail, fEmail, new NetworkManager.OnResultListener() {
                                @Override
                                public void onSuccess(Object success) {

                                    fAdapter.remove(position);

                                }

                                @Override
                                public void onFail(int code) {

                                }
                            });

                            //친구삭제 -- 서버에 전송
                            fAdapter.remove(position);
                            return true;
                        }

                    }
                });
                popupMenu.show();*/
            }
        });

        listView.setAdapter(fAdapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent((MainActivity) getActivity(), FriendAddActivity.class);
                //intent.putStringArrayListExtra(USER_FRIEND,fEmailList);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setFriendList();
        setTitleFont();

        textSafeBikeMainTitle.setVisibility(View.GONE);
        textMainTitle.setVisibility(View.VISIBLE);
    }

    public void setFriendList() {
        fAdapter.clear();
        UserFriendList.getInstance().removeAll();
        //fEmailList.clear();
        String email = PropertyManager.getInstance().getUserEmail();
        NetworkManager.getInstance().getUserFriends(getContext(), email, new NetworkManager.OnResultListener<FriendResult>() {
            @Override
            public void onSuccess(FriendResult result) {
                int count = result.count;
                for (int i = 0; i < count; i++) {
                    FriendItem friendItem = new FriendItem();
                    friendItem.pname = result.friendlist.get(i).pname;
                    friendItem.pemail = result.friendlist.get(i).pemail;
                    friendItem.photo = result.friendlist.get(i).photo;
                    UserFriendList.getInstance().addFriend(friendItem);
                    fAdapter.add(friendItem);
                }

            }

            @Override
            public void onFail(int code) {

            }
        });
    }

/*
    public ArrayList<String> getFriendEmail(){
        return fEmailList;
    }
*/

    public void setFont(){
        textInvite.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));

    }

    public void setTitleFont(){
        textMainTitle.setText("친구");
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

    }


}
