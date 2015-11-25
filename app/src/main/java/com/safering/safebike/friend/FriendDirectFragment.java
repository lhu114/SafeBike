package com.safering.safebike.friend;


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
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.navigation.NavigationFragment;
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendDirectFragment extends Fragment {
    public static int SEARCH_ONOFF = 1;
    ListView listView;
    FriendAdapter fAdapter;
    EditText inputEmail = null;
    Button searchDirect;
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
        fAdapter = new FriendAdapter(1);
        listView.setAdapter(fAdapter);
        fAdapter.setOnButtonClickListener(new FriendItemView.OnButtonClickListener() {
            @Override
            public void onButtonClick(FriendItemView view, FriendItem data) {

                String uEmail = PropertyManager.getInstance().getUserEmail();
                String fEmail = data.pemail;
                String fId = data.pname;
                String fPhoto = data.photo;
                if(UserFriendList.getInstance().isFriend(fEmail) == true){
                    Toast.makeText(getContext(),"이미등록된 친구",Toast.LENGTH_SHORT).show();
                }else {
                    NetworkManager.getInstance().addUserFriend(getContext(), uEmail, fEmail, fId, fPhoto, new NetworkManager.OnResultListener() {
                        @Override
                        public void onSuccess(Object result) {
                            fAdapter.clear();

                        }

                        @Override
                        public void onFail(int code) {

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
        Toast.makeText(getContext(),"DirectOnPause",Toast.LENGTH_SHORT).show();
        ((FriendAddActivity)getActivity()).actionBarSetting();



    }

    public void searchDirect(String email){
        NetworkManager.getInstance().getUserFriendDirect(getContext(), email, new NetworkManager.OnResultListener<FriendDirectSearchResult>() {
            @Override
            public void onSuccess(FriendDirectSearchResult result) {
                if(result.usereserch != null){
                    if(!result.usereserch.uemail.equals(PropertyManager.getInstance().getUserEmail())) {
                        fAdapter.clear();
                        FriendItem friend = new FriendItem();
                        friend.pemail = result.usereserch.uemail;
                        friend.pname = result.usereserch.name;
                        friend.photo = result.usereserch.photo;
                        Log.i("friend pmail", result.usereserch.uemail);
                        Log.i("friend pname",result.usereserch.name);
                        Log.i("friend pphoto", result.usereserch.photo);
/*
                        for(int i = 0; i < UserFriendList.getInstance().getFriendList().size(); i++){
                            Log.i("isFriend",UserFriendList.getInstance().getFriendList().get(i).pemail);

                        }
                        String e = UserFriendList.getInstance().getFriendList().get(0).pemail;
                        Log.i("isFriendT/F",UserFriendList.getInstance().isFriend(e) + "");
*/



                        fAdapter.add(friend);
                    }
                    Log.i("friend pphoto", result.usereserch.photo);

                }
                else{
                    Log.i("directSearch", "null!");
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
