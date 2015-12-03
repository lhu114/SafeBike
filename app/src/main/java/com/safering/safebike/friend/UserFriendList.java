package com.safering.safebike.friend;

import com.safering.safebike.adapter.*;

import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.List;
import com.safering.safebike.adapter.FriendItem;

/**
 * Created by Tacademy on 2015-11-23.
 */
public class UserFriendList {

    ArrayList<FriendItem> items;

    private static UserFriendList instance;

    private UserFriendList(){
        items = new ArrayList<FriendItem>();
    }
    public static UserFriendList getInstance(){
        if(instance == null){
            instance = new UserFriendList();
        }
        return instance;
    }

    public void addFriend(FriendItem item){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).pemail.equals(item.pemail)){
                return;
            }
        }
        items.add(item);

    }

    public void removeAll(){
        if(items.size() > 0){
            items.clear();
        }
    }

    public ArrayList<FriendItem> getFriendList(){
        return items;
    }

    public boolean isFriend(String email){
        boolean isfriend = false;
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).pemail.equals(email)){
                isfriend = true;
                break;
            }
        }
        return isfriend;
    }

}
