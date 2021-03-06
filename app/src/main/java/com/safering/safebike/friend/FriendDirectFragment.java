package com.safering.safebike.friend;


import android.content.pm.FeatureGroupInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
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
import com.safering.safebike.adapter.FriendItemView;
import com.safering.safebike.manager.InformDialogFragment;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendDirectFragment extends Fragment {
    public static int SEARCH_ONOFF = 1;
    private static final int FRIEND_SELECT_IMAGE = 2;
    ListView listView;
    FriendAdapter fAdapter;
    EditText inputEmail = null;

    public FriendDirectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_direct, container, false);

        inputEmail = (EditText)view.findViewById(R.id.edit_search_friend_direct);
        listView = (ListView) view.findViewById(R.id.listview_direct_friend);
        fAdapter = new FriendAdapter(FRIEND_SELECT_IMAGE);
        listView.setAdapter(fAdapter);
        fAdapter.setOnButtonClickListener(new FriendItemView.OnButtonClickListener() {
            @Override
            public void onButtonClick(FriendItemView view, FriendItem data) {

                String uEmail = PropertyManager.getInstance().getUserEmail();
                final String fEmail = data.pemail;
                final String fId = data.pname;
                final String fPhoto = data.photo;
                if (!UserFriendList.getInstance().isFriend(fEmail)) {
                    NetworkManager.getInstance().addUserFriend(getContext(), uEmail, fEmail, fId, fPhoto, new NetworkManager.OnResultListener() {
                        @Override
                        public void onSuccess(Object result) {
                            FriendItem friend = new FriendItem();
                            friend.pname = fId;
                            friend.pemail = fEmail;
                            friend.photo = fPhoto;
                            fAdapter.add(friend);
                            UserFriendList.getInstance().addFriend(friend);
                            fAdapter.clear();

                        }

                        @Override
                        public void onFail(int code) {
                            InformDialogFragment dialog = new InformDialogFragment();
                            dialog.setContent("네트워크 실패","네트워크 연결에 실패했습니다. 다시 시도해주세요");
                            dialog.show(getChildFragmentManager(),"network");

                        }
                    });
                }
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((FriendAddActivity)getActivity()).actionBarSetting();



    }

    public void searchDirect(String email){
        NetworkManager.getInstance().getUserFriendDirect(getContext(), email, new NetworkManager.OnResultListener<FriendDirectSearchResult>() {
            @Override
            public void onSuccess(FriendDirectSearchResult result) {
                if(result.usereserch != null){
                    if(!result.usereserch.uemail.equals(PropertyManager.getInstance().getUserEmail())) {
                        if(!UserFriendList.getInstance().isFriend(result.usereserch.uemail)) {


                            fAdapter.clear();
                            FriendItem friend = new FriendItem();
                            friend.pemail = result.usereserch.uemail;
                            friend.pname = result.usereserch.name;
                            friend.photo = result.usereserch.photo;


                            fAdapter.add(friend);
                        }
                    }

                }

            }

            @Override
            public void onFail(int code) {

            }
        });


    }

    public void setList(){
        for (int i = 0; i < 20; i++) {
            //서버랑 비교해서 가져오기
            FriendItem item = new FriendItem();
            item.pname = "friendNumber/" + i;
            fAdapter.add(item);
        }

    }
}
