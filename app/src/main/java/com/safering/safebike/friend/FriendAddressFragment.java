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
import android.widget.ListView;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.adapter.FriendItemView;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendAddressFragment extends Fragment {
    public static final int FRIEND_SELECT = 1;

    ListView listView;
    FriendAdapter fAdapter;
    ArrayList<Contact> arContactList = new ArrayList<Contact>();

    public FriendAddressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_address, container, false);
        fAdapter = new FriendAdapter(FRIEND_SELECT);
        fAdapter.setOnButtonClickListener(new FriendItemView.OnButtonClickListener() {
            @Override
            public void onButtonClick(FriendItemView view, FriendItem data) {
              //  Toast.makeText(getContext(),"addclick" + data.friendId,Toast.LENGTH_SHORT).show();
                //네트워크에 요청
                String uEmail = PropertyManager.getInstance().getUserEmail();
                String fEmail = data.pemail;
                String fId = data.pname;
                NetworkManager.getInstance().addUserFriend(getContext(), uEmail, fEmail, fId, new NetworkManager.OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {
                    //서버에만 추가 요청하고 FriendFragment에서 OnResume에서 친구리스트 요청
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
            }
        });
        listView = (ListView)view.findViewById(R.id.listview_address_friend);
        listView.setAdapter(fAdapter);
        Button btn = (Button)view.findViewById(R.id.btn_search_friend_address);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          /*
          String email = PropertyManager.getInstance().getUserEmail();
                ArrayList phoneList = new ArrayList();
                NetworkManager.getInstance().getUserFriendAddress(getContext(), email, phoneList, new NetworkManager.OnResultListener<FriendSearchResult>() {
                    @Override
                    public void onSuccess(FriendSearchResult result) {
                        int count = Integer.valueOf(result.count);
                        for(int i = 0; i < count; i++){
                            FriendItem friend = new FriendItem();
                            friend.friendId = result.friendlist.get(i).name;
                            friend.friendImage = result.friendlist.get(i).photo;
                            friend.friendEmail = result.friendlist.get(i).email;
                            fAdapter.add(friend);
                        }

                    }

                    @Override
                    public void onFail(int code) {

                    }
                });*/
                arContactList = getContactList();
                for(int i = 0; i < arContactList.size(); i++){
                    FriendItem friend = new FriendItem();
                    friend.pname = "friend / " + arContactList.get(i).getPhonenum();
                    fAdapter.add(friend);
                }
            }
        });
        return view;
    }
    private ArrayList<Contact> getContactList() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor contactCursor = getActivity().getContentResolver().query(uri, projection, null, selectionArgs, sortOrder);

        ArrayList<Contact> contactlist = new ArrayList<Contact>();

        if (contactCursor.moveToFirst()) {
            do {
                String phonenumber = contactCursor.getString(1);
                /*
                String phonenumber = contactCursor.getString(1).replaceAll("-",
                        "");
                if (phonenumber.length() == 10) {
                    phonenumber = phonenumber.substring(0, 3) + "-"
                            + phonenumber.substring(3, 6) + "-"
                            + phonenumber.substring(6);
                } else if (phonenumber.length() > 8) {
                    phonenumber = phonenumber.substring(0, 3) + "-"
                            + phonenumber.substring(3, 7) + "-"
                            + phonenumber.substring(7);
                }*/
                Contact acontact = new Contact();
                acontact.setPhotoid(contactCursor.getLong(0));
                acontact.setPhonenum(phonenumber);
                acontact.setName(contactCursor.getString(2));
                contactlist.add(acontact);
            } while (contactCursor.moveToNext());
        }
        return contactlist;
    }

    public class Contact {
        long photoid;
        String phonenum;
        String name;

        public long getPhotoid() {
            return photoid;
        }

        public void setPhotoid(long photoid) {
            this.photoid = photoid;
        }

        public String getPhonenum() {
            return phonenum;
        }

        public void setPhonenum(String phonenum) {
            this.phonenum = phonenum;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}
