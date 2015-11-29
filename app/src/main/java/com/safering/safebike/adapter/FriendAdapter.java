package com.safering.safebike.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tacademy on 2015-10-30.
 */
public class FriendAdapter extends BaseAdapter  implements FriendItemView.OnButtonClickListener,Serializable{
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
            view.setOnButtonClickListener(this);
        }
        else{
            view = (FriendItemView)convertView;
        }

        if(viewType == 1){
            view.setAddButtonVisible(true);
        }
        else if(viewType == 2){
            view.setAddButtonVisible(true);
            view.setDisplayEmail(true);
        }


        view.setFriendData(items.get(position));

        return view;
    }

    public void add(FriendItem item){
        items.add(item);
        notifyDataSetChanged();

    }

    public void remove(int position){
        items.remove(position);
        notifyDataSetChanged();

    }

    public void remove(String email){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).pemail.equals(email)){
                items.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }
    FriendItemView.OnButtonClickListener bListener;
    public void setOnButtonClickListener(FriendItemView.OnButtonClickListener listener){
        bListener = listener;

    }

    @Override
    public void onButtonClick(FriendItemView view, FriendItem data) {
        bListener.onButtonClick(view,data);

    }

    public void clear(){
        if(items.size() > 0) {
            items.clear();
            notifyDataSetChanged();
        }

    }
}
