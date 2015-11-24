package com.safering.safebike.navigation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhu on 2015-11-02.
 */
public class FavoriteAdapter extends BaseAdapter {
    List<FavoriteItem> items =new ArrayList<FavoriteItem>();

    public void add(FavoriteItem itemData) {
        items.add(itemData);
        notifyDataSetChanged();
    }

    public void remove() {
        if (items.size() > 0) {
            items.clear();
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
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
        FavoriteItemView view;

        if (convertView == null) {
            view = new FavoriteItemView(parent.getContext());
        } else {
            view = (FavoriteItemView) convertView;
        }

        view.setItemData(items.get(position));

        return view;
    }
}
