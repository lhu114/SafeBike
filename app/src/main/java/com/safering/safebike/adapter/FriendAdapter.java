package com.safering.safebike.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class FriendAdapter extends BaseAdapter{
    List<FriendItem> items = new ArrayList<FriendItem>();
    int viewType = 0;

    public FriendAdapter(int viewType) {
        this.viewType = viewType;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //뷰 타입에 따라 추가버튼 visible 결정
        FriendItemView view;
        if(convertView == null){
            view = new FriendItemView(parent.getContext());
        }
        else{
            view = (FriendItemView)convertView;
        }

        if(viewType == 1){
            view.setAddButtonVisible(true);
        }

        view.setFriendData(items.get(position));

        return view;
    }

    public void add(FriendItem item){
        items.add(item);
        notifyDataSetChanged();

    }
}
