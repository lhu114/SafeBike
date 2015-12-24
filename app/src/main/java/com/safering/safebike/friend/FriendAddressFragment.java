package com.safering.safebike.friend;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.safering.safebike.R;
import com.safering.safebike.adapter.FriendAdapter;
import com.safering.safebike.adapter.FriendItem;
import com.safering.safebike.adapter.FriendItemView;
import com.safering.safebike.manager.InformDialogFragment;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendAddressFragment extends Fragment {
    public static final int FRIEND_SELECT = 1;
    int listPosition = 0;

    ListView listView;
    FriendAdapter fAdapter;
    ArrayList<Contact> arContactList = new ArrayList<Contact>();
    ArrayList<FriendItem> addressFriendList;
    ArrayList position;

    public FriendAddressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_address, container, false);
        fAdapter = new FriendAdapter(FRIEND_SELECT);
        listView = (ListView) view.findViewById(R.id.listview_address_friend);
        listView.setAdapter(fAdapter);
        listView.setFooterDividersEnabled(false);

        fAdapter.setOnButtonClickListener(new FriendItemView.OnButtonClickListener() {
            @Override
            public void onButtonClick(FriendItemView view, FriendItem data) {
                String uEmail = PropertyManager.getInstance().getUserEmail();
                final String fEmail = data.pemail;
                final String fId = data.pname;
                final String fPhoto = data.photo;
                if(!UserFriendList.getInstance().isFriend(fEmail)){
                    NetworkManager.getInstance().addUserFriend(getContext(), uEmail, fEmail, fId, fPhoto, new NetworkManager.OnResultListener() {
                        @Override
                        public void onSuccess(Object result) {
                            FriendItem friend = new FriendItem();
                            friend.pname = fId;
                            friend.pemail = fEmail;
                            friend.photo = fPhoto;
                            UserFriendList.getInstance().addFriend(friend);
                            fAdapter.remove(fEmail);
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });
                }
            }
        });

        setList();


        return view;
    }

    public void setList() {
        listPosition = 0;
        position = new ArrayList();
        addressFriendList = new ArrayList<FriendItem>();
        arContactList = getContactList();
        NetworkManager.getInstance().getUserFriendAddress(getContext(),arContactList, new NetworkManager.OnResultListener<FriendSearchResult>() {
            @Override
            public void onSuccess(FriendSearchResult result) {
                int count = result.count;
                if(count > 0){
                    for(int i = 0; i < result.userpserch.size(); i++){
                        if(!UserFriendList.getInstance().isFriend(result.userpserch.get(i).uemail)) {
                            FriendItem friend = new FriendItem();
                            friend.pname = result.userpserch.get(i).name;
                            friend.pemail = result.userpserch.get(i).uemail;
                            friend.photo = result.userpserch.get(i).photo;
                            fAdapter.add(friend);
                            listPosition++;
                            position.add(listPosition++);
                            addressFriendList.add(friend);
                        }
                    }
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
                String phonenumber = contactCursor.getString(1).replaceAll("-", "");
                Contact acontact = new Contact();
                acontact.setPhotoid(contactCursor.getLong(0));
                acontact.setPhonenum(phonenumber);
                acontact.setName(contactCursor.getString(2));
                contactlist.add(acontact);
            } while (contactCursor.moveToNext());
        }
        return contactlist;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((FriendAddActivity) getActivity()).actionBarSetting();

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
