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
import com.safering.safebike.manager.InformDialogFragment;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {
    public static final int FRIEND_NO_SELECT = 0;
    public static final String FRIEND_INFORM = "friendInform";
    public static final String FRIEND_ADAPTER = "friendAdapter";
    public static final String FRIEND_POSITION = "friendPosition";
    FriendAdapter fAdapter;
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
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        fAdapter = new FriendAdapter(FRIEND_NO_SELECT);
        textSafeBikeMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_safebike_main_title);
        textMainTitle = (TextView) ((MainActivity) getActivity()).findViewById(R.id.text_main_title);
        listFooter = inflater.inflate(R.layout.custom_friend_view,null);
        listFooter.setClickable(false);
        textInvite = (TextView)listFooter.findViewById(R.id.text_invite_friend);
        listView = (ListView) view.findViewById(R.id.listview_myfriend);
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
            }
        });

        listView.setAdapter(fAdapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent((MainActivity) getActivity(), FriendAddActivity.class);
                startActivity(intent);
            }
        });

        setFont();

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
                InformDialogFragment dialog = new InformDialogFragment();
                dialog.setContent("네트워크 실패","네트워크 연결에 실패했습니다. 다시 시도해주세요");
                dialog.show(getChildFragmentManager(),"network");

            }
        });
    }


    public void setFont(){
        textInvite.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS_M));
    }

    public void setTitleFont(){
        textMainTitle.setText("친구");
        textMainTitle.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));
    }


}
